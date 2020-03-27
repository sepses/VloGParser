package sepses.ondemand_extractor;

import sepses.parser.GrokHelper;
import java.io.BufferedReader;
import java.util.UUID;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.json.simple.JSONObject;

import com.nflabs.grok.GrokException;
import sepses.parser.JSONRDFParser;
import sepses.parser.Util;

/**
 * Hello world!
 * @author KKurniawan
 * @version 1
 */
public class StartService2
{
	
	private String content;
   


	public StartService2(String qs, String pq, String st, String et) throws IOException, ParseException, InterruptedException, URISyntaxException, org.json.simple.parser.ParseException, GrokException {
//    	System.out.println(qs);
//    	System.out.println(pq);
//		System.out.println(st);
//    	System.out.println(et);
    	    	
		String logfile = "experiment/logfile/auth.log";
		String ldflog =  "experiment/output/ldf.log";
		String grokfile = "experiment/input/pattern.grok";
		String grokpattern = "%{SYSLOGBASE} %{GREEDYDATA:message}";
    	String RMLFile= "experiment/input/json.rml";
    	String queryString = qs; 
    	String parsedQuery = pq; 
    	//String queryString = new String(Files.readAllBytes(Paths.get(queryFile))); 
    	String outputModel = "experiment/output/model.ttl";
    	String regexMeta =  "experiment/input/regexMeta.ttl";
        String regexOntology =  "experiment/input/regexOntology.ttl";
        String sparqlEndpoint = "http://localhost:8890/sparql";
        String namegraph = "http://w3id.org/sepses/logsource";
        String user = "dba";
        String pass = "dba";
      	String startTime = st; //"Dec 31 07:39:00";
    	String endDate = et ; //"Dec 31 17:20:00";
         //=======translate the query ===========
    	//delete existing output file
    	//deleteFile(outputResult);
    	deleteFile(outputModel);
         QueryTranslator qt = new QueryTranslator(parsedQuery);
         
         Model m = qt.loadRegexModel(regexMeta, regexOntology);
          qt.parseJSONQuery(m);
	     // System.exit(0);
	      List<FilterRegex> filterRegex= qt.filterregex;
	      List<RegexPattern> regexPattern= qt.regexpattern;
	      //qt.printRegexPattern();    	  	
	      
   
    	
    	String dateTimeRegex = "\\w+\\s+\\d+\\s\\d{2}:\\d{2}:\\d{2}";
    	SimpleDateFormat sdf = new SimpleDateFormat("MMM d HH:mm:ss");
    	Date startt = sdf.parse(startTime);
    	Date endt = sdf.parse(endDate);
    	
    	
    	
    	
    	FileInputStream fis = new FileInputStream(logfile);
    	BufferedReader in = new BufferedReader(new InputStreamReader(fis));
    	
    	Integer logdata = 0;
    	JSONRDFParser jp = new JSONRDFParser(RMLFile);
    	Model model = ModelFactory.createDefaultModel();
    	try {
    		
    		GrokHelper gh = new GrokHelper(grokfile, grokpattern);
    		
    		while (in.ready()) {
    			String line = in.readLine();
    			 Date dt1 = sdf.parse(parseRegex(line,dateTimeRegex));
    			 String jsondata = "";
    			if(dt1.after(startt) && dt1.before(endt)) {
    			
    			 if(filterRegex.size()!=0) {
       			     if(checkRegexExist(line,filterRegex.get(0).regex)) {
       			    	jsondata = gh.parseGrok(line);
       			    	logdata++;
       			     }
    			 }else if(regexPattern.size()!=0) {
       			     if(checkRegexExistForObject(line,regexPattern.get(0).regexPattern, regexPattern.get(0).object)) {
       			    	jsondata= gh.parseGrok(line);
       			    	logdata++;
       			     } 
       			 
    			 } else{
    				 jsondata = gh.parseGrok(line);
    				logdata++;
    				 
    			 }	
    			
    			}
    			
    			
    			//mapping to RDF
    			
    			if(!(jsondata.isEmpty())){
    				String jd = addUUID(jsondata);
    				model.add(jp.Parse(jd));
    			}
    			
    		}
			System.out.println("logdata :"+logdata);
			
    	 }finally {
			   try {
		    	   in.close();
            	   Util ut = new Util();
            	   ut.saveModel(model, outputModel);
            	   ut.storeFileInRepo(outputModel, sparqlEndpoint, namegraph, user, pass);
            	   this.content = ut.executeQuery(queryString, model);
            	   //ut.relodLDF(ldflog);
       			   model.close();
               }
               catch (IOException closeException) {
                   // ignore
               }
		}
    	
    }
  
	
	public void translateQuery(String pq,String rm,String ro) throws IOException, org.json.simple.parser.ParseException{
		QueryTranslator qt = new QueryTranslator(pq);
	      Model m = qt.loadRegexModel(rm, ro);
	      qt.parseJSONQuery(m);
	      
        
	}
    private boolean checkRegexExist(String Line,String regex) {
		String uri = parseRegex(Line,regex);
		if(uri!=null) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean checkRegexExistForObject(String Line,String regex, String Object) {
		String uri = parseRegex(Line,regex);
		if(uri!=null) {
			if(uri.contains(Object)) {
				return true;
			}else {
			    return false;
			}
		}else {
			return false;
		}
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
	public static void deleteFile(String file) throws IOException {
		File f = new File(file);
		f.delete();
	}
    
	public boolean isFileExist(String outputResult) {
		File file = new File(outputResult); 
		if(file.exists()) {
			return true;
		}else {
			return false;
		}
	}

	public String getContents(String outputResult) throws IOException, InterruptedException {
		//System.out.println("getContent: "+outputResult);
		String r = null;
		File file = new File(outputResult); 
		while (!file.exists()) {
			Thread.sleep(10);
		}
		//System.out.println("getContent: "+outputResult);
		  BufferedReader br = new BufferedReader(new FileReader(file)); 
		  String st; 
		  while ((st = br.readLine()) != null) {
			  if(r==null) {
				  r=st;
			  }else {
				  r=r+"\r\n"+st;
			  }
		    //System.out.println(st); 
		  }
		  br.close();
		  file.delete();
		  return r;
		}
		
		

	public String getContent() {
		return this.content;
	}
	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
        return randomUUIDString;
	}
	public static void main( String[] args ) throws IOException, ParseException, InterruptedException, URISyntaxException, GrokException, org.json.simple.parser.ParseException
  {
		String parsedQueryFile = "experiment/input/query.json";
		String parsedQuery = new String(Files.readAllBytes(Paths.get(parsedQueryFile))); 
		String queryStringFile = "experiment/input/query2.sparql";
		String queryString = new String(Files.readAllBytes(Paths.get(queryStringFile))); 
		String startTime = "Dec 31 07:39:00";
    	String endDate = "Dec 31 23:59:00";
    	
			StartService2 ss = new StartService2(queryString,parsedQuery, startTime, endDate);
		
		
 	}
	
	String addUUID(String jsondata) throws org.json.simple.parser.ParseException{
		UUID ui = UUID.randomUUID();
		org.json.simple.parser.JSONParser jp = new org.json.simple.parser.JSONParser();
		JSONObject json = (JSONObject) jp.parse(jsondata);
		json.put("id", ui);
		//System.out.print(json.toString());
		return json.toString();
			
		
	}

	
}
