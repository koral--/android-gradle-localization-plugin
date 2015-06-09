package pl.droidsonroids.gradle.localization

import org.gradle.testfixtures.ProjectBuilder

abstract class LocalizationPluginTestBase {

    static File TEST_RES_DIR = new File("src/test/resources/pl/droidsonroids/gradle/localization");

    void parseTestFile(String fileName) {
        def config = new ConfigExtension()
        config.csvFileURI = getClass().getResource(fileName).toURI()
        parseTestFile(config)
    }

    static void parseTestFile(ConfigExtension config) throws IOException {
        def project = ProjectBuilder.builder().build()
        def resDir = project.file('src/main/res')

        try {
            new ParserEngine(config, config.outputDirectory).parseSpreadsheet()
        }
        finally {
            resDir.deleteDir()
        }
    }

}
