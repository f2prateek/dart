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

package dart.henson.plugin.attributes;

import static org.gradle.api.attributes.Attribute.of;

import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.internal.dependency.AndroidTypeAttr;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeMatchingStrategy;
import org.gradle.api.attributes.AttributesSchema;
import org.gradle.api.attributes.Usage;
import org.gradle.api.logging.Logger;
import org.gradle.api.model.ObjectFactory;

/**
 * Responsible for assigning matching attributes to both the consumer and the producer
 * configurations. This enables our plugin to be variant aware. Basically, a consumer project can
 * request a navigation dependency from a producer project by using <tt>
 *
 * <pre>
 *     consumer.dependencies {
 *         navigation project(':producer')
 *     }
 * </pre>
 *
 * </tt> We will then create a special configuration that extends 'navigation' (to obtain the
 * dependencies) but that will receive attributes including 1) navigation 2) variant. This
 * configuration will match the producer's navigation artifacts per variant. It will trigger a task
 * to compile the variant dependent navigation sourcesets and jar them and create the artifact.
 *
 * <p>In order to benefit from the matching variant attribute mechanism of android, we steal the
 * variant attibutes from a standard configuration and filter them, then we add a navigation
 * attribute.
 *
 * <p>We should add more tests and use spock DSL to do so:
 * https://github.com/gradle/gradle/blob/master/subprojects/dependency-management/src/integTest/groovy/org/gradle/integtests/resolve/AbstractConfigurationAttributesResolveIntegrationTest.groovy#L25
 */
public class AttributeManager {

  private Project project;
  private Logger logger;
  private ObjectFactory factory;

  public AttributeManager(Project project, Logger logger) {
    this.project = project;
    this.logger = logger;
    this.factory = project.getObjects();
  }

  public void applyNavigationAttributeMatchingStrategy() {
    AttributesSchema attributesSchema = project.getDependencies().getAttributesSchema();
    AttributeMatchingStrategy<NavigationTypeAttr> navigationAttrStrategy =
        attributesSchema.attribute(NavigationTypeAttr.ATTRIBUTE);
    navigationAttrStrategy.getCompatibilityRules().add(NavigationTypeAttrCompatRule.class);
    navigationAttrStrategy.getDisambiguationRules().add(NavigationTypeAttrDisambiguationRule.class);
  }

  public void applyAttributes(BaseVariant variant, Configuration configuration) {
    applyAttributesFromVariantCompileToConfiguration(variant, configuration);
    applyNavigationAttributes(configuration);
  }

  private void applyAttributesFromVariantCompileToConfiguration(
      BaseVariant variant, Configuration configuration) {
    Configuration configFrom = variant.getCompileConfiguration();
    configFrom
        .getAttributes()
        .keySet()
        .stream()
        .forEach(
            attributeKey -> {
              if (!attributeKey.getName().equals(Usage.class.getName())
                  && !attributeKey.getName().equals(AndroidTypeAttr.class.getName())) {
                Object value = configFrom.getAttributes().getAttribute(attributeKey);
                if (value != null) {
                  configuration
                      .getAttributes()
                      .attribute(((Attribute<Object>) attributeKey), value);
                }
              }
            });
  }

  private void applyNavigationAttributes(Configuration configuration) {
    NavigationTypeAttr value =
        factory.named(NavigationTypeAttr.class, NavigationTypeAttr.NAVIGATION);
    Attribute<NavigationTypeAttr> attributeKey = of(NavigationTypeAttr.class);
    configuration.getAttributes().attribute(attributeKey, value);

    logger.debug(
        "navigation attributes for configuration: "
            + configuration.getName()
            + " are : "
            + configuration.getAttributes().keySet());
  }
}
