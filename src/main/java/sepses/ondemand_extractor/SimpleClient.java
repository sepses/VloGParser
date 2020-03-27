package sepses.ondemand_extractor;

import sepses.parser.JSONRDFParser;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class SimpleClient extends WebSocketClient {
	 private static Logger logger = LoggerFactory.getLogger(SimpleClient.class.getName());
	 private String queryString;
	 private String outputModel;
	 private String outputResult;
	 private Integer logdata;
	 private Integer logstashData=0;
	 Model mastermodel;
	 JSONRDFParser jp; 
	//private static Model mastermodel = ModelFactory.createDefaultModel();
	// Model tmodel = ModelFactory.createDefaultModel();
	 

	public SimpleClient( URI serverUri , Draft draft ) {
		super( serverUri, draft );
		
		
	}

	public SimpleClient( URI serverURI, String queryString, String outputModel, String outputResult, String RMLFile, Integer logdata) {
		super( serverURI );
		this.queryString = queryString;
		this.outputModel = outputModel;
		this.outputResult= outputResult;
		this.jp = new JSONRDFParser(RMLFile);
		this.logdata=logdata;
		//mastermodel = ModelFactory.createDefaultModel();
		
	}

	public SimpleClient( URI serverUri, Map<String, String> httpHeaders ) {
		super(serverUri, httpHeaders);
	}

	@Override
	public void onOpen( ServerHandshake handshakedata ) {
		System.out.println("client web socket open...");
		this.mastermodel = ModelFactory.createDefaultModel();
		logger.info("open websocket connection");
		}

	@Override
	public  void onMessage( String message ) {
		//System.out.println(message);
		System.out.println("Received Message...");
//		System.out.println("logtashdata:"+this.logstashData);
	//	System.out.print(" logdata: "+this.logdata);
		if(this.logstashData>=this.logdata) {
			close();
		}else {
		//this.allmessage = message;
		//try {
			
			//System.out.println(message);
	         try {
	        	 this.logstashData=this.logstashData+1; 
				
	        	 this.mastermodel.add(this.jp.Parse(message));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	
		 }	
			//InputStream stream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
			//RDFDataMgr.read(tmodel, stream, null, Lang.JSONLD);
			//Model merged = tmodel.getUnionModel();
			//update model
			
			//model.close();
		//} catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		
		

		
//		try {
//			saveModel(merged);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//merged.write(System.out,"TURTLE");
		//merged.close();
		//System.out.println();
		
		//merged.write(System.out,"TURTLE");
		//this.model.add(merged);
				
		 
	}



	@Override
	public void onClose( int code, String reason, boolean remote ) {
		logger.info("close websocket connection: " + code + " - " + reason);
		System.out.println("execute Query");
		//this.mastermodel.write(System.out,"TURTLE");
	
		//save model
		try {
			//this.mastermodel = JSONRDFParser.Parse(jsonString, RMLFile);
			saveModel2(this.mastermodel,this.outputModel);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//query model
			String r = executeQuery(this.queryString,this.mastermodel);

			System.out.println("logdata :"+this.logdata);
			System.out.println("logstashData :"+this.logstashData);
			//System.out.print(r);
			try {
				saveResult(r, this.outputResult);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
	
			
			this.mastermodel.close();
			//return this.queryResults;
			//System.out.print(getQueryResults());
			//getQueryResults();

		}

	
	@Override
	public void onError( Exception ex ) {
		logger.error("error websocket: " + ex);
	}
	
	
	
    public void saveModel(Model model,String outputModel) throws IOException {
    	Model tmodel = ModelFactory.createDefaultModel();
    	Model nmodel = ModelFactory.createDefaultModel();
    	
        InputStream tempInput = new FileInputStream(outputModel);
        
        RDFDataMgr.read(tmodel, tempInput, Lang.TURTLE);
        
        nmodel = model.union(tmodel);	
    	FileWriter out = new FileWriter(outputModel);
    	nmodel.write(out,"TURTLE");
    	nmodel.close();
    	tmodel.close();
	}
	
    public void saveModel2(Model model,String outputModel) throws IOException { 	
    	FileWriter out = new FileWriter(outputModel);
    	model.write(out,"RDF/XML");
    	System.out.println("Model is saved!");
    	out.flush();
    	out.close();
    	
	}
    
    public void saveResult(String result,String outputResult) throws IOException { 	
    	FileWriter writer = new FileWriter(outputResult);
    	BufferedWriter bw = new BufferedWriter(writer);
    	bw.write(result);
    	bw.flush();
    	bw.close();

	}
    
    public String executeQuery(String queryString,Model model) {
    	JenaQueryEngine jena = new JenaQueryEngine(queryString,model);
		return jena.executeQuery();
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

public boolean checkBroadcastFinished(String m) {
	String regex = "broadcast finished!";
	String r = parseRegex(m,regex);
	if(r!=null) {
		return true;
	}else {
		return false;
	}
}

public Integer getLogstashData() {
	return this.logstashData;
}
  
    
//
//	public static void main( String[] args ) throws URISyntaxException {
//		ExampleClient c = new ExampleClient( new URI( "ws://localhost:8887" )); // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
//		c.connect();
//	}

}