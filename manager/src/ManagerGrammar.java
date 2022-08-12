import ru.spbstu.pipeline.BaseGrammar;

class ManagerGrammar extends BaseGrammar {
    static final String input = "INPUT_FILE_NAME";
    static final String output = "OUTPUT_FILE_NAME";
    static final String count = "NUMBER_OF_WORKERS";
    static final String worker = "WORKER";
    static final String config = "CONFIG";
    ManagerGrammar(){
        super(new String[] { input, output, count, worker, config});
    }
}