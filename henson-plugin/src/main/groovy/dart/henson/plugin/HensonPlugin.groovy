package dart.henson.plugin

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Task
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.UnionFileCollection
import org.gradle.api.plugins.PluginCollection
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile

class HensonPlugin implements Plugin<Project> {

    public static final String INTENT_BUILDER_COMPILE_TASK_PREFIX = 'intentBuilderCompile'
    public static final String INTENT_BUILDER_JAR_TASK_PREFIX = 'intentBuilderJar'

    void apply(Project project) {
        final def log = project.logger
        final String LOG_TAG = "HENSON"

        //check project
        def hasAppPlugin = project.plugins.withType(AppPlugin)
        def hasLibPlugin = project.plugins.withType(LibraryPlugin)
        checkProject(hasAppPlugin, hasLibPlugin)

        //we use the file build.properties that contains the version of
        //the extension to use. This avoids all problems related to using version x.y.+
        def dartVersionName = getVersionName()

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
        def suffix = ""
        def pathSuffix = "main/"
        def sourceSetName = "main"

        createSourceSetAndConfiguration(project, sourceSetName, suffix, pathSuffix, dartVersionName)

        createEmptyIntentBuilderCompileTask(project, suffix)
        createEmptyIntentBuilderJarTask(project, suffix)

        project.android.buildTypes.all { buildType ->
            println "Processing buildType: ${buildType.name}"
            processFlavorOrBuildType(project, buildType, dartVersionName)
        }

        project.android.productFlavors.all { productFlavor ->
            println "Processing flavor: ${productFlavor.name}"
            processFlavorOrBuildType(project, productFlavor, dartVersionName)
        }

        project.android.with {
            buildTypes.all { buildType ->
                productFlavors.all { productFlavor ->
                    println "Processing variant: ${productFlavor.name}${buildType.name.capitalize()}"
                    processVariant(project, productFlavor, buildType, dartVersionName)
                }
            }
        }
    }

    private void processVariant(Project project, productFlavor, buildType, dartVersionName) {
        def variantName = "${productFlavor.name}${buildType.name.capitalize()}"
        def suffix = "${productFlavor.name.capitalize()}${buildType.name.capitalize()}"
        def pathSuffix = "${productFlavor.name}/${buildType.name.capitalize()}/"
        createSourceSetAndConfiguration(project, variantName, suffix, pathSuffix, dartVersionName)

        def navigationVariant = createNavigationVariant(project, productFlavor, buildType)
        def intentBuilderCompiler = createIntentBuilderCompileTask(project, suffix, pathSuffix, navigationVariant)
        def mainCompiler = project.tasks.getByName(INTENT_BUILDER_COMPILE_TASK_PREFIX)
        def productFlavorCompiler = project.tasks.getByName(INTENT_BUILDER_COMPILE_TASK_PREFIX + String.valueOf(productFlavor.name.capitalize()))
        def buildTypeCompiler = project.tasks.getByName(INTENT_BUILDER_COMPILE_TASK_PREFIX + String.valueOf(buildType.name.capitalize()))
        intentBuilderCompiler.dependsOn(mainCompiler, productFlavorCompiler, buildTypeCompiler)

        def intentBuilderJarTask = createIntentBuilderJarTask(project, intentBuilderCompiler, suffix)
        def mainIntentBuilderJarTask = project.tasks.getByName(INTENT_BUILDER_JAR_TASK_PREFIX)
        def productFlavorIntentBuilderJarTask = project.tasks.getByName(INTENT_BUILDER_JAR_TASK_PREFIX + String.valueOf(productFlavor.name.capitalize()))
        def buildTypeIntentBuilderJarTask = project.tasks.getByName(INTENT_BUILDER_JAR_TASK_PREFIX + String.valueOf(buildType.name.capitalize()))
        intentBuilderJarTask.dependsOn(mainIntentBuilderJarTask, productFlavorIntentBuilderJarTask, buildTypeIntentBuilderJarTask)

        addArtifact(project, suffix, suffix)
        addNavigationArtifactToDependencies(project, suffix, variantName)
    }

    private void processFlavorOrBuildType(Project project, productFlavorOrBuildType, dartVersionName) {
        def sourceSetName = "${productFlavorOrBuildType.name}"
        def suffix = "${productFlavorOrBuildType.name.capitalize()}"
        def pathSuffix = "${productFlavorOrBuildType.name}/"

        createSourceSetAndConfiguration(project, sourceSetName, suffix, pathSuffix, dartVersionName)

        createEmptyIntentBuilderCompileTask(project, suffix)
        createEmptyIntentBuilderJarTask(project, suffix)
    }

    private void createSourceSetAndConfiguration(Project project, String sourceSetName, String suffix, String pathSuffix, dartVersionName) {
        def newSourceSetName = "navigation" + suffix
        def newSourceSetPath = "src/navigation/" + pathSuffix
        createNavigationSourceSet(project, sourceSetName, newSourceSetName, newSourceSetPath)

        def newArtifactName = "intentBuilder" + suffix
        createNavigationConfiguration(project, newArtifactName, suffix, dartVersionName)
    }

    private Object getVersionName() {
        Properties properties = new Properties()
        properties.load(getClass().getClassLoader().getResourceAsStream("build.properties"))
        properties.get("dart.version")
    }

    private NavigationVariant createNavigationVariant(Project project, productFlavor, buildType) {
        def navigationVariant = new NavigationVariant()
        navigationVariant.flavorName = productFlavor.name
        navigationVariant.buildTypeName = buildType.name
        navigationVariant.sourceSets = [project.android.sourceSets["navigation"] \
            , project.android.sourceSets["navigation${navigationVariant.buildTypeName.capitalize()}"] \
            , project.android.sourceSets["navigation${navigationVariant.flavorName.capitalize()}"] \
            , project.android.sourceSets["navigation${navigationVariant.flavorName.capitalize()}${navigationVariant.buildTypeName.capitalize()}"]
                ]
        navigationVariant.apiConfigurations = [project.configurations["navigationApi"] \
            , project.configurations["navigation${navigationVariant.buildTypeName.capitalize()}Api"] \
            , project.configurations["navigation${navigationVariant.flavorName.capitalize()}Api"] \
            , project.configurations["navigation${navigationVariant.flavorName.capitalize()}${navigationVariant.buildTypeName.capitalize()}Api"]
                ]
        navigationVariant.implementationConfigurations = [project.configurations["navigationImplementation"] \
            , project.configurations["navigation${navigationVariant.buildTypeName.capitalize()}Implementation"] \
            , project.configurations["navigation${navigationVariant.flavorName.capitalize()}Implementation"] \
            , project.configurations["navigation${navigationVariant.flavorName.capitalize()}${navigationVariant.buildTypeName.capitalize()}Implementation"]
                ]
        navigationVariant.compileOnlyConfigurations = [project.configurations["navigationCompileOnly"] \
            , project.configurations["navigation${navigationVariant.buildTypeName.capitalize()}CompileOnly"] \
            , project.configurations["navigation${navigationVariant.flavorName.capitalize()}CompileOnly"] \
            , project.configurations["navigation${navigationVariant.flavorName.capitalize()}${navigationVariant.buildTypeName.capitalize()}CompileOnly"]
                ]
        navigationVariant.annotationProcessorConfigurations = [project.configurations["navigationAnnotationProcessor"] \
            , project.configurations["navigation${navigationVariant.buildTypeName.capitalize()}AnnotationProcessor"] \
            , project.configurations["navigation${navigationVariant.flavorName.capitalize()}AnnotationProcessor"] \
            , project.configurations["navigation${navigationVariant.flavorName.capitalize()}${navigationVariant.buildTypeName.capitalize()}AnnotationProcessor"]
                ]
        navigationVariant
    }

    private Task createEmptyIntentBuilderCompileTask(Project project, taskSuffix) {
        project.tasks.create(INTENT_BUILDER_COMPILE_TASK_PREFIX + String.valueOf(taskSuffix))
    }

    private Task createEmptyIntentBuilderJarTask(Project project, taskSuffix) {
        project.tasks.create(INTENT_BUILDER_JAR_TASK_PREFIX + String.valueOf(taskSuffix))
    }

    private Task createIntentBuilderCompileTask(Project project, taskSuffix, destinationPath, navigationVariant) {
        def newDestinationDirName = "${project.buildDir}/navigation/classes/java/${destinationPath}"
        def newGeneratedDirName = "${project.buildDir}/generated/source/apt/navigation/${destinationPath}"

        FileCollection effectiveClasspath = new UnionFileCollection()
        navigationVariant.apiConfigurations.each { effectiveClasspath.add(it) }
        navigationVariant.implementationConfigurations.each { effectiveClasspath.add(it) }
        navigationVariant.compileOnlyConfigurations.each { effectiveClasspath.add(it) }

        FileCollection effectiveAnnotationProcessorPath = new UnionFileCollection()
        navigationVariant.annotationProcessorConfigurations.each { effectiveAnnotationProcessorPath.add(it) }

        project.tasks.create("intentBuilderCompile${taskSuffix}", JavaCompile) {
            setSource(navigationVariant.sourceSets.collect { sourceSet -> sourceSet.java.sourceFiles })
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

    private Task createIntentBuilderJarTask(Project project, intentBuilderCompileTask, taskSuffix) {
        def task = project.tasks.create(INTENT_BUILDER_JAR_TASK_PREFIX + String.valueOf(taskSuffix), Jar) {
            baseName = "${project.name}-intentBuilder${taskSuffix}"
            from intentBuilderCompileTask.destinationDir
        }
        task.dependsOn(intentBuilderCompileTask)
        task
    }

    private void addArtifact(Project project, artifactSuffix, taskSuffix) {
        println project.configurations.getByName("intentBuilder")
        project.artifacts.add("intentBuilder${artifactSuffix}", project.tasks[INTENT_BUILDER_JAR_TASK_PREFIX + String.valueOf(taskSuffix)])
    }

    private void addNavigationArtifactToDependencies(Project project, artifactSuffix, configurationPrefix) {
        //the project main source itself will depend on the navigation
        //we must wait until the variant created the proper configurations to add the dependency.
        project.afterEvaluate {
            //we use the api configuration to make sure the resulting apk will contain the classes of the navigation jar.
            project.dependencies.add("${configurationPrefix}Api", project.dependencies.project(path: "${project.path}", configuration: "intentBuilder${artifactSuffix}"))
        }
    }

    private void addDependenciesToConfiguration(Project project, configurationSuffix, dartVersionName) {
        project.dependencies {
            "navigation${configurationSuffix}CompileOnly" "com.google.android:android:4.1.1.4"
            "navigation${configurationSuffix}CompileOnly" "com.f2prateek.dart:dart:${dartVersionName}"
            "navigation${configurationSuffix}CompileOnly" "com.f2prateek.dart:henson:${dartVersionName}"
            "navigation${configurationSuffix}Api" "com.f2prateek.dart:dart-annotations:${dartVersionName}"
            "navigation${configurationSuffix}AnnotationProcessor" "com.f2prateek.dart:henson-processor:${dartVersionName}"
            "navigation${configurationSuffix}AnnotationProcessor" "com.f2prateek.dart:dart-processor:${dartVersionName}"
        }
    }

    private void createNavigationConfiguration(Project project, newArtifactName, newConfigurationSuffix, dartVersionName) {
        println "Creating configuration: ${newArtifactName}"
        project.configurations {
            //the name of the artifact
            "${newArtifactName}" {
                canBeResolved true
            }

            //the api scope: is there any convention ? apiElements?
            "navigation${newConfigurationSuffix}Api" {
                canBeResolved true
            }

            "navigation${newConfigurationSuffix}Implementation" {
                canBeResolved true
            }

            "navigation${newConfigurationSuffix}CompileOnly" {
                canBeResolved true
            }

            //the ap scope
            "navigation${newConfigurationSuffix}AnnotationProcessor" {
                canBeResolved true
            }
        }
        addDependenciesToConfiguration(project, newConfigurationSuffix, dartVersionName)
    }

    private void createNavigationSourceSet(Project project, sourceSetName, newSourceSetName, newSourceSetPath) {
        println "Creating sourceSet: ${sourceSetName}->${newSourceSetName} with root in '${newSourceSetPath}'"
        project.android.sourceSets {
            "${newSourceSetName}" {
                setRoot "${newSourceSetPath}"
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