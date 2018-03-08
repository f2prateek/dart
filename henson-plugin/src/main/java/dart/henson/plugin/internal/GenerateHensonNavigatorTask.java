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

import static com.android.build.gradle.internal.publishing.AndroidArtifacts.ARTIFACT_TYPE;
import static java.util.Collections.singletonList;

import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.internal.publishing.AndroidArtifacts;
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
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.UnionFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.JavaCompile;

@CacheableTask
public class GenerateHensonNavigatorTask extends DefaultTask {
  @InputFiles
  @Classpath
  FileCollection getJarDependencies() {
    //Thanks to Xavier Durcrohet for this
    //https://android.googlesource.com/platform/tools/base/+/gradle_3.0.0/build-system/gradle-core/src/main/java/com/android/build/gradle/internal/scope/VariantScopeImpl.java#1037
    Action<AttributeContainer> attributes =
        container ->
            container.attribute(ARTIFACT_TYPE, AndroidArtifacts.ArtifactType.CLASSES.getType());
    boolean lenientMode = false;
    return variant
        .getCompileConfiguration()
        .getIncoming()
        .artifactView(
            config -> {
              config.attributes(attributes);
              config.lenient(lenientMode);
            })
        .getArtifacts()
        .getArtifactFiles();
  }

  @Input String hensonNavigatorPackageName;

  File destinationFolder;

  @OutputFile
  public File getHensonNavigatorSourceFile() {
    String generatedFolderName = hensonNavigatorPackageName.replace('.', '/').concat("/");
    File generatedFolder = new File(destinationFolder, generatedFolderName);
    generatedFolder.mkdirs();
    return new File(generatedFolder, "HensonNavigator.java");
  }

  BaseVariant variant;
  Project project;
  Logger logger;
  HensonNavigatorGenerator hensonNavigatorGenerator;

  @TaskAction
  public void generateHensonNavigator() {
    JavaCompile javaCompiler = (JavaCompile) variant.getJavaCompiler();
    FileCollection variantCompileClasspath = getJarDependencies();
    FileCollection uft =
        new UnionFileCollection(javaCompiler.getSource(), project.fileTree(destinationFolder));
    javaCompiler.setSource(uft);
    logger.debug("Analyzing configuration: " + variantCompileClasspath.getFiles());
    Set<String> targetActivities = new HashSet<>();
    Streams.stream(variantCompileClasspath)
        .forEach(
            dependency -> {
              logger.debug("Detected dependency: {}", dependency.getAbsolutePath());
              if (dependency.getName().endsWith(".jar")) {
                logger.debug("Detected navigation API dependency: {}", dependency.getName());
                if (!dependency.exists()) {
                  logger.debug("Dependency jar doesn't exist {}", dependency.getAbsolutePath());
                } else {
                  File file = dependency.getAbsoluteFile();
                  List<String> entries = getJarContent(file);
                  entries.forEach(
                      entry -> {
                        if (entry.matches(".*__IntentBuilder.class")) {
                          logger.debug("Detected intent builder: {}", entry);
                          String targetActivityFQN =
                              entry
                                  .substring(0, entry.length() - "__IntentBuilder.class".length())
                                  .replace('/', '.');
                          targetActivities.add(targetActivityFQN);
                        }
                      });
                }
              }
            });
    String hensonNavigator =
        hensonNavigatorGenerator.generateHensonNavigatorClass(
            targetActivities, hensonNavigatorPackageName);
    destinationFolder.mkdirs();
    String generatedFolderName = hensonNavigatorPackageName.replace('.', '/').concat("/");
    File generatedFolder = new File(destinationFolder, generatedFolderName);
    generatedFolder.mkdirs();
    File generatedFile = getHensonNavigatorSourceFile();
    try {
      logger.debug("Generating Henson navigator in " + generatedFile.getAbsolutePath());
      logger.debug(hensonNavigator);
      Files.write(generatedFile.toPath(), singletonList(hensonNavigator));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
