package com.local.datalake.privacy;

import static com.local.datalake.common.Constants.EMPTY;
import static com.local.datalake.common.ViewHelper.turnNullOrWhiteSpaceToEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.local.datalake.exception.DataPrivacyReadException;
import com.local.datalake.exception.ViewException;

/**
 * Data Privacy Reader
 * 
 * @author manoranjan
 */
public class DataPrivacyReader {

    private static final Logger              log       = LoggerFactory.getLogger(DataPrivacyReader.class);

    private Map<MergedCellKey, String>       reference = new HashMap<>();

    public static final Map<String, Integer> header    = Collections.unmodifiableMap(new HashMap<String, Integer>() {
                                                           {
                                                               put("feed_identifier", 0);
                                                               put("source_feed_name", 1);
                                                               put("error_root_object", 2);
                                                               put("root_object", 3);
                                                               put("json_path", 4);
                                                               put("yml_file_attached", 5);
                                                               put("classification", 6);
                                                           }
                                                           private static final long serialVersionUID = -2605618015411712260L;
                                                       });

    public List<PrivacyInfo> read(String path) throws IllegalStateException, IOException, ViewException {
        return read(new File(path));
    }

    /**
     * reads privacy excel file and de-normalize all of its rows
     * 
     * @param excel
     * @return
     * @throws IllegalStateException
     * @throws IOException
     * @throws ViewException
     */
    public List<PrivacyInfo> read(File excel) throws IllegalStateException, IOException, ViewException {

        List<PrivacyInfo> privacyInfos = new ArrayList<>();

        try (FileInputStream is = new FileInputStream(excel); XSSFWorkbook workbook = new XSSFWorkbook(is);) {

            log.info("reads privacy excel file");
            final int SHEET_INDEX = 0;
            final int ROW_OFFSET = 1;

            Sheet sheet = workbook.getSheetAt(SHEET_INDEX);

            List<String> store = new ArrayList<String>();
            int physicalNumberOfRows = sheet.getLastRowNum() + 1;
            log.info("number of rows:" + physicalNumberOfRows);
            // get the csv writer

            // loop over each excel rows
            for (int i = ROW_OFFSET; i < physicalNumberOfRows; i++) {
                store.clear();
                log.info("reading row:" + i);
                Row row = sheet.getRow(i);
                // loop through each cell
                if (row != null) {
                    for (int j = 0; j < header.size(); j++) {
                        Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                        CellRangeAddress mergedRegion = getMergedRegionForCell(cell, sheet);
                        if (mergedRegion != null) {
                            MergedCellKey key = new MergedCellKey(mergedRegion.getFirstRow(), mergedRegion.getLastRow(),
                                    cell.getColumnIndex());
                            if (reference.get(key) == null) {
                                reference.put(key,
                                        cell != null && cell instanceof XSSFCell ? getResultFromCell((XSSFCell) cell)
                                                : EMPTY);
                            }
                            store.add(reference.get(key));
                        } else {
                            if (cell != null && cell instanceof XSSFCell) {
                                // get the cell value
                                store.add(getResultFromCell((XSSFCell) cell));
                            } else
                                store.add(EMPTY);
                        }
                    }
                }
                if (!store.isEmpty())
                    privacyInfos.add(transform(store));
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new DataPrivacyReadException(ex.getMessage(), ex);
        }

        return privacyInfos;
    }

    /**
     * transfer a single row/record values into PrivacyInfo object
     * 
     * @param store
     * @return
     */
    private PrivacyInfo transform(List<String> store) {
        PrivacyInfo info = new PrivacyInfo();
        info.setIdentifier(turnNullOrWhiteSpaceToEmpty(store.get(header.get("feed_identifier"))));
        info.setFeedName(turnNullOrWhiteSpaceToEmpty(store.get(header.get("source_feed_name"))));
        info.setErrorRootObject(turnNullOrWhiteSpaceToEmpty(store.get(header.get("error_root_object"))));
        info.setRootObject(turnNullOrWhiteSpaceToEmpty(store.get(header.get("root_object"))));
        info.setFieldJsonPath(turnNullOrWhiteSpaceToEmpty(store.get(header.get("json_path"))));
        info.setYmlAttached(turnNullOrWhiteSpaceToEmpty(store.get(header.get("yml_file_attached"))));
        info.setClassification(turnNullOrWhiteSpaceToEmpty(store.get(header.get("classification"))));
        info.setComponent(info.getFeedName());
        return info;
    }

    /**
     * checks for a cell if it belongs to merged region
     * 
     * @param cell
     * @param sheet
     * @return
     */
    private CellRangeAddress getMergedRegionForCell(Cell cell, Sheet sheet) {
        for (CellRangeAddress mergedRegion : sheet.getMergedRegions()) {
            if (mergedRegion.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                // This region contains the cell in question
                return mergedRegion;
            }
        }
        // Not in any
        return null;
    }

    /**
     * fetch actual value from a given cell
     * 
     * @param cell
     * @return
     */
    private static String getResultFromCell(XSSFCell cell) {
        String result = null;

        switch (cell.getCellType()) {
            case STRING:
                // for string cell
                result = cell.getStringCellValue();
                break;
            case NUMERIC:
                // checks if the cell is a date
                if (DateUtil.isCellDateFormatted(cell)) {
                    result = new DataFormatter().formatCellValue(cell);
                } else {
                    // reads the cell data as plain string instead of its formatted counterpart
                    // to handle exponential representation of floating point data
                    result = NumberToTextConverter.toText(cell.getNumericCellValue());
                    BigDecimal dec = new BigDecimal(result);
                    result = dec.toPlainString();
                }
                break;
            case BOOLEAN:
                // instead of POI's TRUE/FALSE, it reads as true/false
                result = Boolean.toString(cell.getBooleanCellValue());
                break;
            default:
                break;
        }
        // returns the cell value
        return result;
    }
}
