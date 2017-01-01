package pl.droidsonroids.gradle.localization

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder

abstract class LocalizationPluginTestBase {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    File parseTestFile(String fileName) {
        def config = new ConfigExtension()
        config.csvFileURI = new File(fileName).toURI()
        config.tagEscapingStrategyColumnName = 'tagEscapingStrategy'
        parseTestFile(config)
    }

    File parseTestFile(ConfigExtension config) {
        def file = new File("/tmp/test")
        file.deleteDir()
        def project = ProjectBuilder.builder()
                .withProjectDir(file)
                .build()
        def resDir = project.file('src/main/res')
        new ParserEngine(config, resDir).parseSpreadsheet()
        return resDir
    }

}
