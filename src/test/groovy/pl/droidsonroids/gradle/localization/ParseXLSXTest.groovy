package pl.droidsonroids.gradle.localization

import org.junit.Test

class ParseXLSXTest extends LocalizationPluginTestBase {

    @Test
    void testXlsxFile() {
        def name = 'valid.xlsx'
        def file = new File(getClass().getResource(name).getPath())
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
        config.multiSheets = true
        parseTestFile(config)
    }

    @Test
    void testXlsxURI() {
        ConfigExtension config = new ConfigExtension()
        config.xlsFileURI = 'https://docs.google.com/a/droidsonroids.pl/spreadsheets/d/1sfE3Zk_7syHpq3HPKYQ9gRidm1W7c1IjIfdH1R8z9m4/export?format=xlsx'
        parseTestFile(config)
    }
}
