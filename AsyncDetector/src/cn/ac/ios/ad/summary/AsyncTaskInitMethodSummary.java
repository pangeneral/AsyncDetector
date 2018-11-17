package cn.ac.ios.ad.summary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.ac.ios.ad.AsyncTaskDetector;
import cn.ac.ios.ad.summary.analysis.InitMethodJimpleAnalysis;
import cn.ac.ios.ad.summary.topology.InitMethodTopologyOperation;
import cn.ac.ios.ad.summary.topology.TopologyOperation;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;

/**
 * The summary of init method of AsyncTask.
 * @author panlj
 *
 */
public class AsyncTaskInitMethodSummary extends AbstractMethodSummary{
	
	/**
	 * Key is the index of dirty arguments in the init method of AsyncTask.
	 * Value is the field of AsyncTask which holds the reference to the dirty argument
	 * Here, dirty arguments are those that pass the reference of Activity object to AsyncTask object.
	 * If dirtyArgToTaintedField is null, it means AsyncTask do not hold the reference of Activity after initialization. 
	 */
	private Map<Integer,SootField> dirtyArgIndexToTaintedField;
	
	public Map<Integer, SootField> getDirtyArgIndexToTaintedField() {
		return dirtyArgIndexToTaintedField;
	}

	public void setDirtyArgIndexToTaintedField(
			Map<Integer, SootField> dirtyArgIndexToTaintedField) {
		this.dirtyArgIndexToTaintedField = dirtyArgIndexToTaintedField;
	}


	/**
	 * The list of fields of AsyncTask class
	 */
	private Set<SootField> fieldsOfAsyncTask;

	/**
	 * the set of all possible control flows of current method
	 */
	private List<List<Unit>> unitLists;


	public AsyncTaskInitMethodSummary(SootMethod methodUnderAnalysis,Set<SootField> fieldsOfAsyncTask) {
		super(methodUnderAnalysis);
		// TODO Auto-generated constructor stub
		this.fieldsOfAsyncTask = fieldsOfAsyncTask;
		this.dirtyArgIndexToTaintedField = new HashMap<Integer,SootField>();
		this.unitLists = new ArrayList<List<Unit>>();
	}
	
	
	@Override
	protected void generation(UnitGraph theGraph){
		// TODO Auto-generated method stub
		if( super.methodUnderAnalysis.getParameterCount() == 0 )
			return;
		List<Unit> headUnits = theGraph.getHeads();
		for(Unit unit: headUnits)
			this.generateAllControlFlow(unit, theGraph, new HashMap<Unit,Boolean>(), new ArrayList<Unit>());
		Set<SootField> taintedFields = new HashSet<SootField>();
		for(List<Unit> unitList:this.unitLists){
			this.processControlFlow(unitList,taintedFields);
			if( taintedFields.size() ==  this.fieldsOfAsyncTask.size())
				break;
		}
	}
	
	/**
	 * 
	 * @param unitList
	 */
	private void processControlFlow(List<Unit> unitList,Set<SootField> taintedFields){
		InitMethodJimpleAnalysis initAnalysis = new InitMethodJimpleAnalysis();
		for(int i=unitList.size()-1; i >= 0; i--){
			Stmt currentStmt = (Stmt)unitList.get(i);
			Map<SootField, Integer> taintedFieldToDirtyArg = null;
			InvokeExpr invokeExpr = initAnalysis.getInvokeExprOfCurrentStmt(currentStmt);
			if( invokeExpr != null && AsyncTaskDetector.methodSignatureToBody.get(invokeExpr.getMethod().getSignature())!=null){//current stmt contains invoke expression 
				String key = InitMethodTopologyOperation.getInitMethodKey(invokeExpr.getMethod());
				AsyncTaskInitMethodSummary currentMethodSummary = (AsyncTaskInitMethodSummary)TopologyOperation.methodKeyToSummary.get(key);
				if( currentMethodSummary != null ){
					List<Value> args = initAnalysis.getActualParameter(currentStmt);
					for(Map.Entry<Integer, SootField> entry:currentMethodSummary.dirtyArgIndexToTaintedField.entrySet()){
						if( taintedFields.contains(entry.getValue()))
							continue;
						taintedFields.add(entry.getValue());
						int argIndex = initAnalysis.getValueReferToArgIndex(args.get(entry.getKey()),entry.getValue(),unitList.subList(0,i+1));
						if( argIndex != -1 )
							this.dirtyArgIndexToTaintedField.put(argIndex,entry.getValue());
					}
				}
			}
			if((taintedFieldToDirtyArg = initAnalysis.getTaintedFieldToDirtyArgIndex(currentStmt,unitList.subList(0, i+1),taintedFields,this.fieldsOfAsyncTask)) != null){
				for(Map.Entry<SootField, Integer> entry:taintedFieldToDirtyArg.entrySet()){
					taintedFields.add(entry.getKey());
					this.dirtyArgIndexToTaintedField.put(entry.getValue(),entry.getKey());
				}
			}
			if( taintedFields.size() == this.fieldsOfAsyncTask.size())
				break;
		}
	}
	
	/**
	 * Generate all the possible control flow of current method
	 * @param currentUnit
	 * @param theGraph
	 * @param unitToIsVisit
	 * @param currentUnitList
	 */
	private void generateAllControlFlow(Unit currentUnit,UnitGraph theGraph,Map<Unit,Boolean> unitToIsVisit,List<Unit> currentUnitList){
		assert(currentUnit.branches()||(!currentUnit.branches()&&theGraph.getSuccsOf(currentUnit).size()<=1));
		unitToIsVisit.put(currentUnit, true);
		currentUnitList.add(currentUnit);
		List<Unit> succeedUnits = theGraph.getSuccsOf(currentUnit);
		if( succeedUnits.size() == 0){//Current unit is the tail of the unit graph which means we find a new control flow
			List<Unit> newList = new ArrayList<Unit>();
			newList.addAll(currentUnitList);
			this.unitLists.add(newList);
		}
		for(Unit unit: succeedUnits){
			Boolean isVisit = unitToIsVisit.get(unit);
			if( isVisit != null && isVisit.booleanValue() )
				continue;
			generateAllControlFlow(unit,theGraph,unitToIsVisit,currentUnitList);
		}
		unitToIsVisit.remove(currentUnit);
		currentUnitList.remove(currentUnitList.size()-1);
	}


	@Override
	public void printMethodSummary() {
		// TODO Auto-generated method stub
		System.out.println("AsyncTaskInitMethod is "+this.methodUnderAnalysis.getSignature());
		for(Map.Entry<Integer,SootField> entry:this.dirtyArgIndexToTaintedField.entrySet())
			System.out.println("Arg index "+entry.getKey()+" tainted "+entry.getValue().getSignature());
		System.out.println("");
	}
}