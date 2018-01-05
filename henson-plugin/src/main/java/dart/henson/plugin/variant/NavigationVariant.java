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

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;

/**
 * Ideally, our plugin would be variant aware and would let us define multiple navigation source
 * sets and configurations to match the build types, flavors and variants of the main source code.
 * However, there is a limitation in the current gradle android plugin and we can't do that.
 * We are falling back on a single source set that will contain all the navigation models, for all
 * activities of a module (the union of all variants of the module). This is sad but we can't do it
 * well.
 *
 * This class represents the navigation source set and its various configurations to compile
 * and jar it.
 */
public class NavigationVariant {
  public String name = "";
  public SourceSet sourceSet;
  public Configuration apiConfiguration;
  public Configuration implementationConfiguration;
  public Configuration compileOnlyConfiguration;
  public Configuration annotationProcessorConfiguration;
  public JavaCompile compilerTask;
  public Jar jarTask;
}
