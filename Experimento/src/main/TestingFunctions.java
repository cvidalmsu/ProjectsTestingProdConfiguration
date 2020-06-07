package main;
//
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.RunnableFuture;

import choco.kernel.model.constraints.Constraint;
import es.us.isa.Choco.fmdiag.ChocoExplainErrorFMDIAG;
import es.us.isa.Choco.fmdiag.ChocoExplainErrorFMDIAGMut1;
import es.us.isa.Choco.fmdiag.ChocoExplainErrorFMDIAGMut2;
import es.us.isa.Choco.fmdiag.ChocoExplainErrorFMDIAGMut3;
import es.us.isa.Choco.fmdiag.ChocoExplainErrorFMDIAGMut4;
import es.us.isa.Choco.fmdiag.ChocoExplainErrorQXPlain;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.FAMA.Exceptions.FAMAException;
import es.us.isa.FAMA.Reasoner.Question;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;
import helpers.ProductManager;
import testing.FDTesting;
import testing.QXTesting;

public class TestingFunctions {
	public static Collection<String[]> getTestData(String fileName) throws IOException{
		ArrayList<String[]> records = new ArrayList<String[]>();
		
		String record;
		BufferedReader file = new BufferedReader(new FileReader(fileName));
		while ((record = file.readLine())!=null){
			String fields[]=record.split(",");
//			System.out.println(fields[0] + "," + fields[1]);
			records.add(fields);
		}
		
		String fields[] = records.get(0);
		
//		System.out.println(fields[0] + "," + fields[1]);
		file.close();
		return records;
	}
	
	static long MaxTimeQX = 0, MaxTimeFD = 0;

	public static void main(String[] args) throws WrongFormatException, IOException, FAMAException, InterruptedException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
		String fileName = args[0];
		
		ArrayList<String[]> pruebas = new ArrayList<String[]>(getTestData(fileName));		
		Set<String>[] setConflicts = new HashSet[5];
		Set<String>[] setDiagnosis = new HashSet[5];
			
		Integer[] killed = new Integer[] {0, 0, 0, 0, 0}; Integer nKilled = 0;

		Integer[] alived = new Integer[] {0, 0, 0, 0, 0}; Integer nAlived = 0;

		////////////////	      
		for(int i=0; i < pruebas.size(); i++) {
			String fields[] = pruebas.get(i);
			
			if (args[1].equals("QX")) {
				for(int j=0; j<=4; j++) {
					setConflicts[j] = new HashSet<String>(conflictTask(fields[0], fields[1], j));
					
					if (j > 0) {
						if (setConflicts[j].isEmpty() || (!setConflicts[j].containsAll(setConflicts[0]) || !setConflicts[0].containsAll(setConflicts[j]))) {
							killed[j]++;
							nKilled++;
						}else {
							alived[j]++;
							nAlived++;
						}
					}					
				}
			}else {
				for(int j=0; j<=4; j++) {
					setDiagnosis[j] = new HashSet<String>(diagnosisTask(fields[0], fields[1], j));

					if (j > 0) {
						if (setDiagnosis[j].isEmpty() || (!setDiagnosis[j].containsAll(setDiagnosis[0]) || !setDiagnosis[0].containsAll(setDiagnosis[j]))) {
							killed[j]++;
							nKilled++;
						}else {
							alived[j]++;
							nAlived++;
						}
					}					
				}				
			}
		}
	
		///////////////////Comparación Resultados
		System.out.println("Total Killed " + nKilled + " - Total Alived " + nAlived);

		for(int i=1;i < 5;i ++) {
			System.out.println("Alived[" + i + "]: " + alived[i] + " - Killed[" + i + "]: " + killed[i]);
		}
	}
	
	public static Set<String> conflictTask(String model, String prod, int op) throws InterruptedException {
		ForkJoinPool pool = new ForkJoinPool();

		QXTesting qx = new QXTesting(model, prod, op);
		long start = System.currentTimeMillis();	
		Set<String>conf = new HashSet<String>();
		pool.execute(qx);
		
		if (op==0) {
			long end = System.currentTimeMillis();
			conf = qx.join();
		    MaxTimeQX = (end-start) * 2;
			
		}else {
			Thread.sleep(MaxTimeQX);

			if (qx.isDone()) {
				conf = qx.join();
		//		System.out.println("Finished qx" + op + ": " + conf);
			}else {
		//		System.out.println("No Finished qx" + op);
				
			}
		}
		
		pool.shutdown();
//		System.out.println(conf);
		return conf;
	}
	
	public static Set<String> diagnosisTask(String model, String prod, int op) throws InterruptedException {
		ForkJoinPool pool = new ForkJoinPool();

		FDTesting fd = new FDTesting(model, prod, op);
		long start = System.currentTimeMillis();
		Set<String> diag = new HashSet<String>();
		pool.execute(fd);
		
		if (op==0) {
			long end = System.currentTimeMillis();
			diag = fd.join();
		    MaxTimeFD = (end-start) * 2;
			
		}else {
			Thread.sleep(MaxTimeFD);

			if (fd.isDone()) {
				diag = fd.join();
			//	System.out.println("Finished fd" + op + ": " + diag);
			}else {
			//	System.out.println("No Finished fd" + op);
			}
		}
		
		pool.shutdown();
		return diag;
	}
}