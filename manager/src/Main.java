import ru.spbstu.pipeline.RC;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static void main(String[] args)  {
        if (args.length != 1)
            return;
        Manager manager = new Manager(args[0], LOGGER);
        if(manager.checkStatus()) {
            manager.constructPipeline();
            if (manager.checkStatus())
                manager.run();
        }
    }
    private static void generateTable(){
        int i, j, pos;
        char[] arr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
        char[] perm= new char[arr.length];
        int len = arr.length;
        for(i = 0; i < arr.length; i++, len --) {
            pos = (int)(Math.random() * len);
            for(j = 0; j <= pos; j ++) {
                if(perm[j] != 0)
                    pos++;
            }
            perm[pos] = arr[i];
        }
        try(FileWriter writer = new FileWriter("table.txt", false)) {
            for(i = 0; i < arr.length; i++) {
                writer.append(arr[i]);
                writer.append(' ');
                writer.append(perm[i]);
                writer.append('\n');
            }
        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
        }
    }
}
