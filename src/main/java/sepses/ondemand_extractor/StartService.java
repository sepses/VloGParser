package sepses.ondemand_extractor;

import java.io.BufferedReader;
import java.util.UUID;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URI;
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
import org.java_websocket.server.WebSocketServer;

import sepses.ondemand_extractor.SimpleServer;

/**
 * Hello world!
 * @author KKurniawan
 * @version 1
 */
public class StartService
{
	
	private String content;
   


	public StartService(String qs, String pq, String st, String et) throws IOException, ParseException, InterruptedException, URISyntaxException, org.json.simple.parser.ParseException {
    	System.out.println(qs);
    	System.out.println(pq);
		System.out.println(st);
    	System.out.println(et);
    	
//    	System.exit(0);
		
		String host = "localhost";
		int port = 8887;

		WebSocketServer server = new SimpleServer(new InetSocketAddress(host, port));
		server.start();
    	
		String logfile = "experiment/logfile/auth.log";
		String grokfile = "experiment/input/pattern.grok";
    	String queryFile= "experiment/input/query2.sparql";
    	String RMLFile= "experiment/input/json.rml";
    	String queryString = qs; 
    	String parsedQuery = pq; 
    	//String queryString = new String(Files.readAllBytes(Paths.get(queryFile))); 
    	String uuid = getUUID();
    	String outputModel = "experiment/output/model_"+uuid+".ttl";
    	String outputResult = "experiment/output/result_"+uuid+".json";
    	 String regexMeta =  "experiment/input/regexMeta.ttl";
         String regexOntology =  "experiment/input/regexOntology.ttl";
      	String startTime = st; //"Dec 31 07:39:00";
    	String endDate = et ; //"Dec 31 17:20:00";
         //=======translate the query ===========
    	//delete existing output file
//    	deleteFile(outputResult);
    	deleteFile(outputModel);
         QueryTranslator qt = new QueryTranslator(parsedQuery);

         Model m = qt.loadRegexModel(regexMeta, regexOntology);
	     
         System.out.print(m);
         //System.exit(0);
	      qt.parseJSONQuery(m);
	     // System.exit(0);
	      List<FilterRegex> filterRegex= qt.filterregex;
	      List<RegexPattern> regexPattern= qt.regexpattern;
	      
	      qt.printRegexPattern();
	      //System.exit(0);
//	      for(int i=0;i<filterRegex.size();i++) {
//	    	  System.out.println(filterRegex.get(i).variable);
//	    	  System.out.print(filterRegex.get(i).regex);
//	      }
    	//System.exit(0);
    	//String endDate = parseQuery(queryString);
    	  	
   
    	
    	String dateTimeRegex = "\\w+\\s+\\d+\\s\\d{2}:\\d{2}:\\d{2}";
    	SimpleDateFormat sdf = new SimpleDateFormat("MMM d HH:mm:ss");
    	Date startt = sdf.parse(startTime);
    	Date endt = sdf.parse(endDate);
    	
    	
    	
    	
    	FileInputStream fis = new FileInputStream(logfile);
    	BufferedReader in = new BufferedReader(new InputStreamReader(fis));
    	
    	Integer logdata = 0;
    	try {
    		
    		
    		
    		while (in.ready()) {
    			String line = in.readLine();
    			 Date dt1 = sdf.parse(parseRegex(line,dateTimeRegex));
    			 
    			 
    		      
    		      
    		      
    			if(dt1.after(startt) && dt1.before(endt)) {
    			//if(dt1.before(endt)) {
    				//System.out.println(line);
    			 if(filterRegex.size()!=0) {
       			     if(checkRegexExist(line,filterRegex.get(0).regex)) {
       			    	server.broadcast(line); 
       			    	logdata++;
       			     }
    			 }else if(regexPattern.size()!=0) {
       			     if(checkRegexExistForObject(line,regexPattern.get(0).regexPattern, regexPattern.get(0).object)) {
       			    	server.broadcast(line);
       			    	logdata++;
       			     } 
       			 
    			 } else{
    				server.broadcast(line);
    				logdata++;
    				 
    			 }	
    			
    			}

    			}
			System.out.println("logdata :"+logdata);
    	 }finally {
			   try {
				   //server.broadcast("broadcast finished!");
            	   in.close();
            	   server.stop();
            	   if(logdata!=0) {
            	   startWSClient("ws://localhost:3232",queryString,outputModel,outputResult,RMLFile,logdata);
            	  this.content = getContents(outputResult);
            	   }else {
            		   this.content="Empty Result";
            	   }
            	  // System.out.println(getContent());
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
    private static String parseQuery(String queryString) throws ParseException {
    	//String timestampRegex = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}";
    	String timestampRegex = "\\w+\\s+\\d+\\s\\d{2}:\\d{2}:\\d{2}";
    	SimpleDateFormat sdf = new SimpleDateFormat("MMM d HH:mm:ss");
    	String timestamp1 = parseRegex(queryString,timestampRegex);
    	System.out.println("date: "+timestamp1);
    	return timestamp1;
    	// TODO Auto-generated method stub
		
	}

	private void startWSClient(String uri,String queryString, String outputModel, String outputResult, String RMLFile, Integer logdata) throws URISyntaxException, InterruptedException, IOException {
		// TODO Auto-generated method stub
    	// System.out.println("=================test===============");
    	SimpleClient c = new SimpleClient(new URI(uri), queryString, outputModel, outputResult, RMLFile, logdata);
    	c.connect();
    	System.out.println("logdata :"+logdata);
    	System.out.println("logstashdata :"+c.getLogstashData());
    	
		//Thread.sleep(10000);
		//c.close();
	    //getContent(outputResult);
		//this.qr = c.getQueryResults();
	   // c.printModel();
    	
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
//		System.out.println(Line);
//		System.out.println(regex);
//		System.out.println(Object);
//		System.out.println(uri);
//		System.exit(0);
		
		if(uri!=null) {
//			System.out.println(Object);
//			System.out.println(uri);
//			System.out.println(uri);
//			System.exit(0);
			//System.out.println(Object);
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
	public static void main( String[] args ) throws IOException, ParseException, InterruptedException, URISyntaxException
  {
		String parsedQueryFile = "experiment/input/query.json";
		String parsedQuery = new String(Files.readAllBytes(Paths.get(parsedQueryFile))); 
		String queryStringFile = "experiment/input/query2.sparql";
		String queryString = new String(Files.readAllBytes(Paths.get(queryStringFile))); 
		String startTime = "Dec 31 07:39:00";
    	String endDate = "Dec 31 23:59:00";
		try {
			StartService ss = new StartService(queryString,parsedQuery, startTime, endDate);
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
 	}

	
}
