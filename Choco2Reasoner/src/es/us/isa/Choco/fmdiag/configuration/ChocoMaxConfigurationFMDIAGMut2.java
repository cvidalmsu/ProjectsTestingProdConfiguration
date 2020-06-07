package es.us.isa.Choco.fmdiag.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import es.us.isa.ChocoReasoner.ChocoQuestion;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.ChocoReasoner.ChocoResult;
import es.us.isa.FAMA.Benchmarking.PerformanceResult;
import es.us.isa.FAMA.Exceptions.FAMAException;
import es.us.isa.FAMA.Reasoner.Reasoner;
import es.us.isa.FAMA.Reasoner.questions.ValidConfigurationErrorsQuestion;
import es.us.isa.FAMA.models.featureModel.GenericFeature;
import es.us.isa.FAMA.models.featureModel.Product;

public class ChocoMaxConfigurationFMDIAGMut2 extends ChocoQuestion implements ValidConfigurationErrorsQuestion {

	public boolean returnAllPossibeExplanations = false;
	private ChocoReasoner chReasoner;

	Map<String, Constraint> relations = null;
	public boolean flexactive = false;
	public int m = 1;

	Product s,r;
	public Map<String, Constraint> result = new HashMap<String, Constraint>();

	Integer nParticiones = 2;
	
	public void setConfiguration(Product s) {
		this.s=s;
	}

	public void setRequirement(Product r) {
		this.r=r;
	}
	
	@Override
	public void setProduct(Product p) {
		this.s = p;
	}

	@Override
	public boolean isValid() {

		return false;
	}

	public PerformanceResult answer(Reasoner r) throws FAMAException {

		ChocoResult res = new ChocoResult();
		chReasoner = (ChocoReasoner) r;
		// solve the problem y fmdiag
		relations = new HashMap<String, Constraint>();

		Map<String, Constraint> productConstraint = new HashMap<String, Constraint>();
		ArrayList<String> feats= new ArrayList<String>();
		for (GenericFeature f : this.s.getFeatures()) {
			IntegerVariable var = chReasoner.getVariables().get(f.getName());
			//System.out.println(var);
			String name="U_" + f.getName();
			productConstraint.put(name, Choco.eq(var, 1));
			feats.add(name);
		}

		Map<String, Constraint> requirementConstraint = new HashMap<String, Constraint>();
		for (GenericFeature f : this.r.getFeatures()) {
			IntegerVariable var = chReasoner.getVariables().get(f.getName());
			//System.out.println(var);
			requirementConstraint.put("R_" + f.getName(), Choco.eq(var, 1));
		}

		relations.putAll(chReasoner.getRelations());
		relations.putAll(requirementConstraint);
		relations.putAll(productConstraint);
		ArrayList<String> S = new ArrayList<String>(feats);
		//System.out.println("Order of S: "+S);
		ArrayList<String> AC = new ArrayList<String>(relations.keySet());
		//AC.addAll(productConstraint.keySet());

		if (returnAllPossibeExplanations == false) {

			List<String> fmdiag = fmdiag(S, AC);

			for (String s : fmdiag) {
				result.put(s, productConstraint.get(s));
			}

		} else {
			List<String> allExpl = new LinkedList<String>();
			List<String> fmdiag = fmdiag(S, AC);

			while (fmdiag.size() != 0) {
				allExpl.addAll(fmdiag);
				S.removeAll(fmdiag);
				AC.removeAll(fmdiag);
				fmdiag = fmdiag(S, AC);
			}
			for (String s : allExpl) {
				result.put(s, productConstraint.get(s));
			}
		}

		return new ChocoResult();

	}

	
	public List<String> fmdiag(List<String> S, List<String> AC) {
//		System.out.println("AC:     " + AC + " - " + isConsistent(AC));
//		System.out.println("(AC-S): " + less(AC,S) + " - " + isConsistent(less(AC,S)) + "\n" );

		
		if (S.size() == 0 || !isConsistent(less(AC, S)) || isConsistent(AC)){
			return new ArrayList<String>();
		} else {
			return diag(new ArrayList<String>(), S, AC);
		}
	}
	
	class resultado{
		List<String> d;
		int res;
		
		resultado(List<String> d, int res){
			this.d = d;
			this.res = res;
		}
	}
			
	public int diag1(List<String> D, List<String> P, List<String> AC) {	
//		System.out.println("{" + D + "}, {" + P + "}, {" + S + "}");
//		System.out.println("AC: " + AC + " - " + isConsistent(AC) + "\n");

		if (D.size() != 0 && isConsistent(less(AC, P))) {
			return 2;
		}
			
		if (D.size() != 0 && isConsistent(AC)) {
			return 1;
		}
		
		return 0;
	}

	public List<String> diag(List<String> D, List<String> S, List<String> AC) {		
//		System.out.println( D + " - " + S);
//		System.out.println("AC: " + AC + " - " + isConsistent(AC));

		if (D.size() != 0 && isConsistent(AC)) {
			return(new ArrayList<String>());
		}
		
		if (S.size() == 1) {
			return(S);
		}
	
		/* outList corresponds to a results list */
		List<String> outList = new ArrayList<String>();

		//// *DIVISION PHASE*////
		int div = 0; // div is the size of the partitions

		if (S.size() >= nParticiones) {
			div = S.size() / nParticiones;
			if ((S.size() % nParticiones) > 0) {
				div++;
			}
		} else
			div = 1;

		List<List<String>> splitListToSubLists = splitListToSubLists(S, div);
//		System.out.println(splitListToSubLists + "\n");
		
		List<List<String>> sPrevious = new ArrayList<List<String>>();		
		List<List<String>> Ss  = new ArrayList<List<String>>();
		List<List<String>> Ds  = new ArrayList<List<String>>();
		List<List<String>> ACs = new ArrayList<List<String>>();
			
		List<Integer> resS = new ArrayList<Integer>();
		
		int j = 0; 
		for (int i = splitListToSubLists.size() - 1; i >= 1; i--) {
		    /*particiones ya revisadas*/
			if (j==0)
		    	sPrevious.add(new ArrayList<String>()); 
		    else
			    sPrevious.add(plus(sPrevious.get(j-1), Ds.get(j-1)));
			
		    Ds.add(splitListToSubLists.get(i)); // Di: partición actual				    
		    Ss.add(getRest(Ds.get(j), splitListToSubLists)); // Si: elementos de particiones no revisadas
			ACs.add(less(AC, Ds.get(j))); //ACi

			resS.add(diag1(Ds.get(j), sPrevious.get(j), ACs.get(j)));							
			j++;
		}

	    sPrevious.add(plus(sPrevious.get(j-1), Ds.get(j-1)));
		
		///Búsqueda de partición fuente de diagnosis
		int origen = -1;
		
		for(j=0;j < resS.size(); j++){
			if (resS.get(j) > 0 && origen == -1){
				origen = j;
			}	
		}

		List<String> s1 = new ArrayList<String>(); 
		List<String> ac1 = new ArrayList<String>();
		
		if (origen == -1){
			origen = sPrevious.size()-1;
			resS.add(1);
			s1 = splitListToSubLists.get(0);
			ac1 = less(AC, sPrevious.get(origen)); ///AC menos los ya revisados
		}else if (resS.get(origen)==1){
			s1 = Ds.get(origen);
			ac1 = plus(ACs.get(origen), s1);
		}else if (resS.get(origen)==2){
			s1 = plus(Ds.get(origen), sPrevious.get(origen));
			ac1 = plus(ACs.get(origen), s1);
		}
		
		List<String> r1 = diag(new ArrayList<String>(), s1, ac1);	
		List<String> r2 = new ArrayList<String>();
		
		if (sPrevious.get(origen).size()>0 && resS.get(origen)== 1){	
		   r2 = diag(r1, sPrevious.get(origen), less(plus(ac1, sPrevious.get(origen)), r1));
		}
		
		return plus(r1, r2);
	}


	private List<String> getRest(List<String> s2, List<List<String>> splitListToSubLists) {
		List<String> res = new ArrayList<String>();

		for (List<String> c : splitListToSubLists) {
			if (c != s2) {
				res.addAll(c);
			}
		}
		
		return res;
	}

	public <T> List<List<T>> splitListToSubLists(List<T> parentList, int subListSize){
		List<List<T>> subLists = new ArrayList<List<T>>();

		if (subListSize > parentList.size()) {
			subLists.add(parentList);
		} else {
			int remainingElements = parentList.size();
			int startIndex = 0;
			int endIndex = subListSize;
			do {
				List<T> subList = parentList.subList(startIndex, endIndex);
				subLists.add(new ArrayList<T>(subList));
				startIndex = endIndex;
				if (remainingElements - subListSize >= subListSize) {
					endIndex = startIndex + subListSize;
				} else {
					endIndex = startIndex + remainingElements - subList.size();
				}
				remainingElements -= subList.size();
			} while (remainingElements > 0);

		}
		return subLists;
	}

	private List<String> plus(List<String> a1, List<String> a2) {
		List<String> res = new ArrayList<String>();
		res.addAll(a1);
		res.addAll(a2);
		return res;
	}

	private List<String> less(List<String> aC, List<String> s2) {
		List<String> res = new ArrayList<String>();
		res.addAll(aC);
		res.removeAll(s2);
		return res;
	}

	private boolean isConsistent(Collection<String> aC) {
		Model p = new CPModel();
		p.addVariables(chReasoner.getVars());

		for (String rel : aC) {
			Constraint c = relations.get(rel);

			if (c == null) {
				System.out.println("Error");
			}
			p.addConstraint(c);
		}
		Solver s = new CPSolver();
		s.read(p);
		s.solve();
		return s.isFeasible();
	}
}
