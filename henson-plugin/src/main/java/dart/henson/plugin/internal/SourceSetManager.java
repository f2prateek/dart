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

import static java.util.Collections.singletonList;

import com.android.build.gradle.api.BaseVariant;
import com.android.builder.model.BuildType;
import com.android.builder.model.ProductFlavor;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

/**
 * We create multiple sourceset in the project:
 *
 * <ul>
 *   <li>a navigation source set per variant in src/navigation/&lt;<variant name>&gt;/java
 *   <li>a navigation source set per build type in src/navigation/&lt;<build type name>&gt;/java
 *   <li>a navigation source set per product flavor in src/navigation/&lt;<product flavor
 *       name>&gt;/java
 *   <li>a main navigation source set in src/navigation/main/java
 * </ul>
 *
 * They are taken into account in this order (we currently don't merge and priority is not tested).
 */
public class SourceSetManager {
  private static final String NAVIGATION_SOURCESET_RADIX = "navigationSourceSet";
  private static final String NAVIGATION_SOURCESET_SUFFIX = "NavigationSourceSet";

  private Project project;
  private Logger logger;

  public SourceSetManager(Project project, Logger logger) {
    this.project = project;
    this.logger = logger;
  }

  public SourceSet maybeCreateNavigationSourceSet() {
    String newSourceSetName = NAVIGATION_SOURCESET_RADIX;
    String newSourceSetPath = getSourceSetPath("main");
    return maybeCreateNavigationSourceSet(newSourceSetName, newSourceSetPath);
  }

  public SourceSet maybeCreateNavigationSourceSet(BuildType buildType) {
    String name = buildType.getName();
    String newSourceSetName = getSourceSetName(name);
    String newSourceSetPath = getSourceSetPath(name);
    return maybeCreateNavigationSourceSet(newSourceSetName, newSourceSetPath);
  }

  public SourceSet maybeCreateNavigationSourceSet(ProductFlavor productFlavor) {
    String name = productFlavor.getName();
    String newSourceSetName = getSourceSetName(name);
    String newSourceSetPath = getSourceSetPath(name);
    return maybeCreateNavigationSourceSet(newSourceSetName, newSourceSetPath);
  }

  public SourceSet maybeCreateNavigationSourceSet(BaseVariant variant) {
    String name = variant.getName();
    String newSourceSetName = getSourceSetName(name);
    String newSourceSetPath = getSourceSetPath(name);
    return maybeCreateNavigationSourceSet(newSourceSetName, newSourceSetPath);
  }

  public List<SourceSet> getAllNavigationSourceSets() {
    return new ArrayList<>(getSourceSets());
  }

  private String getSourceSetName(String name) {
    return name + NAVIGATION_SOURCESET_SUFFIX;
  }

  private String getSourceSetPath(String name) {
    return "src/navigation/" + name + "/java";
  }

  private SourceSet maybeCreateNavigationSourceSet(
      String newSourceSetName, String newSourceSetPath) {
    logger.debug("Creating sourceSet: " + newSourceSetName + " with root in " + newSourceSetPath);
    SourceSet sourceSet = getSourceSets().maybeCreate(newSourceSetName);
    sourceSet.getJava().setSrcDirs(singletonList(newSourceSetPath));
    return sourceSet;
  }

  private SourceSetContainer getSourceSets() {
    return project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
  }
}
