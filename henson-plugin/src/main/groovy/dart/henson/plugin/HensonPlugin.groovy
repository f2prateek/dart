package dart.henson.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import dart.henson.plugin.attributes.AttributeManager
import dart.henson.plugin.attributes.NavigationTypeAttr
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.PluginCollection
import org.gradle.internal.logging.slf4j.OutputEventListenerBackedLogger

import java.security.InvalidParameterException

import static dart.henson.plugin.Combinator.Tuple

class HensonPlugin implements Plugin<Project> {

    public static final String NAVIGATION_ARTIFACT_PREFIX = 'navigation'
    public static final String LOG_TAG = "HENSON: "

    private OutputEventListenerBackedLogger logger
    private ObjectFactory factory
    private VariantManager variantManager
    private TaskManager taskManager
    private ArtifactManager artifactManager
    private ConfigurationManager configurationManager
    private AttributeManager attributeManager
    private HensonNavigatorGenerator hensonNavigatorGenerator

    void apply(Project project) {

        logger = project.logger
        factory = project.getObjects()
        variantManager = new VariantManager(logger)
        artifactManager = new ArtifactManager(project, logger)
        configurationManager = new ConfigurationManager(project, logger, artifactManager)
        hensonNavigatorGenerator = new HensonNavigatorGenerator()
        taskManager = new TaskManager(project, logger, configurationManager, hensonNavigatorGenerator)
        attributeManager = new AttributeManager(project, logger)

        //check project
        def hasAppPlugin = project.plugins.withType(AppPlugin)
        def hasLibPlugin = project.plugins.withType(LibraryPlugin)
        checkProject(hasAppPlugin, hasLibPlugin)

        //we use the file build.properties that contains the version of
        //the dart & henson version to use. This avoids all problems related to using version x.y.+
        def dartVersionName = getVersionName()

        //the extension is created but will be read only during execution time
        //(it's not available before)
        project.extensions.create('henson', HensonPluginExtension)
        def hensonExtension = project.extensions.getByName('henson')
        boolean isNavigatorOnly = hensonExtension != null && !hensonExtension.navigatorOnly

        //used to communicate the artifact between project
        //this configuration is used from the client project
        //to consume the navigation dependency of a different consummer project
        //the artifact that will be consumed for real will be variant aware.
        //This configuration is only used for the client to declare dependencies,
        //it will not be consumed or resolved directly. We will extend it with a variant
        //aware configuration on the client side.
        configurationManager.createClientPseudoConfiguration();

        //custom matching strategy to take into account the new navigation attribute type
        //we want to match client requests and producer artifacts. For this we introduce
        //a new navigation attribute and define a matching strategy for it.
        attributeManager.applyNavigationAttributeMatchingStrategy();

        //we do the following for all sourcesets, of all build types, of all flavors, and all variants
        //  create source sets
        //  create configurations
        //  create dependencies
        //  create tasks: compile and jar
        //  create artifacts

        //we create all configurations eagerly as we want users
        //to be able to use them before the creation of variants

        //the main configuration (navigation{Api, Implementation, etc.}
        configurationManager.createNavigationConfigurations("")

        //one for each build type
        project.android.buildTypes.all { buildType ->
            log "Processing buildType: ${buildType.name}"
            configurationManager.createNavigationConfigurations(buildType.name.capitalize())
        }

        //one for each flavor
        project.android.productFlavors.all { productFlavor ->
            log "Processing productFlavor: ${productFlavor.name}"
            configurationManager.createNavigationConfigurations(productFlavor.name.capitalize())
        }


        final DomainObjectSet<? extends BaseVariant> variants = getAndroidVariants(project)

        variants.all { variant ->
            if (isNavigatorOnly) {
                log "Processing variant: ${variant.name}"
                processVariant(project, variant, dartVersionName)
            }
        }

    }

    private void log(String msg) {
        logger.debug( LOG_TAG + msg)
    }

    private DomainObjectSet<? extends BaseVariant> getAndroidVariants(Project project) {
        def hasApp = project.plugins.withType(AppPlugin)
        if (hasApp) {
            project.android.applicationVariants
        } else {
            project.android.libraryVariants
        }
    }

    private void processVariant(Project project, variant, dartVersionName) {
        Combinator combinator = new Combinator()
        def navigationVariant = variantManager.createNavigationVariant(variant, combinator)
        createSourceSetAndConfigurations(project, navigationVariant, dartVersionName)
        createNavigationCompilerAndJarTasksAndArtifact(project, navigationVariant)
        //we use the api configuration to make sure the resulting apk will contain the classes of the navigation jar.
        addNavigationArtifactsToVariantConfiguration(project, variant)

        def internalConfiguration = configurationManager.createClientInternalConfiguration(variant)
        attributeManager.applyAttributesFromVariantCompileToConfiguration(variant, internalConfiguration)

        internalConfiguration.attributes.attribute(Attribute.of(NavigationTypeAttr.class), factory.named(NavigationTypeAttr.class, NavigationTypeAttr.NAVIGATION))
        internalConfiguration.resolve()
        project.dependencies.add("${variant.name}Implementation", internalConfiguration)

        //create the task for generating the henson navigator
        //create hensonExtension
        HensonPluginExtension hensonExtension = (HensonPluginExtension) project.getExtensions().getByName("henson");
        if (hensonExtension == null || hensonExtension.navigatorPackageName == null) {
            throw new InvalidParameterException("The property 'henson.navigatorPackageName' must be defined in your build.gradle");
        }
        String hensonNavigatorPackageName = hensonExtension.navigatorPackageName;

        taskManager.detectNavigationApiDependenciesAndGenerateHensonNavigator(project, variant, hensonNavigatorPackageName)
    }

    private void createSourceSetAndConfigurations(project, navigationVariant, dartVersionName) {
        navigationVariant.combinations.each { dimension ->
            dimension.each { tuple ->
                def tupleName = merge(tuple)
                def tuplePath
                if (tuple.isEmpty()) {
                    tuplePath = "src/navigation/main/java"
                } else {
                    tuplePath = "src/navigation/${tupleName}/java"
                }
                tupleName = tupleName.capitalize()
                createNavigationSourceSet(project, "navigation${tupleName}", "${tuplePath}/")

                configurationManager.createNavigationConfigurations(tupleName)
                addDartAndHensonDependenciesToNavigationConfigurations(project, tupleName, dartVersionName)

                navigationVariant.sourceSets << project.sourceSets["navigation${tupleName}"]
                navigationVariant.apiConfigurations << project.configurations["navigation${tupleName}Api"]
                navigationVariant.implementationConfigurations << project.configurations["navigation${tupleName}Implementation"]
                navigationVariant.compileOnlyConfigurations << project.configurations["navigation${tupleName}CompileOnly"]
                navigationVariant.annotationProcessorConfigurations << project.configurations["navigation${tupleName}AnnotationProcessor"]
            }
        }
    }

    private void createNavigationSourceSet(Project project, newSourceSetName, newSourceSetPath) {
        project.logger.debug "Creating sourceSet: ${newSourceSetName} with root in '${newSourceSetPath}'"
        project.sourceSets {
            "${newSourceSetName}" {
                java.srcDirs "${newSourceSetPath}"
            }
        }
    }

    private void createNavigationCompilerAndJarTasksAndArtifact(Project project, navigationVariant) {
        navigationVariant.combinations.last().each { tuple ->
            def tupleName = merge(tuple)
            def suffix = tupleName.capitalize()
            def pathSuffix = "${tupleName}/"
            def navigationApiCompiler = taskManager.createNavigationApiCompileTask(suffix, pathSuffix, navigationVariant)
            def navigationApiJarTask = taskManager.createNavigationApiJarTask(navigationApiCompiler, suffix)
            navigationVariant.compilerTask = navigationApiCompiler
            navigationVariant.jarTask = navigationApiJarTask
            addArtifact(project, navigationVariant)
        }
    }

    private Object getVersionName() {
        Properties properties = new Properties()
        properties.load(getClass().getClassLoader().getResourceAsStream("build.properties"))
        properties.get("dart.version")
    }

    private String merge(Tuple tuple) {
        if (tuple.empty) {
            return ""
        }

        boolean first = true
        def mergedName = ""

        tuple.each { name ->
            if(first) {
                mergedName = name
                first = false
            } else {
                mergedName += name.capitalize()
            }
        }
        mergedName
    }

    private void addArtifact(Project project, navigationVariant) {
        def tuple = navigationVariant.combinations.last().first()
        def tupleName = merge(tuple)
        def suffix = tupleName.capitalize()

        BaseVariant variant = navigationVariant.variant
        //get the attributes from the compile configuration and apply them to
        //the new artifact configuration
        String artifactName = artifactManager.getNavigationArtifactName(variant)
        Configuration artifactConfiguration = configurationManager.createArtifactConfiguration(variant)
        attributeManager.applyAttributesFromVariantCompileToConfiguration(variant, artifactConfiguration)

        artifactConfiguration.attributes.attribute(Attribute.of(NavigationTypeAttr.class), factory.named(NavigationTypeAttr.class, NavigationTypeAttr.NAVIGATION))
        project.artifacts.add(artifactName, taskManager.getNavigationApiJarTask(suffix))
    }

    private void addNavigationArtifactsToVariantConfiguration(Project project, variant) {
        def hensonExtension = project.extensions.getByName('henson')
        if (hensonExtension!= null && !hensonExtension.navigatorOnly) {
            //we use the api configuration to make sure the resulting apk will contain the classes of the navigation jar.
            def configurationPrefix = variant.name
            def artifactSuffix = variant.name.capitalize()
            project.dependencies.add("${configurationPrefix}Api", project.dependencies.project(path: "${project.path}", configuration: "${NAVIGATION_ARTIFACT_PREFIX}${artifactSuffix}"))
        }
    }

    private void addDartAndHensonDependenciesToNavigationConfigurations(Project project, configurationSuffix, dartVersionName) {
        project.dependencies {
            "navigation${configurationSuffix}CompileOnly" "com.google.android:android:4.1.1.4"
            "navigation${configurationSuffix}CompileOnly" "com.f2prateek.dart:dart:${dartVersionName}"
            "navigation${configurationSuffix}CompileOnly" "com.f2prateek.dart:henson:${dartVersionName}"
            "navigation${configurationSuffix}Api" "com.f2prateek.dart:dart-annotations:${dartVersionName}"
            "navigation${configurationSuffix}AnnotationProcessor" "com.f2prateek.dart:henson-processor:${dartVersionName}"
            "navigation${configurationSuffix}AnnotationProcessor" "com.f2prateek.dart:dart-processor:${dartVersionName}"
        }
    }

    private boolean checkProject(PluginCollection<AppPlugin> hasApp,
                              PluginCollection<LibraryPlugin> hasLib) {
        if (!hasApp && !hasLib) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }
        return !hasApp
    }
}
