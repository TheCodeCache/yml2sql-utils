package com.local.datalake.alias;

import static com.local.datalake.common.Constants.ARRAY_SUFFIX;
import static com.local.datalake.common.Constants.EMPTY;
import static com.local.datalake.common.Constants.ROOT_NODE;
import static com.local.datalake.common.Constants.UNDER_SCORE;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.jayway.jsonpath.JsonPath;
import com.local.datalake.dto.JsonKey;
import com.local.datalake.exception.AliasRuleNotFoundException;
import com.local.datalake.exception.ViewException;

/**
 * Generates Alias for a given json-path using below set of Rules:
 * <li>if json node key is in camel case, pull first char and all the subsequent
 * upper case letters
 * <li>if json node key all in lower case, then use them as it is
 * <li>if json node key all in upper case, then lower it and use.
 * 
 * @author manoranjan
 */
public class AliasDefinition {

    private static final Logger      log   = LoggerFactory.getLogger(AliasDefinition.class);

    final static Map<String, String> cache = new HashMap<>();

    /**
     * static cache, in case of we need to use acronym
     */
    static {
        cache.put("amount", "amt");
        cache.put("currency", "curr");
        // cache.put("insured", "ins");
    }

    public static String getAlias(Stack<JsonKey> stack) {

        JsonKey jsonKey = stack.pop();

        final String top = jsonKey.getKey();
        final String endName = jsonKey.getType() == JsonNodeType.ARRAY ? top + ARRAY_SUFFIX : top;

        String alias = stack.size() > 1
                ? stack.stream().filter(elem -> !elem.getKey().equals(ROOT_NODE)).map(jsonPath -> {

                    final String path = jsonPath.getKey();
                    final JsonNodeType type = jsonPath.getType();

                    String aliaz = cache.get(path);
                    if (aliaz != null && !aliaz.isEmpty())
                        return aliaz;

                    Rule[] rules = Rule.values();
                    // in-place as well as stable sorting
                    Arrays.sort(rules, Comparator.comparing(Rule::getOrder));
                    String temp;
                    for (Rule rule : rules) {
                        switch (rule) {
                            case CAMEL_CASE:
                                String als = getSequence(path);
                                if (als != null && !als.isEmpty()) {
                                    temp = als;
                                    break;
                                }
                            case ALL_LOWER_CASE:
                                if (StringUtils.isAllLowerCase(path)) {
                                    temp = path;
                                    break;
                                }
                            case ALL_UPPER_CASE:
                                if (StringUtils.isAllUpperCase(path)) {
                                    temp = path;
                                    break;
                                }
                            default:
                                throw new AliasRuleNotFoundException("Failed to create alias for this "
                                        + (type == JsonNodeType.ARRAY ? "array " : "normal ") + " path: [" + path
                                        + "]");
                        }
                        cache.put(path, type == JsonNodeType.ARRAY ? temp + ARRAY_SUFFIX : temp);
                        return cache.get(path);
                    }
                    return null;
                }).map(String::trim).map(String::toLowerCase).collect(Collectors.joining(UNDER_SCORE))
                : endName;

        stack.push(jsonKey);
        return stack.size() > 2 ? alias + UNDER_SCORE + endName : alias;
    }

    /**
     * Computes alias from json-path
     * 
     * This supports dotted-notation of jayway json-path
     * 
     * @param jsonPath
     * @return
     * @throws ViewException
     */
    public static String getAlias(String jsonPath) throws ViewException {
        try {
            // ensure for the json-path syntax
            JsonPath.compile(jsonPath);

            jsonPath = jsonPath.replaceAll(Pattern.quote("$."), EMPTY).replaceAll(Pattern.quote(".[*]"), "[*]");
            String[] nodes = jsonPath.split("\\.", -1);

            Stack<JsonKey> stack = new Stack<>();
            stack.push(new JsonKey(ROOT_NODE, JsonNodeType.STRING));

            Pattern p = Pattern.compile(".*\\[ *(.*) *\\].*");

            for (int idx = 0; idx < nodes.length; idx++)
                if (!nodes[idx].isEmpty()) {
                    Matcher m = p.matcher(nodes[idx]);
                    String text = m.find() ? nodes[idx].replace("[" + m.group(1) + "]", EMPTY) : nodes[idx];

                    stack.push(new JsonKey(text, idx < nodes.length - 1 ? JsonNodeType.OBJECT
                            : m.find() ? JsonNodeType.ARRAY : JsonNodeType.OBJECT));
                }

            return getAlias(stack);
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
            throw new ViewException("Computing alias got failed for this path: " + jsonPath);
        }
    }

    /**
     * constructs alias in a camel case words
     * <li>a word should not begin with _ char
     * 
     * @param input
     * @return
     */
    private static String getSequence(String input) {

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        boolean isUpperCharFound = false;
        boolean isUnderscoreFound = false;
        boolean captureNext = false;
        char under_score = UNDER_SCORE.charAt(0);

        for (char ch : input.toCharArray()) {
            if (first || captureNext) {
                first = false;
                if (ch == under_score) {
                    isUnderscoreFound = true;
                    captureNext = true;
                    continue;
                }
                builder.append(ch);
                captureNext = false;
            } else if (Character.isUpperCase(ch)) {
                isUpperCharFound = true;
                builder.append(ch);
            } else if (ch == under_score) {
                captureNext = true;
                isUnderscoreFound = true;
            } else if (Character.isDigit(ch)) {
                builder.append(ch);
            }
        }
        return (isUpperCharFound || isUnderscoreFound) ? builder.toString() : null;
    }
}
