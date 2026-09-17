package com.mbio.utils;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads test data from TestData.xlsx with Apache POI.
 * Row 0 of every sheet is the header; data starts from row 1.
 */
public final class ExcelReader {

    private ExcelReader() {
    }

    /** Returns all data rows of a sheet as Object[][] - the format a TestNG @DataProvider needs. */
    public static Object[][] getSheetData(String sheetName) {
        try (FileInputStream input = new FileInputStream(ConfigReader.get("testDataPath"));
             Workbook workbook = new XSSFWorkbook(input)) {

            Sheet sheet = getSheet(workbook, sheetName);
            DataFormatter formatter = new DataFormatter();   // reads every cell as text, e.g. 1920 -> "1920"
            int rowCount = sheet.getLastRowNum();
            int columnCount = sheet.getRow(0).getLastCellNum();

            Object[][] data = new Object[rowCount][columnCount];
            for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                    data[rowIndex - 1][columnIndex] = row == null
                            ? ""
                            : formatter.formatCellValue(row.getCell(columnIndex)).trim();
                }
            }
            return data;
        } catch (IOException e) {
            throw new RuntimeException("Unable to read sheet '" + sheetName + "' from test data file", e);
        }
    }

    /** Returns all values of one column, e.g. the list of expected navigation items. */
    public static List<String> getColumnData(String sheetName, int columnIndex) {
        List<String> values = new ArrayList<>();
        for (Object[] row : getSheetData(sheetName)) {
            values.add(String.valueOf(row[columnIndex]));
        }
        return values;
    }

    private static Sheet getSheet(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            throw new RuntimeException("Sheet '" + sheetName + "' not found in test data file");
        }
        return sheet;
    }
}
