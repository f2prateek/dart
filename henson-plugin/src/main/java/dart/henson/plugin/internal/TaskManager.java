/*
 * Copyright 2013 Jake Wharton
 * Copyright 2014 Prateek Srivastava (@f2prateek)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dart.henson.plugin.internal;

import static dart.henson.plugin.util.StringUtil.capitalize;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.gradle.api.JavaVersion.VERSION_1_7;

import com.android.build.gradle.api.BaseVariant;
import com.android.builder.model.Variant;
import com.google.common.collect.Streams;
import dart.henson.plugin.generator.HensonNavigatorGenerator;
import dart.henson.plugin.variant.NavigationVariant;
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

public class TaskManager {
  public static final String NAVIGATION_API_COMPILE_TASK_PREFIX = "navigationApiCompileJava";
  public static final String NAVIGATION_API_JAR_TASK_PREFIX = "navigationApiJar";
  private static final String LIST_ALL_SOURCESETS_TASK = "navigationSourceSets";

  private Project project;
  private Logger logger;
  private HensonNavigatorGenerator hensonNavigatorGenerator;

  public TaskManager(Project project, Logger logger) {
    this.project = project;
    this.logger = logger;
    this.hensonNavigatorGenerator = new HensonNavigatorGenerator();
  }

  public void createNavigationCompilerAndJarTasks(NavigationVariant navigationVariant) {
    createNavigationApiCompileTask(navigationVariant);
    createNavigationApiJarTask(navigationVariant);
  }

    /**
     * A henson navigator is a class that helps a consumer to consume the navigation api that it
     * declares in its dependencies. The henson navigator will wrap the intent builders.
     * Thus, a henson navigator, is driven by consumption of intent builders, whereas the
     * henson classes are driven by the production of an intent builder.
     *
     * This task is created per android variant:
     * <ul>
     *     <li>we scan the variant compile configuration for navigation api dependencies</li>
     *     <li>we generate a henson navigator class for this variant that wraps the intent builders</li>
     * </ul>
     * @param variant the variant for which to create a builder.
     * @param hensonNavigatorPackageName the package name in which we create the class.
     */
  public void createHensonNavigatorGenerationTask(BaseVariant variant,
                                                  String hensonNavigatorPackageName) {
    Task generateHensonNavigatorTask =
        project.getTasks().create("generate" + capitalize(variant.getName()) + "HensonNavigator");
    generateHensonNavigatorTask.doFirst(
        task -> {
            Configuration clientInternalConfiguration = variant.getCompileConfiguration();
          logger.debug("Analyzing configuration: " + clientInternalConfiguration.getName());
          clientInternalConfiguration.resolve();
          Set<String> targetActivities = new HashSet<>();
          Streams.stream(clientInternalConfiguration)
              .forEach(
                  dependency -> {
                    logger.debug("Detected dependency: %s", dependency.getName());
                    if (dependency.getName().matches(".*-navigationApi.*.jar")) {
                      logger.debug("Detected navigation API dependency: %s", dependency.getName());
                      File file = dependency.getAbsoluteFile();
                      List<String> entries = getJarContent(file);
                      entries.forEach(
                          entry -> {
                            if (entry.matches(".*__IntentBuilder.class")) {
                              logger.debug("Detected intent builder: %s", entry);
                              String targetActivityFQN =
                                  entry
                                      .substring(
                                          0, entry.length() - "__IntentBuilder.class".length())
                                      .replace('/', '.');
                              targetActivities.add(targetActivityFQN);
                            }
                          });
                    }

                    File variantSrcFolder =
                        new File(project.getProjectDir(), "src/" + variant.getName() + "/java/");
                    String hensonNavigator =
                        hensonNavigatorGenerator.generateHensonNavigatorClass(
                            targetActivities, hensonNavigatorPackageName);
                    variantSrcFolder.mkdirs();
                    String generatedFolderName =
                        hensonNavigatorPackageName.replace('.', '/').concat("/");
                    File generatedFolder = new File(variantSrcFolder, generatedFolderName);
                    generatedFolder.mkdirs();
                    File generatedFile = new File(generatedFolder, "HensonNavigator.java");
                    try {
                      logger.debug(
                          "Generating Henson navigator in " + generatedFile.getAbsolutePath());
                      logger.debug(hensonNavigator);
                      Files.write(generatedFile.toPath(), singletonList(hensonNavigator));
                    } catch (IOException e) {
                      throw new RuntimeException(e);
                    }
                  });
        });
    //we put the task right before compilation so that all dependencies are resolved
    // when the task is executed
    generateHensonNavigatorTask.setDependsOn(variant.getJavaCompiler().getDependsOn());
    variant.getJavaCompiler().dependsOn(generateHensonNavigatorTask);
  }

  public void createListSourceSetTask(List<SourceSet> javaSourceSets) {
    Task task = project.getTasks().create(LIST_ALL_SOURCESETS_TASK);
    task.setActions(
        singletonList(
            task1 -> {
              logger.lifecycle("\n");
              javaSourceSets
                  .stream()
                  .forEach(
                      sourceSet -> {
                        String sourceSetName = sourceSet.getName();
                        logger.lifecycle(sourceSetName);

                        //separator
                        StringBuilder builder = new StringBuilder();
                        for (int indexChar = 0; indexChar < sourceSetName.length(); indexChar++) {
                          builder.append('-');
                        }
                        logger.lifecycle(builder.toString());

                        logger.lifecycle("build.gradle name: project.sourceSet" + sourceSetName);
                        logger.lifecycle("java sources: " + sourceSet.getJava().getSrcDirs());

                        logger.lifecycle("\n");
                      });
            }));
  }

  private void createNavigationApiCompileTask(NavigationVariant navigationVariant) {
    String taskSuffix = capitalize(navigationVariant.name);
    String destinationPath = navigationVariant.name + "/";
    File newDestinationDir =
        new File(project.getBuildDir(), "/navigation/classes/java/" + destinationPath);
    File newGeneratedDir =
        new File(project.getBuildDir(), "/generated/source/apt/navigation/" + destinationPath);

    FileCollection effectiveClasspath = new UnionFileCollection();
    effectiveClasspath.add(navigationVariant.apiConfiguration);
    effectiveClasspath.add(navigationVariant.implementationConfiguration);
    effectiveClasspath.add(navigationVariant.compileOnlyConfiguration);

    FileCollection effectiveAnnotationProcessorPath = new UnionFileCollection();
    effectiveAnnotationProcessorPath.add(navigationVariant.annotationProcessorConfiguration);

    String compileTaskName = NAVIGATION_API_COMPILE_TASK_PREFIX + taskSuffix;
    JavaCompile compileTask = (JavaCompile) project.getTasks().findByName(compileTaskName);
    if (compileTask == null) {
      compileTask = project.getTasks().create(compileTaskName, JavaCompile.class);
      List<FileTree> sources = singletonList(
          navigationVariant
              .sourceSet
                  .getJava()
                  .getAsFileTree());
      String javaVersion = VERSION_1_7.toString();

      compileTask.setSource(sources);
      compileTask.setDestinationDir(newDestinationDir);
      compileTask.setClasspath(effectiveClasspath);
      CompileOptions options = compileTask.getOptions();
      options.setCompilerArgs(asList("-s", newGeneratedDir.getAbsolutePath()));
      options.setAnnotationProcessorPath(effectiveAnnotationProcessorPath);
      compileTask.setTargetCompatibility(javaVersion);
      compileTask.setSourceCompatibility(javaVersion);
      compileTask.doFirst(
          task -> {
            newGeneratedDir.mkdirs();
            newDestinationDir.mkdirs();
          });
    }
    navigationVariant.compilerTask = compileTask;
  }

  private void createNavigationApiJarTask(NavigationVariant navigationVariant) {
    JavaCompile navigationApiCompileTask = navigationVariant.compilerTask;
    String taskSuffix = capitalize(navigationVariant.name);
    String jarTaskName = NAVIGATION_API_JAR_TASK_PREFIX + taskSuffix;
    Jar jarTask = project.getTasks().create(jarTaskName, Jar.class);
    jarTask.setBaseName(project.getName() + "-navigationApi" + taskSuffix);
    jarTask.from(navigationApiCompileTask.getDestinationDir());
    jarTask.dependsOn(navigationApiCompileTask);
    navigationVariant.jarTask = jarTask;
  }

  private List<String> getJarContent(File file) {
    final List<String> result = new ArrayList<>();
    try {
      if (file.getName().endsWith(".jar")) {
        ZipFile zip = new ZipFile(file);
        Collections.list(zip.entries()).stream().map(ZipEntry::getName).forEach(result::add);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return result;
  }
}
