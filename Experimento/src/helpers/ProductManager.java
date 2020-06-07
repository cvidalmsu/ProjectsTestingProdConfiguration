package helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import choco.kernel.model.constraints.Constraint;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.ChocoReasoner.questions.ChocoValidConfigurationQuestion;
import es.us.isa.ChocoReasoner.questions.ChocoValidProductQuestion;
import es.us.isa.FAMA.Exceptions.FAMAException;
import es.us.isa.FAMA.models.FAMAfeatureModel.Dependency;
import es.us.isa.FAMA.models.FAMAfeatureModel.ExcludesDependency;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.Feature;
import es.us.isa.FAMA.models.featureModel.GenericFeature;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.VariabilityElement;
import es.us.isa.FAMA.stagedConfigManager.Configuration;

public class ProductManager {

	public Product generateProduct(FAMAFeatureModel fm, Integer inSize) throws FAMAException, InterruptedException {
		// Verificamos
		Integer size = inSize;
		if (size > fm.getFeaturesNumber()) {
			size = fm.getFeaturesNumber();
		}

		Product res = null;
		int tries = 0;
		while (res == null) {
			tries++;
			List<Feature> feats = (List<Feature>) fm.getFeatures();
			Collections.shuffle(feats);
			Product temp = new Product();
			for (Feature f : feats.subList(0, size)) {
				temp.addFeature(f);
			}
			ChocoReasoner reasoner = new ChocoReasoner();
			fm.transformTo(reasoner);
			ChocoValidProductQuestion vqp = new ChocoValidProductQuestion();
			vqp.setProduct(temp);
			reasoner.ask(vqp);
			if (!vqp.isValid()) {
				res = temp;
			}
			if (tries > 9) {
				res = new Product();
				System.err.println("Number of tries exceeded");
			}

		}
		return res;
	}

	private boolean isValidProduct(FAMAFeatureModel fm, Product p) throws FAMAException, InterruptedException {
		ChocoReasoner reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
		ChocoValidProductQuestion vqp = new ChocoValidProductQuestion();
		vqp.setProduct(p);
		reasoner.ask(vqp);
		return vqp.isValid();
	}

	public Product generateProductUsingExcludes(FAMAFeatureModel fm, Integer inSize) throws FAMAException, InterruptedException {
		// Verificamos
		Integer size = inSize;
		if (size > fm.getFeaturesNumber()) {
			size = fm.getFeaturesNumber();
		}
		Map<Feature, Set<Feature>> excludesPairs = getExcludesPairs(fm);
		System.out.println(new PrettyPrintingMap<Feature, Set<Feature>>(excludesPairs));

		Product res = null;
		int tries = 0;
		while (res == null) {
			tries++;
			List<Feature> feats = (List<Feature>) fm.getFeatures();
			Collections.shuffle(feats);
			Product temp = new Product();
			for (Feature f : feats.subList(0, size)) {
				temp.addFeature(f);
			}
			if (!isValidProduct(fm, temp)) {
				// Here we have a non-valid product. Now we introduce conflicts
				// based on exludes to enable more than one minimal diagnonsys
				Collection<Feature> maxConflictSet = getMaxConflictSet(excludesPairs);
				// remove max number of features and add the ones in the
				// conflict set
				if (temp.getNumberOfFeatures() >= maxConflictSet.size()) {
					Random r = new Random();
					for (int i = 0; i < maxConflictSet.size(); i++) {
						int todel = r.nextInt(temp.getFeatures().size());
						((List) temp.getFeatures()).remove(todel);
					}
				}
				for (Feature f : maxConflictSet) {
					if (!temp.getFeatures().contains(f)) {
						temp.addFeature(f);
					}
				}
				if (!isValidProduct(fm, temp)) {
					res = temp;
				}
			}
			if (tries > 9) {
				res = new Product();
				System.err.println("Number of tries exceeded");
			}

		}
		return res;
	}

	private Collection<Feature> getMaxConflictSet(Map<Feature, Set<Feature>> map) {
		ArrayList<Collection<Feature>> tmp = new ArrayList<Collection<Feature>>();
		for (Entry<Feature, Set<Feature>> e : map.entrySet()) {
			Collection<Feature> col = new ArrayList<Feature>();
			col.add(e.getKey());
			col.addAll(e.getValue());
			tmp.add(col);
		}
		int max = 0;
		for (Collection<Feature> c : tmp) {
			if (c.size() > max) {
				max = c.size();
			}
		}
		Iterator<Collection<Feature>> it = tmp.iterator();
		while (it.hasNext()) {
			Collection<Feature> c = it.next();
			if (c.size() < max) {
				it.remove();
			}
		}
		if (tmp.size() > 0) {
			// if there is only one max return that one; else, a random in the
			// max
			Random r = new Random();
			return tmp.get(r.nextInt(tmp.size()));
		} else {
			return new ArrayList<Feature>();
		}
	}

	private Map<Feature, Set<Feature>> getExcludesPairs(FAMAFeatureModel fm) {
		Map<Feature, Set<Feature>> res = new HashMap<Feature, Set<Feature>>();
		Iterator<Dependency> dependencies = fm.getDependencies();
		while (dependencies.hasNext()) {
			Dependency dep = dependencies.next();
			if (dep instanceof ExcludesDependency) {
				Feature origin = dep.getOrigin();
				Feature destination = dep.getDestination();
				if (!res.containsKey(origin)) {
					Set<Feature> subset = new HashSet<>();
					subset.add(destination);
					res.put(origin, subset);
				} else {
					Set<Feature> subset = res.get(origin);
					subset.add(destination);
				}

				if (!res.containsKey(destination)) {
					Set<Feature> subset = new HashSet<>();
					subset.add(origin);
					res.put(destination, subset);
				} else {
					Set<Feature> subset = res.get(destination);
					subset.add(origin);
				}

			}
		}
		return res;
	}

	
//	public Map<Configuration, String> generateConfiguration(FAMAFeatureModel fm, Integer[] percentages, int num,String partialPath) {
//	    Map<Configuration, String> result= new HashMap<Configuration,String>();
//	    
//	    Configuration maxconfiguration = new Configuration();
//		for(Feature f:fm.getFeatures()) {
//			maxconfiguration.addElement(f, 1);
//		}
//		ChocoExplainErrorFMDIAG cop = new ChocoExplainErrorFMDIAG();
//		cop.configuration=maxconfiguration;
//		ChocoReasoner reasoner = new ChocoReasoner();
//		fm.transformTo(reasoner);
//		reasoner.ask(cop);
//		//elimino las que ya están asignadas
//
//		maxconfiguration = new Configuration();
//		for(Feature f:fm.getFeatures()) {
//			if(!cop.result.containsKey("C_"+f.getName())) {
//				maxconfiguration.addElement(f, 1);	
//			}
//			
//		}
//		
//
//	    for(Integer p:percentages) {
//	    	for(int i=0;i<num;i++) {
//	    		List<VariabilityElement> feats = new LinkedList<VariabilityElement>();
//	    		feats.addAll(maxconfiguration.getElements().keySet());
//	    		Collections.shuffle(feats);
//	    		
//	    		Integer size=(fm.getFeaturesNumber()*p)/100;
//	    		if (size > feats.size()) {
//	    			size = feats.size();
//	    		}
//	    		Configuration res= new Configuration();
//	    		for (VariabilityElement f : feats.subList(0, size)) {
//	    			res.addElement(f, 1);
//	    		}
//	    		result.put(res, partialPath.replaceAll(".xml", "")+"-"+p+"-"+i+".prod");
//	    	}
//	    	
//	    }
//	    return result;
//	}
	
//	public Configuration generateConfiguration(FAMAFeatureModel fm, Integer size) {
//
//		Configuration res = new Configuration();
//
//		//create a configuration with all selected features
//		Configuration configuration = new Configuration();
//		for(Feature f:fm.getFeatures()) {
//			configuration.addElement(f, 1);
//		}
//		ChocoFMDiag cop = new ChocoFMDiag();
//		cop.configuration=configuration;
//		ChocoReasoner reasoner = new ChocoReasoner();
//		fm.transformTo(reasoner);
//		reasoner.ask(cop);
//		//elimino las que ya están asignadas
//
//		configuration = new Configuration();
//		for(Feature f:fm.getFeatures()) {
//			if(!cop.result.containsKey("C_"+f.getName())) {
//				configuration.addElement(f, 1);	
//			}
//			
//		}
//		
//		List<VariabilityElement> feats = new LinkedList<VariabilityElement>();
//		feats.addAll(configuration.getElements().keySet());
//		Collections.shuffle(feats);
//		if (size > feats.size()) {
//			size = feats.size();
//		}
//		for (VariabilityElement f : feats.subList(0, size)) {
//			res.addElement(f, 1);
//		}
//
//		reasoner = new ChocoReasoner();
//		fm.transformTo(reasoner);
//		ChocoValidConfigurationQuestion vqp = new ChocoValidConfigurationQuestion();
//		vqp.setConfiguration(res);
//		reasoner.ask(vqp);
//		if (!vqp.isValid()) {
//			throw new IllegalStateException("Cannot build a product with the desired amount of features");
//		}
//		return res;
//	}
//
//	public Product generateProduct(ChocoModel fm, Integer inSize) {
//		Integer size = inSize;
//		if (size > fm.variables.size()) {
//			size = fm.variables.size();
//		}
//
//		Product res = null;
//		int tries = 0;
//		while (res == null) {
//			tries++;
//			List<Integer> tmpProd = new LinkedList<>(fm.variables.keySet());
//			Collections.shuffle(tmpProd);
//			tmpProd = tmpProd.subList(0, size);
//
//			if (!fm.isValidProduct(tmpProd)) {
//				System.out.println("ping");
//				res = new Product();
//				for (Integer i : tmpProd) {
//					res.addFeature(new Feature(i + ""));
//				}
//			}
//			if (tries > 10) {
//				res = new Product();
//				System.err.println("Number of tries exceeded");
//			}
//
//		}
//		return res;
//	}

	public void saveProduct(Product p, String path) {
		try (PrintWriter out = new PrintWriter(path)) {
			out.println(p.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void saveShuffledProduct(Product p, String path) {
		try (PrintWriter out = new PrintWriter(path)) {
			Collections.shuffle((List<?>) p.getFeatures());
			out.println(p.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void saveProducts(Collection<Product> prods, String path) {
		try (PrintWriter out = new PrintWriter(path)) {
			for (Product p : prods) {
				out.println(p.toString());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public Collection<Product> readProducts(FAMAFeatureModel fm, String path) {
		Collection<Product> res = null;
		try {
			res = new LinkedList<Product>();
			File file = new File(path);
			
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				Product p = new Product();
				StringTokenizer tokenizer = new StringTokenizer(line, ";");
				while (tokenizer.hasMoreTokens()) {
					Feature feature = fm.searchFeatureByName(tokenizer.nextToken());
					p.addFeature(feature);
				}
				res.add(p);
			}
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	public Product readProduct(FAMAFeatureModel fm, String path) {
		List<Product> prods = (List<Product>) readProducts(fm, path);
		return prods.get(0);
	}

//	public Collection<Product> readProducts(ChocoModel fm, String path) {
//		Collection<Product> res = null;
//		try {
//			res = new LinkedList<Product>();
//			File file = new File(path);
//			FileReader fileReader = new FileReader(file);
//			BufferedReader bufferedReader = new BufferedReader(fileReader);
//			String line;
//			while ((line = bufferedReader.readLine()) != null) {
//				Product p = new Product();
//				StringTokenizer tokenizer = new StringTokenizer(line, ";");
//				while (tokenizer.hasMoreTokens()) {
//					Feature feature = new Feature(tokenizer.nextToken());
//					p.addFeature(feature);
//				}
//				res.add(p);
//			}
//			fileReader.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return res;
//	}

//	public Product readProduct(ChocoModel fm, String path) {
//		List<Product> prods = (List<Product>) readProducts(fm, path);
//		return prods.get(0);
//	}

//	public Product generateProductUsingExcludes(ChocoModel fm, int inSize) {
//		Integer size = inSize;
//		if (size > fm.variables.size()) {
//			size = fm.variables.size();
//		}
//
//		Product res = null;
//		int tries = 0;
//		while (res == null) {
//			tries++;
//			List<Integer> tmpProd = new LinkedList<>(fm.variables.keySet());
//			Collections.shuffle(tmpProd);
//			tmpProd = tmpProd.subList(0, size);
//
//			if (!fm.isValidProduct(tmpProd)) {
//
//				// add the new stuff for promoting the conflict set
//
//				Collection<Integer> maxConfictSet = fm.getMaxConfictSet();
//
//				// remove max number of features and add the ones in the
//				// conflict set
//				if (tmpProd.size() >= maxConfictSet.size()) {
//					Random r = new Random();
//					for (int i = 0; i < maxConfictSet.size(); i++) {
//						int todel = r.nextInt(tmpProd.size() - 1);
//						tmpProd.remove(todel);
//					}
//				}
//
//				for (Integer f : maxConfictSet) {
//					if (!tmpProd.contains(f)) {
//						tmpProd.add(f);
//					}
//				}
//				if (!fm.isValidProduct(tmpProd)) {
//					res = new Product();
//					for (Integer i : tmpProd) {
//						res.addFeature(new Feature(i + ""));
//					}
//				}
//			}
//			if (tries > 10) {
//				res = new Product();
//				System.err.println("Number of tries exceeded");
//			}
//
//		}
//		return res;
//
//	}

	
}
