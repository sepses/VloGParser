package sepses.ondemand_extractor;

import java.io.ByteArrayOutputStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;


/**
 * @author KKurniawan
 *
 */
public class JenaQueryEngine {
	private Model model;
	private String queryString;

	
	public JenaQueryEngine(String queryString, Model model){
		this.model = model;
		this.queryString = queryString;
		
	}

		
	public  String executeQuery() {
		String queryResults=null;
		Query query = QueryFactory.create(queryString);
			if(query.isSelectType()){
			try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
				ResultSet result = qexec.execSelect();
				// write to a ByteArrayOutputStream
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

				ResultSetFormatter.outputAsJSON(outputStream, result);

				// and turn that into a String
				queryResults = new String(outputStream.toByteArray());

				//System.out.println(json);
				 
//				while(result.hasNext()) {				
//					QuerySolution sol = result.nextSolution();
//					//System.out.println(sol);
//					if (queryResults==null) {
//						queryResults=sol.toString();
//					}else {
//						queryResults= queryResults+"\r\n"+sol.toString();
//					}
//				}
			}
		}else{
			try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
				Model result = qexec.execConstruct();
				//System.out.println(result);
				queryResults= result.toString();
			}
		}
		
		//remove all statements
		model.removeAll();
		return queryResults;
		
	}

	
}
