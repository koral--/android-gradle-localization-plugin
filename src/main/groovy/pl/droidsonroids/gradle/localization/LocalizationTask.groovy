package pl.droidsonroids.gradle.localization

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class LocalizationTask extends DefaultTask {
    {
        description = "Generates XML" //TODO
        group = "android"
    }
    @TaskAction
    def parseFile() {
        def resDir = project.localization.outputDirectory ?: project.file('src/main/res')
        new ParserEngine(project.localization, resDir).parseSpreadsheet()
    }
}
