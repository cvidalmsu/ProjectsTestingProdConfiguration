/*
	This file is part of FaMaTS.

    FaMaTS is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FaMaTS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with FaMaTS.  If not, see <http://www.gnu.org/licenses/>.

 */
package es.us.isa.ChocoReasoner.questions;

import choco.Choco;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import es.us.isa.ChocoReasoner.ChocoQuestion;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.ChocoReasoner.ChocoResult;
import es.us.isa.FAMA.Benchmarking.PerformanceResult;
import es.us.isa.FAMA.Reasoner.Reasoner;
import es.us.isa.FAMA.Reasoner.questions.OneProductQuestion;
import es.us.isa.FAMA.models.featureModel.GenericFeature;
import es.us.isa.FAMA.models.featureModel.Product;

public class ChocoOneProductQuestion extends ChocoQuestion implements
		OneProductQuestion {

	private Product prod;

	public ChocoOneProductQuestion() {
	}

	public void preAnswer(Reasoner r) {
		prod = new Product();
		;
	}

	public PerformanceResult answer(Reasoner choco) {

		ChocoReasoner r = (ChocoReasoner) choco;
		ChocoResult res = new ChocoResult();
		Model chocoProblem = r.getProblem();
		Solver solver = new CPSolver();


		IntegerVariable suma = Choco
				.makeIntVar("_suma", 0, r.getVariables().values().size());
		IntegerExpressionVariable sumatorio = Choco.sum(r.getVars());
		Constraint sumReifieds = Choco.eq(suma, sumatorio);

		
		chocoProblem.addConstraint(sumReifieds);

		solver.read(chocoProblem);
		Boolean solution=false;

		try {
			solver.propagate();
			solution = true;
		} catch (ContradictionException e1) {
			//e1.printStackTrace();
		}
		
		if (solution){
			IntDomainVar maxVar = solver.getVar(suma);
			solver.minimize(maxVar, true);
			// Buscamos el minimo valor de la suma. es la misma chapuza de explain
			// errors :S
			Solver sol2 = new CPSolver();
			Constraint cons2 = Choco.eq(suma, solver.getVar(suma).getVal());
			chocoProblem.addConstraint(cons2);
	
			sol2.read(chocoProblem);
			
			try {
				sol2.propagate();
				solution=true;
			} catch (ContradictionException e1) {
				e1.printStackTrace();
			}
			// Obtener todo los valores que tengan ese valor
			if (sol2.solve() == Boolean.TRUE && sol2.isFeasible()) {
					 prod = new Product();
					for (int i = 0; i < chocoProblem.getNbIntVars(); i++) {
						IntDomainVar aux = sol2.getVar(chocoProblem.getIntVar(i));
						if (aux.getVal() > 0) {
							GenericFeature f = getFeature(aux, r);
							if (f != null) {
								prod.addFeature(f);
							}
						}
					}
				
			}
			
			if (solution)
				res.fillFields(solver);
			else
				res=null;
		}else
			res = null;
		
		return res;
	}

	private GenericFeature getFeature(IntDomainVar aux, ChocoReasoner reasoner) {
		String temp = new String(aux.toString().substring(0,
				aux.toString().indexOf(":")));
		GenericFeature f = reasoner.searchFeatureByName(temp);
		return f;
	}

	public Product getProduct() {
		return prod;
	}

}
