package dart.henson.plugin.attributes;

import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.internal.dependency.AndroidTypeAttr;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeMatchingStrategy;
import org.gradle.api.attributes.AttributesSchema;
import org.gradle.api.attributes.Usage;
import org.gradle.internal.logging.slf4j.OutputEventListenerBackedLogger;

public class AttributeManager {

    private Project project;
    private OutputEventListenerBackedLogger logger;

    public AttributeManager(Project project, OutputEventListenerBackedLogger logger) {
        this.project = project;
        this.logger = logger;
    }

    public void applyNavigationAttributeMatchingStrategy() {
        AttributesSchema attributesSchema = project.getDependencies().getAttributesSchema();
        AttributeMatchingStrategy<NavigationTypeAttr> navigationAttrStrategy =
                attributesSchema.attribute(NavigationTypeAttr.ATTRIBUTE);
            navigationAttrStrategy.getCompatibilityRules().add(NavigationTypeAttrCompatRule.class);
            navigationAttrStrategy.getDisambiguationRules().add(NavigationTypeAttrDisambiguationRule.class);
    }

    public void applyAttributesFromVariantCompileToConfiguration(BaseVariant variant, Configuration configuration) {
        Configuration configFrom = variant.getCompileConfiguration();
        configFrom.getAttributes().keySet().stream().forEach( attributeKey -> {
            if(!attributeKey.getName().equals(Usage.class.getName())
                    && !attributeKey.getName().equals(AndroidTypeAttr.class.getName())) {
                Object value = configFrom.getAttributes().getAttribute(attributeKey);
                if (value != null) {
                    configuration.getAttributes().attribute(((Attribute<Object>)attributeKey), value);
                }
            }
        });
    }
}
