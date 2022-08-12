import ru.spbstu.pipeline.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;

public class Reader implements IReader {
    private final Logger logger;
    private IConsumer consumer;
    private FileInputStream input;
    private Parser parser;
    private byte[] buffer;
    private TYPE[] outputTypes = {TYPE.CHAR, TYPE.BYTE};

    public Reader (Logger LOGGER) {
        logger = LOGGER;
    }

    @Override
    public RC setInputStream(FileInputStream fileInputStream) {
        input = fileInputStream;
        return input == null ? RC.CODE_INVALID_INPUT_STREAM : RC.CODE_SUCCESS;
    }

    @Override
    public RC setConsumer(IConsumer iconsumer) {
        consumer = iconsumer;
        return consumer == null ? RC.CODE_FAILED_PIPELINE_CONSTRUCTION : RC.CODE_SUCCESS;
    }

    @Override
    public RC setProducer(IProducer producer) {
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConfig(String filename) {
        parser = new Parser(filename, new ReaderGrammar(), logger);
        if (parser.getError() != RC.CODE_SUCCESS)
            return parser.getError();
        int bufferSize = Integer.parseInt(parser.getValue(ReaderGrammar.size));
        buffer = new byte[bufferSize];
        if (bufferSize < 1)
            logger.warning(readerMessages.incorrectSize());
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC execute() {
        int i, len;
        RC error;
        try {
            while ((len = input.read(buffer)) != -1) {
                if(len != buffer.length)
                    buffer = Arrays.copyOfRange(buffer, 0, len);
                error = consumer.execute();
                if (error != RC.CODE_SUCCESS)
                    return error;
            }
            buffer = null;
            error = RC.CODE_SUCCESS;
            //error = consumer.execute();
        } catch (IOException ex) {
            logger.warning(readerMessages.readingFail());
            return RC.CODE_FAILED_TO_READ;
        }
        return error;
    }

    @Override
    public TYPE[] getOutputTypes() {
        return outputTypes;
    }

    @Override
    public IMediator getMediator(TYPE type) {
        return (type == null)? null: new ReaderMediator(type);
    }

    private class ReaderMediator implements IMediator {
        TYPE type;

        ReaderMediator(TYPE readerType) {
            type = readerType;
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

    private static class readerMessages {
        static String incorrectSize() {
            return ("Reading buffer size is less than one.");
        }
        static String readingFail() {
            return ("Failed to read data from the stream.");
        }
    }
}
