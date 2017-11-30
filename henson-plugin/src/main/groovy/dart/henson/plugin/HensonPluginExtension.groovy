package dart.henson.plugin

import org.gradle.api.Project

class HensonPluginExtension {
    String dartVersion
    private Project project

    HensonPluginExtension(Project project) {
        this.project = project
    }
}