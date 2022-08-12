import ru.spbstu.pipeline.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

public class Manager {
    private final Logger logger;
    private Parser parser;
    private RC error;
    private IReader beginner = null;

    public Manager (String config, Logger LOGGER) {
        logger = LOGGER;
        parser = new Parser(config, new ManagerGrammar(), logger);
        error = parser.getError();
    }

    private void setStreams(IReader reader, IWriter writer) {
        try {
            FileInputStream fileInputStream = new FileInputStream(parser.getValue(ManagerGrammar.input));
            reader.setInputStream(fileInputStream);
        }
        catch (FileNotFoundException exp) {
            error = RC.CODE_INVALID_INPUT_STREAM;
            logger.warning(managerMessages.invalidInputStream());
            return ;
        }
        try {
            FileOutputStream fileInputStream = new FileOutputStream(parser.getValue(ManagerGrammar.output));
            writer.setOutputStream(fileInputStream);
        }
        catch (FileNotFoundException exp) {
            error = RC.CODE_INVALID_OUTPUT_STREAM;
            logger.warning(managerMessages.invalidOutputStream());
            return ;
        }
    }

    public void constructPipeline () {
        int count = Integer.parseInt(parser.getValue(ManagerGrammar.count));
        if (count < 3) {
            logger.warning(managerMessages.notEnoughWorkers());
            error = RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
            return;
        }
        String workerName;
        Class clazz = null;
        IExecutor[] executors = new IExecutor[count];
        IReader reader;
        IWriter writer;
        int i = 1, j = 0;
        reader = (IReader)getWorker(parser,i++);
        if( !checkStatus()) return ;
        reader.setProducer(null);
        executors[j] = (IExecutor)getWorker(parser,i++);
        if( !checkStatus()) return ;
        reader.setConsumer(executors[j]);
        executors[j++].setProducer(reader);
        for ( ;i < count; i++, j++) {
            executors[j] = (IExecutor)getWorker(parser,i);
            if(!checkStatus()) return;
            executors[j - 1].setConsumer(executors[j]);
            executors[j].setProducer(executors[j - 1]);
        }
        writer = (IWriter)getWorker(parser, i);
        if(!checkStatus()) return;
        executors[j - 1].setConsumer(writer);
        writer.setConsumer(null);
        writer.setProducer(executors[j - 1]);
        setStreams(reader, writer);
        if(!checkStatus()) return;
        beginner = reader;
        logger.info(managerMessages.success());
    }

    public void run () {
        RC error;
        error = beginner.execute();
        if(error != RC.CODE_SUCCESS)
            logger.warning(managerMessages.result(error));
        else logger.info(managerMessages.result(error));
    }

    private Object getWorker (Parser parser, int i) {
        Class clazz = null;
        Object worker;
        String workerName = parser.getValue(ManagerGrammar.worker + i);
        String configFile = parser.getValue(ManagerGrammar.config + i);
        error = parser.getError();
        if (error != RC.CODE_SUCCESS)
            return null;
        try {
            clazz = Class.forName(workerName);
            Class[] workerParams = {Logger.class};
            worker = clazz.getConstructor(workerParams).newInstance(logger);
            error = ((IConfigurable)worker).setConfig(configFile);
            return worker;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | NoSuchMethodException | InvocationTargetException e) {
            logger.warning(managerMessages.workerNotFound(workerName));
            error = RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
            return null;
        }
    }

    public boolean checkStatus () {
        if (error == RC.CODE_SUCCESS)
            return true;
        else return false;
    }

    private static class managerMessages {
        static String notEnoughWorkers () {
            return ("Error in building pipeline: not enough workers.");
        }
        static String workerNotFound (String worker) {
            return ("Class \"" + worker + "\" not found.");
        }
        static String invalidInputStream () {
            return ("Unable to open input stream");
        }
        static String invalidOutputStream () {
            return ("Unable to open output stream");
        }
        static String success () {
            return ("Pipeline build successfully");
        }
        static String result (RC error) {
            return ("Process finished with code: " + error.toString());
        }
    }
}
