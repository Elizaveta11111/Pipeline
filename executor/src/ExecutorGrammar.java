import ru.spbstu.pipeline.BaseGrammar;

class ExecutorGrammar extends BaseGrammar {
    static final String table = "TABLE_FILE_NAME";
    static final String mode = "MODE";
    static final String encode = "ENCODE";
    static final String decode = "DECODE";
    static final String tableDelimiter = " ";
    ExecutorGrammar(){
        super(new String[] {table,mode});
    }
}