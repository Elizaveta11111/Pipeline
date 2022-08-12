import ru.spbstu.pipeline.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;

import static java.lang.Integer.min;

public class Writer implements IWriter {
    private final Logger logger;
    private FileOutputStream output;
    private int bufferSize;
    private Parser parser;
    private IMediator prodMediator;
    private TYPE[] inputTypes = {TYPE.CHAR, TYPE.BYTE};
    private TYPE[] outputTypes = {TYPE.CHAR, TYPE.BYTE};

    public Writer (Logger LOGGER) {
        logger = LOGGER;
    }

    @Override
    public RC setOutputStream(FileOutputStream fileOutputStream) {
        output = fileOutputStream;
        return output == null ? RC.CODE_INVALID_OUTPUT_STREAM : RC.CODE_SUCCESS;
    }

    @Override
    public RC setConsumer(IConsumer consumer) {
        return RC.CODE_SUCCESS;
    }

    public RC setProducer(IProducer producer) {
        prodMediator = producer.getMediator(TypesComparer.matchTypes(inputTypes, producer.getOutputTypes()));
        if (producer == null) {
            logger.warning(writerMessages.matchingFailure());
            return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
        }
        else return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConfig(String filename) {
        parser = new Parser(filename, new WriterGrammar(), logger);
        if (parser.getError() != RC.CODE_SUCCESS)
            return parser.getError();
        bufferSize = Integer.parseInt(parser.getValue(WriterGrammar.size));
        if(bufferSize < 1) {
            logger.warning(writerMessages.incorrectSize());
            return RC.CODE_CONFIG_SEMANTIC_ERROR;
        }
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC execute() {
        byte[] data;
        byte buffer[];
        Object prodData = prodMediator.getData();
        if(prodData == null)
            return null;
        if (prodData instanceof char[])
            data = new String((char[]) prodData).getBytes(StandardCharsets.UTF_8);
        else data = (byte[]) prodData;
        try {
            for (int i = 0; i < data.length; i += bufferSize) {
                buffer = Arrays.copyOfRange(data, i, min(data.length, i + bufferSize));
                output.write(buffer);
            }
        } catch (IOException ex) {
            logger.warning(writerMessages.writingFail());
            return RC.CODE_FAILED_TO_WRITE;
        }
        return RC.CODE_SUCCESS;
    }

    private static class writerMessages {
        static String incorrectSize() {
            return ("Wring buffer size is less than one.");
        }
        static String writingFail() {
            return ("Failed to write data to the stream");
        }
        static String matchingFailure() {
            return ("Failed to match types");
        }
    }
}
