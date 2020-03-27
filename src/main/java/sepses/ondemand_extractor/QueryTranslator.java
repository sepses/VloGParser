package sepses.ondemand_extractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.jayway.jsonpath.JsonPath;


public class QueryTranslator {
	private String queryString;
    private List<Triples> triples = new ArrayList<>();
    protected List<RegexPattern> regexpattern = new ArrayList<>();
    protected List<FilterRegex> filterregex = new ArrayList<>();

    
	public QueryTranslator(String queryString){
		this.queryString = queryString;
		//System.out.print(this.queryString);
		//System.exit(0);
		
	}
	
	
	public void parseJSONQuery(Model model) throws IOException, ParseException{
		Object json = readJSONQuery(this.queryString);
//		System.out.print(json.toString());
//		System.exit(0);
	    parseTriple(json);
		parseFilter(json);
		lookupRegex(model);
		
		
		
			//System.out.println(subject.get(i)+" "+predicate.get(i)+" "+object.get(i));
			//triples.add(new Triples(subject.get(i), predicate.get(i), object.get(i)));
		
	}
	
	public void parseTriple(Object json){
		List<String> subject = JsonPath.read(json, "$.where[*].triples[*].subject.value");
		List<String> predicate = JsonPath.read(json, "$.where[*].triples[*].predicate.value");
		List<String> object = JsonPath.read(json, "$.where[*].triples[*].object.value");
		List<String> objectType = JsonPath.read(json, "$.where[*].triples[*].object.termType");
		for(int i=0;i<subject.size();i++){
			if(checkIsURI(predicate.get(i))) {
				//check if object is variable or string
				if(!objectType.get(i).contains("Variable")) {
					triples.add(new Triples(subject.get(i),"<"+predicate.get(i)+">", object.get(i)));
				}
				//else
				//note yet
			}
		}
	}
	
	public void parseFilter(Object json){
		List<String> type = JsonPath.read(json, "$.where[*].expression.operator");
		for(int i=0;i<type.size();i++) {
			if(type.get(i).contains("regex")) {
				List<String> args = JsonPath.read(json, "$.where[*].expression.args[*].value");
				filterregex.add(new FilterRegex(args.get(0),args.get(1)));
			}
		}
	}
	
	public Object readJSONQuery(String qs) throws IOException, ParseException {
		//read file

		 
//		System.out.print(qs);
//		System.exit(0);
		   JSONParser jsonParser = new JSONParser();
		   Object obj = jsonParser.parse(qs);
	          
	        return obj;
	}
	

	private boolean checkIsURI(String URI) {
		String regexURI = "https?:\\/\\/(www\\.)?[a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&\\/\\/=]*)";
		String uri = parseRegex(URI,regexURI);
		if(uri!=null) {
			return true;
		}else {
			return false;
		}
	}
	
	
	
	public Model loadRegexModel(String regexMeta, String regexOntology) {
		
		Model rmModel = RDFDataMgr.loadModel(regexMeta);
		Model romodel = RDFDataMgr.loadModel(regexOntology);
		
		rmModel.add(romodel);
		//rmModel.write(System.out);
		//System.exit(0);
		return rmModel;
	
	}
	
	public String executeQuery(Model model,String uri) {
		
		String query="PREFIX regex:<http://w3id.org/sepses/vocab/ref/regex#> \r\n"
				+ "SELECT ?rp WHERE { \r\n"
				+ uri+" regex:hasRegexPattern ?rn. \r\n"
				+ "?rn regex:regexPattern ?rp. \r\n}";
		//System.out.println(query);
//		System.exit(0);
		
		String regexPattern=null;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet result = qexec.execSelect();
			while(result.hasNext()) {				
				QuerySolution sol = result.nextSolution();
				regexPattern = sol.getLiteral("rp").toString();
			}
		}
		return regexPattern;
	}
	
	public void lookupRegex(Model model) {
		String regexPattern;
		for(int i=0;i<this.triples.size();i++) {
			//
			//System.out.println(this.triples.get(i).predicate);
			regexPattern = executeQuery(model, this.triples.get(i).predicate);
			if(regexPattern!=null) {
			 this.regexpattern.add(new RegexPattern(this.triples.get(i).predicate, regexPattern, this.triples.get(i).object));
			}
			
		}
	}
	
	
	
	public void printTriples() {
		for(int i = 0;i<this.triples.size();i++) {
		  //System.out.println(this.triples.get(i).subject+" "+this.triples.get(i).predicate+" "+this.triples.get(i).object);
		}
	}
	
	public void printFilterRegex() {
		for(int i = 0;i<this.filterregex.size();i++) {
		  System.out.println(this.filterregex.get(i).variable+" "+this.filterregex.get(i).regex);
		}
	}
	
	public void printRegexPattern() {
		for(int i = 0;i<this.regexpattern.size();i++) {
		  //System.out.println(this.regexpattern.get(i).uri+" "+this.regexpattern.get(i).regexPattern+" "+this.regexpattern.get(i).object);
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
	public static void main( String[] args ) throws IOException, ParseException  {
		
      String queryString = "experiment/input/query.json";
      String regexMeta =  "experiment/input/regexMeta.ttl";
      String regexOntology =  "experiment/input/regexOntology.ttl";
      String queryStr = new String(Files.readAllBytes(Paths.get(queryString)));
      QueryTranslator qt = new QueryTranslator(queryStr);
      Model m = qt.loadRegexModel(regexMeta, regexOntology);
      qt.parseJSONQuery(m);
      //qt.printTriples();
      //qt.printRegexPattern();
qt.printFilterRegex();
      //qs.executeQuery(m,"<http://purl.org/sepses/vocab/log/authLog#userName>");
      //qs.printPrefixes();
    
     
     

  }
	

}

class Triples {
	String subject,predicate,object;
	Triples(String s, String p, String o){
		this.subject=s;
		this.predicate=p;
		this.object=o;
	}
	
}	

class RegexPattern {
	String uri,regexPattern,object;
	RegexPattern(String uri, String rp, String o){
		this.uri=uri;
		this.regexPattern=rp;
		this.object=o;
	}
	
}

class grokLabel {
	String grokLabel,paramValue;
	grokLabel(String gl, String pv){
		this.grokLabel=gl;
		this.paramValue=pv;
	}
	
}

class FilterRegex {
	String variable,regex;
	FilterRegex(String variable, String regex){
		this.variable=variable;
		this.regex=regex;
	}
	
}	




