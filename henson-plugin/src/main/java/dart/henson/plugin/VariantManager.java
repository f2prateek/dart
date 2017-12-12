package dart.henson.plugin;

import com.android.build.gradle.api.BaseVariant;
import com.android.builder.model.ProductFlavor;

import org.gradle.internal.logging.slf4j.OutputEventListenerBackedLogger;

import java.util.List;

import static java.util.stream.Collectors.*;

public class VariantManager {

    private OutputEventListenerBackedLogger logger;

    public VariantManager(OutputEventListenerBackedLogger logger) {
        this.logger = logger;
    }

    public NavigationVariant createNavigationVariant(BaseVariant variant, Combinator combinator) {
        NavigationVariant navigationVariant = new NavigationVariant();
        navigationVariant.variant = variant;
        List<String> flavorNamesAndBuildType = variant.getProductFlavors()
                .stream()
                .map(ProductFlavor::getName)
                .collect(toList());
        flavorNamesAndBuildType.add(variant.getBuildType().getName());
        logger.debug("FlavorNamesAndBuildType: %s", flavorNamesAndBuildType);
        navigationVariant.combinations = combinator.combine(flavorNamesAndBuildType);
        logger.debug("Combinations: %s", navigationVariant.combinations);

        return navigationVariant;
    }

}
