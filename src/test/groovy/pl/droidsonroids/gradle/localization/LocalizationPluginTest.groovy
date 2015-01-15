package pl.droidsonroids.gradle.localization

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
//TODO add more tests
class LocalizationPluginTest extends GroovyTestCase {

    @Test
    void testCsvFileConfig() {
        def config = new ConfigExtension()
        config.sourceFile = new File(getClass().getResource('valid.csv').getPath())
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
        try {
            new Parser(config, resDir).parseCSV()
        }
        finally {
            resDir.deleteDir()
        }
    }

    @Test
    void testXls() {
        def name = 'language_iOS_append_ALL_340_333_rev.xlsx'
        def file = new File(getClass().getResource(name).getPath())
        ConfigExtension config = new ConfigExtension()
        config.csvFile = file
        config.allowEmptyTranslations = true
        config.defaultColumnName = "EN"
        config.nameColumnName = "Android"
        config.ignorableColumns.add("WinPhone")
        config.ignorableColumns.add("iOS")
        config.ignorableColumns.add("END")

        parseTestCSV(config)
    }

    @Test
    void testWriteXls() {

//        def map = getMap(new File(getClass().getResource('res').getPath()))

//        writer(file, map)
        def lanuages = ['', 'cs', 'de', 'es', 'fr', 'hu', 'it', 'ja', 'ko', 'nl', 'pl', 'pt-rBR',
                        'ru', 'sv', 'zh-rCN', 'zh-rTW'] as String[]
        def sourceDir = new File("/Users/zhangls/AndroidStudioProjects/android/lite/res")
        def files = filter(sourceDir, lanuages)
        def map = getMap(files);

        def outFile = new File("/Users/zhangls/AndroidStudioProjects/ccc.xlsx")
        writer(outFile, map)

    }

    private static File[] filter(dir, strs) {
        File[] files = new File[strs.length]
        int i = 0;
        strs.each {
            def s = ''.equals(it) ? "values" : "values-" + it
            def var = dir.getPath() + "/" + s
            File file = new File(var, "strings.xml")
            if (file.exists()) {
                files[i] = file
            } else {
                throw new IllegalArgumentException(file.getAbsolutePath() + " not exist")
            }
            i++
        }
        return files
    }

    private static Map<String, HashMap<String, String>> getMap(File[] files) {
        Map<String, HashMap<String, String>> map = new LinkedHashMap<String, HashMap<String, String>>()
        files.each {
            String subDir = it.getParentFile().getName()
            new XmlParser().parse(it).each {
                it.toString()
                def name = it.attributes().get('name')
                def value = it.value().text();

                if (subDir.equals('values') && !map.containsKey(name)) {
                    HashMap<String, String> m = new HashMap<String, String>()
                    map.put(name, m)
                }
                map.get(name).put(subDir, value)
            }
        }
        return map;
    }

    private
    static void writer(File file, Map<String, HashMap<String, String>> map) throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet1 = (Sheet) wb.createSheet("sheet1");
        int i = 0
        map.each {
            Row row = (Row) sheet1.createRow(i);
            row.createCell(0).setCellValue(it.key)
            int j = 1
            it.value.each {
                row.createCell(j).setCellValue(it.value)
                j++
            }
            i++
        }

        OutputStream stream = new FileOutputStream(file);
        wb.write(stream);
        stream.close();
    }

}
