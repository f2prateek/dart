package dart.henson.plugin.internal;

import com.android.build.gradle.api.BaseVariant;
import com.google.common.collect.Streams;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.UnionFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dart.henson.plugin.generator.HensonNavigatorGenerator;
import dart.henson.plugin.variant.NavigationVariant;

import static dart.henson.plugin.util.StringUtil.capitalize;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.gradle.api.JavaVersion.VERSION_1_7;

public class TaskManager {
    public static final String NAVIGATION_API_COMPILE_TASK_PREFIX = "navigationApiCompileJava";
    public static final String NAVIGATION_API_JAR_TASK_PREFIX = "navigationApiJar";

    private Project project;
    private Logger logger;
    private ConfigurationManager configurationManager;
    private HensonNavigatorGenerator hensonNavigatorGenerator;

    public TaskManager(Project project,
                       Logger logger,
                       ConfigurationManager configurationManager,
                       HensonNavigatorGenerator hensonNavigatorGenerator) {
        this.project = project;
        this.logger = logger;
        this.configurationManager = configurationManager;
        this.hensonNavigatorGenerator = hensonNavigatorGenerator;
    }

    public JavaCompile createNavigationApiCompileTask(String taskSuffix, String destinationPath, NavigationVariant navigationVariant) {
        File newDestinationDir = new File(project.getBuildDir(), "/navigation/classes/java/" + destinationPath);
        File newGeneratedDir = new File(project.getBuildDir(), "/generated/source/apt/navigation/" + destinationPath);

        FileCollection effectiveClasspath = new UnionFileCollection();
        Streams.concat(navigationVariant.apiConfigurations.stream(),
                navigationVariant.implementationConfigurations.stream(),
                navigationVariant.compileOnlyConfigurations.stream())
                .forEach(effectiveClasspath::add);

        FileCollection effectiveAnnotationProcessorPath = new UnionFileCollection();
        navigationVariant.annotationProcessorConfigurations.
                forEach(effectiveAnnotationProcessorPath::add);

        String compileTaskName = NAVIGATION_API_COMPILE_TASK_PREFIX + taskSuffix;
        JavaCompile compileTask = (JavaCompile) project.getTasks().findByName(compileTaskName);
        System.out.println("Before source trees");
        if (compileTask == null) {
            compileTask = project.getTasks().create(compileTaskName, JavaCompile.class);
            List<FileTree> sources = navigationVariant.sourceSets
                    .stream()
                    .map(SourceSet::getJava)
                    .map(SourceDirectorySet::getAsFileTree)
                    .collect(toList());
            for (SourceSet sourceSet : navigationVariant.sourceSets) {
                System.out.println("source tree: " + sourceSet.getName());
            }
            for (FileTree source : sources) {
                System.out.println("source tree: " + source.getAsPath() + " : " + source.getFiles());
            }
            navigationVariant.apiConfigurations.stream().forEach( configuration -> {
                System.out.println("classpath: " + configuration.getName());
                //System.out.println("classpath: " + configuration.getAsPath());
            });

            navigationVariant.implementationConfigurations.stream().forEach( configuration -> {
                System.out.println("implementation classpath: " + configuration.getName());
                //System.out.println("implementation classpath: " + configuration.getAsPath());
                System.out.println("implementation classpath: " + configuration.isCanBeResolved());
                System.out.println("implementation classpath: " + configuration.isCanBeConsumed());
            });
            String javaVersion = VERSION_1_7.toString();

            compileTask.setSource(sources);
            compileTask.setDestinationDir(newDestinationDir);
            compileTask.setClasspath(effectiveClasspath);
            CompileOptions options = compileTask.getOptions();
            options.setCompilerArgs(asList("-s", newGeneratedDir.getAbsolutePath()));
            options.setAnnotationProcessorPath(effectiveAnnotationProcessorPath);
            compileTask.setTargetCompatibility(javaVersion);
            compileTask.setSourceCompatibility(javaVersion);
            compileTask.doFirst(task -> {
                newGeneratedDir.mkdirs();
                newDestinationDir.mkdirs();
            });
        }
        return compileTask;
    }

    public String getNavigationApiJarTaskName(String taskSuffix) {
        return NAVIGATION_API_JAR_TASK_PREFIX + taskSuffix;
    }

    public Jar getNavigationApiJarTask(String taskSuffix) {
        return (Jar) project.getTasks().findByName(getNavigationApiJarTaskName(taskSuffix));
    }

    public Jar createNavigationApiJarTask(JavaCompile navigationApiCompileTask, String taskSuffix) {
        String jarTaskName = getNavigationApiJarTaskName(taskSuffix);
        Jar jarTask = (Jar) project.getTasks().findByName(jarTaskName);
        if (jarTask == null) {
            jarTask = project.getTasks().create(jarTaskName, Jar.class);
            jarTask.setBaseName(project.getName() + "-navigationApi" + taskSuffix);
            jarTask.from(navigationApiCompileTask.getDestinationDir());
            jarTask.dependsOn(navigationApiCompileTask);
        }
        return jarTask;
    }

    public void detectNavigationApiDependenciesAndGenerateHensonNavigator(BaseVariant variant, String hensonNavigatorPackageName) {
        Task taskDetectModules = project.getTasks().create("detectModule" + capitalize(variant.getName()));
        taskDetectModules.doFirst(task -> {
            Configuration clientInternalConfiguration = configurationManager.getClientInternalConfiguration(variant);
            System.out.println("Before resolve 2");
            clientInternalConfiguration.resolve();
            System.out.println("After resolve 2");
            Set<String> targetActivities = new HashSet();
            clientInternalConfiguration.getFiles().forEach(dependency -> {
                if (dependency.getName().matches(".*-navigationApi.*.jar")) {
                    logger.debug("Detected navigation API dependency: %s", dependency.getName());
                    File file = dependency.getAbsoluteFile();
                    List<String> entries = getJarContent(file);
                    entries.forEach(entry -> {
                        if (entry.matches(".*__IntentBuilder.class")) {
                            logger.debug("Detected intent builder: %s", entry);
                            String targetActivityFQN = entry.substring(0, entry.length() - "__IntentBuilder.class".length()).replace('/', '.');
                            targetActivities.add(targetActivityFQN);
                        }
                    });
                }

                File variantSrcFolder = new File(project.getProjectDir(), "src/" + variant.getName() + "/java/");
                String hensonNavigator = hensonNavigatorGenerator.generateHensonNavigatorClass(targetActivities, hensonNavigatorPackageName);
                variantSrcFolder.mkdirs();
                String generatedFolderName = hensonNavigatorPackageName.replace('.', '/').concat("/");
                File generatedFolder = new File(variantSrcFolder, generatedFolderName);
                generatedFolder.mkdirs();
                File generatedFile = new File(generatedFolder, "HensonNavigator.java");
                try {
                    Files.write(generatedFile.toPath(), singletonList(hensonNavigator));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        //we put the task right before compilation so that all dependencies are resolved
        // when the task is executed
        taskDetectModules.setDependsOn(variant.getJavaCompiler().getDependsOn());
        variant.getJavaCompiler().dependsOn(taskDetectModules);
    }

    public Task createEmptyNavigationApiCompileTask(String taskSuffix) {
        return project.getTasks().create(NAVIGATION_API_COMPILE_TASK_PREFIX + taskSuffix);
    }

    public Task createEmptyNavigationApiJarTask(String taskSuffix) {
        return project.getTasks().create(NAVIGATION_API_JAR_TASK_PREFIX + taskSuffix);
    }

    private List<String> getJarContent(File file) {
        final List<String> result = new ArrayList<>();
        try {
            if (file.getName().endsWith(".jar")) {
                ZipFile zip = new ZipFile(file);
                Collections.list(zip.entries())
                        .stream()
                        .map(ZipEntry::getName)
                        .forEach(result::add);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
