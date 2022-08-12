import ru.spbstu.pipeline.BaseGrammar;

class  ReaderGrammar extends BaseGrammar {
    static final String size = "BUFFER_SIZE";
    ReaderGrammar(){
        super(new String[] {size});
    }
}