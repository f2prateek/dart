package dart.henson.plugin.attributes;

import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;

/**
 * Navigation attributes used for matching consumer
 * and producer projects. It will be used to decorate
 * artifacts in both projects configurations.
 */
interface NavigationTypeAttr extends Named {
    Attribute<NavigationTypeAttr> ATTRIBUTE = Attribute.of(NavigationTypeAttr.class);

    String NAVIGATION = "Navigation";
}
