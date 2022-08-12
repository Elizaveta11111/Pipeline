import ru.spbstu.pipeline.BaseGrammar;
import ru.spbstu.pipeline.RC;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;

class Parser {
    private final Logger logger;
    private RC error;
    private HashMap<String, String> config;
    Parser(String fileName, BaseGrammar grammar, Logger LOGGER) {
        logger = LOGGER;
        error = RC.CODE_SUCCESS;
        config = new HashMap<>();
        String s;
        String[] params;
        try(FileReader reader = new FileReader(fileName)) {
            int line = 0;
            Scanner scan = new Scanner(reader);
            while (scan.hasNextLine()) {
                line++;
                s = scan.nextLine();
                s = s.replaceAll(" ","");
                if(s.length() == 0)
                    continue;
                params = s.split(grammar.delimiter());
                if(params.length != 2) {
                    error = RC.CODE_CONFIG_GRAMMAR_ERROR;
                    logger.warning(parserMessages.incorrectString(fileName, line));
                    return;
                }
                int i;
                for(i = 0; i < grammar.numberTokens(); i++) {
                    if(params[0].indexOf(grammar.token(i)) == 0) {
                        if (config.containsKey(params[0])) {
                            logger.warning(parserMessages.redefinition(fileName, params[0], line));
                            error = RC.CODE_CONFIG_GRAMMAR_ERROR;
                            return;
                        }
                        config.put(params[0], params[1]);
                        break;
                    }
                }
                if (i == grammar.numberTokens()) {
                    logger.warning(parserMessages.tokenDoesntExist(fileName, params[0]));
                    error = RC.CODE_CONFIG_GRAMMAR_ERROR;
                    return;
                }
            }
            logger.info(parserMessages.success(fileName));
        }
        catch(IOException ex) {
            logger.warning(parserMessages.fileNotFound(fileName));
            error = RC.CODE_CONFIG_GRAMMAR_ERROR;
        }
    }
    RC getError(){
        return error;
    };
    String getValue(String key) {
        if(!config.isEmpty())
            return config.get(key);
        else {
            error = RC.CODE_CONFIG_GRAMMAR_ERROR;
            logger.warning(parserMessages.missingValue(key));
            return null;
        }
    }

    private static class parserMessages {
        static String fileNotFound(String file) {
            return ("Unable to load :" + file);
        }
        static String incorrectString(String file, int line) {
            return ("Parsing error in " + file + ". Wrong format of line " + line);
        }
        static String tokenDoesntExist(String file, String token) {
            return ("Parsing error in " + file + ".Token \"" + token + "\" not found.");
        }
        static String success(String file) {
            return (file + " parsed successfully");
        }
        static String redefinition (String file, String token, int line) {
            return ("Parsing error in " + file + ". Token \"" + token + "\" is redefined in line " + line);
        }
        static String missingValue(String token) {
            return ("Semantic error. Token \"" + token + "\" not found");
        }
    }
}
