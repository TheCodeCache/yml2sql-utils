package com.local.datalake.parser;

import static com.local.datalake.common.Constants.DOT;
import static com.local.datalake.common.Constants.ESC;

import static com.local.datalake.common.Constants.OPEN_BRACKET;
import static com.local.datalake.common.Constants.CLOSE_BRACKET;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.wnameless.json.flattener.JsonFlattener;

import com.local.datalake.common.ViewHelper;

import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 * Enum Parser
 * 
 * @author manoranjan
 */

public class EnumParser {

    private static final Logger log = LoggerFactory.getLogger(EnumParser.class);
    
    /**
     * Getter & Setter for Enum Parse
     * 
     */

	public class Result {

		private boolean isMissingTypeFound;
		private Map<String, Object> content;
		//private String modifiedFilePath;

		public boolean isMissingTypeFound() {
			return isMissingTypeFound;
		}

		protected void setMissingTypeFound(boolean isMissingTypeFound) {
			this.isMissingTypeFound = isMissingTypeFound;
		}

		public Map<String, Object> getContent() {
			return content;
		}

		protected void setContent(Map<String, Object> content) {
			this.content = content;
		}


	}

    /**
     * checks whether a string has missing value
     * 
     * @param word
     * @return
     */
    public static boolean isNull(Map < String, Object > word) {
        return word == null;
    }

    /**
     * Get array element name as sting
     * 
     * @param arrString
     * @return
     */
    public static String getArrayPart(String arrString) {
        String arrPart = arrString.substring(arrString.indexOf(OPEN_BRACKET), arrString.indexOf(CLOSE_BRACKET) + 1);
        return arrPart;
    }
    
    /**
     * Get array element Index
     * 
     * @param partString
     * @return
     */
    public static String getArrayIndex(String partString) {
        String arrayIndex = "";
        if (partString.length() == 3)
            arrayIndex = String.valueOf(partString.charAt(1));
        else
            arrayIndex = partString.substring(1, (partString.length() - 1));
        return arrayIndex;
    }
    

    /**
     * Update yaml file for type as enum
     * 
     * @param yamlPath
     * @return
     */
	
    public Result parseYaml(String yamlPath) 
    		throws JsonGenerationException, JsonMappingException, IOException, ParseException {
    	
    	Result result = new Result();
    	
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(yamlPath);

            Map < String, Object > tree = yaml.load(inputStream);

            ObjectMapper jsonWriter = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
           
            byte[] objTreeValue = null;            
            objTreeValue = jsonWriter.writeValueAsBytes(tree);
     
            @SuppressWarnings("deprecation")
            JSONParser parser1 = new JSONParser();
           
            Object obj1 = parser1.parse(objTreeValue);
            String json_string1 = obj1.toString();

            Map < String, Object > flattenJson = JsonFlattener.flattenAsMap(json_string1);
           
            List < String > allYamlPath = new ArrayList < String > ();
            List < String > typeList = new ArrayList < String > ();

            for (String name: flattenJson.keySet()) {

                if (name.contains("enum")) {
                    allYamlPath.add(name);
                    allYamlPath.add(name.replaceAll("enum\\[\\d\\]", "") + "type");
                }
            }

            Set < String > set = new HashSet < > (allYamlPath);
            allYamlPath.clear();
            allYamlPath.addAll(set);


            int j = 0;

            for (String name: flattenJson.keySet()) {
                for (j = 0; j < allYamlPath.size(); j++) {
                    if (allYamlPath.get(j).trim().contains(name.trim())) {
                        typeList.add(allYamlPath.get(j));
                    }
                }
            }
            //System.out.println(typeList);
            allYamlPath.removeAll(typeList);
            
            log.info("Size of enum list [{}]",allYamlPath.size());
                     
            // Update yaml  
            ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

            // read YAML file
            Map < String, Object > yamlContent = objectMapper.readValue(new File(yamlPath),
                new TypeReference < Map < String, Object >> () {});

            log.debug("Yaml file path for creating object [{}]", yamlPath);
            
            result.setMissingTypeFound(allYamlPath.size() > 0 ? true : false);
            
            j = 0;

            for (j = 0; j < allYamlPath.size(); j++) {
                String[] tempType = {};
                String temp_var = "";
                log.info("Key for which add type {}", allYamlPath.get(j));
                temp_var = allYamlPath.get(j);

                tempType = temp_var.split(ESC+DOT);

                List < Map < String, Object >> maps = new ArrayList < Map < String, Object >> ();

                String arrIndex = "";

                if (Pattern.matches(".*\\[.*\\d.*.*\\]", tempType[0])) {
                    log.info("Element is type of array [{}]", tempType[0]);
                    List < Map < String, Object >> arrYaml = (List < Map < String, Object >> ) yamlContent.get(tempType[0].replaceAll("\\[.*.\\]", ""));
                    log.debug("after array yaml conversion");

                    arrIndex = getArrayIndex(getArrayPart(tempType[0]));
                    log.debug("Index part from key[{}]", arrIndex);

                    maps.add((Map < String, Object > ) arrYaml.get(Integer.parseInt(arrIndex)));
                    log.debug("Key [{}]", tempType[0]);
                } else {
                    log.info("Element is not a type of array [{}]", tempType[0]);
                    maps.add((Map < String, Object > ) yamlContent.get(tempType[0]));
                    log.debug("Key [{}]", tempType[0]);
                }


                int i = 1;
                for (i = 1; i < tempType.length; i++) {
                    arrIndex = "";
                    if (Pattern.matches(".*\\[.*\\d.*.*\\]", tempType[i])) {
                        log.info("Element is type of array [{}]", tempType[i]);
                        List < Map < String, Object >> arrYaml = (List < Map < String, Object >> ) maps.get(i - 1).get(tempType[i].replaceAll("\\[.*.\\]", ""));
                        log.debug("after array yaml conversion");

                        arrIndex = getArrayIndex(getArrayPart(tempType[i]));
                        log.debug("Index part from key[{}]", arrIndex);

                        maps.add((Map < String, Object > ) arrYaml.get(Integer.parseInt(arrIndex)));
                        log.debug("Key [{}]", tempType[i]);

                    } else {
                        maps.add((Map < String, Object > ) maps.get(i - 1).get(tempType[i]));
                        log.debug("Key [{}]", tempType[i]);
                    }

                }

                maps.get(tempType.length - 2).put("type", "string");
                inputStream.close();  
            }
            log.info("***********************Enum Processing Completed*************************");
			result.setContent(yamlContent);
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
            e.printStackTrace();
        }
        
        return result;
    }
}
