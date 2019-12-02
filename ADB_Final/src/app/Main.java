package app;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.*;

public class Main {
    /**
     * 
     * @param args[1]: input file
     * @param args[2]: output file
     */
   public static void main(String args[]){
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        FileHandler fh;
        rootLogger.setLevel(Level.FINE);
        for (Handler h : rootLogger.getHandlers()) {
            h.setLevel(Level.FINE);
        }
        FileOperation fp = new FileOperation();


        try {
            PrintStream out = new PrintStream(new FileOutputStream(args[1]));
            System.setOut(out);
            fh = new FileHandler(args[1], true);
            rootLogger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fp.readFromFile(args[0]);



    }
}
