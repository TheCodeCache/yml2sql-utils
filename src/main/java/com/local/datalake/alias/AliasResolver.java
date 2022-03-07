package com.local.datalake.alias;

import static com.local.datalake.common.Constants.BACK_QUOTE;
import static com.local.datalake.common.Constants.UNDER_SCORE;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.local.datalake.dto.Input;
import com.local.datalake.exception.AliasCollisionException;
import com.local.datalake.query.SimpleProjection;

/**
 * alias resolver
 */
public class AliasResolver {

    // used as reference
    private List<String>                  referenceAlias;
    // alias generated from yaml schema file
    private LinkedHashMap<String, String> expandedAlias;
    // input metadata
    private Input                         input;
    // holds details about ambiguous alias or non-unique alias
    private Map<String, String>           ambiguousAlias;

    public AliasResolver(Input input, LinkedHashMap<String, String> expandedAlias) {

        this.input = input;
        this.expandedAlias = expandedAlias;
        // reference alias are those which are derived from simple projections
        this.referenceAlias = Stream
                .of(SimpleProjection.AUDIT_TRAIL_COLS.stream(), SimpleProjection.HEADER_COLS.stream(),
                        SimpleProjection.getOtherColumns(this.input).stream())
                .flatMap(Function.identity()).map(column -> column.getAlias()).sorted().collect(Collectors.toList());
    }

    /**
     * applies a set of rules to resolve the conflicts between aliases
     * 
     * @throws AliasCollisionException
     */
    public void tryResolve() throws AliasCollisionException {

        expandedAlias.entrySet().stream().forEach(entry -> {
            if (Collections.binarySearch(referenceAlias, entry.getValue()) > 0)
                entry.setValue(input.getRootObject() + UNDER_SCORE + entry.getValue());
            if (KeyWords.have(entry.getValue()))
                entry.setValue(BACK_QUOTE + entry.getValue() + BACK_QUOTE);
        });

        verify();
    }

    /**
     * checks for any ambiguous alias
     * 
     * @throws AliasCollisionException
     */
    public void verify() throws AliasCollisionException {

        // finds the duplicate alias
        List<String> duplicates = expandedAlias.values().stream().collect(Collectors.groupingBy(Function.identity()))
                .entrySet().stream().filter(e -> e.getValue().size() > 1).map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // retrieves the key value for all the duplicates
        Map<String, String> conflictedAlias = expandedAlias.entrySet().stream()
                .filter(entry -> duplicates.contains(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // store them for force resolution
        this.ambiguousAlias = conflictedAlias;

        if (!duplicates.isEmpty())
            throw new AliasCollisionException(
                    "Alias conflicts reported for [" + input.getFeedName() + "] and root-object ["
                            + input.getRootObject() + "]\nAlias Details: [" + conflictedAlias.toString() + "]");
    }

    /**
     * Resolves all the ambiguous aliases through indexing
     */
    public void forceResolve() {
        AtomicInteger indexer = new AtomicInteger(0);
        ambiguousAlias.entrySet().stream().forEach(entry -> {
            expandedAlias.entrySet().stream().forEach(pair -> {
                if (pair.getValue().equals(entry.getValue())) {
                    pair.setValue(pair.getValue() + UNDER_SCORE + indexer.incrementAndGet());
                }
            });
            indexer.set(0);
        });
    }
}
