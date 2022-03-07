package com.local.datalake.parser;

import com.local.datalake.dto.Input;

/**
 * parser decorator
 * 
 * @author manoranjan
 */
public class IParserDecorator implements IParser {

    private IParser parser;

    public IParserDecorator(IParser parser) {
        this.parser = parser;
    }

    /**
     * delegates call to the actual instance
     */
    @Override
    public String parse(Input input) throws Exception {
        return parser.parse(input);
    }
}
