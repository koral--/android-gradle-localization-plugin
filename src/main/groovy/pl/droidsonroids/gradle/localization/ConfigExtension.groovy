package pl.droidsonroids.gradle.localization

import org.apache.commons.csv.CSVStrategy

import java.text.Normalizer

/**
 * Plugin configuration extension. See README.md for details
 * @author koral--
 */
class ConfigExtension {
    //Source File
    File csvFile
    String csvFileURI
    String csvGenerationCommand
    File xlsFile
    String xlsFileURI
    String sheetName

    //Name
    String defaultLocaleQualifier
    boolean skipInvalidName
    boolean skipDuplicatedName
    List<String> ignorableColumns = []
    String nameColumnName = 'Android'
    String defaultColumnName = 'EN'
    String translatableColumnName = 'translatable'
    String formattedColumnName = 'formatted'
    String commentColumnName = 'comment'
    List<String> ignorableNames = []

    //Escape
    boolean allowNonTranslatableTranslation = false
    boolean allowEmptyTranslations = false
    boolean escapeApostrophes = true
    boolean escapeQuotes = true
    boolean escapeNewLines = false
    boolean convertTripleDotsToHorizontalEllipsis = false
    boolean escapeSlashes = false

    //Strategy
    TagEscapingStrategy tagEscapingStrategy = TagEscapingStrategy.IF_TAGS_ABSENT
    Normalizer.Form normalizationForm = Normalizer.Form.NFC
    CSVStrategy csvStrategy

    //Output
    String outputFileName = 'strings.xml'
    String outputIndent = '  '
    File outputDirectory

    //Xml update
    File inputDirectory
    File report
    Map<String, String> map
    boolean append
    boolean replace

}
