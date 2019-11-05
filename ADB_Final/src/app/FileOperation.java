package app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileOperation {
   String filepath;

   void readFromFile(String filepath){
       this.filepath = filepath;
       TransactionManager tm = new TransactionManager();
       DataManager dm = DataManager.getInstance();
       int transNumber = -1;
       int variableNumber = -1;
       int siteNumber = -1;
      
       try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
           String line;
           while ((line = br.readLine()) != null) {
               String transaction = line.substring(0, line.indexOf("("));
               switch (transaction){
                   case "begin":
                       transNumber = Integer.parseInt(line.substring(line.indexOf("T")+1, line.indexOf(")")));
                       tm.begin(transNumber);
                       break;
                   case "beginRO":
                       transNumber = Integer.parseInt(line.substring(line.indexOf("T")+1, line.indexOf(")")));
                       tm.beginRO(transNumber);
                       break;
                   case "R":
                       transNumber = Integer.parseInt(line.substring(line.indexOf("T")+1, line.indexOf(",")));
                       variableNumber = Integer.parseInt(line.substring(line.indexOf("x")+1, line.indexOf(")")));
                       tm.read(transNumber, variableNumber);
                       break;
                   case "W":
                       transNumber = Integer.parseInt(line.substring(line.indexOf("T")+1, line.indexOf(",")));
                       variableNumber = Integer.parseInt(line.substring(line.indexOf("x")+1, line.indexOf(",")));
                       int value = Integer.parseInt(line.substring(line.lastIndexOf(",")+1, line.indexOf(")")));
                       tm.write(transNumber, variableNumber);
                       break;
                   case "dump":
                       dm.dump();
                       break;
                   case "end":
                       transNumber = Integer.parseInt(line.substring(line.indexOf("T")+1, line.indexOf(")")));
                       tm.end(transNumber);
                       break;
                   case "fail":
                       siteNumber = Integer.parseInt(line.substring(line.indexOf("(")+1, line.indexOf(")")));
                       dm.fail(siteNumber);
                       break;
                   case "recover":
                       siteNumber = Integer.parseInt(line.substring(line.indexOf("(")+1, line.indexOf(")")));
                       dm.recover(siteNumber);
                       break;
                   default:
                       System.out.println("Invalid transaction");
                       break;
               }
           }
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

   void writeToFile(){

   }
}

