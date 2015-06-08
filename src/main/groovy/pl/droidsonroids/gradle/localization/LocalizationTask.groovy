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
        ConfigExtension config = project.localization
        def resDir = config.outputDirectory ?: project.file('src/main/res')
        new ParserEngine(config, resDir).parseSpreadsheet()
    }
}
