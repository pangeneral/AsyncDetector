package cn.ac.ios.ad.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import soot.Unit;
import soot.toolkits.graph.UnitGraph;

public class ControlFlowProcess {
	/**
	 * Generate all the possible control flow begin from currentUnit
	 * @param unitLists the list to save all the of all possible control flows
	 * @param currentUnit
	 * @param theGraph
	 * @param unitToIsVisit
	 * @param currentUnitList
	 */
	public static void generateAllControlFlow(List<List<Unit>> unitLists,Unit currentUnit,UnitGraph theGraph,Map<Unit,Boolean> unitToIsVisit,List<Unit> currentUnitList){
		assert(currentUnit.branches()||(!currentUnit.branches()&&theGraph.getSuccsOf(currentUnit).size()<=1));
		unitToIsVisit.put(currentUnit, true);
		currentUnitList.add(currentUnit);
		List<Unit> succeedUnits = theGraph.getSuccsOf(currentUnit);
		if( succeedUnits.size() == 0){//Current unit is the tail of the unit graph which means we find a new control flow
			List<Unit> newList = new ArrayList<Unit>();
			newList.addAll(currentUnitList);
			unitLists.add(newList);
		}
		for(Unit unit: succeedUnits){
			Boolean isVisit = unitToIsVisit.get(unit);
			if( isVisit != null && isVisit.booleanValue() )
				continue;
			generateAllControlFlow(unitLists,unit,theGraph,unitToIsVisit,currentUnitList);
		}
		unitToIsVisit.remove(currentUnit);
		currentUnitList.remove(currentUnitList.size()-1);
	}
}
