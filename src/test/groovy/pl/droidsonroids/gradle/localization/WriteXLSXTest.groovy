package pl.droidsonroids.gradle.localization

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Ignore
import org.junit.Test

@Ignore
class WriteXLSXTest extends LocalizationPluginTestBase {
    @Test
    //TODO provide data source
    void testWriteXlsx() {

//        def map = getMap(new File(getClass().getResource('res').getPath()))

//        writer(file, map)
        def languages = ['', 'cs', 'de', 'es', 'fr', 'hu', 'it', 'ja', 'ko', 'nl', 'pl', 'pt-rBR',
                        'ru', 'sv', 'zh-rCN', 'zh-rTW'] as String[]
        def sourceDir = new File(srcDirPath)
        def files = filter(sourceDir, lanuages)
        def map = getMap(files);

        def outFile = new File(outPath)
        writer(outFile, map)

    }

    private static File[] filter(dir, strs) {
        File[] files = new File[strs.length]
        int i = 0;
        strs.each {
            def s = '' == it ? "values" : "values-$it"
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

                if (subDir == 'values' && !map.containsKey(name)) {
                    HashMap<String, String> m = new HashMap<String, String>()
                    map.put(name, m)
                }
                map.get(name).put(subDir, value)
            }
        }
        return map;
    }

    private
    static void writer(File file, Map<String, HashMap<String, String>> map) {
        Workbook workbook = file.getAbsolutePath().endsWith("xls") ?
                new HSSFWorkbook() : new XSSFWorkbook();
        Sheet sheet1 = (Sheet) workbook.createSheet("sheet1");
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
        workbook.write(stream);
        stream.close();
    }

}
