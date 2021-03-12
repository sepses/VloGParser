package sepses.ondemand_extractor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.RDFDataMgr;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class QueryTranslator2 {
    protected List<Triples> triples = new ArrayList<>();
    protected List<RegexPattern> regexpattern = new ArrayList<>();
    protected List<FilterRegex> filterregex = new ArrayList<>();
	protected ArrayList<String> prefixes = new ArrayList<String>();

    	
	public static String parseGeneralRegexPattern(String regexPattern, String vocab) {
		//this function check general grok pattern (single grok pattern for the whole log line)
		   String grokPattern = null;
		   Model regexPatternModel = RDFDataMgr.loadModel(regexPattern) ;
		
		   String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
	        		+ "PREFIX reg:   <http://w3id.org/sepses/vocab/ref/regex#>\r\n" + 
	        		"select ?gp  where {\r\n" + 
	        		"    <"+vocab+">  reg:generalGrokPattern ?gp.\r\n" + 
	        		"} \r\n";
		   
	
		   QueryExecution qe = QueryExecutionFactory.create(query, regexPatternModel);
	        ResultSet rs = qe.execSelect();
	        while (rs.hasNext()) {
	            QuerySolution qs = rs.nextSolution();
	            RDFNode co = qs.get("?gp");
	            grokPattern = co.asLiteral().toString();
	        }       
	        

		return grokPattern;
		
	}

	
	public static ArrayList<String> parseRegexPattern(String queryString, String regexPattern) throws ParseException, Exception {
		//read regex pattern model
		JsonNode json = readJSONQuery(queryString);
		
		ArrayList<String> predicates = parsePredicate(json);
		String filterPredicates = "";
		ArrayList<String> regexPatterns = new ArrayList<String>();
		for(int i=0;i<predicates.size();i++) {
			if(i==0) {
				filterPredicates+="<"+predicates.get(i)+">";
			}else {
				filterPredicates+=",<"+predicates.get(i)+">";
			}

		}
		
		Model regexPatternModel = RDFDataMgr.loadModel(regexPattern) ;
		
	
		
	        String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
	        		+ "PREFIX reg:   <http://w3id.org/sepses/vocab/ref/regex#>\r\n" + 
	        		"select ?gp  where {\r\n" + 
	        		"    ?s  reg:grokPattern ?gp.\r\n" + 
	        		"    FILTER(?s in ("+filterPredicates+") )\r\n" + 
	        		"} \r\n";
	        

        QueryExecution qe = QueryExecutionFactory.create(query, regexPatternModel);
        ResultSet rs = qe.execSelect();
        while (rs.hasNext()) {
            QuerySolution qs = rs.nextSolution();
            RDFNode co = qs.get("?gp");
            regexPatterns.add(co.asLiteral().toString());
        }       

		
		return regexPatterns;
	}

	public static ArrayList<String> parsePredicate(JsonNode json){
		ArrayList<String> predicate = new ArrayList<String>();
		JsonNode where = json.get("where");

			for(int i=0;i<where.size();i++) {
				JsonNode triples = where.get(i).get("triples");
				if(triples!=null) {
				for(int n=0;n<triples.size();n++) {
					predicate.add(triples.get(n).get("predicate").get("value").textValue());
					
				}	
				}
			}
		
	return predicate;
	}
	
	public static ArrayList<String> parseFilter(String queryString) throws ParseException, Exception{
		JsonNode json = readJSONQuery(queryString);
		ArrayList<String> filterregex = new ArrayList<String>();
		JsonNode where = json.get("where");

		for(int i=0;i<where.size();i++) {
			JsonNode type = where.get(i).get("type");
			
			if(type.textValue().contains("filter")) {
			

				JsonNode args = where.get(i).get("expression").get("args");
		
					filterregex.add(args.get(0).get("args").get(0).get("value").textValue()+"="+args.get(1).get("value").textValue());
					
					
			
			}
		}
	
		
		return filterregex;
	}


	
		
	public static ArrayList<String> parsePrefixes(String qs) throws ParseException, Exception{
		JsonNode json = readJSONQuery(qs);
		JsonNode pref = json.get("prefixes");
		ArrayList<String> prefixes= new ArrayList<String>();
		Iterator<Map.Entry<String,JsonNode>> prefixes2= pref.fields();
		while(prefixes2.hasNext()) {
			Map.Entry<String,JsonNode> field = prefixes2.next();
			prefixes.add(field.getValue().textValue());
		}
		return prefixes;
	}
	
	
	public static JsonNode readJSONQuery(String qs) throws Exception, ParseException {
		 ObjectMapper mapper = new ObjectMapper();
		 JsonNode qo = mapper.readTree(qs);
		return qo;


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
	public static void main( String[] args ) throws Exception, ParseException  {
		
      String queryString = "experiment/input/query.json";
      String queryStr = new String(Files.readAllBytes(Paths.get(queryString)));
      QueryTranslator qt = new QueryTranslator(queryStr);
    
      //qt.parseJSONQuery(m);
      //qt.printTriples();
      //qt.printRegexPattern();
qt.printFilterRegex();
      //qs.executeQuery(m,"<http://purl.org/sepses/vocab/log/authLog#userName>");
      //qs.printPrefixes();
    
     
     

  }
	

}

		




