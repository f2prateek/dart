package dart.henson.plugin.internal;

import com.android.build.gradle.api.BaseVariant;
import com.android.builder.model.BuildType;
import com.android.builder.model.ProductFlavor;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import static dart.henson.plugin.util.StringUtil.capitalize;
import static java.util.Collections.singletonList;

public class SourceSetManager {
    public static final String NAVIGATION_SOURCESET_PREFIX = "navigationSource";

    private Project project;
    private Logger logger;

    public SourceSetManager(Project project, Logger logger) {
        this.project = project;
        this.logger = logger;
    }


    public void createNavigationSourceSetForMain() {
        String newSourceSetName = NAVIGATION_SOURCESET_PREFIX;
        String newSourceSetPath = "src/navigation/main/java";
        createNavigationSourceSet(newSourceSetName, newSourceSetPath);
    }

    public SourceSet getNavigationSourceSetForMain() {
        return getSourceSets().findByName(NAVIGATION_SOURCESET_PREFIX);
    }

    public void createNavigationSourceSet(BuildType buildType) {
        String newSourceSetName = NAVIGATION_SOURCESET_PREFIX + capitalize(buildType.getName());
        String newSourceSetPath = "src/navigation/" + buildType.getName() + "/java";
        createNavigationSourceSet(newSourceSetName, newSourceSetPath);
    }

    public SourceSet getNavigationSourceSet(BuildType buildType) {
        return getSourceSets().findByName(NAVIGATION_SOURCESET_PREFIX + capitalize(buildType.getName()));
    }

    public void createNavigationSourceSet(ProductFlavor productFlavor) {
        String newSourceSetName = NAVIGATION_SOURCESET_PREFIX + capitalize(productFlavor.getName());
        String newSourceSetPath = "src/navigation/" + productFlavor.getName() + "/java";
        createNavigationSourceSet(newSourceSetName, newSourceSetPath);
    }

    public SourceSet getNavigationSourceSet(ProductFlavor productFlavor) {
        return getSourceSets().findByName(NAVIGATION_SOURCESET_PREFIX + capitalize(productFlavor.getName()));
    }

    public void createNavigationSourceSet(BaseVariant variant) {
        String newSourceSetName = variant.getName();
        String newSourceSetPath = "src/navigation/" + newSourceSetName + "/java";
        newSourceSetName = NAVIGATION_SOURCESET_PREFIX + capitalize(newSourceSetName);
        createNavigationSourceSet(newSourceSetName, newSourceSetPath);
    }

    private void createNavigationSourceSet(String newSourceSetName, String newSourceSetPath) {
        SourceSet sourceSet = getSourceSets().findByName(newSourceSetName);
        if (sourceSet == null) {
            logger.debug("Creating sourceSet: " + newSourceSetName + " with root in " + newSourceSetPath);
            sourceSet = getSourceSets().create(newSourceSetName);
            sourceSet.getJava().setSrcDirs(singletonList(newSourceSetPath));
        }
    }

    private SourceSetContainer getSourceSets() {
        return project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
    }
}
