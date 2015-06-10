package pl.droidsonroids.gradle.localization

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class LocalizationTask extends DefaultTask {
    {
        group = 'localization'
        description = """Generates Android string resource XML files
    See https://github.com/koral--/android-gradle-localization-plugin#configuration for more information.

    Available configuration options and default values:
    boolean allowNonTranslatableTranslation = false
    boolean allowEmptyTranslations = false
    boolean escapeApostrophes = true
    boolean escapeQuotes = true
    boolean escapeNewLines = true
    boolean convertTripleDotsToHorizontalEllipsis = true
    boolean escapeSlashes = true
    TagEscapingStrategy tagEscapingStrategy = TagEscapingStrategy.IF_TAGS_ABSENT
    Normalizer.Form normalizationForm = Normalizer.Form.NFC
    String defaultColumnName = 'default'
    String nameColumnName = 'name'
    String translatableColumnName = 'translatable'
    String formattedColumnName = 'formatted'
    String commentColumnName = 'comment'
    File csvFile
    String csvFileURI
    String csvGenerationCommand
    File xlsFile
    String xlsFileURI
    File outputDirectory
    String sheetName
    List<String> ignorableColumns = []
    CSVStrategy csvStrategy
    String outputFileName = 'strings.xml'
    String outputIndent = '  '
    boolean skipInvalidName
    boolean skipDuplicatedName
    String defaultLocaleQualifier"""
    }
    @TaskAction
    def parseFile() {
        ConfigExtension config = project.localization
        def resDir = config.outputDirectory ?: project.file('src/main/res')
        new ParserEngine(config, resDir).parseSpreadsheet()

        if (config.report != null && !config.report.exists()) {
            config.report.mkdir()
        }
        if (config.outputDirectory != null && config.inputDirectory != null) {
            new XmlUpdate().update(config)
        }
    }
}
