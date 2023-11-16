package pl.droidsonroids.gradle.localization

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.assertj.core.api.AssertionsForClassTypes.assertThat

@RunWith(Parameterized)
class BuildLogicFunctionalTest {

    @Parameterized.Parameters(name = 'Gradle: {0}')
    static Collection<Object> data() {
        return ['7.0', '8.0', '8.1', '8.3', '8.4']
    }

    @Parameterized.Parameter
    public String gradleVersion

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    File testProjectDir
    File buildFile
    File csvFile

    @Before
    void setUp() {
        testProjectDir = temporaryFolder.newFolder()
        buildFile = new File(testProjectDir, 'build.gradle')
        csvFile = new File(testProjectDir, 'strings.csv')
    }

    @Test
    void pluginWorks() {
        buildFile << """
            plugins {
                id 'pl.droidsonroids.localization'
            }
            localization {
                csvFile = file('strings.csv')
            }
        """
        csvFile << """
            name,default
            app_name,My App
        """
        def xmlFileContent = """
            |<?xml version="1.0" encoding="UTF-8"?>
            |<resources>
            |  <string name="app_name">My App</string>
            |</resources>
        """.stripMargin().trim()

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withPluginClasspath()
                .withGradleVersion(gradleVersion)
                .withArguments('localization')
                .build()

        assertThat(result.task(":localization").outcome).isEqualTo(SUCCESS)
        assertThat(new File(testProjectDir, 'src/main/res/values/strings.xml')).hasContent(xmlFileContent)
    }
}
