package dart.henson.plugin.internal;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

public class ArtifactManager {

    public static final String NAVIGATION_ARTIFACT_PREFIX = "NavigationArtifact";

    private Project project;
    private Logger logger;

    public ArtifactManager(Project project, Logger logger) {
        this.project = project;
        this.logger = logger;
    }

    public String getNavigationArtifactName(BaseVariant variant) {
        return variant.getName() + NAVIGATION_ARTIFACT_PREFIX;
    }
}
