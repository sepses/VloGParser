# VloGParser
Virtual Log Graph Parser is a simple log parser engine that receives and analyzes SPARQL queries sent by users and accordingly extracts raw log data from hosts, parses the extracted log data into an RDF representation and compress the resulting RDF data into the HDT format.

![ ](https://raw.githubusercontent.com/sepses/VloGParser/hdt-version/docs/querytranslationexample.png)<p align="center">**Figure 1** SPARQL query translation Overview.</p>

This engine run several several components:
- **Query Translator**, a part of Log Parser and decomposes the SPARQL query to identify relevant elements for log source selection and log line matching (see Figure 1).
- **Log Extractor**, this component extract the selected raw log lines (identified by Query Translation) and split them into a key-value pair representation by using predefined regular expression pattern.
- **RDF Mapping**, the extracted log data are mapped and parsed into RDF representation.
- **RDF Compression**, this component compressed the generated RDF log data into a compact binary format of RDF.

This project can be setup by cloning and installing it as follows:

```bash
$ git clone https://github.com/sepses/VloGParser.git
$ cd VlogParser
$ mvn clean install
```

