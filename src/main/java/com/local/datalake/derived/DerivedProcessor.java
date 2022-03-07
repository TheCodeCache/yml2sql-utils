package com.local.datalake.derived;

import static com.local.datalake.common.Constants.OUTPUT_INVALID_SHEET_NAME;
import static com.local.datalake.common.Constants.OUTPUT_VALID_SHEET_NAME;
import static com.local.datalake.common.Constants.OUTPUT_DERIVED_LOGIC;
import static com.local.datalake.common.Constants.JSONINPUT_PATH;
import static com.local.datalake.common.Constants.TXT_FILEFORMAT;
import static com.local.datalake.common.Constants.ZERO;
import static com.local.datalake.common.Constants.JSON_FILEFORMAT;
import static com.local.datalake.common.Constants.FEED_PATH;
import static com.local.datalake.common.Constants.ASTERISK;
import static com.local.datalake.common.Constants.LPAREN;
import static com.local.datalake.common.Constants.JSON_SCHEMA_FILE_PATH;
import static com.local.datalake.common.Constants.COMMA;
import static com.local.datalake.common.Constants.DOT;
import static com.local.datalake.common.Constants.DASH;
import static com.local.datalake.common.Constants.GREATER_THEN;
import static com.local.datalake.common.Constants.ENCRYPTED_JSON;
import static com.local.datalake.common.Constants.ALPHA_STR;
import static com.local.datalake.common.Constants.NEW_LINE;
import static com.local.datalake.common.Constants.EMPTY;
import static com.local.datalake.common.Constants.INPUT_DERIVED_METADATA_PATH;

import static com.local.datalake.common.Constants.JSON_STRING;

import static com.local.datalake.common.Constants.ROOT_NODE;
import static com.local.datalake.common.Constants.RPAREN;
import java.io.FileReader;
import java.io.IOException;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import java.util.Properties;




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


import com.opencsv.exceptions.CsvValidationException;


import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;


import com.local.datalake.privacy.PrivacyReader;
import com.local.datalake.metareader.*;
import com.local.datalake.parser.IParser;

import com.local.datalake.parser.ParserFactory;
import com.local.datalake.common.FileExtn;
import com.local.datalake.common.Readconfig;
import com.local.datalake.common.ViewHelper;
import com.local.datalake.dto.DerivedInput;
import com.local.datalake.dto.Input;
import com.local.datalake.dto.ParserType;
import com.local.datalake.excelwriter.ExcelWriter;
import com.local.datalake.exception.ViewException;

/**
 * Derived Processor 
 * Entry Point for Derived Utility 
 * @author manoranjan
 * @version 1.0
 */
public class DerivedProcessor {

    private static final Logger log = LoggerFactory.getLogger(DerivedProcessor.class);

    private static Properties props = null;

    static {
        props = Readconfig.loadProperties();
        log.debug("Loaded configuration: {}", props);
    }

    public static void main(String[] args) throws ParseException, Exception {
        DerivedProcessor processor = new DerivedProcessor();

        List < List <String>> resultList = new ArrayList < List <String>> ();
        List <String> validPath = new ArrayList <String> ();
        List <String> invalidList = new ArrayList <String> ();
        List <String> privacyPaths = new ArrayList <String> ();
        List <String> privacyJsonPaths = new ArrayList <String> ();
        List <String> outputPaths = new ArrayList <String> ();
        List <String> prefixList = new ArrayList <String> ();
        Map<String, List <String>> invalidPaths = new HashMap<String, List <String>>();
        Map<String, List <String>> validPaths = new HashMap<String, List <String>>();
        
        HashMap < String, String > derivedMap = new HashMap < String, String > ();
        
        //Read Metadata
        DerivedMetaReader derivedReader = new DerivedMetaReader();
        List <DerivedInput> derivedMeta = derivedReader.read(props.getProperty(INPUT_DERIVED_METADATA_PATH));
        
        derivedMeta.stream().map(DerivedInput::toString).forEach(log::info);
        
        //Genearte Json Schema File 
        processor.generateJsonSchema(derivedMeta);
        
        //Get unique privacy paths
        derivedMeta.stream().map(DerivedInput::getPrivacyPath).forEach(privacyPath -> privacyPaths.add(privacyPath));
        
        Set < String > set = new HashSet < > (privacyPaths);
        privacyPaths.clear();
        privacyPaths.addAll(set);
        
        privacyPaths.stream().forEach(p -> log.info(p));
        
        PrivacyReader privacyReader = new PrivacyReader();
        Multimap < String, String > privacyInfo = ArrayListMultimap.create();
        
        //Read Privacy Path
        privacyPaths.stream().forEach(p -> privacyInfo.putAll(privacyReader.readprivacy(p)));

        for (DerivedInput input : derivedMeta) {
        	privacyJsonPaths = privacyReader.getJsonPaths(input.getFeedIdentifier().trim(), privacyInfo);
            resultList = getValidPath(JSONINPUT_PATH + input.getRootObject() + JSON_FILEFORMAT, privacyJsonPaths);
            log.info("All Path " + JSONINPUT_PATH + input.getRootObject() + JSON_FILEFORMAT + NEW_LINE + FEED_PATH + input.getFeedIdentifier() + TXT_FILEFORMAT);

            validPath = resultList.get(0);
            invalidList = resultList.get(1);
            
            log.info("Size of valid  [{}] and invalid list [{}]",validPath.size(), invalidList.size());
            
            invalidPaths.put(input.getFeedIdentifier(), invalidList);
            validPaths.put(input.getFeedIdentifier(), validPath);

            if (!validPath.isEmpty()) {
                String fnName = "";
                if (input.isArrayMsg()) {
                    prefixList = addPrefix(input.getArrayMsgTag(), validPath);
                    fnName = logicFormatter(input.getFeedIdentifier(), prefixList);
                    derivedMap.put(input.getFeedIdentifier(), fnName);
                } else {
                    fnName = logicFormatter(input.getFeedIdentifier(), validPath);
                    derivedMap.put(input.getFeedIdentifier(), fnName);
                }

            }
        }
        
        derivedMeta.stream().map(DerivedInput::getOutputDerivedPath).forEach(outputPath -> outputPaths.add(outputPath));
                      
        Set < String > outputSet = new HashSet < > (outputPaths);
        outputPaths.clear();
        outputPaths.addAll(outputSet);
        
        log.debug("Size of outputlist [{}]",outputPaths.get(0));
               
        //Write derived logic to Excel File
        ExcelWriter.WriteValue(derivedMap, outputPaths.get(0), OUTPUT_DERIVED_LOGIC);
        //Write valid paths to Excel File
        ExcelWriter.WritePaths(validPaths, outputPaths.get(0), OUTPUT_VALID_SHEET_NAME);
        //Write invalid paths to excel File
        ExcelWriter.WritePaths(invalidPaths, outputPaths.get(0), OUTPUT_INVALID_SHEET_NAME);
        
        
    }

    /**
     * Get Valid Paths
     * 
     * @param jsonPath
     * @param privacyPath
     * @throws ParseException, IOException
     * @return List < List <String>>
     */

    public static List < List <String>> getValidPath(String jsonPath, List <String> privacyPath) throws ParseException, IOException {
        @SuppressWarnings("deprecation")
        JSONParser parser = new JSONParser();
        List <String> allJsonPath = new ArrayList <String> ();
        List <String> tempList = new ArrayList <String> ();
        List <String> finalList = new ArrayList <String> ();
        List <String> singleList = new ArrayList <String> ();
        List <String> singlePaths = new ArrayList <String> ();
        Map<String, Integer> keyCount = new HashMap<String, Integer>();

        Object obj = parser.parse(new FileReader(jsonPath));
        String json_string = obj.toString();

        Map < String, Object > flattenJson = JsonFlattener.flattenAsMap(json_string);

        for (String name: flattenJson.keySet()) {

            allJsonPath.add(name.replaceAll(ZERO, ASTERISK));
        }

        List <String> tempFinaLlist = new ArrayList <String> ();
        tempFinaLlist.addAll(allJsonPath);

        List < List <String>> result = new ArrayList < List <String>> ();

        tempList = allJsonPath;

        log.debug("Size of list from privacy [{}]",privacyPath.size());
        
        privacyPath.stream().forEach(key -> {if (!key.trim().contains(".")) {singleList.add(key);}});
        log.debug("single element list from privacy {}",singleList);

        allJsonPath.retainAll(privacyPath);
        privacyPath.removeAll(tempList);
                      
        finalList.addAll(allJsonPath);
        log.debug("size of final list [{}]",finalList.size());
        
        int valueCount = 0;
        String strTemp="";
        singlePaths = getLeafNodePath(tempFinaLlist, singleList);
        
        int i,j = 0;
        for (i=0; i < singleList.size(); i++) {
        	for (j=0; j < singlePaths.size(); j++) {
        		strTemp = singlePaths.get(j).trim().substring(singlePaths.get(j).trim().lastIndexOf(".") + 1).trim();
        		if(singleList.get(i).equals(strTemp)) {
        		valueCount++;	
        		}
        	}
        	keyCount.put(singleList.get(i), valueCount);
        }
              
        finalList.addAll(getLeafNodePath(tempFinaLlist, singleList));
        log.info("size of final list after adding single element path [{}]",finalList.size());
        
        
        // Create a new LinkedHashSet 
        Set<String> set = new LinkedHashSet<>(); 
  
        // Add the elements to set 
        set.addAll(finalList); 
  
        // Clear the list 
        finalList.clear(); 
  
        // add the elements of set 
        // with no duplicates to the list 
        finalList.addAll(set); 
        
        log.info("size of final list after removing duplicate path [{}]",finalList.size());
        
        keyCount.forEach((key,value) -> log.info("key [{}] & count [{}]",key,value));
 
        for (Map.Entry<String, Integer> entry : keyCount.entrySet()) {
        	String k =   entry.getKey();
        	int v = entry.getValue();
        	
        	if (v > 0 ) {
        		log.info("key need to remove [{}] and value [{}]",k,v);
        		privacyPath.remove(k);
        	}

        }

        //return common & single path
        result.add(finalList);
        //return path not there in all path list
        result.add(privacyPath);

        return result;
    }
    /**
     * Partial path finder
     * 
     * @param allList
     * @param invalidList
     */

    public static void getPartialPath(List <String> allList, List <String> invalidList) {
        try {
	    	List <String> partialList = new ArrayList <String> ();
	        int i, j = 0;	        
	        for (i = 0; i < invalidList.size(); i++) {
	            for (j = 0; j < allList.size(); j++) {
	                if (allList.get(j).trim().contains(invalidList.get(i).trim())) {
	                    partialList.add(allList.get(j));
	                }
	            }
	        }
        } catch (Exception e) {
        	log.error("{}", e.getMessage(), e);
        }
    }
    
    /**
     * For single element, list Json path where element comes at last position in path
     * 
     * @param allList
     * @param invalidList
     * @return List <String>
     */
    public static List <String> getLeafNodePath(List <String> allList, List <String> invalidList) {
    	List <String> single_elepath = new ArrayList <String> ();
    	try {      
	        int i, j = 0;
	        int total_len = 0;
	        int pathCount = 0;
	        String strTemp;
	        Map<String, Integer> keyCount = new HashMap<String, Integer>();
	
	        for (i = 0; i < invalidList.size(); i++) {
            	pathCount = 0;
	            if (!invalidList.get(i).trim().contains(DOT)) {
	                log.debug("single element form privacy [{}]",invalidList.get(i).trim());
	                for (j = 0; j < allList.size(); j++) {
	                    if (allList.get(j).trim().contains(invalidList.get(i).trim())) {
	                        log.debug("Path for single element [{}]",allList.get(j).trim());	                        
	                        if (allList.get(j).trim().contains(DOT)) {

	                            total_len = allList.get(j).trim().length();
	                            strTemp = allList.get(j).trim().substring(allList.get(j).trim().lastIndexOf(".") + 1).trim();
	                            if (strTemp.equals(invalidList.get(i).trim())) {
	                                single_elepath.add(allList.get(j));
	                                pathCount++;
	                            }
	                        }
	                        else {
	                        	single_elepath.add(allList.get(j));
	                        	pathCount++;
	                        }
	                    } 
	                }	
	            }
	            log.debug("Key count for [{} is {}]",invalidList.get(i),pathCount);
	            keyCount.put(invalidList.get(i).trim(), pathCount);
	        }
    	} catch (Exception e) {
    		log.error("{}", e.getMessage(),e);
    	}
        log.debug("single element list " + single_elepath);
        return single_elepath;
    }

    /**
     * Derived Logic formatter
     * 
     * @param feedId
     * @param jsonPath
     * @throws IOException
     * @return String
     */

    public static String logicFormatter(String feedId, List <String> jsonPath) throws IOException {
        String fnName = ENCRYPTED_JSON+LPAREN+JSON_STRING+COMMA;
        try {
	        for (String derivedField: jsonPath) {
	            fnName = fnName + ROOT_NODE+DOT + derivedField + DASH+GREATER_THEN+ALPHA_STR+COMMA;
	        }
	
	        fnName = fnName.substring(0, (fnName.length() - 1));
	        fnName = fnName + RPAREN;

        	} catch (Exception e) {
        	log.error("{}", e.getMessage(),e);
        }
        return fnName;
    }

    /**
     * Add prefix tag in paths
     * 
     * @param prefixPath
     * @param jsonPath
     * @return List <String>
     */
    
    public static List <String> addPrefix(String prefixPath, List <String> jsonPath) {
        List <String> prefixList = new ArrayList <String> ();
        for (String path: jsonPath) {
            prefixList.add(prefixPath + DOT + path);
        }
        return prefixList;
    }

    /**
     * Generate JSON Schema file
     * 
     * @param metadata
     * @return 
     */
    
    public void generateJsonSchema(List < DerivedInput > metadata)
    throws IOException, CsvValidationException, ViewException {

        // loop through each feed/record in metadata and generate View Hql
    	for (DerivedInput derivedinput: metadata)
            try {
                String jsonSwaggerSchema = EMPTY;
                
                //Set values from DerivedInput to Input
                Input input2 = new Input();
                input2.setRootObject(derivedinput.getRootObject());
                input2.setFeedName(derivedinput.getFeedName());
                input2.setYamlFilePath(derivedinput.getYamlFilePath());
                input2.setSpecFileFormat(derivedinput.getSpecFileFormat());
                input2.setYamlVersion(derivedinput.getYamlVersion());
                
                // get parser
                IParser parser = this.getParser(input2, false);
                //IParser parser = this.getParser(input2, false);

                // parse returns same copy of schema directly from yaml but into json format
                jsonSwaggerSchema = input2.hasMultipleRoots() ? parser.parseMultiple(input2) : parser.parse(input2);

                // saving intermediate nested json (view schema) file
                ViewHelper.save(jsonSwaggerSchema,
                    JSON_SCHEMA_FILE_PATH + (ViewHelper.isNullOrNotFound(input2.getRootObject()) ? input2.getFeedName() :
                    	input2.getRootObject()) + FileExtn.JSON.getValue());
            } catch (Exception e) {
                log.error("{}", e.getMessage(), e);
            }
        }
      

    /**
     * Retrieves Parser Instance
     * 
     * @param input
     * @return
     */
    private IParser getParser(Input input, boolean errorCase) {
        // prepares parser type
        ParserType type = new ParserType(input.getYamlVersion(), input.getSpecFileFormat(), input.getRootObject());
        if (errorCase)
            type.setError(true);
        // get parser
        return ParserFactory.getParser(type);
    }
}
