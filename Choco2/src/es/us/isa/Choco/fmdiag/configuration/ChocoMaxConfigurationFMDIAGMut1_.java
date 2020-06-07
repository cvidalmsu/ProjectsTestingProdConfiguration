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

public class ChocoMaxConfigurationFMDIAGMut1_ extends ChocoQuestion implements ValidConfigurationErrorsQuestion {

	public boolean returnAllPossibeExplanations = false;
	private ChocoReasoner chReasoner;

	public Map<String, Constraint> relations = null;
	public boolean flexactive = false;
	public int m = 1;

	Product s,r;
	public Map<String, Constraint> result = new HashMap<String, Constraint>();

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

		relations.putAll(chReasoner.getRelations());
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

	public List<String> AC;

	public List<String> fmdiag(List<String> S, List<String> AC) {
//		System.out.println("AC:     " + AC + " - " + isConsistent(AC));
//		System.out.println("(AC-S): " + less(AC,S) + " - " + isConsistent(less(AC,S)) );

		this.AC = new ArrayList<String>(AC);
		
		if (S.size() == 0 || !isConsistent(less(AC, S)) || isConsistent(AC)){
			return new ArrayList<String>();
		} else {
			return diag(new ArrayList<String>(), S, AC);
		}
	}
	
	public List<String> diag(List<String> D, List<String> S, List<String> AC) {	
//		System.out.println( D + " - " + S);
//		System.out.println("AC: " + AC + " - " + isConsistent(AC) + "\n");

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
		List<String> A2 = diag(S1, S2, less(AC, S1));
	
		if (A1.isEmpty())
			A2 = diag(A1, S2, less(AC, A1));
		
		if (A2.isEmpty())
			A1 = diag(A2, S1, less(AC, A2));
		
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