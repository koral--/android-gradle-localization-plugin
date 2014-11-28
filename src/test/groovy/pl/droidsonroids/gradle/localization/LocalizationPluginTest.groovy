package pl.droidsonroids.gradle.localization

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

//TODO add more tests
class LocalizationPluginTest extends GroovyTestCase {

    @Test
    void testCsvFileConfig() {
        def config = new ConfigExtension()
        config.csvFile = new File(getClass().getResource('valid.csv').getPath())
        parseTestCSV(config)
    }

    @Test
    void testValidFile() {
        println 'testing valid file'
        parseTestFile('valid.csv')
    }

    @Test
    void testMissingTranslation() {
        println 'testing invalid file'
        try {
            parseTestFile('missing_translation.csv')
            fail(IOException.class.getName() + ' expected')
        }
        catch (IOException ignored) {
            println 'expected exception thrown'
        }
    }

    private void parseTestFile(String fileName) {
        def config = new ConfigExtension()
        config.csvFileURI = getClass().getResource(fileName).toURI()
        parseTestCSV(config)
    }

    private static void parseTestCSV(ConfigExtension config) throws IOException {
        def project = ProjectBuilder.builder().build()
        def resDir = project.file('src/main/res')
        resDir=new File("src/test/res")
        try {
            new Parser(config, resDir).parseCSV()
        }
        finally {
//            resDir.deleteDir()
        }
    }

    @Test
    void testXls() {
        def name = 'language_iOS_append_ALL_340_333_rev.xlsx'
        def file = new File(getClass().getResource(name).getPath())
        ConfigExtension config=new ConfigExtension()
        config.allowEmptyTranslations=true
        config.file=file
        config.csvFile=file
        config.defaultColumnName="EN"
        config.name="Android"
        config.ignorableColumns.add("WinPhone")
        config.ignorableColumns.add("iOS")
        config.ignorableColumns.add("END")

        parseTestCSV(config)
    }

}
