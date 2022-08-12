import ru.spbstu.pipeline.*;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;


public class Executor implements IExecutor {
    private final Logger logger;
    private IConsumer consumer;
    private Parser parser;
    private IMediator prodMediator;
    private HashMap<Byte, Byte> table;
    private byte[] buffer;
    private TYPE[] inputTypes = {TYPE.CHAR, TYPE.BYTE};
    private TYPE[] outputTypes = {TYPE.CHAR, TYPE.BYTE};

    public Executor (Logger LOGGER) {
        logger = LOGGER;
    }

    @Override
    public RC execute() {
        byte[] data;
        Object prodData = prodMediator.getData();
        if(prodData == null)
            return consumer.execute();
        if (prodData instanceof char[])
            data = new String((char[]) prodData).getBytes(StandardCharsets.UTF_8);
        else data = (byte[]) prodData;
        byte newData[] = new byte[data.length];
        for (int i = 0; i < data.length; i++)
            newData[i] = getValue(data[i]);
        buffer = newData;
        return consumer.execute();
    }

    @Override
    public RC setProducer(IProducer producer) {
        prodMediator = producer.getMediator(TypesComparer.matchTypes(inputTypes, producer.getOutputTypes()));
        if (producer == null) {
            logger.warning(executorMessages.matchingFailure());
            return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
        }
        else return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConsumer(IConsumer iconsumer) {
        consumer = iconsumer;
        return consumer == null ? RC.CODE_FAILED_PIPELINE_CONSTRUCTION : RC.CODE_SUCCESS;
    }

    @Override
    public RC setConfig(String filename) {
        parser = new Parser(filename, new ExecutorGrammar(), logger);
        if (parser.getError() != RC.CODE_SUCCESS)
            return parser.getError();
        return loadTable(parser.getValue(ExecutorGrammar.table),parser.getValue(ExecutorGrammar.mode));
    }

    private RC loadTable (String fileName, String mode) {
        if(fileName == null || mode == null)
            return RC.CODE_CONFIG_GRAMMAR_ERROR;
        table = new HashMap<>();
        int line = 0;
        String s;
        String[] params;
        Byte first, second;
        try (FileReader reader = new FileReader(fileName)) {
            Scanner scan = new Scanner(reader);
            while (scan.hasNextLine()) {
                line++;
                s = scan.nextLine();
                params = s.split(ExecutorGrammar.tableDelimiter);
                if (params.length == 2 && params[0].length() == 1 && params[1].length() == 1) {
                    if(mode.equals(ExecutorGrammar.encode)) {
                        first = (byte) params[0].charAt(0);
                        second = (byte) params[1].charAt(0);
                    }
                    else if (mode.equals(ExecutorGrammar.decode)) {
                        first = (byte) params[1].charAt(0);
                        second = (byte) params[0].charAt(0);
                    }
                    else {
                        logger.warning(executorMessages.unknownMode(mode));
                        return RC.CODE_CONFIG_GRAMMAR_ERROR;
                    }
                    if (!table.containsKey(first) && !table.containsValue(second))
                        table.put(first, second);
                    else {
                        logger.warning(executorMessages.nonBijectiveTable());
                        table.clear();
                        return RC.CODE_CONFIG_SEMANTIC_ERROR;
                    }
                } else {
                    table.clear();
                    logger.warning(executorMessages.incorrectLine(line));
                    return RC.CODE_CONFIG_GRAMMAR_ERROR;
                }
            }
        } catch (IOException ex) {
            logger.warning(executorMessages.fileNotFound(fileName));
            return RC.CODE_INVALID_ARGUMENT;
        }
        logger.info(executorMessages.success());
        return RC.CODE_SUCCESS;
    }

    byte getValue(byte key) {
        if(table.containsKey(key))
            return table.get(key);
        else return key;
    }

    public TYPE[] getOutputTypes() {
        return outputTypes;
    }

    @Override
    public IMediator getMediator(TYPE type) {
        return (type == null)? null: new ExecutorMediator(type);
    }

    private class ExecutorMediator implements IMediator {
        TYPE type;

        ExecutorMediator(TYPE executorType) {
            type = executorType;
        }

        @Override
        public Object getData() {
            if (buffer == null) {
                return null;
            }
            if (type == TYPE.CHAR) {
                return new String(buffer,0,buffer.length,StandardCharsets.UTF_8).toCharArray();
            }
            else {
                return buffer;
            }
        }
    }

    private static class executorMessages {
        static String fileNotFound(String file) {
            return ("Unable to open a file" + file);
        }

        static String unknownMode(String mode) {
            return ("Unknown mode: \"" + mode + "\"");
        }

        static String nonBijectiveTable() {
            return ("Unable to use nonbijective table");
        }

        static String incorrectLine(int line) {
            return ("Incorrect binary table. Wrong format of line " + line);
        }

        static String success() {
            return ("Bijective table loaded successfully");
        }

        static String matchingFailure() {
            return ("Failed to match types");
        }
    }
}
