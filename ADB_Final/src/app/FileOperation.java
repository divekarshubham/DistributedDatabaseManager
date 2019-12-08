package app;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @file FileOperation.java
 * @author Shubham Divekar, Himani Shah (sjd451@nyu.edu, has482@nyu.edu)
 *  Reads the input lines and generates a corresponding operation
 * @version 0.1
 * Date: 2019-12-02
 *
 * @copyright Copyright (c) 2019
 *
 */

public class FileOperation {
    String filepath;
    private final static Logger LOGGER = Logger.getLogger( FileOperation.class.getName() );

    /**
     *  Reads each line in a file and directs the TM to issue a corresponding
     *        action
     * @param filepath path to input file
     */
    void readFromFile( String filepath )
    {
        this.filepath = filepath;
        /* Initialize the Transaction and Data Manager */
        TransactionManager tm = new TransactionManager();
        DataManager dm = DataManager.getInstance();
        Operation op;
        int transNumber = -1;
        int variableNumber = -1;
        int siteNumber = -1;

        try ( BufferedReader br = new BufferedReader( new FileReader( filepath ) ) ) {
            String line;

            while( ( line = br.readLine() ) != null )
            {
                line = line.replace( " ", "" );

                /* Get the action */
                if( line.startsWith( "/" ) )
                {
                    continue;
                }

                String transaction = line.substring( 0, line.indexOf( "(" ) );

                switch( transaction )
                {
                    case "begin":
                        transNumber = Integer.parseInt( line.substring( line.indexOf( "T" ) + 1, line.indexOf( ")" ) ) );
                        tm.begin( transNumber );
                        break;

                    case "beginRO":
                        transNumber = Integer.parseInt( line.substring( line.indexOf( "T" ) + 1, line.indexOf( ")" ) ) );
                        tm.beginRO( transNumber );
                        break;

                    case "dump":
                        dm.dump();
                        break;

                    case "end":
                        transNumber = Integer.parseInt( line.substring( line.indexOf( "T" ) + 1, line.indexOf( ")" ) ) );
                        tm.end( transNumber );
                        break;

                    case "fail":
                        siteNumber = Integer.parseInt( line.substring( line.indexOf( "(" ) + 1, line.indexOf( ")" ) ) );
                        tm.fail( siteNumber );
                        break;

                    case "recover":
                        siteNumber = Integer.parseInt( line.substring( line.indexOf( "(" ) + 1, line.indexOf( ")" ) ) );
                        tm.recover( siteNumber );
                        break;

                    case "R":
                        transNumber = Integer.parseInt( line.substring( line.indexOf( "T" ) + 1, line.indexOf( "," ) ) );
                        variableNumber = Integer.parseInt( line.substring( line.indexOf( "x" ) + 1, line.indexOf( ")" ) ) );
                        op = new Operation( tm.getActiveTransactions( transNumber ),
                                            tm.getActiveTransactions( transNumber ).isReadOnly() ? OperationType.READONLY
                                    : OperationType.READ,
                                            variableNumber, -1 );
                        tm.addAndExecuteOperation( op );
                        break;

                    case "W":
                        transNumber = Integer.parseInt( line.substring( line.indexOf( "T" ) + 1, line.indexOf( "," ) ) );
                        variableNumber = Integer.parseInt( line.substring( line.indexOf( "x" ) + 1, line.lastIndexOf( "," ) ) );
                        int value = Integer.parseInt( line.substring( line.lastIndexOf( "," ) + 1, line.indexOf( ")" ) ) );
                        op = new Operation( tm.getActiveTransactions( transNumber ), OperationType.WRITE, variableNumber,
                                            value );
                        tm.addAndExecuteOperation( op );
                        break;

                    default:
                        LOGGER.info( "Invalid transaction" );
                        break;
                }
            }
        }
        catch( IOException e ) {
            e.printStackTrace();
        }
    }
}
