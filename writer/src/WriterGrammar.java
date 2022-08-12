import ru.spbstu.pipeline.BaseGrammar;

class WriterGrammar extends BaseGrammar {
    static final String size = "BUFFER_SIZE";
    WriterGrammar(){
        super(new String[] {size});
    }
}
