package pl.droidsonroids.gradle.localization

import org.apache.commons.csv.CSVStrategy
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.tasks.TaskAction

/**
 * @author koral--
 */
class ConfigExtension{
    boolean allowNonTranslatableTranslation = false
    boolean allowEmptyTranslations = false
    boolean escapeApostrophes = true
    boolean escapeQuotes = true
    boolean escapeNewLines = true
    boolean escapeBoundarySpaces = true
    String defaultColumnName = 'default'
    String csvFilePath
    String csvFileURI
    CSVStrategy csvStrategy
}

class LocalizationTask extends DefaultTask {

    @TaskAction
    def parseFile() {
        new Parser(project.localization).parseCells()
    }
}

public class LocalizationPlugin implements Plugin<Project> {

    void apply(Project project)
    {
        project.extensions.create('localization',ConfigExtension)
        project.task('localization', type: LocalizationTask)
    }

}