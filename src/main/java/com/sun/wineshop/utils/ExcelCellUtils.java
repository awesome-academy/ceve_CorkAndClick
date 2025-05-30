package com.sun.wineshop.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

public class ExcelCellUtils {

    public static boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = 0; c <= row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    public static String getString(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    public static Double getDouble(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        return cell == null ? null : cell.getNumericCellValue();
    }

    public static Integer getInteger(Row row, int cellIndex) {
        Double value = getDouble(row, cellIndex);
        return value == null ? null : value.intValue();
    }
}
