package pl.droidsonroids.gradle.localization

import org.junit.Test

class ParseXLSXTest extends LocalizationPluginTestBase {

    private static final RESOURCE_FOLDER = "src/test/resources/pl/droidsonroids/gradle/localization/"
    private static final VALID_FILE_NAME = "valid.xlsx"

    @Test
    void testXlsxFile() {
        def file = new File(RESOURCE_FOLDER + VALID_FILE_NAME)
        ConfigExtension config = new ConfigExtension()
        config.xlsFile = file
        config.allowEmptyTranslations = true
        config.skipInvalidName = true
        config.skipDuplicatedName = true
        config.defaultColumnName = "EN"
        config.nameColumnName = "Android"
        config.ignorableColumns.add("WinPhone")
        config.ignorableColumns.add("iOS")
        config.ignorableColumns.add("END")
        config.defaultLocaleQualifier = "en"
        parseTestFile(config)
    }

    @Test
    void testXlsxURI() {
        ConfigExtension config = new ConfigExtension()
        config.xlsFileURI = 'https://docs.google.com/a/droidsonroids.pl/spreadsheets/d/1sfE3Zk_7syHpq3HPKYQ9gRidm1W7c1IjIfdH1R8z9m4/export?format=xlsx'
        parseTestFile(config)
    }
}
