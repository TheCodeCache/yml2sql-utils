package com.local.datalake.validator;

import java.util.stream.Stream;

/**
 * Enums used for input-metadata file validation
 * 
 * @author manoranjan
 */
public class Enums {

    /**
     * checks whether a given value exists in a given list of supplied values
     */
    private interface Exists {
        static boolean doesExist(Stream<? extends java.lang.Enum<?>> stream, String object) {
            try {
                return stream.filter(instance -> instance.toString().equalsIgnoreCase(object)).count() == 1 ? true
                        : false;
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * Swagger (yml) File Format
     */
    public static enum FileFormat {
        YML,
        YAML,
        RTF;

        static boolean exists(String format) {
            return Exists.doesExist(Stream.of(FileFormat.values()), format);
        }
    }

    /**
     * FLAG (True/False)
     */
    public static enum Flag {
        TRUE,
        FALSE;

        static boolean exists(String flag) {
            return Exists.doesExist(Stream.of(Flag.values()), flag);
        }
    }

    /**
     * Version (Swagger File)
     */
    static enum Version {
        SWAGGER_2(com.local.datalake.common.Constants.SWAGGER_2),
        OPENAPI_3(com.local.datalake.common.Constants.OPENAPI_3);

        private String version;

        private Version(String version) {
            this.version = version;
        }

        private String getVersion() {
            return version;
        }

        /**
         * Check for a given value existence
         * 
         * @param version
         * @return
         */
        static boolean exists(String version) {
            if (version == null || version.isEmpty())
                return false;
            return Exists.doesExist(Stream.of(Version.values()), version);
        }

        @Override
        public String toString() {
            return getVersion();
        }
    }

    /**
     * Secure-Data-Format, this is optional
     */
    public static enum SecureDataFormat {
        VLS_FPE_ALPHANUM("VLS-FPE-AlphaNum"),
        VLS_FPE_ALL("VLS-FPE-ALL"),
        SSN_FPE_9P("SSN-FPE-9P"),
        SAL_FPE_ALL("SAL-FPE-ALL");

        private String sdf;

        private SecureDataFormat(String sdf) {
            this.sdf = sdf;
        }

        private String getSecureDataFormat() {
            return sdf;
        }

        /**
         * Check for a given value existence
         * 
         * @param sdf
         * @return
         */
        static boolean exists(String sdf) {
            return exists(sdf, SecureDataFormat.values());
        }

        /**
         * Check for a given value existence from the list of provided constraints
         * 
         * @param sdf
         * @param formats
         * @return
         */
        static boolean exists(String sdf, SecureDataFormat[] formats) {
            if (sdf == null || sdf.isEmpty())
                return false;
            return Exists.doesExist(Stream.of(formats), sdf);
        }

        @Override
        public String toString() {
            return getSecureDataFormat();
        }
    }
}
