package pl.droidsonroids.gradle.localization

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class XlsxParser {

    private String[][] mAllCells

    public XlsxParser(InputStream inputStream, boolean isXls) throws IOException {
        mAllCells = getAll(inputStream, isXls)
    }


    private String[][] getAll(InputStream inputStream, boolean isXls) throws IOException {

        Workbook book
        book = isXls ? new HSSFWorkbook(inputStream) : new XSSFWorkbook(inputStream)

        Sheet se = book.getSheetAt(0) //TODO add ability to specify sheet name/index ?
        String[][] result = new String[se.getLastRowNum()][]

        for (int i = 0; i < se.getLastRowNum(); i++) {
            Row row = se.getRow(i)
            result[i] = new String[row.getLastCellNum()]
            for (int j = 0; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j)
                //TODO null?
                result[i][j] = cell == null ? "" : cell.toString()
            }
        }
        return result
    }

    String[][] getAllValues() {
        String[][] result = new String[mAllCells.length][]
        for (int i = 1; i < mAllCells.length; i++) {
            int length = mAllCells[i].length
            result[i - 1] = new String[length]
            for (int j = 0; j < length; j++) {
                result[i - 1][j] = mAllCells[i][j]
            }
        }
        result
    }

    String[] getLine() {
        String[] result = new String[mAllCells[0].length]
        for (int i = 0; i < mAllCells[0].length; i++) {
            result[i] = mAllCells[0][i]
        }
        result
    }
}
