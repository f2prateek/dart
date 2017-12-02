package dart.henson.plugin

import com.android.build.gradle.api.AndroidSourceSet

class NavigationVariant {

    def buildTypeName
    def flavorName
    List<AndroidSourceSet> sourceSets
    def apiConfigurations
    def implementationConfigurations
    def compileOnlyConfigurations
    def annotationProcessorConfigurations
}
