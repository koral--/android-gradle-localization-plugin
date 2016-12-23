package pl.droidsonroids.gradle.localization

import org.junit.Test

//TODO add more tests
class ParseCSVTest extends LocalizationPluginTestBase {

    private static final RESOURCE_FOLDER = "src/test/resources/pl/droidsonroids/gradle/localization/"
    private static final VALID_FILE_NAME = "valid.csv"
    private static final MISSING_TRANSLATION_FILE_NAME = "missing_translation.csv"

    @Test
    void testCsvFileConfig() {
        def config = new ConfigExtension()
        config.csvFile = new File(RESOURCE_FOLDER + VALID_FILE_NAME)
        config.tagEscapingStrategyColumnName = 'tagEscapingStrategy'
        parseTestFile(config)
    }

    @Test
    void testValidFile() {
        println 'testing valid file'
        parseTestFile(RESOURCE_FOLDER + VALID_FILE_NAME)
    }

    @Test(expected = IllegalArgumentException.class)
    void testMissingTranslation() {
        println 'testing invalid file'
        parseTestFile(RESOURCE_FOLDER + MISSING_TRANSLATION_FILE_NAME)
    }
}
