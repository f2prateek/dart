package dart.henson.plugin;

import com.android.build.gradle.api.BaseVariant;
import com.android.builder.model.BuildType;
import com.android.builder.model.ProductFlavor;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.SourceSet;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;

import dart.henson.plugin.attributes.AttributeManager;
import dart.henson.plugin.internal.ArtifactManager;
import dart.henson.plugin.internal.ConfigurationManager;
import dart.henson.plugin.internal.DependencyManager;
import dart.henson.plugin.internal.SourceSetManager;
import dart.henson.plugin.internal.TaskManager;
import dart.henson.plugin.variant.NavigationVariant;
import dart.henson.plugin.variant.VariantManager;

public class HensonManager {
    private final Project project;
    public final Logger logger;
    public final ObjectFactory factory;
    public final VariantManager variantManager;
    public final TaskManager taskManager;
    public final ArtifactManager artifactManager;
    public final ConfigurationManager configurationManager;
    public final AttributeManager attributeManager;
    public final DependencyManager dependencyManager;
    public final SourceSetManager sourceSetManager;
    public final HensonPluginExtension hensonExtension;

    public HensonManager(Project project) {
        this.project = project;
        this.logger = project.getLogger();
        this.factory = project.getObjects();
        this.variantManager = new VariantManager(logger);
        this.artifactManager = new ArtifactManager(logger);
        this.configurationManager = new ConfigurationManager(project, logger);
        this.sourceSetManager = new SourceSetManager(project, logger);
        this.taskManager = new TaskManager(project, logger);
        this.attributeManager = new AttributeManager(project, logger);
        this.dependencyManager = new DependencyManager(project, logger);
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
        configurationManager.maybeCreateClientPseudoConfiguration();
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
    public void createListNavigationSourceSetsTask() {
        List<SourceSet> allNavigationSourceSets = sourceSetManager.getAllNavigationSourceSets();
        taskManager.createListSourceSetTask(allNavigationSourceSets);
    }

    /**
     * the main configuration (navigation{Api, Implementation, etc.}
     */
    public void createMainNavigationConfigurationsAndSourceSet() {
        configurationManager.maybeCreateNavigationConfigurations("");
        sourceSetManager.maybeCreateNavigationSourceSet();
    }

    public void process(BuildType buildType) {
        String buildTypeName = buildType.getName();
        logger.debug("Processing buildType: %s", buildTypeName);
        configurationManager.maybeCreateNavigationConfigurations(buildTypeName);
        sourceSetManager.maybeCreateNavigationSourceSet(buildType);
    }

    public void process(ProductFlavor productFlavor) {
        String productFlavorName = productFlavor.getName();
        logger.debug("Processing productFlavor: %s", productFlavorName);
        configurationManager.maybeCreateNavigationConfigurations(productFlavorName);
        sourceSetManager.maybeCreateNavigationSourceSet(productFlavor);
    }

    public void process(BaseVariant variant, String dartVersionName) {
        String variantName = variant.getName();
        logger.debug("Processing variant: %s", variantName);

        NavigationVariant navigationVariant = variantManager.createNavigationVariant(variant);
        createSourceSetAndConfigurations(navigationVariant, dartVersionName);
        taskManager.createNavigationCompilerAndJarTasks(navigationVariant);
        String artifactName = addArtifact(navigationVariant);
        //we use the api configuration to make sure the resulting apk will contain the classes of the navigation jar.
        dependencyManager.addNavigationArtifactToVariantConfiguration(artifactName, variant);

        Configuration internalConfiguration = configurationManager.maybeCreateClientInternalConfiguration(variant);
        navigationVariant.clientInternalConfiguration = internalConfiguration;
        attributeManager.applyAttributes(variant, internalConfiguration);
        project.getDependencies().add(variant.getName() + "Implementation", internalConfiguration);

        //create the task for generating the henson navigator
        //create hensonExtension
        if (hensonExtension == null || hensonExtension.getNavigatorPackageName() == null) {
            throw new InvalidParameterException("The property 'henson.navigatorPackageName' must be defined in your build.gradle");
        }
        String hensonNavigatorPackageName = hensonExtension.getNavigatorPackageName();

        taskManager.detectNavigationApiDependenciesAndGenerateHensonNavigator(navigationVariant, hensonNavigatorPackageName);
    }

    private void createSourceSetAndConfigurations(NavigationVariant navigationVariant, String dartVersionName) {
        BaseVariant variant = navigationVariant.variant;
        sourceSetManager.maybeCreateNavigationSourceSet(variant);
        navigationVariant.sourceSets.add(sourceSetManager.maybeCreateNavigationSourceSet());
        navigationVariant.sourceSets.add(sourceSetManager.maybeCreateNavigationSourceSet(variant.getBuildType()));
        variant.getProductFlavors().stream().forEach(productFlavor -> {
            navigationVariant.sourceSets.add(sourceSetManager.maybeCreateNavigationSourceSet(productFlavor));
        });

        String variantName = variant.getName();
        Map<String, Configuration> navigationConfigurations = configurationManager.maybeCreateNavigationConfigurations(variantName);
        dependencyManager.addDartAndHensonDependenciesToNavigationConfigurations(variantName, dartVersionName);

        variantManager.addNavigationConfigurationsToNavigationVariant(navigationVariant, navigationConfigurations);

        Map<String, Configuration> navigationConfigurationsBuildType = configurationManager.maybeCreateNavigationConfigurations(variant.getBuildType().getName());
        variantManager.addNavigationConfigurationsToNavigationVariant(navigationVariant, navigationConfigurationsBuildType);

        variant.getProductFlavors().stream().forEach(productFlavor -> {
            Map<String, Configuration> navigationConfigurationsProductFlavor
                    = configurationManager.maybeCreateNavigationConfigurations(productFlavor.getName());
            variantManager.addNavigationConfigurationsToNavigationVariant(navigationVariant, navigationConfigurationsProductFlavor);
        });

        Map<String, Configuration> navigationConfigurationsMain = configurationManager.maybeCreateNavigationConfigurations("");
        variantManager.addNavigationConfigurationsToNavigationVariant(navigationVariant, navigationConfigurationsMain);
    }

    private String addArtifact(NavigationVariant navigationVariant) {
        BaseVariant variant = navigationVariant.variant;
        //get the attributes from the compile configuration and apply them to
        //the new artifact configuration
        String artifactName = artifactManager.getNavigationArtifactName(variant);
        Configuration artifactConfiguration = configurationManager.createArtifactConfiguration(artifactName);
        attributeManager.applyAttributes(variant, artifactConfiguration);
        project.getArtifacts().add(artifactName, navigationVariant.jarTask);
        return artifactName;
    }
}