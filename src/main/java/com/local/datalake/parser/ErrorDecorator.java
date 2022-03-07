package com.local.datalake.parser;

import com.local.datalake.dto.Input;

/**
 * Decorator for supporting error scenarios for response feeds
 * 
 * @author manoranjan
 */
public class ErrorDecorator extends IParserDecorator {

    public ErrorDecorator(IParser parser) {
        super(parser);
    }

    /**
     * it uses error root object to scan for schema from swagger yml file,
     */
    @Override
    public String parse(Input input) throws Exception {
        String rootObj = input.getRootObject();
        input.setRootObject(input.getErrorRootObject());

        String json = super.parse(input);
        input.setRootObject(rootObj);

        return json;
    }
}
