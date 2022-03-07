package com.local.datalake.parser;

import static com.local.datalake.common.Constants.COMMA;
import static com.local.datalake.common.Constants.DOUBLE_QUOTE;
import static com.local.datalake.common.Constants.ESC;
import static com.local.datalake.common.Constants.NO;
import static com.local.datalake.common.Constants.REQUEST;
import static com.local.datalake.common.Constants.RESPONSE;
import static com.local.datalake.common.Constants.VIEW_SUFFIX;
import static com.local.datalake.common.Constants.YES;
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

import com.local.datalake.dto.Input;
import com.local.datalake.exception.InvalidMetadataException;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

/**
 * Csv Metadata Reader
 */
public class MetadataReader {

    private static final Logger              log         = LoggerFactory.getLogger(MetadataReader.class);
    private static char                      SEPARATOR   = COMMA.charAt(0);
    private static char                      ESCAPE_CHAR = ESC.charAt(0);
    private static char                      QUOTE_CHAR  = DOUBLE_QUOTE.charAt(0);

    /**
     * input metadata csv header and its column position in the file
     */
    public static final Map<String, Integer> header      = Collections.unmodifiableMap(new HashMap<String, Integer>() {
                                                             {
                                                                 put("feed_identifier", 0);
                                                                 put("feed_name", 1);
                                                                 put("root_object", 2);
                                                                 put("yaml_path", 3);
                                                                 put("yaml_version", 4);
                                                                 put("spec_file_format", 5);
                                                                 put("full_msg_encryption", 6);
                                                                 put("database", 7);
                                                                 put("table", 8);
                                                                 put("secure_data_format", 9);
                                                                 put("has_sensitive_fields", 10);
                                                                 put("output_view_path", 11);
                                                                 put("is_json_array_msg", 12);
                                                                 put("msg_tag", 13);
                                                                 put("placeholder_vars", 14);
                                                             }
                                                             private static final long serialVersionUID = -6219297068852490205L;
                                                         });

    public List<Input> read(String path) throws CsvValidationException, InvalidMetadataException, IOException {
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
    public List<Input> read(File file) throws CsvValidationException, InvalidMetadataException, IOException {

        final int METADATA_FIELDS_COUNT = header.size();

        final CSVParser parser = new CSVParserBuilder().withSeparator(SEPARATOR).withEscapeChar(ESCAPE_CHAR)
                .withQuoteChar(QUOTE_CHAR).withIgnoreQuotations(true).build();
        try (final CSVReader csvReader = new CSVReaderBuilder(new FileReader(file)).withCSVParser(parser).build();) {

            // reads header
            String[] headers = csvReader.readNext();
            if (headers == null || !(headers.length == METADATA_FIELDS_COUNT))
                throw new InvalidMetadataException("Input Metadata Csv file header must have " + METADATA_FIELDS_COUNT
                        + " columns in total, whereas it has only " + (METADATA_FIELDS_COUNT - headers.length)
                        + " columns");

            String[] tokens = null;
            List<Input> metadata = new ArrayList<>();

            int line = 1;
            while ((tokens = csvReader.readNext()) != null) {
                line++;
                if (!(tokens.length == METADATA_FIELDS_COUNT))
                    throw new InvalidMetadataException("Input Metadata Csv Record (at line number: " + line
                            + ") must have " + METADATA_FIELDS_COUNT + " fields in total, whereas it has only "
                            + (METADATA_FIELDS_COUNT - tokens.length) + " fields");
                Input input = new Input();
                input.setIdentifier(turnNullOrWhiteSpaceToEmpty(tokens[header.get("feed_identifier")]));
                input.setFeedName(turnNullOrWhiteSpaceToEmpty(tokens[header.get("feed_name")]));
                input.setRootObject(turnNullOrWhiteSpaceToEmpty(tokens[header.get("root_object")]));
                input.setYamlFilePath(turnNullOrWhiteSpaceToEmpty(tokens[header.get("yaml_path")]));
                input.setYamlVersion(turnNullOrWhiteSpaceToEmpty(tokens[header.get("yaml_version")]));
                input.setSpecFileFormat(turnNullOrWhiteSpaceToEmpty(tokens[header.get("spec_file_format")]));
                assertFlagValue(tokens[header.get("full_msg_encryption")]);
                input.setCompleteMsgEncrypted(
                        YES.equalsIgnoreCase(turnNullOrWhiteSpaceToEmpty(tokens[header.get("full_msg_encryption")]))
                                ? true
                                : false);
                input.setDatabase(turnNullOrWhiteSpaceToEmpty(tokens[header.get("database")]));
                input.setTable(turnNullOrWhiteSpaceToEmpty(tokens[header.get("table")]));
                input.setSecureDataFormat(turnNullOrWhiteSpaceToEmpty(tokens[header.get("secure_data_format")]));
                assertFlagValue(tokens[header.get("has_sensitive_fields")]);
                input.setSensitive(
                        YES.equalsIgnoreCase(turnNullOrWhiteSpaceToEmpty(tokens[header.get("has_sensitive_fields")]))
                                ? true
                                : false);
                input.setOutputViewPath(turnNullOrWhiteSpaceToEmpty(tokens[header.get("output_view_path")]));
                assertFlagValue(tokens[header.get("is_json_array_msg")]);
                input.setArrayMsg(
                        YES.equalsIgnoreCase(turnNullOrWhiteSpaceToEmpty(tokens[header.get("is_json_array_msg")]))
                                ? true
                                : false);
                input.setArrayMsgTag(turnNullOrWhiteSpaceToEmpty(tokens[header.get("msg_tag")]));
                assertFlagValue(tokens[header.get("placeholder_vars")]);
                input.setPlaceholder(
                        YES.equalsIgnoreCase(turnNullOrWhiteSpaceToEmpty(tokens[header.get("placeholder_vars")])) ? true
                                : false);
                input.setMessageType(
                        input.getFeedName().toLowerCase().contains(REQUEST.toLowerCase()) ? REQUEST : RESPONSE);
                input.setView(input.getTable() + VIEW_SUFFIX);
                input.setComponent(input.getView());

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
