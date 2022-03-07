package com.local.datalake.privacy;

import static com.local.datalake.common.Constants.EXCEL_HEADER;
import static com.local.datalake.common.Constants.NEW_LINE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;

import java.util.List;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.local.datalake.common.Readconfig;

/**
 * Privacy Reader
 * 
 * @author manoranjan
 */
public class PrivacyReader {

    private static final Logger log   = LoggerFactory.getLogger(PrivacyReader.class);

    private static Properties   props = null;

    static {
        props = Readconfig.loadProperties();
        log.debug("Loaded configuration: {}", props);
    }

    /**
     * Get Merged Cell Address
     * 
     * @param cell
     */

    public static CellRangeAddress getMergedRegion(Cell cell) {
        Sheet sheet = cell.getSheet();
        for (CellRangeAddress range : sheet.getMergedRegions()) {
            if (range.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                return range;
            }
        }
        return null;
    }

    /**
     * Get Cell Value
     * 
     * @param cell
     * @param sheet
     * @return String
     */

    public static String getCellValue(Cell cell, XSSFSheet sheet) {
        String cellValue = "";
        try {
            if (cell.getCellType() == CellType.BLANK) {
                CellRangeAddress range = getMergedRegion(cell);
                if (range != null) {
                    Cell mergeValue = sheet.getRow(range.getFirstRow()).getCell(range.getFirstColumn());
                    cellValue = mergeValue.getStringCellValue();
                }
            } else if (cell.getCellType() == CellType.STRING) {
                cellValue = cell.getStringCellValue();
            }
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
            e.printStackTrace();
        }
        return cellValue;
    }

    /**
     * Read privacy sheet
     * 
     * @param dataPrivacPath
     * @return Multimap < String, String >
     */

    public Multimap<String, String> readprivacy(String dataPrivacPath) {
        Multimap<String, String> dataMap = ArrayListMultimap.create();
        try {
            int skipRows = Integer.parseInt(props.getProperty(EXCEL_HEADER));

            FileInputStream file = new FileInputStream(new File(dataPrivacPath));

            // Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            // Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);

            // Iterate through each rows one by one
            Cell feed_id = null;

            String feedKey = "";
            String feedJsonPath = "";
            int excelRowNum = 0;

            for (Row row : sheet) {
                if (row.getRowNum() > skipRows) {
                    for (Cell cell : row) {
                        if (cell.getColumnIndex() == 0) {
                            feedKey = getCellValue(cell, sheet);
                        } else if (cell.getColumnIndex() == 4) {
                            feedJsonPath = getCellValue(cell, sheet);
                        }
                    }
                    Cell piCell = row.getCell(6);
                    if (piCell.getStringCellValue().trim().equals("PI")) {
                        log.debug("key is [{}], and value [{}], and row_num is [{}]", feedKey, feedJsonPath,
                                excelRowNum);
                        dataMap.put(feedKey.trim(), feedJsonPath.trim());
                    }
                    excelRowNum++;
                }
            }
            file.close();
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
            e.printStackTrace();
        }
        return dataMap;
    }

    /**
     * Get Json paths feed wise
     * 
     * @param feedId
     * @param dataMap
     * @return List< String >
     */

    public List<String> getJsonPaths(String feedId, Multimap<String, String> dataMap) throws IOException {
        List<String> store = new ArrayList<String>();
        try {
            store.addAll(dataMap.get(feedId));
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
            e.printStackTrace();
        }
        return store;

    }

    /**
     * Save json paths to txt file
     * 
     * @param txtFilePath
     * @param pathList
     * @return
     */

    public void saveDataTxtfile(String txtFilePath, List<String> pathList) throws IOException {
        try {
            FileWriter writer = new FileWriter(txtFilePath, true);
            for (String jsonpath : pathList) {
                writer.write(jsonpath + NEW_LINE);
            }
            writer.close();
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
