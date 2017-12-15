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

import static dart.henson.plugin.internal.ConfigurationManager.NAVIGATION_CONFIGURATION_SUFFIX_ANNOTATION_PROCESSOR;
import static dart.henson.plugin.internal.ConfigurationManager.NAVIGATION_CONFIGURATION_SUFFIX_API;
import static dart.henson.plugin.internal.ConfigurationManager.NAVIGATION_CONFIGURATION_SUFFIX_COMPILE_ONLY;
import static dart.henson.plugin.internal.ConfigurationManager.NAVIGATION_CONFIGURATION_SUFFIX_IMPLEMENTATION;
import static java.util.stream.Collectors.toList;

import com.android.build.gradle.api.BaseVariant;
import com.android.builder.model.ProductFlavor;
import java.util.List;
import java.util.Map;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;

public class VariantManager {

  private Logger logger;

  public VariantManager(Logger logger) {
    this.logger = logger;
  }

  public NavigationVariant createNavigationVariant(BaseVariant variant) {
    NavigationVariant navigationVariant = new NavigationVariant();
    navigationVariant.variant = variant;
    List<String> flavorNamesAndBuildType =
        variant.getProductFlavors().stream().map(ProductFlavor::getName).collect(toList());
    flavorNamesAndBuildType.add(variant.getBuildType().getName());
    logger.debug("FlavorNamesAndBuildType: %s", flavorNamesAndBuildType);
    return navigationVariant;
  }

  public void addNavigationConfigurationsToNavigationVariant(
      NavigationVariant navigationVariant, Map<String, Configuration> navigationConfigurations) {
    Configuration api = navigationConfigurations.get(NAVIGATION_CONFIGURATION_SUFFIX_API);
    Configuration implementation =
        navigationConfigurations.get(NAVIGATION_CONFIGURATION_SUFFIX_IMPLEMENTATION);
    Configuration compileOnly =
        navigationConfigurations.get(NAVIGATION_CONFIGURATION_SUFFIX_COMPILE_ONLY);
    Configuration annotationProcessors =
        navigationConfigurations.get(NAVIGATION_CONFIGURATION_SUFFIX_ANNOTATION_PROCESSOR);
    navigationVariant.apiConfigurations.add(api);
    navigationVariant.implementationConfigurations.add(implementation);
    navigationVariant.compileOnlyConfigurations.add(compileOnly);
    navigationVariant.annotationProcessorConfigurations.add(annotationProcessors);
  }
}
