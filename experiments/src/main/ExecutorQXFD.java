package main;
//////

import java.io.File;
import java.io.IOException;

import es.us.isa.Choco.fmdiag.ChocoExplainErrorFMDIAG;
import es.us.isa.Choco.fmdiag.ChocoExplainErrorQXPlain;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.FAMA.Exceptions.FAMAException;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;
import es.us.isa.Sat4j.fmdiag.Sat4jExplainErrorQuickXplain;
import es.us.isa.Sat4jReasoner.Sat4jReasoner;
import helpers.ProductManager;


public class ExecutorQXFD {

   public static void main(String[] args) throws WrongFormatException, IOException, FAMAException, InterruptedException {
		String op = args[0];// flexdiag - prods - evolutionary
		String modelPath = args[1];
		String productPath="";
		
		try {
			productPath = args[2];        
		}catch(Exception ex) {}
		
		if (op.equals("DIAG")) {
			FastDiag(modelPath, productPath);
		}		
		
		if (op.equals("QUICK")){
			QuickXplain(modelPath, productPath);        
		}	
	}

	public static long FastDiag(String modelPath, String productPath) throws WrongFormatException, FAMAException, InterruptedException {
		XMLReader reader = new XMLReader();
		ProductManager pman = new ProductManager();

		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelPath);
		Product prod = pman.readProduct(fm, productPath);
//		System.out.println(prod.getFeatures());
		ChocoReasoner reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);

		long start=0, end=0;
				
		//ChocoMaxConfigurationFMDIAGMutX fmdiag = new ChocoMaxConfigurationFMDIAGMutX();
		ChocoExplainErrorFMDIAG fmdiag = new ChocoExplainErrorFMDIAG();
		fmdiag.setConfiguration(prod);
		
		start = System.currentTimeMillis();
		reasoner.ask(fmdiag);
		end = System.currentTimeMillis();
		long dif = (end-start);
		
		System.out.println(modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
				+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + 0 + "|"
				+ fm.getFeaturesNumber() + "|" + fm.getNumberOfDependencies() + "|" + prod.getElements() + "|" 
				+ reasoner.getVariables().size() + "|" + prod.getElements() + "|"
				+ reasoner.getPConstraintsNumber() + "|" + start + "|" + end + "|" + dif + "|" + fmdiag.result.keySet());

		return end-start;			
	}

	static long QuickXplain(String modelPath, String productPath) throws WrongFormatException, FAMAException, InterruptedException {
		XMLReader reader = new XMLReader();
		ProductManager pman = new ProductManager();

		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelPath);
		Product prod = pman.readProduct(fm, productPath);

		ChocoReasoner reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);

		long start=0, end=0;
			
		ChocoExplainErrorQXPlain qxplain = new ChocoExplainErrorQXPlain();	
		qxplain.setConfiguration(prod);
		
		start = System.currentTimeMillis();
		reasoner.ask(qxplain);
		end = System.currentTimeMillis();
		long dif = (end-start);
		
		System.out.println(modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
				+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + 1 + "|"
				+ fm.getFeaturesNumber() + "|" + fm.getNumberOfDependencies() + "|" + prod.getNumberOfElements() + "|" 
				+ reasoner.getVariables().size() + "|" + prod.getElements() + "|"
				+ reasoner.getVariablesSize() + "|" + start + "|" + end + "|" + dif + "|" + qxplain.result.keySet());

//			}
		
		return end-start;	
	}
}