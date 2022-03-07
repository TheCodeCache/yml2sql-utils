package com.local.datalake.metareader;

import static com.local.datalake.common.Constants.COMMA;
import static com.local.datalake.common.Constants.DOUBLE_QUOTE;
import static com.local.datalake.common.Constants.ESC;
import static com.local.datalake.common.Constants.NO;
import static com.local.datalake.common.Constants.REQUEST;
import static com.local.datalake.common.Constants.RESPONSE;
import static com.local.datalake.common.Constants.YES;
import static com.local.datalake.common.Constants.HEADER_FEED_IDENTIFIER;
import static com.local.datalake.common.Constants.HEADER_FEED_NAME;
import static com.local.datalake.common.Constants.HEADER_ROOT_OBJECT;
import static com.local.datalake.common.Constants.HEADER_YAML_PATH;
import static com.local.datalake.common.Constants.HEADER_PRIVACY_PATH;
import static com.local.datalake.common.Constants.HEADER_OUTPUT_DERIVED_PATH;
import static com.local.datalake.common.Constants.HEADER_YAML_VERSION;
import static com.local.datalake.common.Constants.HEADER_SPEC_FILE_FORMAT;
import static com.local.datalake.common.Constants.HEADER_IS_JSON_ARRAY_MSG;
import static com.local.datalake.common.Constants.HEADER_MSG_TAG;
import static com.local.datalake.common.Constants.HEADER_PLACEHOLDER_VARS;

import static com.local.datalake.common.ViewHelper.turnNullOrWhiteSpaceToEmpty;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.local.datalake.dto.DerivedInput;
import com.local.datalake.dto.Input;
import com.local.datalake.exception.InvalidMetadataException;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import io.swagger.io.HeaderAuthentication;

/**
 * Csv Metadata Reader
 */
public class DerivedMetaReader {

    private static final Logger              log         = LoggerFactory.getLogger(DerivedMetaReader.class);
    private static char                      SEPARATOR   = COMMA.charAt(0);
    private static char                      ESCAPE_CHAR = ESC.charAt(0);
    private static char                      QUOTE_CHAR  = DOUBLE_QUOTE.charAt(0);

    /**
     * input metadata csv header and its column position in the file
     */
    public static final Map<String, Integer> header      = Collections.unmodifiableMap(new HashMap<String, Integer>() {
                                                             {
                                                                 put(HEADER_FEED_IDENTIFIER, 0);
                                                                 put(HEADER_FEED_NAME, 1);
                                                                 put(HEADER_ROOT_OBJECT, 2);
                                                                 put(HEADER_YAML_PATH, 3);
                                                                 put(HEADER_PRIVACY_PATH, 4);
                                                                 put(HEADER_OUTPUT_DERIVED_PATH, 5);
                                                                 put(HEADER_YAML_VERSION, 6);
                                                                 put(HEADER_SPEC_FILE_FORMAT, 7);
                                                                 put(HEADER_IS_JSON_ARRAY_MSG, 8);
                                                                 put(HEADER_MSG_TAG, 9);
                                                                 put(HEADER_PLACEHOLDER_VARS, 10);
                                                             }
                                                             private static final long serialVersionUID = -6219297068852490205L;
                                                         });

    public List<DerivedInput> read(String path) throws CsvValidationException, InvalidMetadataException, IOException {
        return read(new File(path));
    }

    /**
     * reads csv file with default set of configurations
     * 
     * validates structure of the file
     * 
     * @param file
     * @return
     * @throws CsvValidationException
     * @throws IOException
     * @throws InvalidMetadataException
     */
    public List<DerivedInput> read(File file) throws CsvValidationException, InvalidMetadataException, IOException {

        final int METADATA_FIELDS_COUNT = header.size();
        
        log.info("total no headers {}",METADATA_FIELDS_COUNT);

        final CSVParser parser = new CSVParserBuilder().withSeparator(SEPARATOR).withEscapeChar(ESCAPE_CHAR)
                .withQuoteChar(QUOTE_CHAR).withIgnoreQuotations(true).build();
        try (final CSVReader csvReader = new CSVReaderBuilder(new FileReader(file)).withCSVParser(parser).build();) {

            // reads header
            String[] headers = csvReader.readNext();
            log.info("total length {}",headers.length);
            
            if (headers == null || !(headers.length == METADATA_FIELDS_COUNT))
                throw new InvalidMetadataException("Input Metadata Csv file header must have " + METADATA_FIELDS_COUNT
                        + " columns in total, whereas it has only " + (METADATA_FIELDS_COUNT - headers.length)
                        + " columns");

            String[] tokens = null;
            List<DerivedInput> metadata = new ArrayList<>();

            int line = 1;
            while ((tokens = csvReader.readNext()) != null) {
                line++;
                if (!(tokens.length == METADATA_FIELDS_COUNT))
                    throw new InvalidMetadataException("Input Metadata Csv Record (at line number: " + line
                            + ") must have " + METADATA_FIELDS_COUNT + " fields in total, whereas it has only "
                            + (METADATA_FIELDS_COUNT - tokens.length) + " fields");
                DerivedInput input = new DerivedInput();
                input.setFeedIdentifier(turnNullOrWhiteSpaceToEmpty(tokens[header.get(HEADER_FEED_IDENTIFIER)]));
                input.setFeedName(turnNullOrWhiteSpaceToEmpty(tokens[header.get(HEADER_FEED_NAME)]));
                input.setRootObject(turnNullOrWhiteSpaceToEmpty(tokens[header.get(HEADER_ROOT_OBJECT)]));
                input.setYamlFilePath(turnNullOrWhiteSpaceToEmpty(tokens[header.get(HEADER_YAML_PATH)]));
                input.setPrivacyPath(turnNullOrWhiteSpaceToEmpty(tokens[header.get(HEADER_PRIVACY_PATH)]));
                input.setOutputDerivedPath(turnNullOrWhiteSpaceToEmpty(tokens[header.get(HEADER_OUTPUT_DERIVED_PATH)]));
                input.setYamlVersion(turnNullOrWhiteSpaceToEmpty(tokens[header.get(HEADER_YAML_VERSION)]));
                input.setSpecFileFormat(turnNullOrWhiteSpaceToEmpty(tokens[header.get(HEADER_SPEC_FILE_FORMAT)]));

                assertFlagValue(tokens[header.get(HEADER_IS_JSON_ARRAY_MSG)]);
                input.setArrayMsg(
                        YES.equalsIgnoreCase(turnNullOrWhiteSpaceToEmpty(tokens[header.get(HEADER_IS_JSON_ARRAY_MSG)]))
                                ? true
                                : false);
                input.setArrayMsgTag(turnNullOrWhiteSpaceToEmpty(tokens[header.get(HEADER_MSG_TAG)]));
                assertFlagValue(tokens[header.get(HEADER_PLACEHOLDER_VARS)]);
                input.setPlaceholder(
                        YES.equalsIgnoreCase(turnNullOrWhiteSpaceToEmpty(tokens[header.get(HEADER_PLACEHOLDER_VARS)])) ? true
                                : false);
                input.setMessageType(
                        input.getFeedName().toLowerCase().contains(REQUEST.toLowerCase()) ? REQUEST : RESPONSE);




                metadata.add(input);
            }
            return metadata;
        } catch (IOException ex) {
            log.error("{}", ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * checks for valid values for all the flag fields because it needs to be
     * converted into boolean values
     * 
     * @param value
     * @throws InvalidMetadataException
     */
    private void assertFlagValue(String value) throws InvalidMetadataException {
        if (!(value != null && value.trim().isEmpty() || value.equalsIgnoreCase(YES) || value.equalsIgnoreCase(NO)))
            throw new InvalidMetadataException("Invalid Metadata csv Record value detected: " + value);
    }
}
