package dart.henson.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import dart.henson.plugin.attributes.AttributeManager
import dart.henson.plugin.generator.HensonNavigatorGenerator
import dart.henson.plugin.internal.ArtifactManager
import dart.henson.plugin.internal.ConfigurationManager
import dart.henson.plugin.internal.DependencyManager
import dart.henson.plugin.internal.TaskManager
import dart.henson.plugin.variant.VariantManager
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.PluginCollection

class HensonPlugin implements Plugin<Project> {

    public static final String LOG_TAG = "HENSON: "

    private HensonManager hensonManager
    private Logger logger
    private ObjectFactory factory
    private VariantManager variantManager
    private TaskManager taskManager
    private ArtifactManager artifactManager
    private ConfigurationManager configurationManager
    private AttributeManager attributeManager
    private DependencyManager dependencyManager

    void apply(Project project) {

        //the extension is created but will be read only during execution time
        //(it's not available before)
        project.extensions.create('henson', HensonPluginExtension)
        def hensonExtension = project.extensions.getByName('henson')

        hensonManager = new HensonManager(project)
        logger = hensonManager.logger
        factory = hensonManager.factory
        variantManager = hensonManager.variantManager
        artifactManager = hensonManager.artifactManager
        configurationManager = hensonManager.configurationManager
        taskManager = hensonManager.taskManager
        attributeManager = hensonManager.attributeManager
        dependencyManager = hensonManager.dependencyManager

        //check project
        def hasAppPlugin = project.plugins.withType(AppPlugin)
        def hasLibPlugin = project.plugins.withType(LibraryPlugin)
        checkProject(hasAppPlugin, hasLibPlugin)

        //we use the file build.properties that contains the version of
        //the dart & henson version to use. This avoids all problems related to using version x.y.+
        def dartVersionName = getVersionName()


        hensonManager.createClientPseudoConfiguration()
        hensonManager.applyNavigationAttributeMatchingStrategy()

        //we do the following for all sourcesets, of all build types, of all flavors, and all variants
        //  create source sets
        //  create configurations
        //  create dependencies
        //  create tasks: compile and jar
        //  create artifacts

        //we create all configurations eagerly as we want users
        //to be able to use them before the creation of variants

        //the main configuration (navigation{Api, Implementation, etc.}
        hensonManager.createMainNavigationConfigurationsAndSourceSet()

        //one for each build type
        project.android.buildTypes.all { buildType ->
            hensonManager.process(buildType)
        }

        //one for each flavor
        project.android.productFlavors.all { productFlavor ->
            hensonManager.process(productFlavor)
        }


        final DomainObjectSet<? extends BaseVariant> variants = getAndroidVariants(project)
        variants.all { variant ->
            hensonManager.process(variant, dartVersionName)
        }

        project.afterEvaluate {
            hensonManager.createListNavigationSourceSetsTask()
        }
    }

    private Object getVersionName() {
        Properties properties = new Properties()
        properties.load(getClass().getClassLoader().getResourceAsStream("build.properties"))
        properties.get("dart.version")
    }

    private void log(String msg) {
        logger.debug(LOG_TAG + msg)
    }

    private DomainObjectSet<? extends BaseVariant> getAndroidVariants(Project project) {
        def hasApp = project.plugins.withType(AppPlugin)
        if (hasApp) {
            project.android.applicationVariants
        } else {
            project.android.libraryVariants
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
