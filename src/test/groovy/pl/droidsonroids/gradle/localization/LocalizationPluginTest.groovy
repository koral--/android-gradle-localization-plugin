package pl.droidsonroids.gradle.localization

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

/**
 * Created by koral on 21.06.14.
 */
class LocalizationPluginTest extends GroovyTestCase {

    @Test
    void testGoogleSpreadSheet() {//TODO
        def config=new ConfigExtension()
    }

    @Test
    void testValidFile() {
        parseTestFile('valid.csv')
    }

    @Test
    void testMissingTranslation() {
        try {
            parseTestFile('missingTranslation.csv')
            fail(InputParseException.class.getSimpleName()+' expected')
        }
        catch (InputParseException ex)
        {
            println 'expected exception: '+ex.getMessage()
        }
    }

    private static void parseTestFile(String fileName) {
        def config = new ConfigExtension()
        config.csvFile = new File(getClass().getResource(fileName).getPath())
        parseTestCSV(config)
    }

    private static void parseTestCSV(ConfigExtension config) throws IOException {
        def project = ProjectBuilder.builder().build()
        def resDir = project.file('src/main/res')
        try {
            new Parser(config, resDir).parseCells()
        }
        finally {
            resDir.deleteDir()
        }
    }
}
