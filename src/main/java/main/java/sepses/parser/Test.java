package main.java.sepses.parser;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;

public class Test {
    
        public static void main(  String[] args ) throws Exception
        {
            // System.out.print("test");
         long startTime = System.nanoTime();   
		 BufferedReader in = new BufferedReader(new FileReader("D:\\SANDBOX\\ondemandextraction\\VloGParser\\experiment\\logfile\\auth.log"));
        
        while (in.ready()) {
             String line = in.readLine();
             System.out.print(line);
            }
            System.out.println("Total time execution :"+(System.nanoTime() - startTime)/1000000+" ms");
           }
        

    
}