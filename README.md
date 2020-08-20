# VloGParser
Virtual Log Graph Parser is a simple log parser engine that receives and analyzes SPARQL queries sent by users and accordingly extracts raw log data from hosts, parses the extracted log data into an RDF representation and compress the resulting RDF data into the HDT format.

![ ](https://raw.githubusercontent.com/sepses/VloGParser/hdt-version/docs/querytranslationexample.png)<p align="center"> **Figure 1** SPARQL query translation Overview.

This engine runs several components:
- **Query Translator**, this component decomposes the SPARQL query to identify relevant elements for log source selection and log line matching (see Figure 1).
- **Log Extractor**, this component extract the selected raw log lines (identified by Query Translation) and split them into a key-value pair representation by using predefined regular expression pattern.
- **RDF Mapping**, the extracted log data are mapped and parsed into RDF representation.
- **RDF Compression**, this component compressed the generated RDF log data into a compact binary format of RDF.

This project can be setup by cloning and installing it as follows:

```bash
$ git clone https://github.com/sepses/VloGParser.git
$ cd VlogParser
$ mvn clean install
```

To configure which log sources to parse, please take a look at the configuration file (config.json).

```bash
  "logSources": [ 
     {
	      "title": "authlog",
	      "type": "File",
	      "logLocation": "experiment/logfile/auth.log", #log sources location
	      "mapping": "experiment/rml/authlog.rml", #rml mapping
	      "grokPattern": "%{SYSLOGBASE} %{GREEDYDATA:message}", #grok pattern
		  "outputModel": "experiment/output/authlog_model.ttl", #expected output name in rdf
		  "hdtOutput": "experiment/output/authlog.hdt", #expected output name in hdt
	      "namegraph": "http://w3id.org/sepses/graph/log/authlog", #expected graph name
	      "vocabulary":"https://w3id.org/sepses/vocab/log/authLog#", #respected vocabulary
	      "logDateFormat":"MMM d HH:mm:ss", #log timestamp format
	      "logTimeRegex":"\\w+\\s+\\d+\\s\\d{2}:\\d{2}:\\d{2}" #regex for log timestamp...   
		}..
```





