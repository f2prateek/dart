package dart.henson.plugin.variant;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.tasks.Jar;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an enriched variant for navigation purposes.
 */
public class NavigationVariant {
    public BaseVariant variant;
    public List<Combinator.Dimension<String>> combinations;
    public List<SourceSet> sourceSets = new ArrayList();
    public List<Configuration> apiConfigurations = new ArrayList();
    public List<Configuration> implementationConfigurations = new ArrayList();
    public List<Configuration> compileOnlyConfigurations = new ArrayList();
    public List<Configuration> annotationProcessorConfigurations = new ArrayList();
    public JavaCompile compilerTask;
    public Jar jarTask;
}
