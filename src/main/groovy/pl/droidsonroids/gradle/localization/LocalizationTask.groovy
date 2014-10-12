package pl.droidsonroids.gradle.localization

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class LocalizationTask extends DefaultTask {
    @TaskAction
    def parseFile() {
        new Parser(project.localization, project.file('src/main/res')).parseCells()
    }
}
