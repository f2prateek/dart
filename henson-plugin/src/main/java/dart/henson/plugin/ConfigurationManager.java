package dart.henson.plugin;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.internal.logging.slf4j.OutputEventListenerBackedLogger;

import java.util.Arrays;

import static dart.henson.plugin.StringUtil.capitalize;

public class ConfigurationManager {

    public static final String NAVIGATION_CONFIGURATION_PREFIX = "navigation";

    private Project project;
    private OutputEventListenerBackedLogger logger;
    private ArtifactManager artifactManager;


    public ConfigurationManager(Project project, OutputEventListenerBackedLogger logger, ArtifactManager artifactManager) {
        this.project = project;
        this.logger = logger;
        this.artifactManager = artifactManager;
    }

    public Configuration createArtifactConfiguration(BaseVariant variant) {
        String artifactName = artifactManager.getNavigationArtifactName(variant);
        Configuration artifactConfiguration = project.getConfigurations().create(artifactName);
        artifactConfiguration.setCanBeConsumed(true);
        artifactConfiguration.setCanBeResolved(true);
        return artifactConfiguration;
    }

    public Configuration createClientInternalConfiguration(BaseVariant variant) {
        Configuration internalConfiguration = project.getConfigurations().maybeCreate("__" + NAVIGATION_CONFIGURATION_PREFIX + variant.getName());
        Configuration artifactConfiguration = getClientPseudoConfiguration();
        internalConfiguration.extendsFrom(artifactConfiguration);
        internalConfiguration.setCanBeConsumed(false);
        internalConfiguration.setCanBeResolved(true);
        return internalConfiguration;
    }

    public Configuration getClientPseudoConfiguration() {
        return project.getConfigurations().getByName(NAVIGATION_CONFIGURATION_PREFIX);
    }

    public Configuration createClientPseudoConfiguration() {
        Configuration clientPseudoConfiguration = project.getConfigurations().create(NAVIGATION_CONFIGURATION_PREFIX);
        clientPseudoConfiguration.setCanBeConsumed(false);
        clientPseudoConfiguration.setCanBeResolved(true);
        return clientPseudoConfiguration;
    }

    public void createNavigationConfigurations(String radix) {
        Arrays.asList("Api", "Implementation", "CompileOnly", "AnnotationProcessor")
                .forEach(suffix -> {
                    Configuration configuration = project.getConfigurations().maybeCreate(NAVIGATION_CONFIGURATION_PREFIX + radix + suffix);
                    configuration.setCanBeResolved(true);
                    configuration.setCanBeConsumed(false);
                });
    }
}
