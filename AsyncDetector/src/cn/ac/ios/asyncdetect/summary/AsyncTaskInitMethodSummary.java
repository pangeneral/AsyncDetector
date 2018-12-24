/* AsyncDetecotr - an Android async component misuse detection tool
 * Copyright (C) 2018 Linjie Pan
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package cn.ac.ios.asyncdetect.summary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.ac.ios.asyncdetect.AsyncTaskDetector;
import cn.ac.ios.asyncdetect.summary.analysis.InitMethodJimpleAnalysis;
import cn.ac.ios.asyncdetect.summary.topology.InitMethodTopologyOperation;
import cn.ac.ios.asyncdetect.summary.topology.TopologyOperation;
import cn.ac.ios.asyncdetect.util.Log;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;

/**
 * The summary of init method of AsyncTask.
 * @author Linjie Pan
 * @version 1.0
 */
public class AsyncTaskInitMethodSummary extends AbstractMethodSummary{
	
	/**
	 * Key is the index of dirty arguments in the init method of AsyncTask.
	 * Value is the field of AsyncTask which holds the reference to the dirty argument
	 * Here, dirty arguments are those that pass the reference of Activity object to AsyncTask object.
	 * If dirtyArgToTaintedField is null, it means AsyncTask do not hold the reference of Activity after initialization. 
	 */
	private Map<Integer,SootField> mDirtyArgIndexToTaintedField;
	
	/**
	 * The set of fields of AsyncTask class
	 */
	private Set<SootField> mFieldsOfAsyncTask;

	/**
	 * the set of all possible control flows of current method
	 */
	private List<List<Unit>> mUnitLists;
	
	
	public Map<Integer, SootField> getDirtyArgIndexToTaintedField() {
		return mDirtyArgIndexToTaintedField;
	}

	public void setDirtyArgIndexToTaintedField(
			Map<Integer, SootField> dirtyArgIndexToTaintedField) {
		this.mDirtyArgIndexToTaintedField = dirtyArgIndexToTaintedField;
	}
	
	public AsyncTaskInitMethodSummary(SootMethod methodUnderAnalysis,Set<SootField> fieldsOfAsyncTask) {
		super(methodUnderAnalysis);
		// TODO Auto-generated constructor stub
		this.mFieldsOfAsyncTask = fieldsOfAsyncTask;
		this.mDirtyArgIndexToTaintedField = new HashMap<Integer,SootField>();
		this.mUnitLists = new ArrayList<List<Unit>>();
	}
	
	
	@Override
	protected void generation(UnitGraph theGraph){
		// TODO Auto-generated method stub
		if( super.mMethodUnderAnalysis.getParameterCount() == 0 )
			return;
		List<Unit> headUnits = theGraph.getHeads();
		for(Unit unit: headUnits)
			this.generateAllControlFlow(unit, theGraph, new HashMap<Unit,Boolean>(), new ArrayList<Unit>());
		Set<SootField> taintedFields = new HashSet<SootField>();
		for(List<Unit> unitList:this.mUnitLists){
			this.processControlFlow(unitList,taintedFields);
			if( taintedFields.size() ==  this.mFieldsOfAsyncTask.size())
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
			if( invokeExpr != null && AsyncTaskDetector.sMethodSignatureToBody.get(invokeExpr.getMethod().getSignature())!=null){//current stmt contains invoke expression 
				String key = InitMethodTopologyOperation.getInitMethodKey(invokeExpr.getMethod());
				AsyncTaskInitMethodSummary currentMethodSummary = (AsyncTaskInitMethodSummary)TopologyOperation.getsMethodKeyToSummary().get(key);
				if( currentMethodSummary != null ){
					List<Value> args = initAnalysis.getActualParameter(currentStmt);
					for(Map.Entry<Integer, SootField> entry:currentMethodSummary.mDirtyArgIndexToTaintedField.entrySet()){
						if( taintedFields.contains(entry.getValue()))
							continue;
						taintedFields.add(entry.getValue());
						int argIndex = initAnalysis.getValueReferToArgIndex(args.get(entry.getKey()),entry.getValue(),unitList.subList(0,i+1));
						if( argIndex != -1 )
							this.mDirtyArgIndexToTaintedField.put(argIndex,entry.getValue());
					}
				}
			}
			if((taintedFieldToDirtyArg = initAnalysis.getTaintedFieldToDirtyArgIndex(currentStmt,unitList.subList(0, i+1),taintedFields,this.mFieldsOfAsyncTask)) != null){
				for(Map.Entry<SootField, Integer> entry:taintedFieldToDirtyArg.entrySet()){
					taintedFields.add(entry.getKey());
					this.mDirtyArgIndexToTaintedField.put(entry.getValue(),entry.getKey());
				}
			}
			if( taintedFields.size() == this.mFieldsOfAsyncTask.size())
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
			this.mUnitLists.add(newList);
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
		Log.i("AsyncTaskInitMethod is "+this.mMethodUnderAnalysis.getSignature());
		for(Map.Entry<Integer,SootField> entry:this.mDirtyArgIndexToTaintedField.entrySet())
			Log.i("Arg index "+entry.getKey()+" tainted "+entry.getValue().getSignature());
		Log.i("");
	}
}