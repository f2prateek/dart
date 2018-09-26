package dart.henson.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import dart.henson.plugin.internal.GenerateHensonNavigatorTask
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginCollection

class HensonPlugin implements Plugin<Project> {

    private HensonManager hensonManager

    void apply(Project project) {

        //the extension is created but will be read only during execution time
        //(it's not available before)
        project.extensions.create('henson', HensonPluginExtension)


        //check project
        def hasAppPlugin = project.plugins.withType(AppPlugin)
        def hasLibPlugin = project.plugins.withType(LibraryPlugin)
        checkProject(hasAppPlugin, hasLibPlugin)

        hensonManager = new HensonManager(project)

        //we use the file build.properties that contains the version of
        //the dart & henson version to use. This avoids all problems related to using version x.y.+
        def dartVersionName = getVersionName()

        hensonManager.addDartAndHensonDependenciesToVariantConfigurations(dartVersionName)

        //for all android variants, we create a task to generate a henson navigator.
        final DomainObjectSet<? extends BaseVariant> variants = getAndroidVariants(project)
        variants.all { variant ->
            File destinationFolder =
                    project.file(
                            new File(project.getBuildDir(), "generated/source/navigator/" + variant.getName()))
            GenerateHensonNavigatorTask navigatorTask = hensonManager
                    .createHensonNavigatorGenerationTask(variant, destinationFolder)

            variant.registerJavaGeneratingTask(navigatorTask, destinationFolder)
            project.logger.debug("${navigatorTask.name} registered as Java Generating task")
        }
    }

    private Object getVersionName() {
        Properties properties = new Properties()
        properties.load(getClass().getClassLoader().getResourceAsStream("build.properties"))
        properties.get("dart.version")
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
