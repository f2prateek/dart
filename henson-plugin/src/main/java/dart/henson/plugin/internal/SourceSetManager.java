package dart.henson.plugin.internal;

import com.android.build.gradle.api.BaseVariant;
import com.android.builder.model.BuildType;
import com.android.builder.model.ProductFlavor;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class SourceSetManager {
    private static final String NAVIGATION_SOURCESET_RADIX = "navigationSourceSet";
    private static final String NAVIGATION_SOURCESET_SUFFIX = "NavigationSourceSet";

    private Project project;
    private Logger logger;

    public SourceSetManager(Project project, Logger logger) {
        this.project = project;
        this.logger = logger;
    }

    public void createNavigationSourceSetForMain() {
        String newSourceSetName = NAVIGATION_SOURCESET_RADIX;
        String newSourceSetPath = getSourceSetPath("main");
        createNavigationSourceSet(newSourceSetName, newSourceSetPath);
    }

    public SourceSet getNavigationSourceSetForMain() {
        return getSourceSets().findByName(NAVIGATION_SOURCESET_RADIX);
    }

    public void createNavigationSourceSet(BuildType buildType) {
        String name = buildType.getName();
        String newSourceSetName = getSourceSetName(name);
        String newSourceSetPath = getSourceSetPath(name);
        createNavigationSourceSet(newSourceSetName, newSourceSetPath);
    }

    public SourceSet getNavigationSourceSet(BuildType buildType) {
        return getSourceSets().findByName(getSourceSetName(buildType.getName()));
    }

    public void createNavigationSourceSet(ProductFlavor productFlavor) {
        String name = productFlavor.getName();
        String newSourceSetName = getSourceSetName(name);
        String newSourceSetPath = getSourceSetPath(name);
        createNavigationSourceSet(newSourceSetName, newSourceSetPath);
    }

    public SourceSet getNavigationSourceSet(ProductFlavor productFlavor) {
        return getSourceSets().findByName(getSourceSetName(productFlavor.getName()));
    }

    public void createNavigationSourceSet(BaseVariant variant) {
        String name = variant.getName();
        String newSourceSetName = getSourceSetName(name);
        String newSourceSetPath = getSourceSetPath(name);
        createNavigationSourceSet(newSourceSetName, newSourceSetPath);
    }

    public List<SourceSet> getAllNavigationSourceSets() {
        return new ArrayList<>(getSourceSets());
    }

    private String getSourceSetName(String name) {
        return name + NAVIGATION_SOURCESET_SUFFIX;
    }

    private String getSourceSetPath(String name) {
        return "src/navigation/" + name + "/java";
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
