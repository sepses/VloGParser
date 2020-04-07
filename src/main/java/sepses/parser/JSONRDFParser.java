package sepses.parser;

import com.taxonic.carml.engine.RmlMapper;
import com.taxonic.carml.logical_source_resolver.JsonPathResolver;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.rdf_mapper.Mapper;
import com.taxonic.carml.util.RmlMappingLoader;
import com.taxonic.carml.vocab.Rdf;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.nio.file.Paths;
import java.util.Set;

public class JSONRDFParser {
	private Set<TriplesMap> mapping;
	private RmlMapper mapper;

    /**
     * return turtle file name generated with caRML and RML mappings.
     *
     * @param xmlFileName
     * @param rmlFile
     * @return
     * @throws IOException
     */
	public JSONRDFParser(String rmlFile) {
		initParse(rmlFile);
	}
	
    public org.eclipse.rdf4j.model.Model Parse(String jsonString) throws IOException {
        
        // load RML file and all supporting functions
       
      //System.exit(0);
        
    			InputStream targetStream = new ByteArrayInputStream(jsonString.getBytes());
    	        this.mapper.bindInputStream(targetStream);
    	        
    	        org.eclipse.rdf4j.model.Model model = this.mapper.map(this.mapping);
    	        
    	        
    	        
    	        targetStream.close();

    	//         // create a temp file and return jena model
    	//         File file = File
    	//         file.deleteOnExit();
    	//         OutputStream tempOutput = new FileOutputStream(file);
    	//         Rio.write(model, tempOutput, RDFFormat.TURTLE); // write mapping
    	//         model.clear();
    	//         tempOutput.flush();
    	//         tempOutput.close();

    	//         // create jena model
    	//         Model jmodel = ModelFactory.createDefaultModel();
    	//         InputStream tempInput = new FileInputStream(file);
    	//         RDFDataMgr.read(jmodel, tempInput, Lang.TURTLE);
    	//        // jmodel.write(System.out);
    	//         tempInput.close();
    	        
		// return jmodel;
		return model;
    }

   public void initParse(String rmlFile) {
    	 this.mapping =
       		  RmlMappingLoader
       		    .build()
       		    .load(RDFFormat.TURTLE, Paths.get(rmlFile));
       
       this.mapper = RmlMapper.newBuilder().setLogicalSourceResolver(Rdf.Ql.JsonPath, new JsonPathResolver()).build();
     
	}

	
    
}

