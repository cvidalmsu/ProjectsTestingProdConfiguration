package main;
//
import static org.junit.Assert.assertEquals;
import helpers.ProductManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import es.us.isa.Choco.fmdiag.ChocoExplainErrorQXPlain;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.FAMA.Exceptions.FAMAException;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;

@RunWith(value = Parameterized.class)
public class FMDiagTest{
	private String modelPath;
	private String productPath;
	private String conflicts;	
	
	@Parameters
	public static Collection testData() throws IOException {
		return getTestData("TestSuiteConf.csv");
	}
	
	public FMDiagTest(String modelPath, String productPath, String res){
		this.modelPath = modelPath;
		this.productPath = productPath;
		this.conflicts = res;
	}

	public static Collection<String[]> getTestData(String fileName) throws IOException{
		ArrayList<String[]> records = new ArrayList<String[]>();
		
		String record;
		BufferedReader file = new BufferedReader(new FileReader(fileName));
		while ((record = file.readLine())!=null){
			String fields[]=record.split(",");
			records.add(fields);
		}
		
		file.close();
		return records;
	}
	
	@Test
	public void testQuickXplain() throws WrongFormatException, FAMAException, InterruptedException{
		//System.out.println(modelPath + " - " + productPath);
		Set<String> conflicts_set0  = Conflicts(modelPath, productPath);	
		String conflictsF = String.join(";", conflicts_set0);
		conflictsF = conflictsF.replaceAll("U_", "");
		assertEquals(conflicts, conflictsF);
	}
	
	private Set<String> Conflicts(String modelPath, String productPath) throws WrongFormatException, FAMAException, InterruptedException{
		XMLReader reader = new XMLReader();
		ProductManager pman = new ProductManager();

		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelPath);
		Product prod = pman.readProduct(fm, productPath);

		ChocoReasoner reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
		
		ChocoExplainErrorQXPlain quickX = new ChocoExplainErrorQXPlain();	
		quickX.setConfiguration(prod);
		reasoner.ask(quickX);
		
		return quickX.result.keySet();								
	}
}