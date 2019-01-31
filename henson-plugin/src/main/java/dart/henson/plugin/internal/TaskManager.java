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

import com.android.build.gradle.api.BaseVariant;
import dart.henson.plugin.generator.HensonNavigatorGenerator;
import java.io.File;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskProvider;

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
  public TaskProvider<GenerateHensonNavigatorTask> createHensonNavigatorGenerationTask(
      BaseVariant variant, String hensonNavigatorPackageName, File destinationFolder) {
    TaskProvider<GenerateHensonNavigatorTask> generateHensonNavigatorTask =
        project
            .getTasks()
            .register(
                "generate" + capitalize(variant.getName()) + "HensonNavigator",
                GenerateHensonNavigatorTask.class,
                (Action<GenerateHensonNavigatorTask>)
                    generateHensonNavigatorTask1 -> {
                      generateHensonNavigatorTask1.hensonNavigatorPackageName =
                          hensonNavigatorPackageName;
                      generateHensonNavigatorTask1.destinationFolder = destinationFolder;
                      generateHensonNavigatorTask1.variant = variant;
                      generateHensonNavigatorTask1.logger = logger;
                      generateHensonNavigatorTask1.project = project;
                      generateHensonNavigatorTask1.hensonNavigatorGenerator =
                          hensonNavigatorGenerator;
                    });
    return generateHensonNavigatorTask;
  }
}
