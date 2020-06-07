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

public class QXTesting extends RecursiveTask<Set<String>> {
	String modelPath;
	String productPath;
	int op;
	
	public QXTesting(String modelPath, String productPath, int op) {
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
			ChocoExplainErrorQXPlain qxplain0 = new ChocoExplainErrorQXPlain();	
			qxplain0.setConfiguration(prod);
			try {
				reasoner.ask(qxplain0);
			} catch (FAMAException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return qxplain0.result.keySet();		
			
		}else if (op==1){
			ChocoExplainErrorQXPlainMut1 qxplain1 = new ChocoExplainErrorQXPlainMut1();	
			qxplain1.setConfiguration(prod);
			try {
				reasoner.ask(qxplain1);
			} catch (FAMAException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return qxplain1.result.keySet();								
			
		}else if (op==2){
			ChocoExplainErrorQXPlainMut2 qxplain2 = new ChocoExplainErrorQXPlainMut2();	
			qxplain2.setConfiguration(prod);
			try {
				reasoner.ask(qxplain2);
			} catch (FAMAException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return qxplain2.result.keySet();								

		}else if (op==3){
			ChocoExplainErrorQXPlainMut3 qxplain3 = new ChocoExplainErrorQXPlainMut3();	
			qxplain3.setConfiguration(prod);
			try {
				reasoner.ask(qxplain3);
			} catch (FAMAException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return qxplain3.result.keySet();		
			
		}else if (op==4){
			ChocoExplainErrorQXPlainMut4 qxplain4 = new ChocoExplainErrorQXPlainMut4();	
			qxplain4.setConfiguration(prod);
			try {
				reasoner.ask(qxplain4);
			} catch (FAMAException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return qxplain4.result.keySet();								
		}else
			return null;
	}

}
