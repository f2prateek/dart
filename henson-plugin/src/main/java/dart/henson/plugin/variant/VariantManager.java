package dart.henson.plugin.variant;

import com.android.build.gradle.api.BaseVariant;
import com.android.builder.model.ProductFlavor;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;

import java.util.List;
import java.util.Map;

import static dart.henson.plugin.internal.ConfigurationManager.NAVIGATION_CONFIGURATION_SUFFIX_ANNOTATION_PROCESSOR;
import static dart.henson.plugin.internal.ConfigurationManager.NAVIGATION_CONFIGURATION_SUFFIX_API;
import static dart.henson.plugin.internal.ConfigurationManager.NAVIGATION_CONFIGURATION_SUFFIX_COMPILE_ONLY;
import static dart.henson.plugin.internal.ConfigurationManager.NAVIGATION_CONFIGURATION_SUFFIX_IMPLEMENTATION;
import static java.util.stream.Collectors.toList;

public class VariantManager {

    private Logger logger;

    public VariantManager(Logger logger) {
        this.logger = logger;
    }

    public NavigationVariant createNavigationVariant(BaseVariant variant) {
        NavigationVariant navigationVariant = new NavigationVariant();
        navigationVariant.variant = variant;
        List<String> flavorNamesAndBuildType = variant.getProductFlavors()
                .stream()
                .map(ProductFlavor::getName)
                .collect(toList());
        flavorNamesAndBuildType.add(variant.getBuildType().getName());
        logger.debug("FlavorNamesAndBuildType: %s", flavorNamesAndBuildType);
        return navigationVariant;
    }

    public void addNavigationConfigurationsToNavigationVariant(NavigationVariant navigationVariant, Map<String, Configuration> navigationConfigurations) {
        Configuration api = navigationConfigurations.get(NAVIGATION_CONFIGURATION_SUFFIX_API);
        Configuration implementation = navigationConfigurations.get(NAVIGATION_CONFIGURATION_SUFFIX_IMPLEMENTATION);
        Configuration compileOnly = navigationConfigurations.get(NAVIGATION_CONFIGURATION_SUFFIX_COMPILE_ONLY);
        Configuration annotationProcessors = navigationConfigurations.get(NAVIGATION_CONFIGURATION_SUFFIX_ANNOTATION_PROCESSOR);
        navigationVariant.apiConfigurations.add(api);
        navigationVariant.implementationConfigurations.add(implementation);
        navigationVariant.compileOnlyConfigurations.add(compileOnly);
        navigationVariant.annotationProcessorConfigurations.add(annotationProcessors);
    }
}
