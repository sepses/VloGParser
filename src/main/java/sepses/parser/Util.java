package sepses.parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.function.library.leviathan.log;

import sepses.ondemand_extractor.JenaQueryEngine;

public class Util {
	
	public void saveModel(Model model,String outputModel) throws IOException { 	
    	FileWriter out = new FileWriter(outputModel);
    	model.write(out,"TURTLE");
    	System.out.println("Model is saved!");
    	out.flush();
    	out.close();
    	
	}
	
	
	  public void storeFileInRepo(String filename, String sparqlEndpoint, String namegraph, String user, String pass) {
		  Storage  storage = VirtuosoStorage.getInstance();
		 System.out.println("Store data: "+filename+" to " + sparqlEndpoint + " using graph " + namegraph);
	       storage.replaceData(filename, sparqlEndpoint, namegraph, true, user, pass);
	    }
	  
	  
	
	 public String executeQuery(String queryString,Model model) {
	    	JenaQueryEngine jena = new JenaQueryEngine(queryString,model);
			return jena.executeQuery();
	    }
	 
	 public void relodLDF(String ldflog) throws IOException {
		
			String ldflogS = new String(Files.readAllBytes(Paths.get(ldflog)));
			String regex = "master\\s\\d+";
			String res = parseRegex(ldflogS.toLowerCase(), regex);
			String command = "kill -s SIGHUP "+res.substring(6);
			System.out.print(command);
			emptyFile(ldflog);
			Runtime.getRuntime().exec(command);
			
			//deleteFile(ldflog);
			//return res.substring(6);
		 
	 }
	 
		public static String parseRegex(String logline,String regex) {
	    	
	    	Pattern pattern = Pattern.compile(regex);
	    	Matcher matcher = pattern.matcher(logline);
	    	String dt = null;
	    	if (matcher.find())
	    	{
	    	    dt= matcher.group(0);
	    	}
			return dt;  
		
	    
	}
		
		public static void emptyFile(String file) throws IOException {
			PrintWriter writer = new PrintWriter(file);
			writer.flush();
			writer.close();
		}
}
