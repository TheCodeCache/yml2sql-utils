package com.local.datalake.parser;

import static com.local.datalake.common.Constants.SWAGGER_2;
import static com.local.datalake.common.Constants.OPENAPI_3;

import com.local.datalake.common.FileExtn;
import com.local.datalake.common.ViewHelper;
import com.local.datalake.dto.ParserType;
import com.local.datalake.exception.UnsupportedFileFormatException;

/**
 * factory for parser instances
 * 
 * @author manoranjan
 */
public class ParserFactory {

    /**
     * based on the given parser specification, retrieves the parser object
     * 
     * @param type
     * @return
     */
    public static IParser getParser(ParserType type) {
        IParser parser = null;

        // if root object points to error cases, use ErrorDecorator
        if (type.isError()) {
            if (type.getYamlVersion().equalsIgnoreCase(SWAGGER_2))
                parser = new ErrorDecorator(new YamlV2Parser());

            else if (type.getYamlVersion().equalsIgnoreCase(OPENAPI_3))
                parser = new ErrorDecorator(new YamlV3Parser());
        }
        // if root object is not valid, use NullParser
        else if (ViewHelper.isNullOrNotFound(type.getRootObject())) {
            parser = new NullParser();
        }
        // if swagger file type is rtf, use RtfDecorator
        else if (FileExtn.RTF.toString().equalsIgnoreCase(type.getSpecFileFormat())) {

            if (type.getYamlVersion().equalsIgnoreCase(SWAGGER_2))
                parser = new RtfDecorator(new YamlV2Parser());

            else if (type.getYamlVersion().equalsIgnoreCase(OPENAPI_3))
                parser = new RtfDecorator(new YamlV3Parser());

        }
        // if swagger file type is yml, use Yml Parser
        else if (FileExtn.YAML.toString().equalsIgnoreCase(type.getSpecFileFormat())
                || FileExtn.YML.toString().equalsIgnoreCase(type.getSpecFileFormat())) {

            if (type.getYamlVersion().equalsIgnoreCase(SWAGGER_2))
                parser = new YamlV2Parser();

            else if (type.getYamlVersion().equalsIgnoreCase(OPENAPI_3))
                parser = new YamlV3Parser();
        } else
            new UnsupportedFileFormatException(" File-Format " + type + " Not Supported");

        return parser;
    }
}
