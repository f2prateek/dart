package dart.henson.plugin

import com.android.build.gradle.api.AndroidSourceSet

class NavigationVariant {
    def variant
    def combinations
    List<AndroidSourceSet> sourceSets = new ArrayList()
    def apiConfigurations = new ArrayList()
    def implementationConfigurations = new ArrayList()
    def compileOnlyConfigurations = new ArrayList()
    def annotationProcessorConfigurations = new ArrayList()
}
