package es.us.isa.Choco.fmdiag;
//aaaa
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

public class ChocoExplainErrorFMDIAG extends ChocoQuestion implements ValidConfigurationErrorsQuestion {

	public boolean returnAllPossibeExplanations = false;
	private ChocoReasoner chReasoner;

	Map<String, Constraint> relations = null;
	public boolean flexactive = false;
	public int m = 1;

	Product s,r;
	public Map<String, Constraint> result = new HashMap<String, Constraint>();
	private Map<String, Constraint> deselectedConstraints = new HashMap<String, Constraint>();
	private Map<String, Constraint> productConstraint = new HashMap<String, Constraint>();

	public void setConfiguration(Product s) {
		this.s=s;
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
		ArrayList<String> feats= new ArrayList<String>();
		
		for (GenericFeature f : this.s.getFeatures()) {			
			IntegerVariable var = chReasoner.getVariables().get(f.getName());
			//System.out.println(var);
			String name="U_" + f.getName();

			productConstraint.put(name, Choco.eq(var, 1));
			feats.add(name);
		}
//		System.out.println(productConstraint.keySet());

		for (GenericFeature f : chReasoner.getAllFeatures()) {
			IntegerVariable var = chReasoner.getVariables().get(f.getName());
			//System.out.println(var);
			String name="U_" + f.getName();
			//String name=f.getName();

			if (!s.getFeatures().contains(f))
				deselectedConstraints.put(name, Choco.eq(var, 0));			
		}
		
		productConstraint.putAll(deselectedConstraints);
		feats.addAll(deselectedConstraints.keySet());
		//		System.out.println(productConstraint.keySet());
		
		relations.putAll(chReasoner.getRelations());
		relations.putAll(productConstraint);
				
		ArrayList<String> S = new ArrayList<String>(feats);
		//System.out.println("Order of S: "+S);
		ArrayList<String> AC = new ArrayList<String>(relations.keySet());
		//AC.addAll(productConstraint.keySet());

//		System.out.println(AC + " - " + S);

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

	List<String> errorList = new ArrayList<String>();
	public List<String> fmdiag(List<String> S, List<String> AC) {
		//System.out.println("AC: " + AC + " - " + isConsistent(AC));
		
		if (S.size() == 0 || !isConsistent(less(AC, S)) || isConsistent(AC)) {
			errorList.add("NoDiagnosis");
			return errorList;
		}
		else {
			return diag(new ArrayList<String>(), S, AC);
		}
	}
	
	public List<String> diag(List<String> D, List<String> S, List<String> AC) {
		
		if (D.size() != 0 && isConsistent(AC)) {
			return new ArrayList<String>();
		}

		if (S.size() == 1) {
			return S;
		}
		
		int k = S.size() / 2;
		List<String> S1 = S.subList(0, k);
		List<String> S2 = S.subList(k, S.size());
				
		List<String> A1 = diag(S2, S1, less(AC, S2));
		List<String> A2 = diag(A1, S2, less(AC, A1));
		
		return plus(A1, A2);
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
