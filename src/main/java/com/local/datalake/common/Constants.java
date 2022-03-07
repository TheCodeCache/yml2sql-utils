package com.local.datalake.common;

/**
 * Constants
 * 
 * @author manoranjan
 */
public class Constants {
    
    public static final String RESOURCE_PATH       		= "src/main/resources/";
    public static final String YML_FILE_PATH      	    = RESOURCE_PATH + "yml_tmp/";
    public static final String REPORT_PATH         		= RESOURCE_PATH + "report/";
    public static final String FEED_PATH           		= RESOURCE_PATH + "feed_path/";
    public static final String INVALID_PATH        		= RESOURCE_PATH + "invalid_path/";
    public static final String VALID_PATH          		= RESOURCE_PATH + "valid_path/";
    public static final String DERIVED_LOGIC   	   		= RESOURCE_PATH + "derived_logic/";
    public static final String JSONINPUT_PATH      		= RESOURCE_PATH + "json/";
    public static final String YMLJSON_PATH        		= RESOURCE_PATH + "ymljson/";
    public static final String REPORT_FILE         		= REPORT_PATH + "view_report.json";
    public static final String SCHEMA_FILE_PATH    		= RESOURCE_PATH + "schema_files_tmp/";
    public static final String JSON_SCHEMA_FILE_PATH    = RESOURCE_PATH + "json/";
    public static final String DEPENDENCY_PATH     		= RESOURCE_PATH + "dependency/";
    public static final String RTF_CONTENT_PATH    		= RESOURCE_PATH + "rtf_contents_tmp/";
    public static final String KEYWORDS_FILE       		= RESOURCE_PATH + "keywords.txt";
    public static final String COMMON_HEADERS      		= RESOURCE_PATH + "common_headers.txt";
    public static final String VIEW_SUFFIX         		= "_v";
    public static final String ARRAY_SUFFIX        		= "_arr";
    public static final String ENCRYPTED_JSON      		= "encrypted_json";
    public static final String JSON_STRING         		= "json_string";
    public static final String GET_JSON_OBJECT     		= "get_json_object";
    public static final String INPUT_METADATA_PATH 		   = "input_metadata_path";
    public static final String INPUT_DERIVED_METADATA_PATH = "input_derived_metadata_path";
    public static final String OUTPUT_METADATA_PATH 	   = "output_metadata_path";
    public static final String OUTPUT_VALID_SHEET_NAME     = " Valid Paths ";
    public static final String OUTPUT_INVALID_SHEET_NAME   = " Invalid Paths ";
    public static final String OUTPUT_DERIVED_LOGIC 	   = " Derived Logic ";
    public static final String INPUT_PRIVACY_PATH  		   = "input_privacy_path";
    public static final String OUTPUT_EXCEL_PATH 		   = "output_excel_path";
    public static final String EXCEL_HEADER 			   = "excel_header";
    public static final String HEADER_FIELDS_PATH   	   = "header_fields_path";
    public static final String HEADER_FEED_IDENTIFIER 	   = "feed_identifier";
    public static final String HEADER_FEED_NAME    		   = "feed_name";
    public static final String HEADER_ROOT_OBJECT 		   = "root_object";
    public static final String HEADER_YAML_PATH 		   = "yaml_path";
    public static final String HEADER_PRIVACY_PATH 		   = "privacy_path";
    public static final String HEADER_OUTPUT_DERIVED_PATH  = "output_derived_path";
    public static final String HEADER_YAML_VERSION 		   = "yaml_version";
    public static final String HEADER_SPEC_FILE_FORMAT     = "spec_file_format";
    public static final String HEADER_IS_JSON_ARRAY_MSG    = "is_json_array_msg";
    public static final String HEADER_MSG_TAG    		   = "msg_tag";
    public static final String HEADER_PLACEHOLDER_VARS     = "placeholder_vars";
    public static final String AS                          = " AS ";
    public static final String YES                 = "Yes";
    public static final String NO                  = "No";
    public static final String ERROR               = "error";
    public static final String SWAGGER_2           = "Swagger2.0";
    public static final String OPENAPI_3           = "Openapi3.0";
    public static final String REQUEST             = "Request";
    public static final String RESPONSE            = "Response";
    public static final String BASE_TBL_ALIAS      = "base_tbl";
    public static final String BASE_COL_ALIAS      = "decrypted_msg";
    public static final String ENCRYPT_UDF         = "datalake_m1.encrypt";
    public static final String DECRYPT_UDF         = "datalake_m1.decrypt";
    public static final String SUCCESS             = "Success";
    public static final String SWAGGER             = "swagger";
    public static final String OPENAPI             = "openapi";
    public static final String TXT_FILEFORMAT      = ".txt";
    public static final String JSON_FILEFORMAT     = ".json";
    public static final String ALPHA_STR		   = "VLS-FPE-AlphaNum";
    public static final String ZERO				   = "0";
    public static final String UPDATED			   = "updated";
    public static final String OPEN_BRACKET		   = "[";
    public static final String CLOSE_BRACKET	   = "]";

    /**
     * sets of special characters
     */
    public static final String EMPTY               = "";
    public static final String COMMA               = ",";
    public static final String QUOTE               = "'";
    public static final String PIPE                = "|";
    public static final String DOUBLE_QUOTE        = "\"";
    public static final String BACK_QUOTE          = "`";
    public static final String SEMI_COLN           = ";";
    public static final String DOT                 = ".";
    public static final String SPACE               = " ";
    public static final String ROOT_NODE           = "$";
    public static final String NEW_LINE            = "\n";
    public static final String TAB                 = "    ";
    public static final String LPAREN              = "(";
    public static final String RPAREN              = ")";
    public static final String ESC                 = "\\";
    public static final String UNDER_SCORE         = "_";
    public static final String FWD_SLACE           = "/";
    public static final String DASH				   = "-";
    public static final String GREATER_THEN		   = ">";
    public static final String ASTERISK			   = "*";
}
