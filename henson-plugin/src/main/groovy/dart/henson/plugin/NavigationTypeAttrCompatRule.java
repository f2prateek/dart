package dart.henson.plugin;

import org.gradle.api.attributes.AttributeCompatibilityRule;
import org.gradle.api.attributes.CompatibilityCheckDetails;

import javax.inject.Inject;

/** Custom Compat rule to handle the different values of AndroidTypeAttr. */
public final class NavigationTypeAttrCompatRule
        implements AttributeCompatibilityRule<NavigationTypeAttr> {

    @Inject
    public NavigationTypeAttrCompatRule() {}

    @Override
    public void execute(CompatibilityCheckDetails<NavigationTypeAttr> details) {
        System.out.println("executing NavigationTypeAttrCompatRule");
        final NavigationTypeAttr producerValue = details.getProducerValue();
        final NavigationTypeAttr consumerValue = details.getConsumerValue();
        if (producerValue == null) {
            if(consumerValue==null) {
               details.compatible();
            } else {
                details.incompatible();
            }
        } else if (producerValue.equals(consumerValue)) {
            details.compatible();
        } else {
            details.incompatible();
        }
    }
}