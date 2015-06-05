package pl.droidsonroids.gradle.localization

import org.junit.Test

class ParseXLSXTest extends LocalizationPluginTestBase {

    @Test
    void testXlsxFile() {
        def name = 'valid.xlsx'
        def file = new File(getClass().getResource(name).getPath())
        ConfigExtension config = new ConfigExtension()
        config.xlsFile = file
        testXlsx(config)
    }

    @Test
    void testXlsxURI() {
        def name = 'valid.xlsx'
        ConfigExtension config = new ConfigExtension()
        config.xlsFileURI = getClass().getResource(name)
        testXlsx(config)
    }

    static void testXlsx(ConfigExtension config) {
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
}
