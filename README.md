# VloGParser
Virtual Log Graph Parser is a simple log parser engine that receives and analyzes SPARQL queries sent by users via <a target="_blank" href="https://github.com/sepses/VloGraphQueryProcessor"> Query Processor</a> and accordingly extracts raw log data from hosts, parses the extracted log data into an RDF representation and compress the resulting RDF data into the HDT format.

![ ](https://raw.githubusercontent.com/sepses/VloGParser/hdt-version/docs/querytranslationexample.png)<p align="center"> **Figure 1** SPARQL query translation Overview.

This engine runs several components:
- **Query Translator**, this component decomposes the SPARQL query to identify relevant properties for log source selection and log line matching (see Figure 1).
- **Log Extractor**, this component extract the selected raw log lines (identified by Query Translator) and split them into a key-value pair representation by using predefined regular expression pattern. We use <a target="_blank" href="https://github.com/logstash-plugins/logstash-patterns-core/blob/master/patterns/grok-patterns">Logstash Grok Patterns</a>, a collection of composeable regular expression patterns that can be reused forspecific log sources (e.g., auth-log, apache-log, snort-log)
- **RDF Mapping**, the extracted log data are mapped and parsed into RDF representation. We used <a target="_blank" href="https://github.com/carml/carml">CARML</a>, a Java-based tool that uses the <a target="_blank" href="https://rml.io/">RML</a> Mapping specification to parse JSON data into RDF representation.
- **RDF Compression**, this component compressed the generated RDF log data into a compact binary format of RDF. We used the <a target="_blank" href="http://www.rdfhdt.org/">HDT</a> library, a compact datastructure and binary serialization format for RDF that keeps big datasets compressed to save space while maintaining search and browse operations without prior decompression.	

## Configuration

The example data for experiment can be found at ./experiment directory. 

To configure which log sources to parse, please take a look at the configuration file (config.json).

```bash
  "logSources": [ 
     {
	      "title": "authlog",
	      "type": "File",
	      "logLocation": "experiment/logfile/auth.log", #log sources location
		  "logMeta": "experiment/logmeta/auth_meta.ttl",#log file metadata
	      "mapping": "experiment/rml/authlog.rml", #rml mapping
	      "outputModel": "experiment/output/authlog_model.ttl", #expected output name in rdf
		  "hdtOutput": "experiment/output/authlog.hdt", #expected output name in hdt
	      "namegraph": "http://w3id.org/sepses/graph/log/authlog", #expected graph name
	      "vocabulary":"https://w3id.org/sepses/vocab/log/authLog#", #respected vocabulary
		  "regexPattern": "experiment/pattern/regexPattern.ttl", #regex pattern definition
	      "logDateFormat":"MMM d HH:mm:ss", #log timestamp format
	      "logTimeRegex":"\\w+\\s+\\d+\\s\\d{2}:\\d{2}:\\d{2}" #regex for log timestamp...   
		}..
```


## Run the Code

This project can be setup by cloning and installing it as follows:

```bash
$ git clone https://github.com/sepses/VloGParser.git
$ cd VlogParser
$ mvn clean install
```

To run the compiled project: 

```bash
$ java -jar ./target/rest-service-0.0.1-SNAPSHOT.jar
```

## Evaluation Dataset

We evaluated the log parser against the [AIT log dataset](https://zenodo.org/record/3723083#.YDY5wehKg2x) that simulate six days user access accross multiple web servers. Specifically, we evaluated:

- Audit daemon from Linux,
- ApacheLog,
- EximLog,
- Syslog,
- Authlog

We performed evaluation with several parameters such as log parsing performance from a single-hosts, dynamic log graph generation (with respect to the log properties), graph compression and multiple hosts evaluation (2 to 7 hosts). The evaluation results can be found [here](https://github.com/sepses/VloGParser/tree/hdt-version/evaluation).



## License

The Virtual Log Graph Parser is written by [Kabul Kurniawan](https://kabulkurniawan.github.io/) released under the [MIT license](http://opensource.org/licenses/MIT).

