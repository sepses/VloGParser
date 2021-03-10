package sepses.parser;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.function.library.leviathan.log;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.options.HDTSpecification;

import sepses.ondemand_extractor.JenaQueryEngine;

public class Util {
	
	public static void saveModel(Model model,String outputModel) throws Exception { 	
    	FileWriter out = new FileWriter(outputModel);
    	model.write(out,"TURTLE");
    	System.out.println("Model is saved!");
    	out.flush();
    	out.close();
    	
	}
	
	public static void saveRDF4JModel(org.eclipse.rdf4j.model.Model model,String outputModel) throws Exception { 
				
				OutputStream tempOutput = new FileOutputStream(outputModel);
    	        Rio.write(model, tempOutput, RDFFormat.TURTLE); // write mapping
    	        model.clear();
    	        tempOutput.flush();
    	        tempOutput.close();
	}
	public static void generateHDTFile(String baseURI, String filename, String inputType, String hdtOutput) throws IOException, ParserException {
		
		HDT hdt = HDTManager.generateHDT(filename, baseURI, RDFNotation.parse(inputType), new HDTSpecification(), null);
		
		// Add additional domain-specific properties to the header:
		Header header = hdt.getHeader();
		header.insert("myResource1", "property" , "value");
		
		// Save generated HDT to a file
		hdt.saveToHDT(hdtOutput, null);
	}
	
	  public static void storeFileInRepo(String filename, String sparqlEndpoint, String namegraph, String user, String pass) {
		  Storage  storage = VirtuosoStorage.getInstance();
		 System.out.println("Store data: "+filename+" to " + sparqlEndpoint + " using graph " + namegraph);
	       storage.replaceData(filename, sparqlEndpoint, namegraph, true, user, pass);
	    }
	 
	  public static void storeHDTFile(String filename, String url) throws IOException {
		  String command = "curl -F statement=@"+filename+" "+  url;
          System.out.println(command);
          Process process = Runtime.getRuntime().exec(command);
          InputStream is = process.getInputStream();
          System.out.println("Data stored successfully");
  
	  }
	  
	
	 public String executeQuery(String queryString,Model model) {
	    	JenaQueryEngine jena = new JenaQueryEngine(queryString,model);
			return jena.executeQuery();
	    }
	 
	 public void relodLDF(String ldflog) throws Exception {
		
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
		
		public static void emptyFile(String file) throws Exception {
			PrintWriter writer = new PrintWriter(file);
			writer.flush();
			writer.close();
		}
		
	    public static ArrayList<String> listFilesForFolder(final File folder) {
	    	ArrayList<String> rulefiles = new ArrayList<String>();
	    	
	        for (final File fileEntry : folder.listFiles()) {
	            if (fileEntry.isDirectory()) {
	                listFilesForFolder(fileEntry);
	            } else {
	            	rulefiles.add(fileEntry.getName());
	                // System.out.println(fileEntry.getName());
	            }
	        }
	        
	        return rulefiles;
	    }
	    public static void deleteFileInDirectory(String directory) throws Exception {
			File f = new File(directory);
			FileUtils.cleanDirectory(f); 
		}
	    public static void deleteFile(String filename) { 
	        File f = new File(filename); 
	        f.delete();
	      }
	    
	    public static void MapHDTFile(String HDTFile) throws IOException{
			// Load HDT file using the hdt-java library
			HDTManager.mapIndexedHDT(HDTFile, null);
	    }
}
