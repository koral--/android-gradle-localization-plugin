package pl.droidsonroids.gradle.localization

import org.gradle.api.Project
import org.gradle.internal.UncheckedException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

/**
 * Created by koral on 21.06.14.
 */
class LocalizationPluginTest extends GroovyTestCase {

    private void parseTestFile(String fileName) throws IOException
    {
        Project project = ProjectBuilder.builder().build()
        def config=new ConfigExtension()
        config.csvFilePath=getClass().getResource(fileName).getPath()
        project.setProperty('localization',config)
        project.apply plugin: 'localization'
        for (action in project.tasks.localization.actions)
            action.execute(project.tasks.localization)
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
        catch (UncheckedException ex)
        {
            assert ex.getCause() instanceof InputParseException
        }
    }
}
