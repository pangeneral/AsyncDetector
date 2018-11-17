package cn.ac.ios.ad.summary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.ac.ios.ad.AsyncTaskDetector;
import cn.ac.ios.ad.summary.analysis.CommonJimpleAnalysis;
import cn.ac.ios.ad.summary.topology.DoInBackgroundTopologyOperation;
import cn.ac.ios.ad.summary.topology.TopologyOperation;
import cn.ac.ios.ad.util.ClassInheritanceProcess;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.GotoStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIfStmt;
import soot.toolkits.graph.UnitGraph;

public class DoInBackgroundMethodSummary extends AbstractMethodSummary {
	
	public DoInBackgroundMethodSummary(SootMethod methodUnderAnalysis) {
		super(methodUnderAnalysis);
		mLoopStartUnits = new ArrayList<>();
		mCancelledUnits = new ArrayList<>();
	}
	
	private List<Unit> mLoopStartUnits = null;

	private List<Unit> mCancelledUnits = null;

	public List<Unit> getLoopStartUnits() {
		return mLoopStartUnits;
	}

	public List<Unit> getCancelledUnits() {
		return mCancelledUnits;
	}

	public boolean isAllLoopCancelled() {
		return mCancelledUnits.size() >= mLoopStartUnits.size();
	}

	private boolean searchPath(Unit subStart, Unit start,
			Map<Unit, Boolean> unitMap, UnitGraph unitGraph,
			List<Unit> searchedUnits) {
		if (unitMap.containsKey(subStart)) {
			return unitMap.get(subStart);
		}
		if (searchedUnits.contains(subStart)) {
			return true;
		}
		searchedUnits.add(subStart);
		List<Unit> units = unitGraph.getSuccsOf(subStart);
		if (units.size() > 0) {
			if (units.contains(start)) {
				unitMap.put(subStart, true);
				return true;
			} else {
				for (Unit unit : units) {
//					if (start instanceof JIfStmt) {
//						if (unit.equals(((JIfStmt) start).getTarget())) {
//							unitMap.put(unit, false);
//							continue;
//						}
//					}
					boolean has = searchPath(unit, start, unitMap, unitGraph,
							searchedUnits);
					if (has) {
						unitMap.put(subStart, has);
					}
				}
				if (unitMap.containsKey(subStart)) {
					return true;
				} else {
					unitMap.put(subStart, false);
					return false;
				}
			}

		} else {
			unitMap.put(subStart, false);
			return false;
		}
	}
	

	@Override
	public void printMethodSummary() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void generation(UnitGraph unitGraph) {
		// TODO Auto-generated method stub
		List<Unit> searchedUnits = new ArrayList<>();
		Set<Unit> gotoStmtTargetUnit = new HashSet<>();
		CommonJimpleAnalysis ja = new CommonJimpleAnalysis();
		
		
		for (Unit unit : unitGraph) {
			
			InvokeExpr theExpr = ja.getInvokeExprOfCurrentStmt((Stmt)unit);
			//Process the summary of invoked method
			if( theExpr != null && AsyncTaskDetector.methodSignatureToBody.get(theExpr.getMethod().getSignature()) != null){
				String key = DoInBackgroundTopologyOperation.getDoInBackgroundKey(theExpr.getMethod());
				DoInBackgroundMethodSummary currentMethodSummary = (DoInBackgroundMethodSummary)TopologyOperation.methodKeyToSummary.get(key);
				if( currentMethodSummary != null ){
					this.mLoopStartUnits.addAll(currentMethodSummary.mLoopStartUnits);
					this.mCancelledUnits.addAll(currentMethodSummary.mCancelledUnits);
				}
			}
			
			if (unit instanceof JAssignStmt) {
				JAssignStmt jAssignStmt = (JAssignStmt) unit;
				if (jAssignStmt.containsInvokeExpr()) {
					InvokeExpr invokeExpr = jAssignStmt.getInvokeExpr();

					SootMethod sootMethod = invokeExpr.getMethod();
					if (ClassInheritanceProcess.isInheritedFromAsyncTask(sootMethod.getDeclaringClass()) 
							&& sootMethod.getName().contains("isCancelled")) {
						// cancelledCount++;
						this.mCancelledUnits.add(unit);
					}
				}
			}

			searchedUnits.add(unit);
			Unit targetUnit = null;

			// Log.i("Unit=", unit);
			// List<Unit> getSuccsOf = unitGraph.getSuccsOf(unit);
			// Log.i("Unit.getSuccsOf=", getSuccsOf.size(), getSuccsOf);

			if (unit instanceof JIfStmt) {
				targetUnit = ((JIfStmt) unit).getTarget();
			}
			if (unit instanceof GotoStmt) {
				targetUnit = ((GotoStmt) unit).getTarget();
				gotoStmtTargetUnit.add(targetUnit);
			}else{
				continue;
			}

			if (targetUnit == null || !searchedUnits.contains(targetUnit) ) {
				continue;
			}

			// if (!exitPath(targetUnit, unit, unitGraph, new HashSet<>())) {
			// continue;
			// }

			List<Unit> units = new ArrayList<>();

			Map<Unit, Boolean> map = new HashMap<Unit, Boolean>();

			searchPath(targetUnit, targetUnit, map, unitGraph,new ArrayList<Unit>());

			for (Entry<Unit, Boolean> entry : map.entrySet()) {
				if (entry.getValue()) {
					units.add(entry.getKey());
				}
			}

			if (units == null || units.size() < 1) {
				continue;
			}
			this.mLoopStartUnits.add(targetUnit);
		}

		// Log.i("LoopUnitNew", loopCount, cancelledCount);
	}

}
