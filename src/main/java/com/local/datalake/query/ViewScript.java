package com.local.datalake.query;

import static com.local.datalake.common.Constants.AS;
import static com.local.datalake.common.Constants.COMMA;
import static com.local.datalake.common.Constants.DECRYPT_UDF;
import static com.local.datalake.common.Constants.DOT;
import static com.local.datalake.common.Constants.EMPTY;
import static com.local.datalake.common.Constants.ENCRYPTED_JSON;
import static com.local.datalake.common.Constants.ERROR;
import static com.local.datalake.common.Constants.INPUT_METADATA_PATH;
import static com.local.datalake.common.Constants.INPUT_PRIVACY_PATH;
import static com.local.datalake.common.Constants.JSON_STRING;
import static com.local.datalake.common.Constants.NEW_LINE;
import static com.local.datalake.common.Constants.REPORT_PATH;
import static com.local.datalake.common.Constants.RESPONSE;
import static com.local.datalake.common.Constants.ROOT_NODE;
import static com.local.datalake.common.Constants.RPAREN;
import static com.local.datalake.common.Constants.RTF_CONTENT_PATH;
import static com.local.datalake.common.Constants.SCHEMA_FILE_PATH;
import static com.local.datalake.common.Constants.SEMI_COLN;
import static com.local.datalake.common.Constants.SPACE;
import static com.local.datalake.common.Constants.SUCCESS;
import static com.local.datalake.common.Constants.TAB;
import static com.local.datalake.common.Constants.UNDER_SCORE;
import static com.local.datalake.common.Constants.VIEW_SUFFIX;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Stack;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.local.datalake.alias.AliasDefinition;
import com.local.datalake.alias.AliasResolver;
import com.local.datalake.common.Configuration;
import com.local.datalake.common.FileExtn;
import com.local.datalake.common.ViewHelper;
import com.local.datalake.dto.BaseDO;
import com.local.datalake.dto.Column;
import com.local.datalake.dto.Input;
import com.local.datalake.dto.JsonKey;
import com.local.datalake.dto.ParserType;
import com.local.datalake.exception.AliasCollisionException;
import com.local.datalake.exception.ViewException;
import com.local.datalake.parser.IParser;
import com.local.datalake.parser.MetadataReader;
import com.local.datalake.parser.ParserFactory;
import com.local.datalake.privacy.DataPrivacyReader;
import com.local.datalake.privacy.PrivacyInfo;
import com.opencsv.exceptions.CsvValidationException;

/**
 * Generates View DDL Query
 * 
 * Entry point for Utility
 * 
 * confluence-link:
 * <li>https://confluence.mrshmc.com/pages/viewpage.action?pageId=165283036
 * required input:
 * <li>required input files are input-metadata & swagger yaml files
 * <li>data_privacy excel file is an optional file to view-utility<br>
 * it supports:
 * <li>local yaml path
 * <li>rtf, yaml, yml files
 * 
 * @note: it does not interact with any persistent store systems
 * 
 * @author manoranjan
 */
public class ViewScript {

    private static final Logger log   = LoggerFactory.getLogger(ViewScript.class);

    private static Properties   props = null;

    static {
        props = Configuration.loadProperties();
        log.debug("Loaded configuration: {}", props);

        try {
            ViewHelper.cleanup(RTF_CONTENT_PATH);
            ViewHelper.cleanup(SCHEMA_FILE_PATH);
            ViewHelper.cleanup(REPORT_PATH);
        } catch (IOException ex) {
            log.error("Temp folder clean up got failed due to {}", ex.getMessage(), ex);
        }
    }

    // view builder
    private StringBuilder           viewBuilder = new StringBuilder();

    // captures view generation report
    private static ValidationReport report      = ValidationReport.getInstance();

    /**
     * Entry Point
     * 
     * <li>Loads the input metadata csv file
     * <li>validates it
     * <li>Loads the data_privacy excel file
     * <li>invokes generate method to create view
     * 
     * @param args
     * @throws IOException
     * @throws CsvValidationException
     * @throws ViewException
     */
    public static void main(String[] args) throws IOException, CsvValidationException, ViewException {
        try {
            ViewScript script = new ViewScript();
            List<PrivacyInfo> privacy = null;

            // Reads input metadata csv file
            MetadataReader metadataReader = new MetadataReader();
            List<Input> metadata = metadataReader.read(props.getProperty(INPUT_METADATA_PATH));

            // logs feeds meta
            metadata.stream().map(Input::toString).forEach(log::info);

            script.validate(metadata);

            if (!ViewHelper.isNull(props.getProperty(INPUT_PRIVACY_PATH))) {
                // Reads Data_Privacy excel file
                DataPrivacyReader privacyReader = new DataPrivacyReader();
                privacy = privacyReader.read(props.getProperty(INPUT_PRIVACY_PATH));
            } else
                privacy = new ArrayList<>();

            // logs privacy meta
            privacy.stream().map(PrivacyInfo::toString).forEach(log::info);

            Supplier<Stream<Input>> fullyEncFeeds = () -> metadata.stream().filter(Input::isCompleteMsgEncrypted);

            if (fullyEncFeeds.get().findAny().isPresent()) {
                List<String> idetifiers = fullyEncFeeds.get().map(Input::getIdentifier).collect(Collectors.toList());
                List<PrivacyInfo> privacies = privacy.stream().filter(x -> idetifiers.contains(x.getIdentifier()))
                        .collect(Collectors.toList());
                script.validate(privacies);
            }

            // generates view ddl script
            script.generate(metadata, privacy);
        } finally {
            report.show();
        }
    }

    /**
     * Validates the Input Metadata
     * 
     * @param viewInput
     * @throws ViewException
     */
    private void validate(List<? extends BaseDO> viewInput) throws ViewException {
        // validates the metadata csv file
        for (BaseDO baseDO : viewInput) {
            boolean isPrivacy = baseDO instanceof PrivacyInfo;

            String feed = ViewHelper.isNull(baseDO.getFeedName()) ? baseDO.getIdentifier() : baseDO.getFeedName();

            // loggers
            if (!isPrivacy)
                log.info("validating input metadata for feed: [{}]", feed);
            else
                log.info("validating json-path [{}] for feed: [{}]", ((PrivacyInfo) baseDO).getFieldJsonPath(), feed);

            try {
                // validates model object
                ViewHelper.validate(baseDO);
            } catch (ViewException ex) {
                report.capture(baseDO.getIdentifier(), baseDO.getFeedName(), baseDO.getComponent(), "Validation Failed",
                        ex.getMessage(), ex);
                throw ex;
            }

            // loggers
            if (!isPrivacy)
                log.info("validated feed: [{}]", feed);
            else
                log.info("validated json-path [{}] for feed: [{}]", ((PrivacyInfo) baseDO).getFieldJsonPath(), feed);
        }
        log.info("All feeds have been validated successfully");
    }

    /**
     * Generate Function
     * 
     * generates View-HQL for every entry in metadata provided the entry is valid
     * 
     * @param args
     * @throws IOException
     * @throws CsvValidationException
     * @throws ViewException
     */
    public void generate(List<Input> metadata, List<PrivacyInfo> privacyInfo)
            throws IOException, CsvValidationException, ViewException {

        // loop through each feed/record in metadata and generate View Hql
        for (Input input : metadata) {
            try {
                String jsonSwaggerSchema = EMPTY;

                // get parser
                IParser parser = this.getParser(input, false);

                // parse returns same copy of schema directly from yaml but into json format
                jsonSwaggerSchema = input.hasMultipleRoots() ? parser.parseMultiple(input) : parser.parse(input);

                // saving intermediate nested json (view schema) file
                ViewHelper.save(jsonSwaggerSchema,
                        SCHEMA_FILE_PATH + (ViewHelper.isNullOrNotFound(input.getRootObject()) ? input.getFeedName()
                                : input.getRootObject()) + FileExtn.JSON.getValue());

                Supplier<Stream<PrivacyInfo>> privacySupplier = () -> privacyInfo.stream()
                        .filter(x -> x != null && x.getIdentifier().equalsIgnoreCase(input.getIdentifier()));

                // generate json-path from json schema except for array nodes & computes alias
                LinkedHashMap<String, String> jPathsAliasMap = getJPathsWithAlias(jsonSwaggerSchema);

                // when feed has placeholder attributes such as additionalProps1,2,3 etc
                if (input.isPlaceholder())
                    populateWithRealJsonPath(jPathsAliasMap, input, privacySupplier);

                // adds error-response msges in View only for Response feeds
                if (RESPONSE.equals(input.getMessageType()))
                    addErrorResponseJsonPath(jPathsAliasMap, input, privacySupplier);

                // checks when expanded json-path is just a root_node
                if (validateGeneratedJPaths(jPathsAliasMap)) // checks for invalid corner-case in aliases
                    throw new ViewException("Failed to generate View-HQL for feed: [" + input.getFeedName() + "] "
                            + "as Json-Path is not correct, \"$\" path has been detected");

                // resolve conflicts between aliases
                AliasResolver resolver = new AliasResolver(input, jPathsAliasMap);
                try {
                    resolver.tryResolve();
                } catch (AliasCollisionException ex) {
                    log.error("{}", ex.getMessage(), ex);
                    resolver.forceResolve();
                } finally {
                    resolver.verify();
                    log.info("Alias Collision has been resolved for feed: [" + input.getFeedName() + "]");
                }

                // builds View Hql query
                String query = buildDDL(jPathsAliasMap, privacySupplier, input);

                log.info("View Scripts Begin: \n\n{}", query);
                String path = ViewHelper.getNormalizedPath(input.getOutputViewPath()) + File.separator + input.getView()
                        + FileExtn.HQL.getValue();
                log.info("View Creation Path: {}", path);
                ViewHelper.save(query, path, false);
                log.info("View Scripts Over!: \n\n");
                report.capture(input.getIdentifier(), input.getFeedName(), input.getComponent(), SUCCESS);
            } catch (Exception ex) {
                log.error("View Creation for Feed [{}] has been failed", input.getFeedName(), ex);
                report.capture(input.getIdentifier(), input.getFeedName(), input.getComponent(), "View-Creation Failed",
                        ex.getMessage(), ex);
            }
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

    /**
     * builds ddl definition
     * 
     * @param jPathsAliasMap map between json-path and computed alias from itself
     * @param input          csv metadata
     * @return
     * @throws IOException
     */
    public String buildDDL(Map<String, String> jPathsAliasMap, Supplier<Stream<PrivacyInfo>> privacySupplier,
            Input input) throws IOException {

        String db = input.getDatabase();
        String table = input.getTable();

        // prepares the DROP statement
        viewBuilder.setLength(0);
        viewBuilder.append("DROP VIEW IF EXISTS ").append(db).append(DOT).append(table).append(VIEW_SUFFIX)
                .append(SEMI_COLN).append(System.lineSeparator()).append("CREATE VIEW IF NOT EXISTS ").append(db)
                .append(DOT).append(table).append(VIEW_SUFFIX).append(AS).append(System.lineSeparator());

        viewBuilder.append("SELECT ");

        // projection builder
        ProjectionBuilder builder = new ProjectionBuilder(input, jPathsAliasMap);

        // Use encrypted_json when at least one nested field from original json is
        // sensitive, else json_string
        String column = input.isCompleteMsgEncrypted()
                ? ViewHelper.getBaseTableName() + DOT + ViewHelper.getBaseColumnName()
                : input.isSensitive() ? ENCRYPTED_JSON : JSON_STRING;

        jPathsAliasMap.entrySet().stream().forEach(entry -> builder.wrapGetJsonObjectHiveUDF(column, entry.getKey()));

        if (input.isCompleteMsgEncrypted())
            jPathsAliasMap.entrySet().stream().forEach(entry -> {
                if (privacySupplier.get().anyMatch(x -> entry.getKey().equals(x.getFieldJsonPath())))
                    builder.wrapWithCustomEncryptUDF(entry.getKey(), ViewHelper.getBaseTableName(),
                            ViewHelper.getBaseColumnName());
            });

        List<String> projections = builder.addAuditTrails().addHeaderFields().addConstantCols().build();

        // logging projections
        projections.stream().forEach(log::info);

        viewBuilder.append(NEW_LINE);
        projections.stream().forEach(
                projection -> viewBuilder.append(TAB).append(projection).append(COMMA).append(System.lineSeparator()));

        // removes the trailing comma
        int index = viewBuilder.lastIndexOf(COMMA);
        viewBuilder.deleteCharAt(index);

        viewBuilder.append("FROM ");

        if (input.isCompleteMsgEncrypted())
            viewBuilder.append(System.lineSeparator()).append(getBaseSelectQuery(input));
        else
            viewBuilder.append(db).append(DOT).append(table);

        viewBuilder.append(SEMI_COLN).append(System.lineSeparator());

        // constructed view
        return viewBuilder.toString();
    }

    /**
     * decrypts the table data and prepares the simple select query
     * 
     * @param input
     * @return
     */
    private static String getBaseSelectQuery(Input input) {

        StringBuilder builder = new StringBuilder();
        builder.append("(SELECT ");

        builder.append(NEW_LINE + TAB);
        builder.append(SimpleProjection.AUDIT_TRAIL_COLS.stream().map(Column::getName)
                .collect(Collectors.joining(COMMA + NEW_LINE + TAB)));
        builder.append(COMMA + NEW_LINE);

        // using datalake_m1.decrypt(..) udf
        builder.append(TAB + DECRYPT_UDF + "('" + input.getSecureDataFormat() + "', "
                + (input.isSensitive() ? ENCRYPTED_JSON : JSON_STRING) + ")" + AS + ViewHelper.getBaseColumnName());

        builder.append(COMMA + NEW_LINE + TAB);
        builder.append(SimpleProjection.HEADER_COLS.stream().map(Column::getName)
                .collect(Collectors.joining(COMMA + NEW_LINE + TAB)));

        builder.append(NEW_LINE + " FROM ");
        builder.append(input.getDatabase()).append(DOT).append(input.getTable()).append(RPAREN).append(SPACE)
                .append(ViewHelper.getBaseTableName());

        return builder.toString();
    }

    /**
     * prepares the input and delegates the call to right function
     * 
     * @param jsonSwaggerSchema
     * @return
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    private LinkedHashMap<String, String> getJPathsWithAlias(String jsonSwaggerSchema)
            throws JsonMappingException, JsonProcessingException {
        // stack initialization
        JsonNode node = new ObjectMapper().readTree(jsonSwaggerSchema);
        Stack<JsonKey> stack = new Stack<>();
        stack.push(new JsonKey(ROOT_NODE, JsonNodeType.STRING));

        // expand json except array nodes and for every json-path, compute alias
        LinkedHashMap<String, String> jPathsAliasMap = new LinkedHashMap<>();
        createJsonPathsWithAlias(node, stack, jPathsAliasMap);
        return jPathsAliasMap;
    }

    /**
     * Replace placeholder paths with real json-path
     * 
     * this is applicable for those feeds which have "placeholder properties" set to
     * Yes in input-metadata csv file
     * 
     * @param jPathsAliasMap
     * @param input
     * @param privacyInfo
     */
    @SuppressWarnings("unchecked")
    private void populateWithRealJsonPath(LinkedHashMap<String, String> jPathsAliasMap, Input input,
            Supplier<Stream<PrivacyInfo>> privacySupplier) {

        // cloning is done to avoid ConcurrentModificationException
        Iterator<String> iterator = ((LinkedHashMap<String, String>) jPathsAliasMap.clone()).keySet().iterator();

        privacySupplier.get().filter(x -> !ViewHelper.isNullOrNotFound(x.getFieldJsonPath()))
                .map(PrivacyInfo::getFieldJsonPath).forEachOrdered(x -> {
                    jPathsAliasMap.remove(iterator.next());
                    try {
                        jPathsAliasMap.put(x, AliasDefinition.getAlias(x));
                    } catch (ViewException ve) {
                        throw new RuntimeException(ve.getMessage());
                    }
                });
    }

    /**
     * Adds error-response msges in View
     * 
     * @param jPathsAliasMap
     * @param input
     * @param privacyInfo
     */
    private void addErrorResponseJsonPath(LinkedHashMap<String, String> jPathsAliasMap, Input input,
            Supplier<Stream<PrivacyInfo>> privacySupplier) {

        LinkedHashMap<String, String> errorJPathsAliasMap = new LinkedHashMap<>();

        privacySupplier.get().filter(x -> !ViewHelper.isNullOrNotFound(x.getErrorRootObject()))
                .map(PrivacyInfo::getErrorRootObject).forEachOrdered(x -> {
                    try {
                        input.setErrorRootObject(x);

                        IParser parser = this.getParser(input, true);
                        String json = parser.parse(input);

                        /**
                         * when json msg is not an array but the error json response is an array,
                         * discard such error msges from view
                         */
                        errorJPathsAliasMap.putAll(getJPathsWithAlias(json).entrySet().stream()
                                .filter(y -> !(y.getKey().equals(ROOT_NODE) || y.getValue().equals(ROOT_NODE)))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

                        input.setErrorRootObject(null);
                    } catch (Exception ex) {
                        log.error(ex.getMessage());
                        throw new RuntimeException(ex);
                    }
                });

        // use base-schema json-path when error json-path collides
        errorJPathsAliasMap.entrySet().stream().filter(x -> !jPathsAliasMap.containsKey(x.getKey())).forEach(x -> {
            int index = x.getValue().indexOf(UNDER_SCORE);
            if (index == -1)
                x.setValue(ERROR + UNDER_SCORE + x.getValue());
            else
                x.setValue(
                        ERROR + UNDER_SCORE + (input.isArrayMsg() ? x.getValue().substring(index + 1) : x.getValue()));
        });
        jPathsAliasMap.putAll(errorJPathsAliasMap);
    }

    /**
     * Checks for a corner-case wherein the json-path is just a root_node i.e. "$"
     * 
     * this is a special case where input msg is an array, so View HQL should not be
     * generated
     * 
     * @param jPathsAliasMap
     * @return
     */
    private boolean validateGeneratedJPaths(LinkedHashMap<String, String> jPathsAliasMap) {
        return jPathsAliasMap.keySet().stream().anyMatch(ROOT_NODE::equals);
    }

    /**
     * builds simple json path expressions along with function signature for all
     * nested nodes
     * 
     * expand json for every node except array nodes, and compute alias
     * 
     * <p>
     * for ex:
     * <li>get_json_object(encrypted_json,'$.quote.id') as id
     * <p>
     * backtracking approach
     * 
     * @param node
     * @param stack
     * @param result
     */
    public void createJsonPathsWithAlias(JsonNode node, Stack<JsonKey> stack, LinkedHashMap<String, String> result) {

        // base case
        if (!node.isObject()) {
            String path = stack.stream().map(elem -> elem.getKey()).collect(Collectors.joining(DOT));
            result.put(path, AliasDefinition.getAlias(stack));
            log.debug("json-paths projections: {}", result);
            return;
        }
        Iterator<Entry<String, JsonNode>> iterator = node.fields();
        // loop when current node is not a leaf node
        while (iterator.hasNext()) {
            Entry<String, JsonNode> item = iterator.next();
            String key = item.getKey();
            JsonNode currentNode = item.getValue();
            stack.push(new JsonKey(key, currentNode.getNodeType()));
            createJsonPathsWithAlias(currentNode, stack, result);
            stack.pop();
        }
    }
}
