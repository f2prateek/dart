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

package dart.henson.plugin.variant;

import com.android.build.gradle.api.BaseVariant;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;

/** Represents an enriched variant for navigation purposes. */
public class NavigationVariant {
  public BaseVariant variant;
  public String name = "";
  public List<SourceSet> sourceSets = new ArrayList();
  public List<Configuration> apiConfigurations = new ArrayList();
  public List<Configuration> implementationConfigurations = new ArrayList();
  public List<Configuration> compileOnlyConfigurations = new ArrayList();
  public List<Configuration> annotationProcessorConfigurations = new ArrayList();
  public JavaCompile compilerTask;
  public Jar jarTask;
  public Configuration clientInternalConfiguration;
}
