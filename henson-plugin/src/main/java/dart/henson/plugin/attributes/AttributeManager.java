package dart.henson.plugin.attributes;

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

import static org.gradle.api.attributes.Attribute.of;

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

    private void applyAttributesFromVariantCompileToConfiguration(BaseVariant variant, Configuration configuration) {
        Configuration configFrom = variant.getCompileConfiguration();
        configFrom.getAttributes().keySet().stream().forEach(attributeKey -> {
            if (!attributeKey.getName().equals(Usage.class.getName())
                    && !attributeKey.getName().equals(AndroidTypeAttr.class.getName())) {
                Object value = configFrom.getAttributes().getAttribute(attributeKey);
                if (value != null) {
                    configuration.getAttributes().attribute(((Attribute<Object>) attributeKey), value);
                }
            }
        });
    }

    private void applyNavigationAttributes(Configuration configuration) {
        NavigationTypeAttr value = factory.named(NavigationTypeAttr.class, NavigationTypeAttr.NAVIGATION);
        Attribute<NavigationTypeAttr> attributeKey = of(NavigationTypeAttr.class);
        configuration.getAttributes().attribute(attributeKey, value);

        System.out.println("attr: " + configuration.getName() + " attrs: " + configuration.getAttributes().keySet());
    }
}
