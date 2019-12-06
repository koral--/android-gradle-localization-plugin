package pl.droidsonroids.gradle.localization

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class XLSXParser implements Parser {

    private Map<String, String[][]> mAllSheets

    XLSXParser(InputStream inputStream, boolean isXls, String sheetName, boolean useAllSheets, boolean evaluateFormulas) {
        Workbook workbook = isXls ? new HSSFWorkbook(inputStream) : new XSSFWorkbook(inputStream)
        workbook.setMissingCellPolicy(Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator()
        if (useAllSheets) {
            mAllSheets = new HashMap<>(workbook.numberOfSheets)
            for (Sheet sheet : workbook.sheetIterator()) {
                mAllSheets.put(sheet.getSheetName(), getAllValues(sheet, evaluator, evaluateFormulas))
            }
        } else {
            mAllSheets = new HashMap<>(1)
            Sheet sheet = sheetName ? workbook.getSheet(sheetName) : workbook.getSheetAt(0)
            if (sheet == null)
                throw new IllegalArgumentException("Sheet $sheetName does not exist")
            mAllSheets.put(null, getAllValues(sheet, evaluator, evaluateFormulas))
        }
    }

    static String[][] getAllValues(Sheet sheet, FormulaEvaluator evaluator, boolean evaluateFormulas) {
        String[][] allCells = new String[sheet.lastRowNum + 1][]
        for (int i = 0; i <= sheet.lastRowNum; i++) {
            Row row = sheet.getRow(i)
            if (row == null)
                continue
            allCells[i] = new String[row.lastCellNum]
            for (int j = 0; j < row.lastCellNum; j++) {
                Cell cell = row.getCell(j)
                if (evaluateFormulas) evaluator.evaluateInCell(cell)
                allCells[i][j] = cell.toString() ?: ""
            }
        }
        return allCells
    }

    @Override
    Map<String, String[][]> getResult() {
        mAllSheets
    }
}
