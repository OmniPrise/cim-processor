# ERCOT CIM Processor
The ERCOT CIM Processor was implemented to perform analysis of the ERCOT CIM based wholesale power grid model.  Features include:

* Load and check validity of a given CIM file
* Detect Islands and Hidden Outages
  * Includes detection after applying outages
  * Applying contingencies is intended to be added in future
* Produce "one-line" Substation diagrams
  * Includes creation of diagrams after applying outages
  * Applying contingencies is intended to be added in the future
* Producing PSSE Bus/Branch models from CIM is intended to be added in fututre

# More to Come
For now this is a placeholder.  Existing code must be updated with copyright and licensing, user documentation must be created, and some minor code issues must be corrected.

# Running
ERCOT rules to not permit unauthorized access to CIM files, and therefore it is not possible to post an actual ERCOT CIM file for testing.  However, assuming such a file is available, and the code has been checked out and built, then an example command would be:

```bash
java -Xss64m -jar ../target/cim-processor-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cimFile CIM_May_ML4_1_05312017_Redacted.xml -substationDiagram SC > diagrams.txt
```

Run help is available:

```bash
java -jar ../target/cim-processor-0.0.1-SNAPSHOT-jar-with-dependencies.jar -help
```