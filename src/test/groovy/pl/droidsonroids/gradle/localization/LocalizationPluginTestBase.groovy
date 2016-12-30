package pl.droidsonroids.gradle.localization

import org.gradle.testfixtures.ProjectBuilder

abstract class LocalizationPluginTestBase {

    void parseTestFile(String fileName) {
        def config = new ConfigExtension()
        config.csvFileURI = new File(fileName).toURI()
        config.tagEscapingStrategyColumnName = 'tagEscapingStrategy'
        parseTestFile(config)
    }

    static void parseTestFile(ConfigExtension config) {
        def project = ProjectBuilder.builder().build()
        def resDir = project.file('src/main/res')

        try {
            new ParserEngine(config, resDir).parseSpreadsheet()
        }
        finally {
            resDir.deleteDir()
        }
    }

}
