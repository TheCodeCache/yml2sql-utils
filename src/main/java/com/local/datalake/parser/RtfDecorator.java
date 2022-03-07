package com.local.datalake.parser;

import static com.local.datalake.common.Constants.RTF_CONTENT_PATH;
import static com.local.datalake.common.Constants.SPACE;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import com.local.datalake.common.FileExtn;
import com.local.datalake.common.ViewHelper;
import com.local.datalake.dto.Input;
import com.local.datalake.exception.ViewException;

/**
 * Decorator for RTF file parsing
 * 
 * @author manoranjan
 */
public class RtfDecorator extends IParserDecorator {

    public RtfDecorator(IParser parser) {
        super(parser);
    }

    /**
     * Takes metadata as input and reads the file, uncovers it and read the content
     * as a string value, and calls super class for parsing this string content
     */
    @Override
    public String parse(Input input) throws Exception {

        String rtfPath = input.getYamlFilePath();
        String ymlPath = RTF_CONTENT_PATH + ViewHelper.getFileName(ViewHelper.changeExtn(rtfPath, FileExtn.YML));
        input.setYamlFilePath(ymlPath);

        // one-time activity for an individual rtf file
        if (!Files.exists(Paths.get(ymlPath))) {

            String content = extract(rtfPath);
            ViewHelper.save(content, ymlPath);

            try {
                checkYmlType(input);
            } catch (ViewException ex) {
                if (!ex.getMessage().equals("InValid Swagger File content")) {
                    String[] tokens = ex.getMessage().split(SPACE, 2);
                    tokens[0] = rtfPath;
                    ex.setMessage(tokens[0] + SPACE + tokens[1]);
                }
                throw ex;
            }
        }

        String json = super.parse(input);

        input.setYamlFilePath(rtfPath);
        return json;
    }

    /**
     * Extract the content of RTF file
     * 
     * @param input
     * @return
     * @throws BadLocationException
     * @throws IOException
     */
    public String extract(String rtfPath) throws BadLocationException, IOException {
        RTFEditorKit rtf = new RTFEditorKit();
        Document document = rtf.createDefaultDocument();

        InputStream istream = new FileInputStream(rtfPath);
        InputStreamReader i = new InputStreamReader(istream, StandardCharsets.UTF_8);

        rtf.read(i, document, 0);
        String content = document.getText(0, document.getLength());
        return content;
    }
}
