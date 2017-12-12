package dart.henson.plugin;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.Project;
import org.gradle.internal.logging.slf4j.OutputEventListenerBackedLogger;

public class ArtifactManager {

    public static final String NAVIGATION_ARTIFACT_PREFIX = "navigation";

    private Project project;
    private OutputEventListenerBackedLogger logger;

    public ArtifactManager(Project project, OutputEventListenerBackedLogger logger) {
        this.project = project;
        this.logger = logger;
    }

    public String getNavigationArtifactName(BaseVariant variant) {
        return NAVIGATION_ARTIFACT_PREFIX + StringUtil.capitalize(variant.getName());
    }
}
