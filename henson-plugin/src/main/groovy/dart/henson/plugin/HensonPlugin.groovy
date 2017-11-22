package dart.henson.plugin

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Task
import org.gradle.api.plugins.PluginCollection
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile

class HensonPlugin implements Plugin<Project> {
    void apply(Project project) {
        final def log = project.logger
        final String LOG_TAG = "HENSON"

        //check project
        def hasAppPlugin = project.plugins.withType(AppPlugin)
        def hasLibPlugin = project.plugins.withType(LibraryPlugin)
        checkProject(hasAppPlugin, hasLibPlugin)

        //we use the file build.properties that contains the version of
        //the extension to use. This avoids all problems related to using version x.y.+
        def versionName = getVersionName()

        //get Variants
        //def variants = getVariants(project, hasAppPlugin)

        //create extension
        createExtension(project)

        //we do the following for all sourcesets, of all build types, of all flavors, and all variants
        //  create source sets
        //  create configurations
        //  create dependencies
        //  create tasks: compile and jar
        //  create artifacts

        //the main source set
        def sourceSetName = "main"
        def newSourceSetName = "navigation"
        def newSourceSetPath = "src/navigation/"
        createNavigationSourceSet(project, sourceSetName, newSourceSetName, newSourceSetPath)

        def newArtifactName = "intentBuilder"
        def newConfigurationName = "navigation"
        createNavigationConfiguration(project, newArtifactName, newConfigurationName, versionName)

        def intentBuilderCompileTask = createIntentBuilderCompileTask(project, "", "main/", "navigation", "navigation")
        def intentBuilderJarTask = createIntentBuilderJarTask(project, intentBuilderCompileTask, "")
        addArtifact(project,"")

        project.android.buildTypes.all { buildType ->
            sourceSetName = "${buildType.name}"
            newSourceSetName = "navigation${buildType.name.capitalize()}"
            newSourceSetPath = "src/navigation/${buildType.name}"
            createNavigationSourceSet(project, sourceSetName, newSourceSetName, newSourceSetPath)

            newArtifactName = "intentBuilder${buildType.name.capitalize()}"
            newConfigurationName = "navigation${buildType.name.capitalize()}"
            createNavigationConfiguration(project, newArtifactName, newConfigurationName, versionName)

            intentBuilderCompileTask = createIntentBuilderCompileTask(project, "${buildType.name.capitalize()}", "${buildType.name}/", "navigation${buildType.name.capitalize()}", "navigation${buildType.name.capitalize()}")
            intentBuilderJarTask = createIntentBuilderJarTask(project, intentBuilderCompileTask, "${buildType.name.capitalize()}")
            addArtifact(project,"${buildType.name.capitalize()}")
        }

        project.android.productFlavors.all { productFlavor ->
            sourceSetName = "${productFlavor.name}"
            newSourceSetName = "navigation${productFlavor.name.capitalize()}"
            newSourceSetPath = "src/navigation/${productFlavor.name}"
            createNavigationSourceSet(project, sourceSetName, newSourceSetName, newSourceSetPath)

            newArtifactName = "intentBuilder${productFlavor.name.capitalize()}"
            newConfigurationName = "navigation${productFlavor.name.capitalize()}"
            createNavigationConfiguration(project, newArtifactName, newConfigurationName, versionName)

            intentBuilderCompileTask = createIntentBuilderCompileTask(project, "${productFlavor.name.capitalize()}", "${productFlavor.name}/", "navigation${productFlavor.name.capitalize()}", "navigation${productFlavor.name.capitalize()}")
            intentBuilderJarTask = createIntentBuilderJarTask(project, intentBuilderCompileTask, "${productFlavor.name.capitalize()}")
            addArtifact(project,"${productFlavor.name.capitalize()}")
        }

        project.android.buildTypes.all { buildType ->
            project.android.productFlavors.all { productFlavor ->
                sourceSetName = "${productFlavor.name}${buildType.name.capitalize()}"
                newSourceSetName = "navigation${productFlavor.name.capitalize()}${buildType.name.capitalize()}"
                newSourceSetPath = "src/navigation/${productFlavor.name}/${buildType.name}"
                createNavigationSourceSet(project, sourceSetName, newSourceSetName, newSourceSetPath)

                newArtifactName = "intentBuilder${productFlavor.name.capitalize()}${buildType.name.capitalize()}"
                newConfigurationName = "navigation${productFlavor.name.capitalize()}${buildType.name.capitalize()}"
                createNavigationConfiguration(project, newArtifactName, newConfigurationName, versionName)

                intentBuilderCompileTask = createIntentBuilderCompileTask(project, "${productFlavor.name.capitalize()}${buildType.name.capitalize()}", "${productFlavor.name}/${buildType.name}/", "${newSourceSetName}", "${newConfigurationName}")
                intentBuilderJarTask = createIntentBuilderJarTask(project, intentBuilderCompileTask, "${productFlavor.name.capitalize()}${buildType.name.capitalize()}")
                addArtifact(project,"${productFlavor.name.capitalize()}${buildType.name.capitalize()}")
            }
        }
    }

    private Object getVersionName() {
        Properties properties = new Properties()
        properties.load(getClass().getClassLoader().getResourceAsStream("build.properties"))
        properties.get("dart.version")
    }

    private Task createIntentBuilderCompileTask(Project project, taskSuffix, path, sourceSetName, configurationName) {
        def newDestinationDirName = "${project.buildDir}/navigation/classes/java/${path}"
        def newGeneratedDirName = "${project.buildDir}/generated/source/apt/navigation/${path}"
        project.tasks.create("intentBuilderCompile${taskSuffix}", JavaCompile) {
            setSource(project.android.sourceSets["${sourceSetName}"].java.sourceFiles)
            setDestinationDir(project.file("${newDestinationDirName}"))
            classpath = project.configurations["${configurationName}Api"]
            options.compilerArgs = ["-s", "${newGeneratedDirName}"]
            options.annotationProcessorPath = project.configurations["${configurationName}AnnotationProcessor"]
            targetCompatibility = JavaVersion.VERSION_1_7
            sourceCompatibility = JavaVersion.VERSION_1_7
            doFirst {
                project.file("${newGeneratedDirName}").mkdirs()
                project.file("${newDestinationDirName}").mkdirs()
            }
        }
    }

    private Task createIntentBuilderJarTask(Project project, intentBuilderCompileTask, taskSuffix) {
        def task = project.tasks.create("intentBuilderJar${taskSuffix}", Jar) {
            baseName = "${project.name}-intentBuilder${taskSuffix}"
            from intentBuilderCompileTask.destinationDir
            include('**/**__IntentBuilder*.class', '**/Henson*.class')
        }
        task.dependsOn(intentBuilderCompileTask)
        task
    }

    private void addArtifact(Project project, taskSuffix) {
        println project.configurations.getByName("intentBuilder")
        project.artifacts.add("intentBuilder${taskSuffix}", project.tasks["intentBuilderJar${taskSuffix}"])
    }

    private void addDependenciesToConfiguration(Project project, configurationName, versionName) {
        project.dependencies {
            "${configurationName}Api" "com.f2prateek.dart:dart-annotations:${versionName}"
            "${configurationName}Api" "com.f2prateek.dart:dart:${versionName}"
            "${configurationName}Api" "com.f2prateek.dart:henson:${versionName}"
            "${configurationName}CompileOnly" "com.google.android:android:4.1.1.4"
            "${configurationName}AnnotationProcessor" "com.f2prateek.dart:henson-processor:${versionName}"
        }
    }

    private void createNavigationConfiguration(Project project, newArtifactName, newConfigurationName, versionName) {
        println "Creating configuration: ${newArtifactName}"
        project.configurations {
            //the name of the artifact
            "${newArtifactName}" {
                canBeResolved true
            }

            //the api scope: is there any convention ? apiElements?
            "${newConfigurationName}Api" {
                canBeResolved true
            }

            //the ap scope
            "${newConfigurationName}AnnotationProcessor" {
                canBeResolved true
            }
        }
        addDependenciesToConfiguration(project, newConfigurationName, versionName)
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