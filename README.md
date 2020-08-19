# VloGParser
Virtual Log Graph Parser is a simple log parser engine that receives and analyzes SPARQL queries sent by users and accordingly extracts raw log data from hosts, parses the extracted log data into an RDF representation and compress the resulting RDF data intothe HDT format.

![ ](https://raw.githubusercontent.com/sepses/VloGParser/hdt-version/docs/querytranslationexample.png)
<p align="center">**Figure 1**. VlogGraph Parser Overview.</p>


This project can be setup by cloning and installing it as follows:

```bash
$ git clone https://github.com/sepses/VloGParser.git
$ cd VlogParser
$ mvn clean install
```