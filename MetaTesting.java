import java.io.*;
import java.util.*;
import java.util.Scanner.*;
import java.io.FileReader.*;

public class MetaTesting{
    public static List<String> parseInput(String fileName){
      String line = null;
      FileReader fileReader=null;
      BufferedReader bufferedReader=null;
      List<String> L = new ArrayList<String>();
        try{
          fileReader= new FileReader(fileName);
        }catch(FileNotFoundException e){
            System.out.println("File does not exist in this folder");
            return null;
          }
        bufferedReader= new BufferedReader(fileReader);
        try{
          while((line = bufferedReader.readLine()) != null){
            String[] tokens= line.split(":");
            try{
                  L.add(tokens[1]);
              }catch(ArrayIndexOutOfBoundsException e){
                System.out.println("Enter valid input");
                return null;
              }
            }
          }catch(IOException e){
            System.out.println("IO error has occured.");
            return null;
        }
        return L;
    }
}
