package com.local.datalake.excelwriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Excel Writer
 * 
 * @author manoranjan
 */
public class ExcelWriter {

    private static final Logger log = LoggerFactory.getLogger(ExcelWriter.class);
    
    /**
     * Write derived logic to seperate sheet
     * 
     * @param derivedVal
     * @param excelPath
     * @param sheetName
     * @return
     */

    public static void WriteValue(HashMap < String, String > derivedVal, String excelPath, String sheetName) throws IOException {

        try {
            Path path = Paths.get(excelPath);
            Files.deleteIfExists(path);

            //Create blank workbook
            XSSFWorkbook workbook = new XSSFWorkbook();
            
            //Create a blank sheet
            XSSFSheet spreadsheet = workbook.createSheet(sheetName);

            //Create row object
            XSSFRow row;

            //This data needs to be written (Object[])
            Map < String, Object[] > derivedInfo = new TreeMap < String, Object[] > ();
            derivedInfo.put("1", new Object[] {
                "Feed_ID",
                "Derived_Logic"
            });


            int i = 2;
            Set set = derivedVal.entrySet();
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                Map.Entry mentry = (Map.Entry) iterator.next();
                log.info("Key is {} & value is {}", mentry.getKey(), mentry.getValue());
                derivedInfo.put(Integer.toString(i), new Object[] {
                    mentry.getKey(), mentry.getValue()
                });
                i++;
            }

            //Iterate over data and write to sheet
            Set < String > keyId = derivedInfo.keySet();
            int rowId = 0;

            for (String key: keyId) {
                row = spreadsheet.createRow(rowId++);
                Object[] objectArr = derivedInfo.get(key);
                int cellId = 0;

                for (Object obj: objectArr) {
                    Cell cell = row.createCell(cellId++);
                    cell.setCellValue((String) obj);
                }
            }
            //Write the workbook in file system
            FileOutputStream out = new FileOutputStream(
                new File(excelPath));

            workbook.write(out);
            out.flush();
            out.close();
            log.info("written successfully to excel file");

        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
            e.printStackTrace();

        }

    }

    /**
     * Write Json paths to seperate sheet
     * 
     * @param invalidPath
     * @param excelPath
     * @param sheetName
     * @return
     */
    
    public static void WritePaths(Map<String, List<String>> invalidPath, String excelPath, String sheetName) {
        try {        	
        File file = new File(excelPath);
        XSSFWorkbook workbook;
        InputStream excelStream = new FileInputStream(file);
        workbook = new XSSFWorkbook(excelStream);	

        //Create a blank sheet
        XSSFSheet spreadsheet = workbook.createSheet(sheetName);

        //Create row object
        XSSFRow row;

        //This data needs to be written (Object[])
        Map < String, Object[] > derivedInfo = new TreeMap < String, Object[] > ();
        derivedInfo.put("1", new Object[] {
            "Feed_ID",
            "Json_Path"
        });


        int i = 2;               
        for (Map.Entry<String, List<String>> entry : invalidPath.entrySet()) {
        	String k = entry.getKey();
        	for (String v : entry.getValue()) {
        		derivedInfo.put(Integer.toString(i), new Object[] {k, v});
        		i++;
        	}       	
        } 
            

        //Iterate over data and write to sheet
        Set < String > keyId = derivedInfo.keySet();
        int rowId = 0;

        for (String key: keyId) {
            row = spreadsheet.createRow(rowId++);
            Object[] objectArr = derivedInfo.get(key);
            int cellId = 0;

            for (Object obj: objectArr) {
                Cell cell = row.createCell(cellId++);
                cell.setCellValue((String) obj);
            }
        }
        //Write the workbook in file system
        FileOutputStream out = new FileOutputStream(
            new File(excelPath));

        workbook.write(out);
        out.flush();
        out.close();
        log.info("written successfully to excel file");

    } catch (Exception e) {
        log.error("{}", e.getMessage(), e);
        e.printStackTrace();

    }
  }
}
