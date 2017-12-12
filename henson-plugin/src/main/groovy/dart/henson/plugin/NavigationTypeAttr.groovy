package dart.henson.plugin

import org.gradle.api.Named
import org.gradle.api.attributes.Attribute

interface NavigationTypeAttr extends Named {
    Attribute<NavigationTypeAttr> ATTRIBUTE = Attribute.of(NavigationTypeAttr.class);

    String NAVIGATION = "Navigation"
}