package cn.ac.ios.ad.summary.alphabet;

import soot.SootMethod;
import soot.Unit;

/**
 * Summary of unit or statement must at least include a string which indicate its operation 
 * @author panlj
 *
 */
public abstract class AbstractUnitSummary{
	
	private SootMethod currentMethod;
	private Unit currentUnit;
	
	public SootMethod getCurrentMethod() {
		return currentMethod;
	}

	public void setCurrentMethod(SootMethod currentMethod) {
		this.currentMethod = currentMethod;
	}

	public Unit getUnit(){
		return currentUnit;
	}
	
	public AbstractUnitSummary(Unit currentUnit){
		this.currentUnit = currentUnit;
	}
	
	public abstract String getSummary();
}
