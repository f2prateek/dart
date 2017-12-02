package dart.henson.plugin

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.UnionFileCollection
import org.gradle.api.plugins.PluginCollection
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile

import java.security.InvalidParameterException
import java.util.zip.ZipFile

class HensonPlugin implements Plugin<Project> {

    public static final String NAVIGATION_API_COMPILE_TASK_PREFIX = 'navigationApiCompileJava'
    public static final String NAVIGATION_API_JAR_TASK_PREFIX = 'navigationApiJar'

    void apply(Project project) {
        final def log = project.logger
        final String LOG_TAG = "HENSON"

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

        //we do the following for all sourcesets, of all build types, of all flavors, and all variants
        //  create source sets
        //  create configurations
        //  create dependencies
        //  create tasks: compile and jar
        //  create artifacts

        processMain(project, dartVersionName)
        processBuildTypes(project, dartVersionName)
        processProductFlavors(project, dartVersionName)
        processVariants(project, dartVersionName)

        //add the artifact of navigation for the variant to the variant configuration
        addNavigationArtifactsToVariantConfigurations(project)

        //create the task for generating the henson navigator
        detectNavigationApiDependenciesAndGenerateHensonNavigator(project)
    }

    private void detectNavigationApiDependenciesAndGenerateHensonNavigator(Project project) {
        project.android.applicationVariants.all { variant ->
            def taskDetectModules = project.tasks.create("detectModule${variant.name.capitalize()}") {
                doFirst {
                    //create hensonExtension
                    def hensonExtension = project.extensions.getByName('henson')
                    if(hensonExtension==null || hensonExtension.navigatorPackageName == null) {
                        throw new InvalidParameterException("The property 'henson.navigatorPackageName' must be defined in your build.gradle")
                    }
                    def hensonNavigatorPackageName = hensonExtension.navigatorPackageName

                    variant.compileConfiguration.resolve()
                    variant.compileConfiguration.each { dependency ->
                        println "Detected dependency: ${dependency.properties}"
                        List<String> targetActivities = new ArrayList()
                        if (dependency.name.matches(".*-navigationApi.*.jar")) {
                            println "Detected navigation API dependency: ${dependency.name}"
                            println "Detected navigation API dependency: ${dependency.name}"
                            def file = dependency.absoluteFile
                            def entries = getJarContent(file)
                            entries.each { entry ->
                                if(entry.matches(".*__IntentBuilder.class")) {
                                    println "Detected intent builder: ${entry}"
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

    private String generateHensonNavigatorClass(List<String> targetActivities, packageName) {
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

    private void processMain(Project project, dartVersionName) {
        def suffix = ""
        def pathSuffix = "main/"
        def sourceSetName = "main"

        createSourceSetAndConfiguration(project, sourceSetName, suffix, pathSuffix, dartVersionName)
        createEmptyNavigationApiCompileTask(project, suffix)
        createEmptyNavigationApiJarTask(project, suffix)
    }

    private Object processVariants(Project project, dartVersionName) {
        project.android.with {
            buildTypes.all { buildType ->
                productFlavors.all { productFlavor ->
                    println "Processing variant: ${productFlavor.name}${buildType.name.capitalize()}"
                    processVariant(project, productFlavor, buildType, dartVersionName)
                }
            }
        }
    }

    private void processProductFlavors(Project project, dartVersionName) {
        project.android.productFlavors.all { productFlavor ->
            println "Processing flavor: ${productFlavor.name}"
            processFlavorOrBuildType(project, productFlavor, dartVersionName)
        }
    }

    private void processBuildTypes(Project project, dartVersionName) {
        project.android.buildTypes.all { buildType ->
            println "Processing buildType: ${buildType.name}"
            processFlavorOrBuildType(project, buildType, dartVersionName)
        }
    }

    private void processVariant(Project project, productFlavor, buildType, dartVersionName) {
        def variantName = "${productFlavor.name}${buildType.name.capitalize()}"
        def suffix = "${productFlavor.name.capitalize()}${buildType.name.capitalize()}"
        def pathSuffix = "${productFlavor.name}${buildType.name.capitalize()}/"
        createSourceSetAndConfiguration(project, variantName, suffix, pathSuffix, dartVersionName)

        def navigationVariant = createNavigationVariant(project, productFlavor, buildType)
        def navigationApiCompiler = createNavigationApiCompileTask(project, suffix, pathSuffix, navigationVariant)
        def mainCompiler = project.tasks.getByName(NAVIGATION_API_COMPILE_TASK_PREFIX)
        def productFlavorCompiler = project.tasks.getByName(NAVIGATION_API_COMPILE_TASK_PREFIX + String.valueOf(productFlavor.name.capitalize()))
        def buildTypeCompiler = project.tasks.getByName(NAVIGATION_API_COMPILE_TASK_PREFIX + String.valueOf(buildType.name.capitalize()))
        navigationApiCompiler.dependsOn(mainCompiler, productFlavorCompiler, buildTypeCompiler)

        def navigationApiJarTask = createNavigationApiJarTask(project, navigationApiCompiler, suffix)
        def mainNavigationApiJarTask = project.tasks.getByName(NAVIGATION_API_JAR_TASK_PREFIX)
        def productFlavorNavigationApiJarTask = project.tasks.getByName(NAVIGATION_API_JAR_TASK_PREFIX + String.valueOf(productFlavor.name.capitalize()))
        def buildTypeNavigationApiJarTask = project.tasks.getByName(NAVIGATION_API_JAR_TASK_PREFIX + String.valueOf(buildType.name.capitalize()))
        navigationApiJarTask.dependsOn(mainNavigationApiJarTask, productFlavorNavigationApiJarTask, buildTypeNavigationApiJarTask)

        addArtifact(project, suffix, suffix)
    }

    private void processFlavorOrBuildType(Project project, productFlavorOrBuildType, dartVersionName) {
        def sourceSetName = "${productFlavorOrBuildType.name}"
        def suffix = "${productFlavorOrBuildType.name.capitalize()}"
        def pathSuffix = "${productFlavorOrBuildType.name}/"

        createSourceSetAndConfiguration(project, sourceSetName, suffix, pathSuffix, dartVersionName)

        createEmptyNavigationApiCompileTask(project, suffix)
        createEmptyNavigationApiJarTask(project, suffix)
    }

    private void createSourceSetAndConfiguration(Project project, String sourceSetName, String suffix, String pathSuffix, dartVersionName) {
        def newSourceSetName = "navigation" + suffix
        def newSourceSetPath = "src/navigation/" + pathSuffix
        createNavigationSourceSet(project, sourceSetName, newSourceSetName, newSourceSetPath)

        def newArtifactName = "navigationApi" + suffix
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

        project.tasks.create("${NAVIGATION_API_COMPILE_TASK_PREFIX}${taskSuffix}", JavaCompile) {
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

    private Task createNavigationApiJarTask(Project project, navigationApiCompileTask, taskSuffix) {
        def task = project.tasks.create(NAVIGATION_API_JAR_TASK_PREFIX + String.valueOf(taskSuffix), Jar) {
            baseName = "${project.name}-navigationApi${taskSuffix}"
            from navigationApiCompileTask.destinationDir
        }
        task.dependsOn(navigationApiCompileTask)
        task
    }

    private void addArtifact(Project project, artifactSuffix, taskSuffix) {
        println project.configurations.getByName("navigationApi")
        project.artifacts.add("navigationApi${artifactSuffix}", project.tasks[NAVIGATION_API_JAR_TASK_PREFIX + String.valueOf(taskSuffix)])
    }

    private void addNavigationArtifactsToVariantConfigurations(Project project) {
        //the project main source itself will depend on the navigation
        //we must wait until the variant created the proper configurations to add the dependency.
        project.android.applicationVariants.all { variant ->
            //we use the api configuration to make sure the resulting apk will contain the classes of the navigation jar.
            def configurationPrefix = variant.name
            def artifactSuffix = variant.name.capitalize()
            project.dependencies.add("${configurationPrefix}Api", project.dependencies.project(path: "${project.path}", configuration: "navigationApi${artifactSuffix}"))
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
