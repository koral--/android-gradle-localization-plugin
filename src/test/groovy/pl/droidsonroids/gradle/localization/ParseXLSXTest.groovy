package pl.droidsonroids.gradle.localization

import org.junit.Test

class ParseXLSXTest extends LocalizationPluginTestBase {

    @Test
    void testXlsx() {
        def name = 'language_iOS_append_ALL_343_RC_20150603_update_russian.xlsx'
        def file = new File(TEST_RES_DIR, name)
        ConfigExtension config = new ConfigExtension()
        config.xlsFile = file
        config.allowEmptyTranslations = true
        config.escapeNewLines = false
        config.skipInvalidName = true
        config.skipDuplicatedName = true
        config.defaultColumnName = "EN"
        config.nameColumnName = "Android"
        config.ignorableColumns.add("WinPhone")
        config.ignorableColumns.add("iOS")
        config.ignorableColumns.add("END")
        config.defaultLocaleQualifier = "en"
        config.outputDirectory = new File(TEST_RES_DIR, "out");
        parseTestFile(config)
    }
}
