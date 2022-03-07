package com.local.datalake.parser;

import com.local.datalake.dto.Input;

/**
 * Null Parser
 * 
 * @author manoranjan
 */
public class NullParser implements IParser {

    @Override
    public String parse(Input input) throws Exception {
        return "{}";
    }
}
