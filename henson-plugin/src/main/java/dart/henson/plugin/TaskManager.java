package dart.henson.plugin;

import com.google.common.collect.Streams;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.UnionFileCollection;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.internal.logging.slf4j.OutputEventListenerBackedLogger;

import java.io.File;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.gradle.api.JavaVersion.VERSION_1_7;

public class TaskManager {
    public static final String NAVIGATION_API_COMPILE_TASK_PREFIX = "navigationApiCompileJava";
    public static final String NAVIGATION_API_JAR_TASK_PREFIX = "navigationApiJar";

    private Project project;
    private OutputEventListenerBackedLogger logger;

    public TaskManager(Project project, OutputEventListenerBackedLogger logger) {
        this.project = project;
        this.logger = logger;
    }

    public JavaCompile createNavigationApiCompileTask(String taskSuffix, String destinationPath, NavigationVariant navigationVariant) {
        File newDestinationDir = new File(project.getBuildDir(), "/navigation/classes/java/" + destinationPath);
        File newGeneratedDir = new File(project.getBuildDir(),"/generated/source/apt/navigation/" + destinationPath);

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
        if(compileTask == null) {
            compileTask = project.getTasks().create(compileTaskName, JavaCompile.class);
            List<FileTree> sources = navigationVariant.sourceSets
                    .stream()
                    .map(SourceSet::getJava)
                    .map(SourceDirectorySet::getAsFileTree)
                    .collect(toList());
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
        if( jarTask == null) {
            jarTask = project.getTasks().create(jarTaskName, Jar.class);
            jarTask.setBaseName(project.getName() + "-navigationApi" +taskSuffix);
            jarTask.from(navigationApiCompileTask.getDestinationDir());
            jarTask.dependsOn(navigationApiCompileTask);
        }
        return jarTask;
    }

    public Task createEmptyNavigationApiCompileTask(Project project, String taskSuffix) {
        return project.getTasks().create(NAVIGATION_API_COMPILE_TASK_PREFIX + taskSuffix);
    }

    public Task createEmptyNavigationApiJarTask(Project project, String taskSuffix) {
        return project.getTasks().create(NAVIGATION_API_JAR_TASK_PREFIX + taskSuffix);
    }

}
