package dart.henson.plugin

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.dependency.AndroidTypeAttr
import dart.henson.plugin.attributes.NavigationTypeAttr
import dart.henson.plugin.attributes.NavigationTypeAttrCompatRule
import dart.henson.plugin.attributes.NavigationTypeAttrDisambiguationRule
import org.gradle.api.DomainObjectSet
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeMatchingStrategy
import org.gradle.api.attributes.Usage
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.UnionFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.PluginCollection
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.internal.logging.slf4j.OutputEventListenerBackedLogger

import java.security.InvalidParameterException
import java.util.zip.ZipFile

import static dart.henson.plugin.Combinator.Tuple


class HensonPlugin implements Plugin<Project> {

    public static final String NAVIGATION_API_COMPILE_TASK_PREFIX = 'navigationApiCompileJava'
    public static final String NAVIGATION_API_JAR_TASK_PREFIX = 'navigationApiJar'
    public static final String NAVIGATION_ARTIFACT_PREFIX = 'navigation'
    public static final String LOG_TAG = "HENSON: "

    private OutputEventListenerBackedLogger logger
    private ObjectFactory factory

    void apply(Project project) {

        logger = project.logger
        factory = project.getObjects()

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
        project.configurations {
            "${NAVIGATION_ARTIFACT_PREFIX}" {
                canBeResolved false
                canBeConsumed false
            }
        }

        //custom matching strategy to take into account the new navigation attribute type
        //we want to match client requests and producer artifacts. For this we introduce
        //a new navigation attribute and define a matching strategy for it.
        def schema = project.dependencies.attributesSchema
        AttributeMatchingStrategy<NavigationTypeAttr> navigationAttrStrategy =
                schema.attribute(NavigationTypeAttr.ATTRIBUTE)
        navigationAttrStrategy.getCompatibilityRules().add(NavigationTypeAttrCompatRule.class)
        navigationAttrStrategy.getDisambiguationRules().add(NavigationTypeAttrDisambiguationRule.class)

        //we do the following for all sourcesets, of all build types, of all flavors, and all variants
        //  create source sets
        //  create configurations
        //  create dependencies
        //  create tasks: compile and jar
        //  create artifacts

        //we create all configurations eagerly as we want users
        //to be able to use them before the creation of variants

        //the main configuration (navigation{Api, Implementation, etc.}
        createNavigationConfigurations(project, "")

        //one for each build type
        project.android.buildTypes.all { buildType ->
            log "Processing buildType: ${buildType.name}"
            createNavigationConfigurations(project, buildType.name.capitalize())
        }

        //one for each flavor
        project.android.productFlavors.all { productFlavor ->
            log "Processing productFlavor: ${productFlavor.name}"
            createNavigationConfigurations(project, productFlavor.name.capitalize())
        }


        final DomainObjectSet<? extends BaseVariant> variants = getVariants(project)

        variants.all { variant ->
            if (isNavigatorOnly) {
                log "Processing variant: ${variant.name}"
                processVariant(project, variant, dartVersionName)
            }
        }

        //create the task for generating the henson navigator
        detectNavigationApiDependenciesAndGenerateHensonNavigator(project)
    }

    private void log(String msg) {
        logger.debug( LOG_TAG + msg)
    }

    private DomainObjectSet<? extends BaseVariant> getVariants(Project project) {
        def hasApp = project.plugins.withType(AppPlugin)
        if (hasApp) {
            project.android.applicationVariants
        } else {
            project.android.libraryVariants
        }
    }

    private void detectNavigationApiDependenciesAndGenerateHensonNavigator(Project project) {
        def hasApp = project.plugins.withType(AppPlugin)
        final def variants
        if (hasApp) {
            variants = project.android.applicationVariants
        } else {
            variants = project.android.libraryVariants
        }
        variants.all { variant ->
            def taskDetectModules = project.tasks.create("detectModule${variant.name.capitalize()}") {
                doFirst {
                    //create hensonExtension
                    def hensonExtension = project.extensions.getByName('henson')
                    if(hensonExtension==null || hensonExtension.navigatorPackageName == null) {
                        throw new InvalidParameterException("The property 'henson.navigatorPackageName' must be defined in your build.gradle")
                    }
                    def hensonNavigatorPackageName = hensonExtension.navigatorPackageName

                    project.configurations["__${NAVIGATION_ARTIFACT_PREFIX}${variant.name}"].resolve()
                    Set<String> targetActivities = new HashSet()
                    project.configurations["__${NAVIGATION_ARTIFACT_PREFIX}${variant.name}"].each { dependency ->
                        project.logger.debug "Detected dependency: ${dependency.properties}"
                        if (dependency.name.matches(".*-navigationApi.*.jar")) {
                            project.logger.debug "Detected navigation API dependency: ${dependency.name}"
                            project.logger.debug "Detected navigation API dependency: ${dependency.name}"
                            def file = dependency.absoluteFile
                            def entries = getJarContent(file)
                            entries.each { entry ->
                                if(entry.matches(".*__IntentBuilder.class")) {
                                    project.logger.debug "Detected intent builder: ${entry}"
                                    def targetActivityFQN = entry.substring(0, entry.length() - "__IntentBuilder.class".length()).replace('/', '.')
                                    targetActivities.add(targetActivityFQN)
                                }
                            }
                        }

                        def variantSrcFolderName = new File(project.projectDir, "src/${variant.name}/java/")
                        String hensonNavigator = generateHensonNavigatorClass(targetActivities, hensonNavigatorPackageName)
                        variantSrcFolderName.mkdirs()
                        File generatedFolder =  new File(variantSrcFolderName, hensonNavigatorPackageName.replace('.', '/').concat('/'))
                        generatedFolder.mkdirs()
                        def generatedFile = new File(generatedFolder, "HensonNavigator.java")
                        generatedFile.withPrintWriter { out ->
                            out.println(hensonNavigator)
                        }
                    }
                }
            }
            //we put the task right before compilation so that all dependencies are resolved
            // when the task is executed
            taskDetectModules.dependsOn = variant.javaCompiler.dependsOn
            variant.javaCompiler.dependsOn(taskDetectModules)
        }
    }

    private String generateHensonNavigatorClass(Set<String> targetActivities, packageName) {
        String packageStatement = "package ${packageName};\n"
        String importStatement = "import android.content.Context;\n"
        targetActivities.each { targetActivity ->
            importStatement += "import ${targetActivity}__IntentBuilder;\n"
        }
        String classStartStatement = "public class HensonNavigator {\n"
        String methodStatement = ""
        targetActivities.each { targetActivity ->
            String targetActivitySimpleName = targetActivity.substring(1+targetActivity.lastIndexOf('.'), targetActivity.length())
            methodStatement += "public static ${targetActivitySimpleName.capitalize()}__IntentBuilder goto${targetActivitySimpleName.capitalize()}(Context context) {\n"
            methodStatement += "  return new ${targetActivitySimpleName}__IntentBuilder(context);\n"
            methodStatement += "}"
            methodStatement += "\n"
        }
        String classEndStatement = "}"
        new StringBuilder()
        .append(packageStatement)
        .append(importStatement)
        .append(classStartStatement)
        .append(methodStatement)
        .append(classEndStatement)
        .toString()
    }

    private void processVariant(Project project, variant, dartVersionName) {
        Combinator combinator = new Combinator()
        def navigationVariant = createNavigationVariant(project, variant, combinator)
        createSourceSetAndConfigurations(project, navigationVariant, dartVersionName)
        createNavigationCompilerAndJarTasksAndArtifact(project, navigationVariant)
        //we use the api configuration to make sure the resulting apk will contain the classes of the navigation jar.
        addNavigationArtifactsToVariantConfiguration(project, variant)

        project.configurations {
            "__${NAVIGATION_ARTIFACT_PREFIX}${variant.name}" {
                extendsFrom project.configurations["${NAVIGATION_ARTIFACT_PREFIX}"]
                canBeResolved true
                canBeConsumed false
            }
        }

        def configuration = project.configurations["__${NAVIGATION_ARTIFACT_PREFIX}${variant.name}"]
        applyAttributesFromVariantCompileToConfiguration(variant, configuration)

        project.configurations["__${NAVIGATION_ARTIFACT_PREFIX}${variant.name}"].attributes.attribute(Attribute.of(NavigationTypeAttr.class), factory.named(NavigationTypeAttr.class, NavigationTypeAttr.NAVIGATION))
        project.configurations["__${NAVIGATION_ARTIFACT_PREFIX}${variant.name}"].resolve()
        project.dependencies.add("${variant.name}Implementation", project.configurations["__${NAVIGATION_ARTIFACT_PREFIX}${variant.name}"])

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

                createNavigationConfigurations(project, tupleName)
                addDartAndHensonDependenciesToNavigationConfigurations(project, tupleName, dartVersionName)

                navigationVariant.sourceSets << project.sourceSets["navigation${tupleName}"]
                navigationVariant.apiConfigurations << project.configurations["navigation${tupleName}Api"]
                navigationVariant.implementationConfigurations << project.configurations["navigation${tupleName}Implementation"]
                navigationVariant.compileOnlyConfigurations << project.configurations["navigation${tupleName}CompileOnly"]
                navigationVariant.annotationProcessorConfigurations << project.configurations["navigation${tupleName}AnnotationProcessor"]
            }
        }
    }

    private void createNavigationConfigurations(Project project, newConfigurationRadix) {
        ["Api", "Implementation", "CompileOnly", "AnnotationProcessor"].each { suffix ->
            Configuration configuration = project.configurations.maybeCreate("navigation${newConfigurationRadix}${suffix}")
            configuration.canBeResolved = true
            configuration.canBeConsumed = false
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
            def navigationApiCompiler = createNavigationApiCompileTask(project, suffix, pathSuffix, navigationVariant)
            def navigationApiJarTask = createNavigationApiJarTask(project, navigationApiCompiler, suffix)
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

    private NavigationVariant createNavigationVariant(Project project, variant, combinator) {
        def navigationVariant = new NavigationVariant()
        navigationVariant.variant = variant
        def flavorNames = variant.productFlavors.collect { it.name }
        project.logger.debug "Flavors: ${flavorNames}"
        def flavorNamesAndBuildType = flavorNames << variant.buildType.name
        project.logger.debug "FlavorNamesAndBuildType: ${flavorNamesAndBuildType}"
        navigationVariant.combinations = combinator.combine(flavorNamesAndBuildType)
        project.logger.debug "Combinations: ${navigationVariant.combinations}"

        navigationVariant
    }

    private Task createEmptyNavigationApiCompileTask(Project project, taskSuffix) {
        project.tasks.create(NAVIGATION_API_COMPILE_TASK_PREFIX + String.valueOf(taskSuffix))
    }

    private Task createEmptyNavigationApiJarTask(Project project, taskSuffix) {
        project.tasks.create(NAVIGATION_API_JAR_TASK_PREFIX + String.valueOf(taskSuffix))
    }

    private Task createNavigationApiCompileTask(Project project, taskSuffix, destinationPath, navigationVariant) {
        def newDestinationDirName = "${project.buildDir}/navigation/classes/java/${destinationPath}"
        def newGeneratedDirName = "${project.buildDir}/generated/source/apt/navigation/${destinationPath}"

        FileCollection effectiveClasspath = new UnionFileCollection()
        navigationVariant.apiConfigurations.each { effectiveClasspath.add(it) }
        navigationVariant.implementationConfigurations.each { effectiveClasspath.add(it) }
        navigationVariant.compileOnlyConfigurations.each { effectiveClasspath.add(it) }

        FileCollection effectiveAnnotationProcessorPath = new UnionFileCollection()
        navigationVariant.annotationProcessorConfigurations.each { effectiveAnnotationProcessorPath.add(it) }

        if(project.tasks.findByName("${NAVIGATION_API_COMPILE_TASK_PREFIX}${taskSuffix}") == null) {
            project.tasks.create("${NAVIGATION_API_COMPILE_TASK_PREFIX}${taskSuffix}", JavaCompile) {
                setSource(navigationVariant.sourceSets.collect { sourceSet -> sourceSet.java.files })
                setDestinationDir(project.file("${newDestinationDirName}"))
                classpath = effectiveClasspath
                options.compilerArgs = ["-s", "${newGeneratedDirName}"]
                options.annotationProcessorPath = effectiveAnnotationProcessorPath
                targetCompatibility = JavaVersion.VERSION_1_7
                sourceCompatibility = JavaVersion.VERSION_1_7
                doFirst {
                    project.file("${newGeneratedDirName}").mkdirs()
                    project.file("${newDestinationDirName}").mkdirs()
                }
            }
        }
    }

    private Task createNavigationApiJarTask(Project project, navigationApiCompileTask, taskSuffix) {
        if(project.tasks.findByName("${NAVIGATION_API_JAR_TASK_PREFIX}${taskSuffix}") == null) {
            def task = project.tasks.create("${NAVIGATION_API_JAR_TASK_PREFIX}${taskSuffix}", Jar) {
                baseName = "${project.name}-navigationApi${taskSuffix}"
                from navigationApiCompileTask.destinationDir
            }
            task.dependsOn(navigationApiCompileTask)
            task
        }
    }

    private void addArtifact(Project project, navigationVariant) {
        def tuple = navigationVariant.combinations.last().first()
        def tupleName = merge(tuple)
        def suffix = tupleName.capitalize()

        project.configurations {
            "${NAVIGATION_ARTIFACT_PREFIX}${suffix}" {
                canBeConsumed true
                canBeResolved true
            }
        }

        //get the attributes from the compile configuration and apply them to
        //the new artifact configuration
        def configuration = project.configurations["${NAVIGATION_ARTIFACT_PREFIX}${suffix}"]
        def variant = navigationVariant.variant
        applyAttributesFromVariantCompileToConfiguration(variant, configuration)

        project.configurations["${NAVIGATION_ARTIFACT_PREFIX}${suffix}"].attributes.attribute(Attribute.of(NavigationTypeAttr.class), factory.named(NavigationTypeAttr.class, NavigationTypeAttr.NAVIGATION))
        project.artifacts.add("${NAVIGATION_ARTIFACT_PREFIX}${suffix}", project.tasks["${NAVIGATION_API_JAR_TASK_PREFIX}${suffix}"])
    }

    private void applyAttributesFromVariantCompileToConfiguration(variant, configuration) {
        def configFrom = variant.compileConfiguration
        configFrom.attributes.keySet().each { attributeKey ->
            if(!attributeKey.name.equals(Usage.class.name)
            && !attributeKey.name.equals(AndroidTypeAttr.class.name)) {
                def value = configFrom.attributes.getAttribute(attributeKey)
                if (value != null) {
                    configuration.attributes.attribute(attributeKey, value)
                }
            }
        }
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


    private List<String> getJarContent(file) {
        def List<String> result
        if(file.name.endsWith('.jar')) {
            result = new ArrayList<>()
            def zip = new ZipFile(file)
            zip.entries().each { entry ->
                result.add(entry.name)
            }
        }
        result
    }
}
