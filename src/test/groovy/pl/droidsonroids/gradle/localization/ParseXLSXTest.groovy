package pl.droidsonroids.gradle.localization

import groovy.io.FileType
import org.junit.Test
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.builder.Input

import static org.assertj.core.api.Assertions.assertThat

class ParseXLSXTest extends LocalizationPluginTestBase {

    @Test
    void testXlsxFile() {
        def name = 'valid.xlsx'

        ConfigExtension config = new ConfigExtension()
        config.xlsFileURI = getClass().getResource(name).toString()
        config.allowEmptyTranslations = true
        config.skipInvalidName = true
        config.skipDuplicatedName = true
        config.defaultColumnName = 'EN'
        config.nameColumnName = 'Android'
        config.ignorableColumns.add('WinPhone')
        config.ignorableColumns.add('iOS')
        config.ignorableColumns.add('END')
        config.defaultLocaleQualifier = 'en'
        config.multiSheets = true

        def resDir = parseTestFile(config)

        resDir.traverse(type: FileType.FILES) {
            def filePath = it.path.replace(resDir.path, '')
            def expectedFileURL = getClass().getResource("parsed_valid_xlsx/$filePath")
            def diff = DiffBuilder.compare(Input.fromFile(it))
                    .withTest(Input.fromURL(expectedFileURL))
                    .ignoreWhitespace()
                    .checkForSimilar()
                    .build()
            assertThat(diff.hasDifferences()).as('file: %s has diff %s', filePath, diff.toString()).isFalse()
        }
    }

    @Test
    void testXlsxURI() {
        ConfigExtension config = new ConfigExtension()
        config.xlsFileURI = 'https://docs.google.com/a/droidsonroids.pl/spreadsheets/d/1sfE3Zk_7syHpq3HPKYQ9gRidm1W7c1IjIfdH1R8z9m4/export?format=xlsx'
        parseTestFile(config)
    }
}
