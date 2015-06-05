package pl.droidsonroids.gradle.localization

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class LocalizationTask extends DefaultTask {
    {
        description = "Generates XML"
        group = 'android'
    }
    @TaskAction
    def parseFile() {
        new ParserEngine(project.localization, project.localization.outputDirectory ?: project.file('src/main/res')).parseSpreadsheet()
    }
}
