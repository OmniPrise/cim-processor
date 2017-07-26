# ERCOT CIM Processor
The ERCOT  (Electric Eeliability Council of Texas) CIM Processor was implemented to perform analysis of the ERCOT CIM based wholesale power grid model.  Features include:

* Load and check validity of a given CIM file
* Detect Islands and Hidden Outages
  * Includes detection before and after applying outages
  * Applying contingencies is intended to be added in future
  * Hidden Outage is defined as a Sink which is de-energized as the result of an Outage, but not lised in the Outage file
* Produce "one-line" Substation diagrams
  * Includes creation of diagrams before or after applying outages
  * Applying contingencies is intended to be added in the future
  * Currently produces Graphviz DOT files which can be converted by Graphvis into SVG (Scalable Vector Graphics) or other formats
* Planned features not yet implemented
  * Producing PSSE Bus/Branch models from CIM
  * Producing Bus/Branch diagrams
  * GUI front end to choose CIM model and (optional) Outage file, perform analysis, and display one-line diagrams

The core system includes a CIM model parser and a number of analysis operations.  The current command line interface provides a simple mechanism for execution of the code against a single CIM file.

# More to Come
User documentation must be created.  Unit tests have to be developed.

Further, future plans include implementing a GUI, likely as a separate project and built around Eclipse GEF, that can be used to perform analyses and view diagrams.

# Running
ERCOT rules do not permit unauthorized access to CIM files, and therefore it is not possible to post an actual ERCOT CIM file for testing, though one of the steps to Unit Testing is to produce a few artificial CIM models from scratch.  However, assuming a CIM model file is available, and the code has been checked out and built, then an example use of the command line interface would be:

```bash
java -Xss64m -jar ../target/cim-processor-0.2.0-SNAPSHOT-jar-with-dependencies.jar -cimFile CIM_MMDDYYYY_Redacted.xml -substationDiagram SC > diagrams.txt
```

This would produce a processing report to diagrams.txt as well as a Graphvis DOT file named SC.dot.  The SC.dot file could then be processed by Graphvis.  For example:

```bash
neato -Tsvg < SC.dot > SC.svg
```

would use Graphvis' Neato layout tool to create an SVG file which could, in turn, be displayed using a Broswer like IE or Chrome.

Command line interface help is available:

```bash
java -jar ../target/cim-processor-0.2.0-SNAPSHOT-jar-with-dependencies.jar -help
```
