package dart.henson.plugin.internal;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dart.henson.plugin.util.StringUtil.capitalize;
import static java.util.Arrays.asList;

public class ConfigurationManager {

    public static final String NAVIGATION_CONFIGURATION = "navigation";
    public static final String NAVIGATION_CONFIGURATION_RADIX = "Navigation";
    public static final String NAVIGATION_CONFIGURATION_SUFFIX_API = "Api";
    public static final String NAVIGATION_CONFIGURATION_SUFFIX_IMPLEMENTATION = "Implementation";
    public static final String NAVIGATION_CONFIGURATION_SUFFIX_COMPILE_ONLY = "CompileOnly";
    public static final String NAVIGATION_CONFIGURATION_SUFFIX_ANNOTATION_PROCESSOR = "AnnotationProcessor";
    private static final List<String> CONFIGURATION_SUFFIXES = asList(NAVIGATION_CONFIGURATION_SUFFIX_API,
            NAVIGATION_CONFIGURATION_SUFFIX_IMPLEMENTATION,
            NAVIGATION_CONFIGURATION_SUFFIX_COMPILE_ONLY,
            NAVIGATION_CONFIGURATION_SUFFIX_ANNOTATION_PROCESSOR);

    private Project project;
    private Logger logger;
    private ArtifactManager artifactManager;


    public ConfigurationManager(Project project, Logger logger, ArtifactManager artifactManager) {
        this.project = project;
        this.logger = logger;
        this.artifactManager = artifactManager;
    }

    public Configuration createArtifactConfiguration(BaseVariant variant) {
        String artifactName = artifactManager.getNavigationArtifactName(variant);
        Configuration artifactConfiguration = project.getConfigurations().create(artifactName);
        artifactConfiguration.setCanBeConsumed(true);
        artifactConfiguration.setCanBeResolved(false);
        return artifactConfiguration;
    }

    public Configuration getClientInternalConfiguration(BaseVariant variant) {
        return project.getConfigurations().findByName("__" + variant.getName() + NAVIGATION_CONFIGURATION_RADIX);
    }

    public Configuration createClientInternalConfiguration(BaseVariant variant) {
        Configuration internalConfiguration = project.getConfigurations().maybeCreate("__" + variant.getName() + NAVIGATION_CONFIGURATION_RADIX);
        Configuration pseudoConfiguration = getClientPseudoConfiguration();
        internalConfiguration.extendsFrom(pseudoConfiguration);
        internalConfiguration.setCanBeConsumed(false);
        internalConfiguration.setCanBeResolved(true);
        return internalConfiguration;
    }

    public Configuration getClientPseudoConfiguration() {
        return project.getConfigurations().getByName(NAVIGATION_CONFIGURATION);
    }

    public Configuration createClientPseudoConfiguration() {
        Configuration clientPseudoConfiguration = project.getConfigurations().create(NAVIGATION_CONFIGURATION);
        clientPseudoConfiguration.setCanBeConsumed(false);
        clientPseudoConfiguration.setCanBeResolved(false);
        return clientPseudoConfiguration;
    }

    public Map<String, Configuration> createNavigationConfigurations(String prefix) {
        Map<String, Configuration> result = new HashMap<>(CONFIGURATION_SUFFIXES.size());
        System.out.println("Adding configuration: suffixes: " + CONFIGURATION_SUFFIXES);
        System.out.println("Adding configuration: suffixes: " + CONFIGURATION_SUFFIXES.size());

        CONFIGURATION_SUFFIXES
                .forEach(suffix -> {
                    String configurationName = getConfigurationName(prefix, suffix);
                    System.out.println("Adding configuration: " + configurationName);
                    Configuration configuration = project.getConfigurations().findByName(configurationName);
                    if (configuration == null) {
                        System.out.println("Creating configuration: " + configurationName);
                        configuration = project.getConfigurations().create(configurationName);
                    }
                    configuration.setCanBeResolved(true);
                    configuration.setCanBeConsumed(false);
                    result.put(suffix, configuration);
                    System.out.println("Added configuration: " + configurationName);
                    System.out.println("Added configuration: canBeResolved: " + configuration.isCanBeResolved());
                });
        return result;
    }

    public Map<String, Configuration> getNavigationConfigurations(String prefix) {
        Map<String, Configuration> result = new HashMap<>(CONFIGURATION_SUFFIXES.size());
        CONFIGURATION_SUFFIXES
                .forEach(suffix -> {
                    String configurationName = getConfigurationName(prefix, suffix);
                    Configuration configuration = project.getConfigurations().findByName(configurationName);
                    result.put(suffix, configuration);
                });
        return result;
    }

    private String getConfigurationName(String prefix, String suffix) {
        String configurationName;
        if (prefix.isEmpty()) {
            configurationName = NAVIGATION_CONFIGURATION + suffix;
        } else {
            configurationName = prefix + NAVIGATION_CONFIGURATION_RADIX + suffix;
        }
        return configurationName;
    }
}
