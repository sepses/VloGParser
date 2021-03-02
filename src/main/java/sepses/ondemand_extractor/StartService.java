package sepses.ondemand_extractor;

import sepses.parser.GrokHelper;

import java.io.BufferedReader;
import java.util.UUID;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import com.jayway.jsonpath.JsonPath;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

import sepses.parser.JSONRDFParser;
import sepses.parser.Util;

/**
 * Hello world!
 * @author KKurniawan
 * @version 1
 */
public class StartService
{
	
	private String content;
	private static final Logger log = LoggerFactory.getLogger(StartService.class);
	private long startTime = System.nanoTime();

  
   


	public StartService(String qs, String pq, String st, String et) throws Exception {
//    	System.out.println(qs);
//    	System.out.println(pq);
//		System.out.println(st);
//    	System.out.println(et);

    	//System.exit(0);
		//read json config
		String JsonConfig = new String(Files.readAllBytes(Paths.get("config.json"))); 
		JSONParser jcparser = new JSONParser(); 
		JSONObject jcobject = (JSONObject) jcparser.parse(JsonConfig);
        
		//endpoint
		String sparqlEndpoint = JsonPath.read(jcobject,"$.endpoint.sparqlEndpoint");
        String user = JsonPath.read(jcobject,"$.endpoint.user");
        String pass = JsonPath.read(jcobject,"$.endpoint.pass");
        
        //logsources
        List<String> logSources = JsonPath.read(jcobject, "$.logSources");

        String outputDir = JsonPath.read(jcobject,"$.outputDir");
        String hdtrepo = JsonPath.read(jcobject,"$.hdt-repo");
    	String hostname = InetAddress.getLocalHost().getHostName();
		//String hostname="localhost";
		String outputModel = outputDir+hostname+".ttl";
		String hdtOutput = outputDir+hostname+".hdt";
        
        List<String> ltitle = JsonPath.read(jcobject,"$.logSources[*].title");
        List<String> ltype = JsonPath.read(jcobject,"$.logSources[*].type");
        List<String> llogLocation = JsonPath.read(jcobject,"$.logSources[*].logLocation");
        List<String> lmapping = JsonPath.read(jcobject,"$.logSources[*].mapping");
        List<String> lgrokFile = JsonPath.read(jcobject,"$.logSources[*].grokFile");
        List<String> lgrokPattern = JsonPath.read(jcobject,"$.logSources[*].grokPattern");
        List<String> loutputModel = JsonPath.read(jcobject,"$.logSources[*].outputModel");
        //List<String> lhdtOutput = JsonPath.read(jcobject,"$.logSources[*].hdtOutput");
        List<String> lnamegraph = JsonPath.read(jcobject,"$.logSources[*].namegraph");
        List<String> lregexMeta = JsonPath.read(jcobject,"$.logSources[*].regexMeta");
        List<String> lregexOntology = JsonPath.read(jcobject,"$.logSources[*].regexOntology");
        List<String> lvocabulary = JsonPath.read(jcobject,"$.logSources[*].vocabulary");
        List<String> ltimeRegex = JsonPath.read(jcobject,"$.logSources[*].logTimeRegex");
        List<String> ldateFormat = JsonPath.read(jcobject,"$.logSources[*].logDateFormat");
        
       // System.out.print(logSources.size());
        //System.exit(0);
		String res="";
		//this.startTime = System.nanoTime();
		QueryTranslator qt = new QueryTranslator(pq);
		ArrayList prefixes= qt.prefixes;
		String limit = qt.limit;
		//System.out.println(prefixes.get(0));
		
		org.eclipse.rdf4j.model.Model rdf4JM = new LinkedHashModel();
		
		for(int i=0;i<logSources.size();i++) {


			//check prefix
			if(prefixes.contains(lvocabulary.get(i))){
				log.info("parsing start");
						res = parse(rdf4JM, llogLocation.get(i), lgrokFile.get(i), lgrokPattern.get(i),
								lmapping.get(i),pq, loutputModel.get(i), hdtOutput,hdtrepo,
								lregexMeta.get(i), lregexOntology.get(i),
								sparqlEndpoint, user, pass, lnamegraph.get(i), st, et, ltimeRegex.get(i),
								ldateFormat.get(i),limit);	
					//System.exit(0);
					this.content=res;
					
			}
		}
		
	//	Util ut = new Util();
	//	String hostname = InetAddress.getLocalHost().getHostName();
		//String hostname="localhost";
	//	String outputModel = outputDir+hostname+".ttl";
	//	String hdtOutput = outputDir+hostname+".hdt";
	
	//	ut.saveRDF4JModel(rdf4JM, outputModel);
	//	System.out.println("generate HDT file..");
	//	ut.generateHDTFile("http://w3id.org/sepses/graph/"+hostname.toString(), outputModel, "TURTLE", hdtOutput);
		//ut.storeHDTFile(hdtOutput, "http://10.5.0.2:3000/upload");
		
		
		// ut.storeHDTFile(hdtOutput, hdtrepo);
		
		
		long elapsedTime = System.nanoTime() - this.startTime;
		System.out.println("Total time execution :"+elapsedTime/1000000+" ms");

		
		
    }
 
	public String parse(org.eclipse.rdf4j.model.Model JModel, String logfile, String grokfile, String grokpattern, 
			String RMLFile, String parsedQuery, String outputModel, String hdtOutput, String hdtrepo,String regexMeta, 
			String regexOntology,String sparqlEndpoint, String user, String pass, 
			String namegraph,String startTime, String endDate, String dateTimeRegex,
			String dateFormat, String limit) throws Exception {
//		System.out.print(endDate);
//		System.exit(0);
		//=======translate the query ===========
    	//delete existing output file
    	//deleteFile(outputResult);
		String response="";
    	deleteFile(outputModel);
        QueryTranslator qt = new QueryTranslator(parsedQuery);

         Model m = qt.loadRegexModel(regexMeta, regexOntology);
          qt.parseJSONQuery(m);
	     // System.exit(0);
	      List<FilterRegex> filterRegex= qt.filterregex;
		  List<RegexPattern> regexPattern= qt.regexpattern;
		 
    	SimpleDateFormat sdfl = new SimpleDateFormat(dateFormat);
    	SimpleDateFormat sdf = new SimpleDateFormat("MMM d HH:mm:ss");
    	Date startt = sdf.parse(startTime);
    	Date endt = sdf.parse(endDate);
    	
    	
    	Integer logdata = 0;
    	JSONRDFParser jp = new JSONRDFParser(RMLFile);
    	Model model = ModelFactory.createDefaultModel();
    	try {
  		
    		GrokHelper gh = new GrokHelper(grokfile, grokpattern);
    		

			 String jsondata = "";
			 String jsondataTemp="";
			 
			int co=0;
			JSONArray alljson = new JSONArray();
			int maxLimit=0;
			if(limit!=null){
				maxLimit = Integer.parseInt(limit);
			}
			int climit=0;
			

		//optionI	
		BufferedReader in = new BufferedReader(new FileReader(logfile));
		//option II
	//	BufferedInputStream bf= new BufferedInputStream(new FileInputStream(logfile));
	//	BufferedReader in = new BufferedReader(new InputStreamReader(bf, StandardCharsets.UTF_8));

		//option III
	//	BufferedReader in = Files.newBufferedReader(Paths.get(logfile), StandardCharsets.UTF_8);
		long timereading=0;
    	        while (in.ready()) {
                String line = in.readLine();
    			
    			String dt0 = parseRegex(line,dateTimeRegex);
    			 Date dt1 = sdfl.parse(dt0);
				
				 // break when limit is reached
				 if(limit!=null){
					 if(climit>=maxLimit){
						 
						 break;
					 }
				 }

				 //break after reaching the end of true line
				if(!dt1.before(endt)){
					log.info("break, true line is reached!, total read: "+co);
					break;
				}
				
				if(logdata.equals(1)){
					timereading = System.nanoTime() - this.startTime;
					
				}
				
    			 if(dt1.after(startt) && dt1.before(endt)) {
					
    				 jsondataTemp = gh.parseGrok(line);

    				 if(filterRegex.size()!=0) {
						boolean c = checkAllFilter(filterRegex, jsondataTemp);
						 if(c) {
							 jsondata=jsondataTemp;
							 climit++;
 							
 						 }else {
 							jsondata="";
 						 }
    				 }else {
						 jsondata=jsondataTemp;
						 climit++;
//    					 System.out.println(jsondata);
    				 }
    			  } else{
    				 jsondata="";
    			 }	
     		
     			if(jsondata!=""){
     				logdata++;     			
					JSONObject jd = addUUID(jsondata);
					alljson.add(jd);
				}
				co++;
			}
			
			JSONObject alljsObj = new JSONObject();
			alljsObj.put("logEntry",alljson);
			long timeextracting = System.nanoTime() - this.startTime;
			//System.out.println(alljsObj.toString());
			log.info("filtering finished");
			org.eclipse.rdf4j.model.Model rdf4jmodel  = jp.Parse(alljsObj.toString());
			//Util ut = new Util();
			long parsingtime = System.nanoTime()-this.startTime;
			log.info("parsing finished");
			JModel.addAll(rdf4jmodel);
			Util.saveRDF4JModel(rdf4jmodel, outputModel);
			//long savingtime = System.nanoTime()-this.startTime;
			//ut.storeFileInRepo(outputModel, sparqlEndpoint, namegraph, user, pass);
			Util.generateHDTFile(namegraph, outputModel, "TURTLE", hdtOutput);
			long compressingtime = System.nanoTime()-this.startTime;
			log.info("compression (hdt) finished..");
			Util.storeHDTFile(hdtOutput, hdtrepo);
			long uploadingtime = System.nanoTime()-this.startTime;
			response = "{\"content\":\"success\",\"endopoint\":\""+sparqlEndpoint+"\"}";
			System.out.println("===========SUMMARY============");
			System.out.println("read line :"+co);
			System.out.println("parsed line :"+logdata);
			System.out.println("reading time :"+timereading/1000000+" ms");
			System.out.println("extraction time :"+(timeextracting-timereading)/1000000+" ms");
			System.out.println("parsing time :"+(parsingtime-timeextracting)/1000000+" ms");
			System.out.println("Compressing time :"+(compressingtime-parsingtime)/1000000+" ms");
			System.out.println("Uploading time :"+(uploadingtime-compressingtime)/1000000+" ms");
			
			
			//System.out.println("Storing to TripleStore time :"+(storingtime-savingtime)/1000000+" ms");
			
			in.close();
			
    	 }
               catch (Exception closeException) {
          	}
		
		return response;
    	

	}
	public boolean checkFilterJsonWithVariableRegex(String jsondata, String variable, String regex) throws org.json.simple.parser.ParseException {
//		JSONParser parser = new JSONParser(); 
//		JSONObject json = (JSONObject) parser.parse(jsondata);
		Any json=JsonIterator.deserialize(jsondata);
		if(json.get(variable)==null){
			return true;
		}else{
			if(checkRegexExist(json.get(variable).toString(),regex)){
				return true;
			}else {
				return false;
			}
		}
	}
	
	public void translateQuery(String pq,String rm,String ro) throws Exception, org.json.simple.parser.ParseException{
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

	private boolean checkAllFilter(List<FilterRegex> filterRegex, String jsondataTemp ) throws org.json.simple.parser.ParseException{
		ArrayList resFilter = new ArrayList<Boolean>();
		
		 for (int k=0;k<filterRegex.size();k++){
			// System.out.print(filterRegex.get(k).variable+"|"+filterRegex.get(k).regex);
    					 	  Boolean  cf = checkFilterJsonWithVariableRegex(jsondataTemp,filterRegex.get(k).variable,filterRegex.get(k).regex);
							resFilter.add(cf);
							
						}
		//System.out.println(jsondataTemp);
		for (int k=0;k<resFilter.size();k++){
					//		   System.out.print(resFilter.get(k)+"|");
							   
							
						}
		//System.out.println("");			
		if(resFilter.contains(false)){
			return false;
		}else{
			return true;
		}
	}
    
    private boolean checkRegexVariableExist(String Line,String variable,String regex) {
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
	public static void deleteFile(String file) throws Exception {
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

	public String getContents(String outputResult) throws Exception, InterruptedException {
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
	public static void main( String[] args ) throws Exception
  {
		String parsedQueryFile = "experiment0/input/query.json";
		String parsedQuery = new String(Files.readAllBytes(Paths.get(parsedQueryFile))); 
		String queryStringFile = "experiment0/input/query2.sparql";
		String queryString = new String(Files.readAllBytes(Paths.get(queryStringFile))); 
		String startTime = "May 30 19:09:00";
    	String endDate = "May 30 19:10:00";
    	
			StartService ss = new StartService(queryString,parsedQuery, startTime, endDate);
		
		
 	}
	
	 JSONObject addUUID(String jsondata) throws org.json.simple.parser.ParseException{
		UUID ui = UUID.randomUUID();
		org.json.simple.parser.JSONParser jp = new org.json.simple.parser.JSONParser();
		JSONObject json = (JSONObject) jp.parse(jsondata);
		json.put("id", ui);
		//System.out.print(json.toString());
		return json;
			
		
	}

	
}