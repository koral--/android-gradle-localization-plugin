package pl.droidsonroids.gradle.localization

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class LocalizationTask extends DefaultTask {
    {
        group = 'localization'
        description = """Generates Android string resource XML files
    See https://github.com/koral--/android-gradle-localization-plugin#configuration for more information."""
    }

    @TaskAction
    def parseFile() {
        new ParserEngine(project.localization, project.localization.outputDirectory ?: project.file('src/main/res')).parseSpreadsheet()
    }
}
