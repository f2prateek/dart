package dart.henson.plugin;

import com.android.build.gradle.api.BaseVariant;
import com.android.builder.model.BuildType;
import com.android.builder.model.ProductFlavor;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;

import java.security.InvalidParameterException;
import java.util.Map;

import dart.henson.plugin.attributes.AttributeManager;
import dart.henson.plugin.generator.HensonNavigatorGenerator;
import dart.henson.plugin.internal.ArtifactManager;
import dart.henson.plugin.internal.ConfigurationManager;
import dart.henson.plugin.internal.DependencyManager;
import dart.henson.plugin.internal.SourceSetManager;
import dart.henson.plugin.internal.TaskManager;
import dart.henson.plugin.variant.Combinator;
import dart.henson.plugin.variant.NavigationVariant;
import dart.henson.plugin.variant.VariantManager;

import static dart.henson.plugin.internal.ConfigurationManager.NAVIGATION_CONFIGURATION_SUFFIX_ANNOTATION_PROCESSOR;
import static dart.henson.plugin.internal.ConfigurationManager.NAVIGATION_CONFIGURATION_SUFFIX_API;
import static dart.henson.plugin.internal.ConfigurationManager.NAVIGATION_CONFIGURATION_SUFFIX_COMPILE_ONLY;
import static dart.henson.plugin.internal.ConfigurationManager.NAVIGATION_CONFIGURATION_SUFFIX_IMPLEMENTATION;
import static dart.henson.plugin.util.StringUtil.capitalize;

public class HensonManager {
    private final Project project;
    public final Logger logger;
    public final ObjectFactory factory;
    public final VariantManager variantManager;
    public final TaskManager taskManager;
    public final ArtifactManager artifactManager;
    public final ConfigurationManager configurationManager;
    public final AttributeManager attributeManager;
    public final HensonNavigatorGenerator hensonNavigatorGenerator;
    public final DependencyManager dependencyManager;
    public final SourceSetManager sourceSetManager;
    public final HensonPluginExtension hensonExtension;

    public HensonManager(Project project) {
        this.project = project;
        this.logger = project.getLogger();
        this.factory = project.getObjects();
        this.variantManager = new VariantManager(logger);
        this.artifactManager = new ArtifactManager(project, logger);
        this.configurationManager = new ConfigurationManager(project, logger, artifactManager);
        this.hensonNavigatorGenerator = new HensonNavigatorGenerator();
        this.taskManager = new TaskManager(project, logger, configurationManager, hensonNavigatorGenerator);
        this.attributeManager = new AttributeManager(project, logger);
        this.dependencyManager = new DependencyManager(project, logger, artifactManager, configurationManager);
        this.sourceSetManager = new SourceSetManager(project, logger);
        this.hensonExtension = (HensonPluginExtension) project.getExtensions().getByName("henson");
    }

    /**
     * Used to communicate the artifact between project
     * this configuration is used from the client project
     * to consume the navigation dependency of a different consummer project
     * the artifact that will be consumed for real will be variant aware.
     * This configuration is only used for the client to declare dependencies,
     * it will not be consumed or resolved directly. We will extend it with a variant
     * aware configuration on the client side.
     */
    public void createClientPseudoConfiguration() {
        configurationManager.createClientPseudoConfiguration();
    }

    /**
     * custom matching strategy to take into account the new navigation attribute type
     * we want to match client requests and producer artifacts. For this we introduce
     * a new navigation attribute and define a matching strategy for it.
     */
    public void applyNavigationAttributeMatchingStrategy() {
        attributeManager.applyNavigationAttributeMatchingStrategy();
    }

    /**
     * the main configuration (navigation{Api, Implementation, etc.}
     */
    public void createMainNavigationConfigurationsAndSourceSet() {
        configurationManager.createNavigationConfigurations("");
        sourceSetManager.createNavigationSourceSetForMain();
    }

    public void process(BuildType buildType) {
        String buildTypeName = buildType.getName();
        logger.debug("Processing buildType: %s", buildTypeName);
        configurationManager.createNavigationConfigurations(buildTypeName);
        sourceSetManager.createNavigationSourceSet(buildType);
    }

    public void process(ProductFlavor productFlavor) {
        String productFlavorName = productFlavor.getName();
        logger.debug("Processing productFlavor: %s", productFlavorName);
        configurationManager.createNavigationConfigurations(productFlavorName);
        sourceSetManager.createNavigationSourceSet(productFlavor);
    }

    public void process(BaseVariant variant, String dartVersionName) {
        String variantName = variant.getName();
        logger.debug("Processing variant: %s", variantName);

        Combinator combinator = new Combinator();
        NavigationVariant navigationVariant = variantManager.createNavigationVariant(variant, combinator);
        createSourceSetAndConfigurations(navigationVariant, dartVersionName);
        createNavigationCompilerAndJarTasksAndArtifact(navigationVariant);
        //we use the api configuration to make sure the resulting apk will contain the classes of the navigation jar.
        dependencyManager.addNavigationArtifactsToVariantConfiguration(variant);

        Configuration internalConfiguration = configurationManager.createClientInternalConfiguration(variant);
        attributeManager.applyAttributes(variant, internalConfiguration);
        project.getDependencies().add(variant.getName() + "Implementation", internalConfiguration);

        //create the task for generating the henson navigator
        //create hensonExtension
        if (hensonExtension == null || hensonExtension.getNavigatorPackageName() == null) {
            throw new InvalidParameterException("The property 'henson.navigatorPackageName' must be defined in your build.gradle");
        }
        String hensonNavigatorPackageName = hensonExtension.getNavigatorPackageName();

        taskManager.detectNavigationApiDependenciesAndGenerateHensonNavigator(variant, hensonNavigatorPackageName);
    }

    private void createSourceSetAndConfigurations(NavigationVariant navigationVariant, String dartVersionName) {
        BaseVariant variant = navigationVariant.variant;
        sourceSetManager.createNavigationSourceSet(variant);
        navigationVariant.sourceSets.add(sourceSetManager.getNavigationSourceSetForMain());
        navigationVariant.sourceSets.add(sourceSetManager.getNavigationSourceSet(variant.getBuildType()));
        variant.getProductFlavors().stream().forEach(productFlavor -> {
            navigationVariant.sourceSets.add(sourceSetManager.getNavigationSourceSet(productFlavor));
        });

        String variantName = variant.getName();
        Map<String, Configuration> navigationConfigurations = configurationManager.createNavigationConfigurations(variantName);
        dependencyManager.addDartAndHensonDependenciesToNavigationConfigurations(variantName, dartVersionName);

        addNavigationConfigurationsToNavigationVariant(navigationVariant, navigationConfigurations);

        Map<String, Configuration> navigationConfigurationsBuildType = configurationManager.getNavigationConfigurations(variant.getBuildType().getName());
        addNavigationConfigurationsToNavigationVariant(navigationVariant, navigationConfigurationsBuildType);

        variant.getProductFlavors().stream().forEach(productFlavor -> {
            Map<String, Configuration> navigationConfigurationsProductFlavor
                    = configurationManager.getNavigationConfigurations(productFlavor.getName());
            addNavigationConfigurationsToNavigationVariant(navigationVariant, navigationConfigurationsProductFlavor);
        });

        Map<String, Configuration> navigationConfigurationsMain = configurationManager.getNavigationConfigurations("");
        addNavigationConfigurationsToNavigationVariant(navigationVariant, navigationConfigurationsMain);
    }

    private void addNavigationConfigurationsToNavigationVariant(NavigationVariant navigationVariant, Map<String, Configuration> navigationConfigurations) {
        Configuration api = navigationConfigurations.get(NAVIGATION_CONFIGURATION_SUFFIX_API);
        Configuration implementation = navigationConfigurations.get(NAVIGATION_CONFIGURATION_SUFFIX_IMPLEMENTATION);
        Configuration compileOnly = navigationConfigurations.get(NAVIGATION_CONFIGURATION_SUFFIX_COMPILE_ONLY);
        Configuration annotationProcessors = navigationConfigurations.get(NAVIGATION_CONFIGURATION_SUFFIX_ANNOTATION_PROCESSOR);
        navigationVariant.apiConfigurations.add(api);
        navigationVariant.implementationConfigurations.add(implementation);
        navigationVariant.compileOnlyConfigurations.add(compileOnly);
        navigationVariant.annotationProcessorConfigurations.add(annotationProcessors);
    }

    private void createNavigationCompilerAndJarTasksAndArtifact(NavigationVariant navigationVariant) {
        String tupleName = navigationVariant.variant.getName();
        String suffix = capitalize(tupleName);
        String pathSuffix = tupleName + "/";
        JavaCompile navigationApiCompiler = taskManager.createNavigationApiCompileTask(suffix, pathSuffix, navigationVariant);
        Jar navigationApiJarTask = taskManager.createNavigationApiJarTask(navigationApiCompiler, suffix);
        navigationVariant.compilerTask = navigationApiCompiler;
        navigationVariant.jarTask = navigationApiJarTask;
        addArtifact(navigationVariant);
    }

    private void addArtifact(NavigationVariant navigationVariant) {
        String suffix = capitalize(navigationVariant.variant.getName());

        BaseVariant variant = navigationVariant.variant;
        //get the attributes from the compile configuration and apply them to
        //the new artifact configuration
        String artifactName = artifactManager.getNavigationArtifactName(variant);
        Configuration artifactConfiguration = configurationManager.createArtifactConfiguration(variant);
        attributeManager.applyAttributes(variant, artifactConfiguration);
        project.getArtifacts().add(artifactName, taskManager.getNavigationApiJarTask(suffix));
    }
}