package dart.henson.plugin.attributes;

import org.gradle.api.attributes.AttributeDisambiguationRule;
import org.gradle.api.attributes.MultipleCandidatesDetails;

import java.util.Set;

import javax.inject.Inject;

/**
 * Custom Compat rule to handle the different values of AndroidTypeAttr.
 */
public final class NavigationTypeAttrDisambiguationRule
        implements AttributeDisambiguationRule<NavigationTypeAttr> {

    @Inject
    public NavigationTypeAttrDisambiguationRule() {
    }

    @Override
    public void execute(MultipleCandidatesDetails<NavigationTypeAttr> details) {
        System.out.println("executing NavigationTypeAttrDisambiguationRule");
        Set<NavigationTypeAttr> candidates = details.getCandidateValues();
        final NavigationTypeAttr consumerValue = details.getConsumerValue();


        if (candidates.contains(consumerValue)) {
            details.closestMatch(consumerValue);
        }
    }
}