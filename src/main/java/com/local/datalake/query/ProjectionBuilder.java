package com.local.datalake.query;

import static com.local.datalake.common.Constants.AS;
import static com.local.datalake.common.Constants.COMMA;
import static com.local.datalake.common.Constants.DOT;
import static com.local.datalake.common.Constants.ENCRYPT_UDF;
import static com.local.datalake.common.Constants.GET_JSON_OBJECT;
import static com.local.datalake.common.Constants.LPAREN;
import static com.local.datalake.common.Constants.QUOTE;
import static com.local.datalake.common.Constants.RPAREN;
import static com.local.datalake.common.Constants.SPACE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.local.datalake.dto.Column;
import com.local.datalake.dto.Input;

/**
 * Projection Builder
 * 
 * <li>includes audit trail fields at first position in the View always
 * <li>includes expanded column/schema details from yaml file
 * <li>includes header fields
 * <li>includes couple of constant columns
 * 
 * @author manoranjan
 */
public class ProjectionBuilder {

    private StringBuilder       exprBuilder = new StringBuilder();
    private List<Column>        projections = new ArrayList<>();
    // metadata
    private Input               input;
    // map between individual expanded json-path and its computed alias
    private Map<String, String> jsonPathAlias;

    public ProjectionBuilder(Input input, Map<String, String> jsonPathAlias) {
        this.input = input;
        this.jsonPathAlias = jsonPathAlias;
    }

    /**
     * wraps get_json_object around encrypted_json field along with json-path
     * 
     * @param column
     * @param jsonPath
     * @return
     */
    public ProjectionBuilder wrapGetJsonObjectHiveUDF(String column, String jsonPath) {

        exprBuilder.setLength(0);
        String expr = exprBuilder.append(GET_JSON_OBJECT).append(LPAREN).append(column).append(COMMA).append(SPACE)
                .append(QUOTE).append(jsonPath).append(QUOTE).append(RPAREN).toString();

        // setting alias to null here as it will be computed later while building it
        Column col = new Column(expr, null);
        col.setJsonPath(jsonPath);

        projections.add(col);

        return this;
    }

    /**
     * wraps encrypt hive-udf call to the decrypted json msg
     * 
     * @param jsonPath
     * @param table
     * @param column
     * @return
     */
    public ProjectionBuilder wrapWithCustomEncryptUDF(String jsonPath, String table, String column) {

        int index = IntStream.range(0, projections.size()).map(i -> projections.size() - 1 - i)
                .filter(i -> jsonPath.equals(projections.get(i).getJsonPath())).findFirst().orElse(-1);

        Column col = projections.get(index);

        String expr = col.getName();

        Pattern p = Pattern.compile("\\((.*?)\\,");
        Matcher m = p.matcher(expr);
        expr = m.find() ? expr.replace("(" + m.group(1) + ",", "(" + table + DOT + column + ",") : expr;

        exprBuilder.setLength(0);
        String newexpr = exprBuilder.append(ENCRYPT_UDF).append(LPAREN).append(QUOTE)
                .append(input.getSecureDataFormat()).append(QUOTE).append(COMMA).append(SPACE).append(expr)
                .append(RPAREN).toString();
        Column newcol = new Column(newexpr, null);
        newcol.setJsonPath(jsonPath);

        projections.set(index, newcol);

        return this;
    }

    /**
     * audit trail fields
     * 
     * @return
     */
    public ProjectionBuilder addAuditTrails() {

        projections.addAll(0, SimpleProjection.AUDIT_TRAIL_COLS);

        return this;
    }

    /**
     * request header fields
     * 
     * @return
     */
    public ProjectionBuilder addHeaderFields() {

        projections.addAll(SimpleProjection.HEADER_COLS);

        return this;
    }

    /**
     * constant columns
     * 
     * @return
     */
    public ProjectionBuilder addConstantCols() {

        projections.addAll(SimpleProjection.getOtherColumns(input));

        return this;
    }

    /**
     * builds the projection list by appending aliases to individual column
     * projection
     * 
     * @return
     */
    public List<String> build() {
        return projections.stream().map(column -> {
            if (column.getJsonPath() != null)
                column.setAlias(jsonPathAlias.get(column.getJsonPath()));
            return column;
        }).map(col -> col.getName() + AS + col.getAlias()).collect(Collectors.toList());
    }
}
