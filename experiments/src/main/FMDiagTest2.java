package main;
//
import static org.junit.Assert.assertEquals;
import helpers.ProductManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import es.us.isa.Choco.fmdiag.ChocoExplainErrorFMDIAG;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.FAMA.Exceptions.FAMAException;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;
import es.us.isa.Sat4j.fmdiag.Sat4jExplainErrorFMDIAG;

import es.us.isa.Sat4j.fmdiag.Sat4jExplainErrorQuickXplain;
import es.us.isa.Sat4jReasoner.Sat4jReasoner;

@RunWith(value = Parameterized.class)
public class FMDiagTest2{
	private String modelPath;
	private String productPath;
	private String diagnosis;
		
	@Parameters
	public static Collection testData() throws IOException {
		return getTestData("TestSuiteDiag.csv");
	}
	
	public FMDiagTest2(String modelPath, String productPath, String res){
		this.modelPath = modelPath;
		this.productPath = productPath;
		this.diagnosis = res;
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
	public void testFastDiag() throws WrongFormatException, FAMAException, InterruptedException{
		Set<String> diagnosis_set0  = Diagnosis(modelPath, productPath);		
		String diagnosisF = String.join(";", diagnosis_set0);
		diagnosisF = diagnosisF.replaceAll("U_", "");
		assertEquals(diagnosis, diagnosisF);
	}

	private Set<String> Diagnosis(String modelPath, String productPath) throws WrongFormatException, FAMAException, InterruptedException{
		XMLReader reader = new XMLReader();
		ProductManager pman = new ProductManager();

		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelPath);
		Product prod = pman.readProduct(fm, productPath);

		ChocoReasoner reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);

		ChocoExplainErrorFMDIAG  fmdiag = new ChocoExplainErrorFMDIAG();	
		fmdiag.setConfiguration(prod);
		reasoner.ask(fmdiag);
	
		return fmdiag.result.keySet();					
	}
}