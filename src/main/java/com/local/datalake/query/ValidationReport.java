package com.local.datalake.query;

import static com.local.datalake.common.Constants.REPORT_FILE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.local.datalake.common.ViewHelper;

/**
 * A singleton
 * 
 * - captures the view generation report details for multiple feeds
 * 
 * @author manoranjan
 */
public class ValidationReport {

    private static final Logger              log     = LoggerFactory.getLogger(ValidationReport.class);

    private transient List<ValidationReport> report  = new ArrayList<ValidationReport>();
    private transient StringBuilder          builder = new StringBuilder();

    private String                           feedId;
    private String                           feedName;
    private String                           component;
    private String                           result;
    private String                           error;
    private String                           trace;

    private ValidationReport() {
    }

    private static class SingletonHelper {
        private static ValidationReport INSTANCE = new ValidationReport();
    }

    /**
     * Singleton
     */
    public static ValidationReport getInstance() {
        return SingletonHelper.INSTANCE;
    }

    /**
     * multi-arg constructor. not exposed to outside
     * 
     * @param feedId
     * @param feedName
     * @param viewName
     * @param result
     * @param error
     * @param trace
     */
    private ValidationReport(String feedId, String feedName, String component, String result, String error,
            Exception trace) {
        this.feedId = feedId;
        this.feedName = feedName;
        this.component = component;
        this.result = result;
        this.error = error;

        builder.setLength(0);
        if (trace != null) {
            for (StackTraceElement traceElement : trace.getStackTrace())
                builder.append("\tat " + traceElement);
            this.trace = builder.toString();
        }
    }

    /**
     * captures the failure case along with precise exception details
     * 
     * @param feedId
     * @param feedName
     * @param viewName
     * @param result
     * @param error
     * @param trace
     */
    public void capture(String feedId, String feedName, String viewName, String result, String error, Exception trace) {
        report.add(new ValidationReport(feedId, feedName, viewName, result, error, trace));
    }

    /**
     * captures the success case
     * 
     * @param feedId
     * @param feedName
     * @param viewName
     * @param result
     */
    public void capture(String feedId, String feedName, String viewName, String result) {
        report.add(new ValidationReport(feedId, feedName, viewName, result, null, null));
    }

    /**
     * saves view-report in a default directory
     * 
     * @throws IOException
     */
    public void save() throws IOException {
        ViewHelper.save(generate(), REPORT_FILE);
    }

    /**
     * saves view-report in a custom location
     * 
     * @param path
     * @throws IOException
     */
    public void save(String path) throws IOException {
        ViewHelper.save(generate(), path);
    }

    /**
     * generate the view in pretty json format
     * 
     * @return
     */
    public String generate() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(report);
    }

    /**
     * for logging/debugging
     */
    public void show() {
        log.info(generate());
    }

    /**
     * toString impl
     */
    @Override
    public String toString() {
        return "ValidationReport [report=" + report + ", feedId=" + feedId + ", feedName=" + feedName + ", component="
                + component + ", result=" + result + ", error=" + error + ", trace=" + trace + "]";
    }
}
