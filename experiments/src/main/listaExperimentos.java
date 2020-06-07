package main;
//
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import es.us.isa.Choco.fmdiag.ChocoExplainErrorFMDIAG;
import es.us.isa.Choco.fmdiag.ChocoExplainErrorQXPlain;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.FAMA.Exceptions.FAMAException;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;
import helpers.ProductManager;

public class listaExperimentos {    
	static Product prod = null;

	public static void main(String[] args) throws FAMAException, WrongFormatException, InterruptedException {
        final File folder = new File(args[0]);
        String op = args[1];
        List<String> resultFP = new ArrayList<>();
        List<String> resultRP = new ArrayList<>();
        
        try {
			search(folder, resultRP, resultFP, true);
		} catch (WrongFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        for (String s : resultRP) {
            String df = s.substring(0,s.length()-4);
            
            List<String> resultDFP = new ArrayList<>();
            List<String> resultDRP = new ArrayList<>();

            final File subfolder = new File(df); 
            
            try {
				search(subfolder, resultDRP, resultDFP, false);
			} catch (WrongFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 
            for (String ds : resultDRP) {
            	if (op.equals("Conf")) {
            		Set<String> conflicts_set0  = Conflicts(s, ds);	
            		String conflictsF = String.join(";", conflicts_set0);
            		conflictsF = conflictsF.replaceAll("U_", "");         		
//            		String prodString = prod.getElements().toString();
                    System.out.println(s + "," + ds + "," + conflictsF );
            	}else {
            		Set<String> diagnosis_set0  = Diagnosis(s, ds);		
            		String diagnosisF = String.join(";", diagnosis_set0);
            		diagnosisF = diagnosisF.replaceAll("U_", "");
 //           		String prodString = prod.getElements().toString();            		            		
                    System.out.println(s + "," + ds + "," + diagnosisF );
            	}            		
            }
        }

    }

	private static Set<String> Diagnosis(String modelPath, String productPath) throws WrongFormatException, FAMAException, InterruptedException{
		XMLReader reader = new XMLReader();
		ProductManager pman = new ProductManager();

		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelPath);
		prod = pman.readProduct(fm, productPath);
//		System.out.println(prod.getElements());
		
		ChocoReasoner reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
				
		ChocoExplainErrorFMDIAG fmdiag = new ChocoExplainErrorFMDIAG();
		fmdiag.setConfiguration(prod);
		reasoner.ask(fmdiag);
	
		return fmdiag.result.keySet();					
	}
	
	private static Set<String> Conflicts(String modelPath, String productPath) throws WrongFormatException, FAMAException, InterruptedException{
		XMLReader reader = new XMLReader();
		ProductManager pman = new ProductManager();

		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelPath);
	    prod = pman.readProduct(fm, productPath);

		ChocoReasoner reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
		
		ChocoExplainErrorQXPlain quickX = new ChocoExplainErrorQXPlain();	
		quickX.setConfiguration(prod);	
		reasoner.ask(quickX);
	
		return quickX.result.keySet();								
	}
	
    public static void search(final File folder, List<String> resultRP, List<String> resultFP, boolean op) throws WrongFormatException {
        for (final File f : folder.listFiles()) {

            if (f.isFile()) {   
            	if (op){
	            	/*Modelos entre min y max Caracter�sticas*/
	        		XMLReader reader = new XMLReader();        		
	        		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(f.getAbsolutePath());
	
            		resultFP.add(f.getAbsolutePath());
            		resultRP.add(f.getPath().replace("\\", "/"));
        	}
            	else{
//            		if (f.getName().contains("-50-") || f.getName().contains("-100-")){
            			resultFP.add(f.getAbsolutePath());
            			resultRP.add(f.getPath().replace("\\", "/"));
 //           		}
            	}
            }

        }
    }

}
