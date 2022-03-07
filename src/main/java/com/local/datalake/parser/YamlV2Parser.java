package com.local.datalake.parser;

import static com.local.datalake.common.Constants.DEPENDENCY_PATH;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.local.datalake.common.ViewHelper;
import com.local.datalake.dto.Input;
import com.local.datalake.exception.InvalidRootObjectException;
import com.local.datalake.parser.EnumParser.Result;

import io.swagger.models.Model;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.SwaggerParser;
import io.swagger.v3.core.util.Json;

/**
 * parser for swagger 2.0 file
 * 
 * @author manoranjan
 */
public class YamlV2Parser implements IParser {

	private static final Logger log = LoggerFactory.getLogger(YamlV2Parser.class);

	/**
	 * parses the yaml 2.0 file and returns the schema in json format
	 */
	@Override
	public String parse(Input input) throws Exception {
		log.info("yaml file path: [{}]", input.getYamlFilePath());

		checkYmlType(input);

		Map<String, Object> tree = null;
		Result result = new EnumParser().parseYaml(input.getYamlFilePath());
		if (result.isMissingTypeFound())
			tree = result.getContent();
		else
			tree = ViewHelper.loadYaml(input.getYamlFilePath());

		JsonNode node = new ObjectMapper().valueToTree(tree);
		Swagger swagger = new SwaggerParser().read(node);

		Map<String, Model> definitions = swagger.getDefinitions();

		log.info("Root-Object: [{}]", input.getRootObject());

		// get specific schema definition for a given root object
		Model model = definitions.get(input.getRootObject());
		if (model == null)
			throw new InvalidRootObjectException("Root-Object [" + input.getRootObject() + "] not found");

		// loading jar resources
		URLClassLoader clsLoader = URLClassLoader
				.newInstance(new URL[] { new File(DEPENDENCY_PATH + "swagger-inflector-1.0.19.jar").toURI().toURL() });

		Class<?> exampleBuilderCls = clsLoader.loadClass("io.swagger.inflector.examples.ExampleBuilder");
		Method method = exampleBuilderCls.getMethod("fromProperty", Property.class, Map.class);

		Object example = method.invoke(null, new RefProperty(input.getRootObject()), definitions);

		Class<?> jsonNodeExampleSerializerCls = clsLoader
				.loadClass("io.swagger.inflector.processors.JsonNodeExampleSerializer");
		Class<?> simpleModuleCls = clsLoader.loadClass("com.fasterxml.jackson.databind.module.SimpleModule");

		Method addSerializer = simpleModuleCls.getMethod("addSerializer", JsonSerializer.class);

		// Configure example serializers
		SimpleModule simpleModule = (SimpleModule) addSerializer.invoke(simpleModuleCls.newInstance(),
				jsonNodeExampleSerializerCls.newInstance());
		Json.mapper().registerModule(simpleModule);

		// model in json format
		return transformJsonMsg(Json.pretty(example), input);
	}
}
