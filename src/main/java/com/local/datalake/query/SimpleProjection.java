package com.local.datalake.query;

import static com.local.datalake.common.Constants.COMMON_HEADERS;
import static com.local.datalake.common.Constants.HEADER_FIELDS_PATH;
import static com.local.datalake.common.Constants.QUOTE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.local.datalake.common.Configuration;
import com.local.datalake.common.ViewHelper;
import com.local.datalake.dto.Column;
import com.local.datalake.dto.Input;

/**
 * Maintains the static columns
 */
public class SimpleProjection {

    private static final Logger log   = LoggerFactory.getLogger(SimpleProjection.class);
    private static Properties   props = Configuration.loadProperties();

    /**
     * columns with constant values
     * 
     * @param input
     * @return
     */
    public static List<Column> getOtherColumns(Input input) {

        List<Column> OTHER_COLS = new ArrayList<>();
        OTHER_COLS.add(new Column(QUOTE + input.getMessageType() + QUOTE, "message_type"));
        OTHER_COLS.add(new Column(QUOTE + input.getTable() + QUOTE, "source_table"));
        return OTHER_COLS;
    }

    public static List<Column> AUDIT_TRAIL_COLS = new ArrayList<>();

    // Audit-Trail Fields
    static {
        AUDIT_TRAIL_COLS.add(new Column("feed_id"));
        AUDIT_TRAIL_COLS.add(new Column("feed_version"));
        AUDIT_TRAIL_COLS.add(new Column("ingestion_id"));
        AUDIT_TRAIL_COLS.add(new Column("ingestion_timestamp"));
    }

    public static List<Column> HEADER_COLS = new ArrayList<>();

    // Request-Header Fields
    static {

        Set<Column> temp = new LinkedHashSet<>();

        // user supplied extra headrs
        String headerFieldsPath = props.getProperty(HEADER_FIELDS_PATH);
        // COMMON_HEADERS denotes headers which are common to all usecases
        String[] headerFiles = { COMMON_HEADERS, ViewHelper.isNull(headerFieldsPath) ? null : headerFieldsPath.trim() };

        try {
            for (String headerFile : headerFiles)
                if (headerFile != null)
                    Files.lines(Paths.get(headerFile)).forEach(line -> {
                        String[] tokens = line.split("\t");
                        String header = tokens[0].trim();
                        String alias = tokens[1].trim();
                        temp.add(new Column(header, alias));
                    });
            // prepares the list of columns
            temp.stream().forEach(HEADER_COLS::add);
        } catch (IOException io) {
            log.error("{}", io.getMessage(), io);
        }
    }
}
