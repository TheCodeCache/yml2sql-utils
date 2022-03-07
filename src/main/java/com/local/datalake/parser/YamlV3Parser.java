package com.local.datalake.parser;

import java.io.FileNotFoundException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.local.datalake.dto.Input;
import com.local.datalake.exception.InvalidRootObjectException;
import com.local.datalake.exception.ViewException;

import io.swagger.oas.inflector.examples.ExampleBuilder;
import io.swagger.oas.inflector.examples.models.Example;
import io.swagger.oas.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;

/**
 * Yaml Reader
 * <li>Reads OpenAPI 3.0.0 Yaml file
 * <li>Generates schema as a json file
 * 
 * @author manoranjan
 */
public class YamlV3Parser implements IParser {

    private static final Logger log = LoggerFactory.getLogger(YamlV3Parser.class);

    /**
     * parses the yaml 3.0 file and returns the schema in json format
     * 
     * @throws FileNotFoundException
     * 
     * @throws InvalidRootObjectException when root object is missing in yml file
     */
    @SuppressWarnings("rawtypes")
    @Override
    public String parse(Input input) throws ViewException, FileNotFoundException {
        log.info("yaml file path: [{}]", input.getYamlFilePath());

        checkYmlType(input);

        OpenAPI swagger = new OpenAPIV3Parser().read(input.getYamlFilePath());
        // read all schemas definition
        Map<String, Schema> schemas = swagger.getComponents().getSchemas();

        log.info("Root-Object: [{}]", input.getRootObject());

        // get specific schema definition for a given root object
        Schema schema = schemas.get(input.getRootObject());
        if (schema == null)
            throw new InvalidRootObjectException("Root-Object [" + input.getRootObject() + "] not found");
        Example example = ExampleBuilder.fromSchema(schema, schemas);

        // Configure example serializers
        SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
        Json.mapper().registerModule(simpleModule);

        // schema in json format
        return transformJsonMsg(Json.pretty(example), input);
    }
}
