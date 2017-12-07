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

import static dart.henson.plugin.Combinator.Tuple

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

        log.debug "Detected Variants:"
        project.android.buildTypes.all { buildType ->
        }

        project.android.productFlavors.all { productFlavor ->
            log.debug "ProductFlavor: ${productFlavor.name}"
        }

        log.debug "------------"

        project.android.sourceSets {
            "navigation2"
        }
        log.debug "------------2"

        processVariants(project, dartVersionName)
        log.debug "------------3"

        //add the artifact of navigation for the variant to the variant configuration
        addNavigationArtifactsToVariantConfigurations(project)

        //create the task for generating the henson navigator
        detectNavigationApiDependenciesAndGenerateHensonNavigator(project)
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

                    variant.compileConfiguration.resolve()
                    variant.compileConfiguration.each { dependency ->
                        project.logger.debug "Detected dependency: ${dependency.properties}"
                        List<String> targetActivities = new ArrayList()
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

    private Object processVariants(Project project, dartVersionName) {
        def hasApp = project.plugins.withType(AppPlugin)
        final def variants
        if (hasApp) {
            variants = project.android.applicationVariants
        } else {
            variants = project.android.libraryVariants
        }

        variants.all { variant ->
            project.logger.debug "Processing variant: ${variant.name}"
            processVariant(project, variant, dartVersionName)
        }
    }

    private void processVariant(Project project, variant, dartVersionName) {
        Combinator combinator = new Combinator()
        def navigationVariant = createNavigationVariant(project, variant, combinator)
        createSourceSetAndConfigurations(project, navigationVariant, dartVersionName)
        createNavigationCompilerAndJarTasks(project, navigationVariant)
        addArtifacts(project, navigationVariant)
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

                def newArtifactName = "navigationApi${tupleName}"
                createNavigationConfiguration(project, newArtifactName, tupleName)
                addDartAndHensonDependenciesToConfiguration(project, tupleName, dartVersionName)

                navigationVariant.sourceSets << project.sourceSets["navigation${tupleName}"]
                navigationVariant.apiConfigurations << project.configurations["navigation${tupleName}Api"]
                navigationVariant.implementationConfigurations << project.configurations["navigation${tupleName}Implementation"]
                navigationVariant.compileOnlyConfigurations << project.configurations["navigation${tupleName}CompileOnly"]
                navigationVariant.annotationProcessorConfigurations << project.configurations["navigation${tupleName}AnnotationProcessor"]
            }
        }
    }

    private void createNavigationConfiguration(Project project, newArtifactName, newConfigurationSuffix) {
        project.logger.debug "Creating configuration: ${newArtifactName}"
        project.logger.debug "Creating configurations: navigation${newConfigurationSuffix}*"
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
    }

    private void createNavigationSourceSet(Project project, newSourceSetName, newSourceSetPath) {
        project.logger.debug "Creating sourceSet: ${newSourceSetName} with root in '${newSourceSetPath}'"
        project.sourceSets {
            "${newSourceSetName}" {
                java.srcDirs "${newSourceSetPath}"
            }
        }

        println "Compiling java"
        println project.sourceSets["${newSourceSetName}"].java.srcDirs
    }

    private void createNavigationCompilerAndJarTasks(Project project, navigationVariant) {
        navigationVariant.combinations.each{ dimension ->
            dimension.each { tuple ->
                def tupleName = merge(tuple)
                def suffix = tupleName.capitalize()
                def pathSuffix = "${tupleName}/"
                def navigationApiCompiler = createNavigationApiCompileTask(project, suffix, pathSuffix, navigationVariant)
                createNavigationApiJarTask(project, navigationApiCompiler, suffix)
            }
        }
    }

    private void addArtifacts(project, navigationVariant) {
        navigationVariant.combinations.each { dimension ->
            dimension.each { tuple ->
                def tupleName = merge(tuple)
                def suffix = tupleName.capitalize()
                addArtifact(project, suffix, suffix)
            }
        }
    }



    private Object getVersionName() {
        Properties properties = new Properties()
        properties.load(getClass().getClassLoader().getResourceAsStream("build.properties"))
        properties.get("dart.version")
    }

    String merge(Tuple tuple) {
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

    private void addArtifact(Project project, artifactSuffix, taskSuffix) {
        if(project.tasks.findByName("${NAVIGATION_API_JAR_TASK_PREFIX}${taskSuffix}") != null) {
            project.logger.debug(project.configurations.getByName("navigationApi").toString())
            project.artifacts.add("navigationApi${artifactSuffix}", project.tasks["${NAVIGATION_API_JAR_TASK_PREFIX}${taskSuffix}"])
        }
    }

    private void addNavigationArtifactsToVariantConfigurations(Project project) {
        //the project main source itself will depend on the navigation
        //we must wait until the variant created the proper configurations to add the dependency.
        def hasApp = project.plugins.withType(AppPlugin)
        final def variants
        if (hasApp) {
            variants = project.android.applicationVariants
        } else {
            variants = project.android.libraryVariants
        }

        variants.all { variant ->
            //we use the api configuration to make sure the resulting apk will contain the classes of the navigation jar.
            def configurationPrefix = variant.name
            def artifactSuffix = variant.name.capitalize()
            project.dependencies.add("${configurationPrefix}Api", project.dependencies.project(path: "${project.path}", configuration: "navigationApi${artifactSuffix}"))
        }
    }

    private void addDartAndHensonDependenciesToConfiguration(Project project, configurationSuffix, dartVersionName) {
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
