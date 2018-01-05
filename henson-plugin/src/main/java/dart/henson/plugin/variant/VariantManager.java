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
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.SourceSet;

import java.util.Map;

import static dart.henson.plugin.internal.ConfigurationManager.NAVIGATION_CONFIGURATION_SUFFIX_ANNOTATION_PROCESSOR;
import static dart.henson.plugin.internal.ConfigurationManager.NAVIGATION_CONFIGURATION_SUFFIX_API;
import static dart.henson.plugin.internal.ConfigurationManager.NAVIGATION_CONFIGURATION_SUFFIX_COMPILE_ONLY;
import static dart.henson.plugin.internal.ConfigurationManager.NAVIGATION_CONFIGURATION_SUFFIX_IMPLEMENTATION;

public class VariantManager {

  private Logger logger;

  public VariantManager(Logger logger) {
    this.logger = logger;
  }

  public NavigationVariant createNavigationVariant(SourceSet sourceSet,
                                                   Map<String, Configuration> mapSuffixToConfigurations) {
    NavigationVariant navigationVariant = new NavigationVariant();
    navigationVariant.sourceSet = sourceSet;
    addNavigationConfigurationsToNavigationVariant(navigationVariant, mapSuffixToConfigurations);
    return navigationVariant;
  }

  private void addNavigationConfigurationsToNavigationVariant(
      NavigationVariant navigationVariant, Map<String, Configuration> navigationConfigurations) {
    navigationVariant.apiConfiguration = navigationConfigurations.get(NAVIGATION_CONFIGURATION_SUFFIX_API);
    navigationVariant.implementationConfiguration =
        navigationConfigurations.get(NAVIGATION_CONFIGURATION_SUFFIX_IMPLEMENTATION);
    navigationVariant.compileOnlyConfiguration =
        navigationConfigurations.get(NAVIGATION_CONFIGURATION_SUFFIX_COMPILE_ONLY);
    navigationVariant.annotationProcessorConfiguration =
        navigationConfigurations.get(NAVIGATION_CONFIGURATION_SUFFIX_ANNOTATION_PROCESSOR);
  }
}
