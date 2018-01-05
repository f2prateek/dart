package dart.henson.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import dart.henson.plugin.variant.NavigationVariant
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

        //the navigation configurations & source sets (to produce a navigation API)
        NavigationVariant navigationVariant = hensonManager.createNavigationVariant(dartVersionName)

        //the artifact configuration that will be consumed by clients
        hensonManager.createConsumableNavigationConfigurationAndArtifact(navigationVariant)

        //for all android variants, we create a task to generate a henson navigator.
        final DomainObjectSet<? extends BaseVariant> variants = getAndroidVariants(project)
        variants.all { variant ->
            hensonManager.createHensonNavigatorGenerationTask(variant)
        }

        project.afterEvaluate {
            hensonManager.createListNavigationSourceSetsTask()
        }

        //https://github.com/eaaltonen/gradle_plugin_function_export/blob/master/plugin/src/main/groovy/gradle/funcs/ExportFunctionPlugin.groovy
        HensonHelper helper = new HensonHelper(project)
        project.ext.navigationApiOf = helper.&navigationApiOf
        project.ext.navigationApiOfSelf = helper.&navigationApiOfSelf
    }

    public static class HensonHelper {
        private Project project;

        HensonHelper(Project project) {
            this.project = project
        }

        private void navigationApiOf(String moduleName) {
            def buildDirPath = project.rootProject.findProject("$moduleName").getBuildDir().absolutePath
            project.dependencies {
                //this is good enough for gradle, but not for AS
                implementation project(path: ":$moduleName", configuration: 'navigation')
                //this is for AS to work, it's not necessary for gradle on CLI
                //we still need the first line to trigger the compilation of the api in the first module
                compileOnly project.fileTree(dir: "${buildDirPath}/libs", include: '*navigationApi.jar')
            }
        }

        private void navigationApiOfSelf() {
            def buildDirPath = project.getBuildDir().absolutePath
            project.dependencies {
                //this is good enough for gradle, but not for AS
                api project(path: project.getPath(), configuration: 'navigation')
                //this is for AS to work, it's not necessary for gradle on CLI
                //we still need the first line to trigger the compilation of the api in the first module
                compileOnly project.fileTree(dir: "${buildDirPath}/libs", include: '*navigationApi.jar')
            }
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
