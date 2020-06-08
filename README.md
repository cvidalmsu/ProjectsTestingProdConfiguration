# FaMaProdConf TestSuite

This are the TestSuite projects for Testing Product Configuration in Feature Models in FaMa. Next, we describe projects experiments and Choco2Reasomer.

* experiments

This project containts files TestSuiteConf.csv and TestSuiteDiag.csv generated by ./src/main/listaExperimentos.java

In ./src/main: 
- ExecutorQXFD.java is used for executing QuickXplain or FastDiag on a defined (parameterized) experiment. 
- FMDiagTest.java is a test code for using JUnit or PIT Mutation Test using the TestSuiteConf.csv file applying QuickXplain.
- FMDiagTest.java is a test code for using JUnit or PIT Mutation Test using the TestSuiteDiag.csv file applying FastDiag.
- listaExperiments generates the TestSuiteConf.csv and TestSuiteDiag.csv using the TestSuite.
- TestingFunctions.java uses the ./src/testing/FDTesting.java and ./src/testing/QXTesting.java for executing the FastDiag and QXPlain mutants on the TestSuite.

* Choco2Reasoner

This contains the folders FMDiag and QXPlain for the FastDiag and QuickXplain solutions along each of their mutants.
