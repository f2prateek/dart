package dart.henson.plugin.variant;

import com.android.build.gradle.api.BaseVariant;
import com.android.builder.model.ProductFlavor;

import org.gradle.api.logging.Logger;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class VariantManager {

    private Logger logger;

    public VariantManager(Logger logger) {
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
