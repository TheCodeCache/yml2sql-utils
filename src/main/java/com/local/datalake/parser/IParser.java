package com.local.datalake.parser;

import static com.local.datalake.common.Constants.OPENAPI;
import static com.local.datalake.common.Constants.OPENAPI_3;
import static com.local.datalake.common.Constants.SWAGGER;
import static com.local.datalake.common.Constants.SWAGGER_2;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.local.datalake.common.ViewHelper;
import com.local.datalake.dto.Input;
import com.local.datalake.exception.ViewException;

/**
 * parser
 * 
 * @author manoranjan
 */
public interface IParser {

    /**
     * Accepts Input Metadata and returns the schema in Json format
     * 
     * Currently only two properties from the argument Input being used:
     * yamlFilePath and rootObject
     * 
     * @param input
     * @return
     * @throws Exception
     */
    String parse(Input input) throws Exception;

    /**
     * when msg type is json-array, then append a tag before the msg <br>
     * in order to turn the whole thing into json-object
     * 
     * TODO pretty json print
     * 
     * @param json json-array msg data
     * @param tag
     * @return
     */
    default String transformJsonMsg(String json, Input input) {

        return (!input.isArrayMsg()
                // this condition is for when feed with json array msg, and it is being parsed
                // for error responses and the error schema is a json-array
                || (input.isArrayMsg() && input.getErrorRootObject() != null && !ViewHelper.isJsonArray(json))) ? json
                        : "{\n \"" + input.getArrayMsgTag() + "\" : " + json + " \n}";
    }

    /**
     * checks swagger yml content against the version specified by user
     * 
     * runtime validation
     * 
     * @param input
     * @throws FileNotFoundException
     * @throws ViewException
     */
    default void checkYmlType(Input input) throws FileNotFoundException, ViewException {

        String ymlPath = input.getYamlFilePath();
        String version = input.getYamlVersion();

        // loads all keys (expanded) from yml
        Set<String> keys = ViewHelper.loadYaml(ymlPath).keySet();

        // throws appropriate exception
        if (keys.contains(OPENAPI) && version.equals(SWAGGER_2))
            throw new ViewException(ymlPath + " does not have Swagger2.0 content");
        else if (keys.contains(SWAGGER) && version.equals(OPENAPI_3))
            throw new ViewException(ymlPath + " does not have Openapi3.0 content");
        else if ((keys.contains(OPENAPI) && keys.contains(SWAGGER))
                || (!keys.contains(OPENAPI) && !keys.contains(SWAGGER)))
            throw new ViewException("InValid Swagger File content");
    }

    /**
     * when feed has multiple root objects, parse each of them and merge
     * 
     * @param input
     * @return
     * @throws Exception
     */
    default String parseMultiple(Input input) throws Exception {
        List<String> jsons = new ArrayList<>();
        String rootRef = "";

        for (String root : input.getRootObjects()) {
            input.setRootObject(root);
            try {
                jsons.add(parse(input));
                rootRef = root;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // just one root is needed as the same is used for resolving alias conflicts
        input.setRootObject(rootRef);
        return merge(jsons);
    }

    /**
     * merge multiple json objects
     * 
     * @param schemas
     * @return
     */
    default String merge(List<String> schemas) {
        JsonObject object = new JsonObject();
        for (String schema : schemas) {
            if (ViewHelper.isJsonArray(schema))
                continue;
            for (Map.Entry<String, JsonElement> elem : JsonParser.parseString(schema).getAsJsonObject().entrySet()) {
                object.add(elem.getKey(), elem.getValue());
            }
        }
        return object.toString();
    }
}
