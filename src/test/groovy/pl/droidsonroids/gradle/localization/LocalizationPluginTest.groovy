package pl.droidsonroids.gradle.localization

import org.junit.Test

/**
 * Created by koral on 21.06.14.
 */
class LocalizationPluginTest extends GroovyTestCase {

    private void parseTestFile(String fileName) throws IOException
    {
//        Project project = ProjectBuilder.builder().build()
        def config=new ConfigExtension()
        config.csvFilePath=getClass().getResource(fileName).getPath()
//        project.setProperty('localization',config)
//        project.apply plugin: 'localization'
//        for (action in project.tasks.localization.actions)
//            action.execute(project.tasks.localization)
        new Parser(config).parseCells()
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
}
