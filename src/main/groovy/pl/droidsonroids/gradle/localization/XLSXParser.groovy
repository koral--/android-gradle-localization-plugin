package pl.droidsonroids.gradle.localization

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class XLSXParser {

    private String[][] mAllCells

    XLSXParser(InputStream inputStream, boolean isXls, String sheetName) {
        Workbook workbook = isXls ? new HSSFWorkbook(inputStream) : new XSSFWorkbook(inputStream)
        workbook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK)

        Sheet sheet = sheetName ? workbook.getSheet(sheetName) : workbook.getSheetAt(0)
        if (sheet == null)
            throw new IllegalArgumentException("Sheet $sheetName does not exist")
        mAllCells = new String[sheet.lastRowNum][]
        for (int i = 0; i < sheet.lastRowNum; i++) {
            Row row = sheet.getRow(i)
            if (row == null)
                continue
            mAllCells[i] = new String[row.lastCellNum]
            for (int j = 0; j < row.lastCellNum; j++) {
                mAllCells[i][j] = row.getCell(j).toString() ?: ""
            }
        }
    }

    String[][] getAllValues() {
        mAllCells
    }
}
