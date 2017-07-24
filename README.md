# ERCOT CIM Processor
The ERCOT  (Electric Eeliability Council of Texas) CIM Processor was implemented to perform analysis of the ERCOT CIM based wholesale power grid model.  Features include:

* Load and check validity of a given CIM file
* Detect Islands and Hidden Outages
  * Includes detection before and after applying outages
  * Applying contingencies is intended to be added in future
* Produce "one-line" Substation diagrams
  * Includes creation of diagrams before or after applying outages
  * Applying contingencies is intended to be added in the future
* Producing PSSE Bus/Branch models from CIM is intended to be added in fututre
* Producing Bus/Branch diagrams is intended to be added in futre

The core system includes a CIM model parser and a number of analysis operations.  The current command line interface provides a simple mechanism for execution of the code against a single CIM file.

# More to Come
User documentation must be created, and some minor code issues must be corrected.  Unit tests have to be developed.

Further, future plans include implementing a GUI, likely as a separate project and built around Eclipse GEF, that can be used to perform analyses and view diagrams.

# Running
ERCOT rules to not permit unauthorized access to CIM files, and therefore it is not possible to post an actual ERCOT CIM file for testing, though one of the steps to Unit Testing is to produce a few artificial CIM models.  However, assuming such a file is available, and the code has been checked out and built, then an example command would be:

```bash
java -Xss64m -jar ../target/cim-processor-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cimFile CIM_MMDDYYYY_Redacted.xml -substationDiagram SC > diagrams.txt
```

This would produce a processing report to diagrams.txt as well as a Graphvis DOT file named SC.gv which could then be processed by Graphvis.  For example the command:

```bash
neato -Tsvg < SC.gv > SC.svg
```

would use Graphvis' Neato layout tool to create an SVG (Scalable Vector Graphics) file which could, in turn, be displayed using a Broswer like IE or Chrome.

Run help is available:

```bash
java -jar ../target/cim-processor-0.0.1-SNAPSHOT-jar-with-dependencies.jar -help
```
