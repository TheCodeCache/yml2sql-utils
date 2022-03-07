package com.local.datalake.common;

import static com.local.datalake.common.Constants.BASE_COL_ALIAS;
import static com.local.datalake.common.Constants.BASE_TBL_ALIAS;
import static com.local.datalake.common.Constants.COMMA;
import static com.local.datalake.common.Constants.DOT;
import static com.local.datalake.common.Constants.EMPTY;
import static com.local.datalake.common.Constants.FWD_SLACE;
import static com.local.datalake.common.Constants.NEW_LINE;
import static org.apache.commons.io.FilenameUtils.getName;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.JsonParser;
import com.local.datalake.dto.BaseDO;
import com.local.datalake.exception.InvalidMetadataException;
import com.local.datalake.exception.ViewException;

/**
 * Common Helper Functions
 * 
 * @author manoranjan
 */
public class ViewHelper {

	private static final Logger log = LoggerFactory.getLogger(ViewHelper.class);

	/**
	 * saves file in create mode
	 * 
	 * @param view
	 * @param file
	 * @throws IOException
	 */
	public static void save(String view, String file) throws IOException {
		save(view, file, false);
	}

	/**
	 * save view to a file in appendable mode
	 * 
	 * @param view
	 * @param file
	 * @param appendMode
	 * @throws IOException
	 */
	public static void save(String view, String file, boolean appendMode) throws IOException {
		new File(file).getParentFile().mkdirs();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, appendMode))) {
			writer.write(view);
		}
	}

	/**
	 * Given an absolute file path, it retrieves the extension
	 * 
	 * @param path
	 * @return
	 */
	public static String getPathExtn(String path) {
		String[] tokens = path.split("\\.(?=[^\\.]+$)", -1);
		return tokens[tokens.length - 1];
	}

	/**
	 * Given a file path/name, it retrieves its extension
	 * 
	 * @param path file path
	 * @return file extn
	 */
	public static FileExtn getFileExtn(String path) {
		String[] tokens = path.split("\\.(?=[^\\.]+$)", -1);
		return FileExtn.getByValue(DOT + tokens[tokens.length - 1]);
	}

	/**
	 * Given a file path/name, it retrieves its extension
	 * 
	 * @param file file
	 * @return file extn
	 */
	public static FileExtn getFileExtn(File file) {
		return getFileExtn(file.getAbsolutePath());
	}

	/**
	 * Basic validation on Input-Metadata CSV
	 * 
	 * TODO generate validation report
	 * 
	 * @param baseDO
	 * @throws ViewException
	 */
	public static void validate(BaseDO baseDO) throws ViewException {

		// widely used validator factory provided by hibernate
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		// sets of all constraints that got failed
		Set<ConstraintViolation<BaseDO>> constraintViolations = validator.validate(baseDO);

		Iterator<ConstraintViolation<BaseDO>> iterator = constraintViolations.iterator();
		boolean isValidInput = true;
		// logging the constraints failure msges
		List<String> errors = new ArrayList<>();
		while (iterator.hasNext()) {
			ConstraintViolation<BaseDO> violation = iterator.next();
			log.error(violation.getMessage());
			errors.add(violation.getMessage());
			isValidInput = false;
		}
		if (!isValidInput)
			throw new InvalidMetadataException(errors.size() == 1 ? errors.toString()
					: errors.stream().map(Function.identity()).collect(Collectors.joining(COMMA + NEW_LINE)));
	}

	/**
	 * get rid of trailing forward /
	 * 
	 * @param path
	 * @return
	 */
	public static String getNormalizedPath(String path) {
		if (path.endsWith(FWD_SLACE))
			return path.substring(0, path.length() - 1);
		else
			return path;
	}

	/**
	 * Get the same file path with different extn
	 * 
	 * @note: input file must have proper extension
	 * @param path single source file absolute path
	 * @param extn To/Target File Extension
	 * @return the absolute path with filename updated with given extn
	 */
	public static String changeExtn(String path, FileExtn extn) {

		String extName = extn.getValue().substring(1);

		String[] tokens = path.split("\\.(?=[^\\.]+$)");
		tokens[1] = extName;

		return String.join(DOT, tokens);
	}

	/**
	 * Get the same path with different extn
	 * 
	 * @param file input file
	 * @param extn custom extn
	 * @return return the absolute path with filename updated with desired extn
	 */
	public static String changeExtn(File file, FileExtn extn) {
		return changeExtn(file.getAbsolutePath(), extn);
	}

	/**
	 * Given an absolute path it returns the file name
	 * 
	 * @param path
	 * @return
	 */
	public static String getFileName(String path) {
		// using org.apache.commons.io.FilenameUtils.getName
		return getName(path);
	}

	/**
	 * turns a given string with null to empty string
	 * 
	 * @param string
	 * @return
	 */
	public static String turnNullOrWhiteSpaceToEmpty(String string) {
		return string == null ? EMPTY : string.trim();
	}

	/**
	 * checks whether a given string is not-empty or not null
	 * 
	 * @param string
	 * @return
	 */
	public static boolean isNotNullOrBlank(String string) {
		return !isNull(string);
	}

	/**
	 * checks for N/A values
	 * 
	 * @param word
	 * @return
	 */
	public static boolean isNotFound(String word) {
		return word != null && word.trim().equalsIgnoreCase("N/A") ? true : false;
	}

	/**
	 * combines isNull and isNotFound
	 * 
	 * @param word
	 * @return
	 */
	public static boolean isNullOrNotFound(String word) {
		return isNull(word) || isNotFound(word);
	}

	/**
	 * checks whether a string has missing value
	 * 
	 * @param word
	 * @return
	 */
	public static boolean isNull(String word) {
		return word == null || word.trim().isEmpty();
	}

	/**
	 * checks whether a given string is an array
	 * 
	 * @param json
	 * @return
	 */
	public static boolean isJsonArray(String json) {
		return JsonParser.parseString(json).isJsonArray();
	}

	/**
	 * alias for inner base query resultset
	 * 
	 * @return
	 */
	public static String getBaseTableName() {
		return BASE_TBL_ALIAS;
	}

	/**
	 * alias for column name which is being operated on
	 * 
	 * for ex: if the main column is being decrypted then the name should be
	 * decrypt_<column_name>
	 * 
	 * @param tag
	 * @param input
	 * @return
	 */
	public static String getBaseColumnName() {
		return BASE_COL_ALIAS;
	}

	/**
	 * loads yaml file and constructs json node tree
	 * 
	 * @param yamlPath
	 * @return
	 * @throws FileNotFoundException
	 */
	public static Map<String, Object> loadYaml(String yamlPath) throws FileNotFoundException {

		Yaml yaml = new Yaml();
		InputStream inputStream = new FileInputStream(yamlPath);

		Map<String, Object> tree = yaml.load(inputStream);
		log.info("{} is loaded properly", yamlPath);
		return tree;
	}

	/**
	 * cleans up all tmp folders
	 * 
	 * @param folder
	 * @throws IOException
	 */
	public static void cleanup(String... folder) throws IOException {
		for (String f : folder) {
			File ff = new File(f);
			if (ff.exists())
				FileUtils.cleanDirectory(ff);
		}
	}

	public static void deleteFiles(Path... files) throws IOException {
		for (Path f : files)
			Files.deleteIfExists(f);
	}
}
