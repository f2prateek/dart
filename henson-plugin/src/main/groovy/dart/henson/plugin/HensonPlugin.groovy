package dart.henson.plugin

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.plugins.PluginCollection

class HensonPlugin implements Plugin<Project> {
    void apply(Project project) {
        final def log = project.logger
        final String LOG_TAG = "HENSON"

        //check project
        def hasAppPlugin = project.plugins.withType(AppPlugin)
        def hasLibPlugin = project.plugins.withType(LibraryPlugin)
        checkProject(hasAppPlugin, hasLibPlugin)

        //get Variants
        //def variants = getVariants(project, hasAppPlugin)

        //create extension
        createExtension(project)

        //create source sets
        //the main source set
        def sourceSetName = "main"
        def newSourceSetName = "navigation"
        def newSourceSetPath = "src/navigation/"
        createNavigationSourceSet(project, sourceSetName, newSourceSetName, newSourceSetPath)

        project.android.buildTypes.all { buildType ->
            sourceSetName = "${buildType.name}"
            newSourceSetName = "navigation${buildType.name.capitalize()}"
            newSourceSetPath = "src/navigation/${buildType.name}"
            createNavigationSourceSet(project, sourceSetName, newSourceSetName, newSourceSetPath)
        }

        project.android.productFlavors.all { productFlavor ->
            sourceSetName = "${productFlavor.name}"
            newSourceSetName = "navigation${productFlavor.name.capitalize()}"
            newSourceSetPath = "src/navigation/${productFlavor.name}"
            createNavigationSourceSet(project, sourceSetName, newSourceSetName, newSourceSetPath)
        }

        project.android.buildTypes.all { buildType ->
            project.android.productFlavors.all { productFlavor ->
                sourceSetName = "${productFlavor.name}${buildType.name.capitalize()}"
                newSourceSetName = "navigation${productFlavor.name.capitalize()}${buildType.name.capitalize()}"
                newSourceSetPath = "src/navigation/${productFlavor.name}/${buildType.name}"
                createNavigationSourceSet(project, sourceSetName, newSourceSetName, newSourceSetPath)
            }
        }

        //create configurations
        //create dependencies
        //create tasks: compile and jar
        //create artifacts


/*
        def extension = project.extensions.create('greeting', GreetingPluginExtension, project)
        project.tasks.create('hello', Greeting) {
            message = extension.message
            outputFiles = extension.outputFiles
        }
*/
    }

    private void createNavigationSourceSet(Project project, sourceSetName, newSourceSetName, newSourceSetPath) {
        println "Creating sourceSet: ${sourceSetName}->${newSourceSetName} with root in '${newSourceSetPath}'"
        project.android.sourceSets {
            "${newSourceSetName}" {
                setRoot "${newSourceSetPath}"
            }
            "${sourceSetName}" {
                java.srcDirs = project.android.sourceSets["${sourceSetName}"].java.srcDirs.collect() << "${newSourceSetPath}/java"
            }
        }
    }

    private boolean checkProject(PluginCollection<AppPlugin> hasApp,
                              PluginCollection<LibraryPlugin> hasLib) {
        if (!hasApp && !hasLib) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }
        return !hasApp
    }

    private Collection<BaseVariant> getVariants(Project project, PluginCollection<AppPlugin> hasApp) {
        if (hasApp) {
            project.android.applicationVariants
        } else {
            project.android.libraryVariants
        }
    }
    private void createExtension(Project project) {
        project.extensions.create('henson', HensonPluginExtension, project)
    }
}