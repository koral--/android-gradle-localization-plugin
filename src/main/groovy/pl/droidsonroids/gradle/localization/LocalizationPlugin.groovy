package pl.droidsonroids.gradle.localization

import org.gradle.api.Plugin
import org.gradle.api.Project

public class LocalizationPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create('localization', ConfigExtension)
        def task = project.tasks.create('localization', LocalizationTask.class);
        task.description = 'Gradle plugin for generating localized string resources'
    }
}