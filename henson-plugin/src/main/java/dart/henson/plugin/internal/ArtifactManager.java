package dart.henson.plugin.internal;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.logging.Logger;

/**
 * We create one artifact per variant.
 * It will include all the classes of the navigation source tree (code + generated code).
 */
public class ArtifactManager {

    public static final String NAVIGATION_ARTIFACT_PREFIX = "NavigationArtifact";

    private Logger logger;

    public ArtifactManager(Logger logger) {
        this.logger = logger;
    }

    public String getNavigationArtifactName(BaseVariant variant) {
        return variant.getName() + NAVIGATION_ARTIFACT_PREFIX;
    }
}
