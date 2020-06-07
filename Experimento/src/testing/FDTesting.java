package testing;
//  
import java.util.Set;
import java.util.concurrent.RecursiveTask;

import es.us.isa.Choco.fmdiag.ChocoExplainErrorFMDIAG;
import es.us.isa.Choco.fmdiag.ChocoExplainErrorFMDIAGMut1;
import es.us.isa.Choco.fmdiag.ChocoExplainErrorFMDIAGMut2;
import es.us.isa.Choco.fmdiag.ChocoExplainErrorFMDIAGMut3;
import es.us.isa.Choco.fmdiag.ChocoExplainErrorFMDIAGMut4;
import es.us.isa.Choco.fmdiag.ChocoExplainErrorQXPlain;
import es.us.isa.Choco.fmdiag.ChocoExplainErrorQXPlainMut1;
import es.us.isa.Choco.fmdiag.ChocoExplainErrorQXPlainMut2;
import es.us.isa.Choco.fmdiag.ChocoExplainErrorQXPlainMut3;
import es.us.isa.Choco.fmdiag.ChocoExplainErrorQXPlainMut4;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.FAMA.Exceptions.FAMAException;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;
import helpers.ProductManager;

public class FDTesting extends RecursiveTask<Set<String>> {
	String modelPath;
	String productPath;
	int op;
	
	public FDTesting(String modelPath, String productPath, int op) {
		this.modelPath = modelPath;
		this.productPath = productPath;
		this.op = op;
	}
	
	@Override
	protected Set<String> compute() {
		XMLReader reader = new XMLReader();
		ProductManager pman = new ProductManager();

		FAMAFeatureModel fm = null;
		try {
			fm = (FAMAFeatureModel) reader.parseFile(modelPath);
		} catch (WrongFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Product prod = pman.readProduct(fm, productPath);

		ChocoReasoner reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
				
		if (op==0) {
			ChocoExplainErrorFMDIAG fdiag0 = new ChocoExplainErrorFMDIAG();	
			fdiag0.setConfiguration(prod);
			try {
				reasoner.ask(fdiag0);
			} catch (FAMAException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return fdiag0.result.keySet();		
			
		}else if (op==1){
			ChocoExplainErrorFMDIAGMut1 fdiag1 = new ChocoExplainErrorFMDIAGMut1();	
			fdiag1.setConfiguration(prod);
			try {
				reasoner.ask(fdiag1);
			} catch (FAMAException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return fdiag1.result.keySet();								
			
		}else if (op==2){
			ChocoExplainErrorFMDIAGMut2 fdiag2 = new ChocoExplainErrorFMDIAGMut2();	
			fdiag2.setConfiguration(prod);
			try {
				reasoner.ask(fdiag2);
			} catch (FAMAException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return fdiag2.result.keySet();								

		}else if (op==3){
			ChocoExplainErrorFMDIAGMut3 fdiag3 = new ChocoExplainErrorFMDIAGMut3();	
			fdiag3.setConfiguration(prod);
			try {
				reasoner.ask(fdiag3);
			} catch (FAMAException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return fdiag3.result.keySet();		
			
		}else if (op==4){
			ChocoExplainErrorFMDIAGMut4 fdiag4 = new ChocoExplainErrorFMDIAGMut4();	
			fdiag4.setConfiguration(prod);
			try {
				reasoner.ask(fdiag4);
			} catch (FAMAException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return fdiag4.result.keySet();								
		}else
			return null;
	}
}
