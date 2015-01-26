package pl.droidsonroids.gradle.localization;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by zhangls on 11/29/14.
 */
public class XlsxParser {

    String all[][];

    public XlsxParser(File file) throws IOException {
        all = getAll(file);
    }


    private String[][] getAll(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);

        Workbook book;
        book = file.getAbsolutePath().endsWith("xls") ? new HSSFWorkbook(inputStream) : new XSSFWorkbook(inputStream);

        //得到Excel第一个sheet
        Sheet se = book.getSheetAt(0);
        String[][] result = new String[se.getLastRowNum()][];

        for (int i = 0; i < se.getLastRowNum(); i++) {
            Row row = se.getRow(i);
            result[i] = new String[row.getLastCellNum()];
            for (int j = 0; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                //TODO null?
                result[i][j] = cell == null ? "" : cell.toString();
            }
        }
        return result;
    }

    public String[] getLine(String[][] all) {
        String[] result = new String[all[0].length];
        for (int i = 0; i < all[0].length; i++) {
            result[i] = all[0][i];
        }
        return result;
    }

    public String[] getLine() {
        return getLine(all);
    }

    public String[][] getAllValues(String[][] all) {
        String[][] result = new String[all.length][];
        for (int i = 1; i < all.length; i++) {
            int length = all[i].length;
            result[i - 1] = new String[length];
            for (int j = 0; j < length; j++) {
                result[i - 1][j] = all[i][j];
            }
        }
        return result;
    }

    public String[][] getAllValues() {
        return getAllValues(all);
    }
}
