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
import static java.util.Collections.singletonList;

import com.android.build.gradle.api.BaseVariant;
import com.google.common.collect.Streams;
import dart.henson.plugin.generator.HensonNavigatorGenerator;
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
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.UnionFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.compile.JavaCompile;

public class TaskManager {

  private Project project;
  private Logger logger;
  private HensonNavigatorGenerator hensonNavigatorGenerator;

  public TaskManager(Project project, Logger logger) {
    this.project = project;
    this.logger = logger;
    this.hensonNavigatorGenerator = new HensonNavigatorGenerator();
  }

  /**
   * A henson navigator is a class that helps a consumer to consume the navigation api that it
   * declares in its dependencies. The henson navigator will wrap the intent builders. Thus, a
   * henson navigator, is driven by consumption of intent builders, whereas the henson classes are
   * driven by the production of an intent builder.
   *
   * <p>This task is created per android variant:
   *
   * <ul>
   *   <li>we scan the variant compile configuration for navigation api dependencies
   *   <li>we generate a henson navigator class for this variant that wraps the intent builders
   * </ul>
   *
   * @param variant the variant for which to create a builder.
   * @param hensonNavigatorPackageName the package name in which we create the class.
   */
  public Task createHensonNavigatorGenerationTask(
      BaseVariant variant, String hensonNavigatorPackageName, File destinationFolder) {
    Task generateHensonNavigatorTask =
        project.getTasks().create("generate" + capitalize(variant.getName()) + "HensonNavigator");
    generateHensonNavigatorTask.doFirst(
        task -> {
          JavaCompile javaCompiler = (JavaCompile) variant.getJavaCompiler();
          FileCollection variantCompileClasspath = javaCompiler.getClasspath();
          FileCollection uft =
              new UnionFileCollection(
                  javaCompiler.getSource(), project.fileTree(destinationFolder));
          javaCompiler.setSource(uft);
          logger.debug("Analyzing configuration: " + variantCompileClasspath.getFiles());
          Set<String> targetActivities = new HashSet<>();
          Streams.stream(variantCompileClasspath)
              .forEach(
                  dependency -> {
                    logger.debug("Detected dependency: {}", dependency.getName());
                    if (dependency.getName().matches("classes.jar")) {
                      logger.debug("Detected navigation API dependency: {}", dependency.getName());
                      File file = dependency.getAbsoluteFile();
                      List<String> entries = getJarContent(file);
                      entries.forEach(
                          entry -> {
                            if (entry.matches(".*__IntentBuilder.class")) {
                              logger.debug("Detected intent builder: {}", entry);
                              String targetActivityFQN =
                                  entry
                                      .substring(
                                          0, entry.length() - "__IntentBuilder.class".length())
                                      .replace('/', '.');
                              targetActivities.add(targetActivityFQN);
                            }
                          });
                    }

                    String hensonNavigator =
                        hensonNavigatorGenerator.generateHensonNavigatorClass(
                            targetActivities, hensonNavigatorPackageName);
                    destinationFolder.mkdirs();
                    String generatedFolderName =
                        hensonNavigatorPackageName.replace('.', '/').concat("/");
                    File generatedFolder = new File(destinationFolder, generatedFolderName);
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
    return generateHensonNavigatorTask;
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
