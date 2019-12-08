package app;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.*;

/**
 * @file Main.java
 * @author Shubham Divekar, Himani Shah (sjd451@nyu.edu, has482@nyu.edu)
 *  Entering class of the program that initiates the file reader and handlers for writing to the file
 * @version 0.1
 * Date: 2019-12-02
 *
 * @copyright Copyright (c) 2019
 *
 */
public class Main {
    /**
     * Main class, starts running from here, it sets the logger level and has logic to write to a file
     * @param args[1]: input file
     * @param args[2]: output file
     */
    public static void main( String args[] )
    {
        Logger rootLogger = LogManager.getLogManager().getLogger( "" );
        FileHandler fh;

        rootLogger.setLevel( Level.INFO );

        for( Handler h : rootLogger.getHandlers() )
        {
            h.setLevel( Level.INFO );
        }

        FileOperation fp = new FileOperation();

        try {
            PrintStream out = new PrintStream( new FileOutputStream( args[ 1 ] ) );
            /*System.setOut( out ); */
            fh = new FileHandler( args[ 1 ], true );
            rootLogger.addHandler( fh );
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter( formatter );
        }
        catch( SecurityException e ) {
            e.printStackTrace();
        }
        catch( IOException e ) {
            e.printStackTrace();
        }
        fp.readFromFile( args[ 0 ] );
    }
}
