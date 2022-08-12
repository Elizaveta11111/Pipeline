import ru.spbstu.pipeline.TYPE;

public class TypesComparer {
    static TYPE matchTypes(TYPE[] list1, TYPE[] list2) {
        for (TYPE type1: list1)
            for (TYPE type2: list2)
                if(type1 == type2)
                    return type1;
    return null;
    }
}
