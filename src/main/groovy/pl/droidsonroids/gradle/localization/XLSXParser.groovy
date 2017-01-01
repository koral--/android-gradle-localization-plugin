package pl.droidsonroids.gradle.localization

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class XLSXParser implements Parser {

    private Map<String, String[][]> mAllSheets

    XLSXParser(InputStream inputStream, boolean isXls, String sheetName, boolean useAllSheets) {
        Workbook workbook = isXls ? new HSSFWorkbook(inputStream) : new XSSFWorkbook(inputStream)
        workbook.setMissingCellPolicy(Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)

        if (useAllSheets) {
            mAllSheets = new HashMap<>(workbook.numberOfSheets)
            for (Sheet sheet : workbook.sheetIterator()) {
                mAllSheets.put(sheet.getSheetName(), getAllValues(sheet))
            }
        } else {
            mAllSheets = new HashMap<>(1)
            Sheet sheet = sheetName ? workbook.getSheet(sheetName) : workbook.getSheetAt(0)
            if (sheet == null)
                throw new IllegalArgumentException("Sheet $sheetName does not exist")
            mAllSheets.put(null, getAllValues(sheet))
        }
    }

    static String[][] getAllValues(Sheet sheet) {
        String[][] allCells = new String[sheet.lastRowNum + 1][]
        for (int i = 0; i <= sheet.lastRowNum; i++) {
            Row row = sheet.getRow(i)
            if (row == null)
                continue
            allCells[i] = new String[row.lastCellNum]
            for (int j = 0; j < row.lastCellNum; j++) {
                allCells[i][j] = row.getCell(j).toString() ?: ""
            }
        }
        return allCells
    }

    @Override
    Map<String, String[][]> getResult() {
        mAllSheets
    }
}
