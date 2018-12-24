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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.ac.ios.asyncdetect.AsyncTaskDetector;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.ActivityNullSummary;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.ActivityOperationUnitSummary;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.AssignAsyncTaskInstanceUnitSummary;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.CancelAsyncTaskUnitSummary;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.StartAsyncTaskUnitSummary;
import cn.ac.ios.asyncdetect.summary.analysis.ActivityAsyncOperationJimpleAnalysis;
import cn.ac.ios.asyncdetect.summary.topology.ActivityTopologyOperation;
import cn.ac.ios.asyncdetect.summary.topology.TopologyOperation;
import cn.ac.ios.asyncdetect.util.Log;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;

/**
 * Method summary about AsyncTask operation
 * The method must be the life cycle method or listener of activity  
 * A method and an AsyncTask field corresponds to one method summary
 * @author Linjie Pan
 * @version 1.0
 */
public class ActivityAsyncOperationMethodSummary extends AbstractMethodSummary{
	private SootField mAsyncFieldUnderAnalysis;
	private SootClass mActivityClass;
	private ActivityAsyncOperationJimpleAnalysis mjpa = new ActivityAsyncOperationJimpleAnalysis();
	
	/**
	 * Key = hash code of current unit+"-"+the hash code of current control flow which is in the form of List<Unit>
	 * Value = the summary of current unit
	 */
	private Map<String,List<List<ActivityOperationUnitSummary>>> mUnitToUnitSummary;
	
	/**
	 * The summary of a method is a list, each element in the list represents the summary of a control flow path
	 * The summary of a control flow path is a list, each element in the list represents the summary of an available statement
	 * Here, available statement means statements that related to AsyncTask operation 
	 */
	private List<List<ActivityOperationUnitSummary>> mControlFlowSummaryList;

	public List<List<ActivityOperationUnitSummary>> getControlFlowSummaryList() {
		return mControlFlowSummaryList;
	}

	public void setMethodSummaryList(List<List<ActivityOperationUnitSummary>> controlFlowSummaryList) {
		this.mControlFlowSummaryList = controlFlowSummaryList;
	}

	public ActivityAsyncOperationMethodSummary(SootMethod methodUnderAnalysis,SootField fieldUnderAnalysis,SootClass activityClass){
		super(methodUnderAnalysis);
		this.mAsyncFieldUnderAnalysis = fieldUnderAnalysis;
		this.mActivityClass = activityClass;
		this.mControlFlowSummaryList= new ArrayList<List<ActivityOperationUnitSummary>>();
		this.mUnitToUnitSummary = new HashMap<String,List<List<ActivityOperationUnitSummary>>>();
	}
	
	public String getSummarySequence(List<ActivityOperationUnitSummary> controlFlowSummary){
		StringBuilder sb = new StringBuilder("");
		for(ActivityOperationUnitSummary summary:controlFlowSummary){
			if( summary.getSummary() != null && summary.isComplete())
				sb.append(summary.getSummary());
		}
		return sb.toString();
	}
	
	public List<List<ActivityOperationUnitSummary>> getPurifiedControlFlowSummaryList(){
		List<List<ActivityOperationUnitSummary>> newControlFlowSummaryList = new ArrayList<List<ActivityOperationUnitSummary>>();
		for(List<ActivityOperationUnitSummary> controlFlowSummary:this.mControlFlowSummaryList){
			List<ActivityOperationUnitSummary> newControlFlowSummary = new ArrayList<ActivityOperationUnitSummary>();
			for(ActivityOperationUnitSummary currentUnitSummary:controlFlowSummary){
				if(currentUnitSummary.isComplete())
					newControlFlowSummary.add(currentUnitSummary);
			}
			if( newControlFlowSummary.size() > 0 )
				newControlFlowSummaryList.add(newControlFlowSummary);
		}
		return newControlFlowSummaryList;
	}
	
	public void printSummaryOperation(List<ActivityOperationUnitSummary> summaryInList){
		for(ActivityOperationUnitSummary summary: summaryInList){
			Log.i(summary.getUnit().toString()+" "+summary.getSummary());
		}
	}
	
	@Override
	public void printMethodSummary(){
		Log.i("Activity async operation method is: "+this.mMethodUnderAnalysis.getSignature());
		for(List<ActivityOperationUnitSummary> currentSummaryInList:this.mControlFlowSummaryList){
			Log.i(this.getSummarySequence(currentSummaryInList)+" ");
		}
	}
	
	public boolean isNull(){
		return this.mControlFlowSummaryList.size()==0;
	}
	
	public boolean isParameterAssignedToField(int argIndex){
		for(List<ActivityOperationUnitSummary> controlFlowSummary: this.mControlFlowSummaryList){
			for(ActivityOperationUnitSummary unitSummary:controlFlowSummary){
				if( unitSummary.getAssignedArgIndex() == argIndex)
					return true;
			}
		}
		return false;
	}
	
	
	@Override
	protected void generation(UnitGraph theGraph) {
		List<Unit> operationUnits = new ArrayList<Unit>();
		if( !AsyncTaskDetector.sSootMethodToIsContainAsyncOperation.containsKey(this.mMethodUnderAnalysis)){
			this.judgeMethodContainAsyncOperation(theGraph,operationUnits);
		}
		if( !AsyncTaskDetector.sSootMethodToIsContainAsyncOperation.get(this.mMethodUnderAnalysis)){
			return;
		}
		else{
			List<Unit> headList = theGraph.getHeads();
			assert(headList.size()<=1);
			for(Unit currentUnit: headList){
				List<List<ActivityOperationUnitSummary>> currentSummary = traverseUnit(currentUnit,theGraph,new HashMap<Unit,Boolean>(),new ArrayList<Unit>());
				if( currentSummary != null)
					this.mControlFlowSummaryList.addAll(currentSummary);
			}
		}
		
	}
	
	/**
	 * 
	 * @param currentStmt, current unit
	 * @param theGraph
	 * @param unitList, current path from the head unit to current unit 
	 * @return
	 */
	protected List<List<ActivityOperationUnitSummary>> processCurrentStmt(Stmt currentStmt,List<Unit> unitList){
		List<ActivityOperationUnitSummary> assignUnitSummaryList = null;
		CancelAsyncTaskUnitSummary cancelUnitSummary = null;
		StartAsyncTaskUnitSummary startUnitSummary = null;
		
		List<List<ActivityOperationUnitSummary>> resultSummaryList = new ArrayList<List<ActivityOperationUnitSummary>>();
		
		InvokeExpr theExpr = this.mjpa.getInvokeExprOfCurrentStmt(currentStmt);
		
		//If current unit invokes a method which belongs to the topology graph, then the summary of current unit is a list of summary
		if( theExpr != null && AsyncTaskDetector.sMethodSignatureToBody.get(theExpr.getMethod().getSignature()) != null){
			String key = ActivityTopologyOperation.getActivityOperationKey(this.mAsyncFieldUnderAnalysis,theExpr.getMethod());
			ActivityAsyncOperationMethodSummary currentMethodSummary = (ActivityAsyncOperationMethodSummary)TopologyOperation.getsMethodKeyToSummary().get(key);
			if( currentMethodSummary != null ){
				List<List<ActivityOperationUnitSummary>> invokedMethodSummaryList = currentMethodSummary.mControlFlowSummaryList;
				this.processInvokedMethodSummary(invokedMethodSummaryList, resultSummaryList,theExpr, currentStmt, unitList);
			}
		}
		if( (assignUnitSummaryList = this.mjpa.getAssignAsyncTaskUnitSummary(currentStmt, this.mAsyncFieldUnderAnalysis,unitList,this.mActivityClass)) != null ){
			for(ActivityOperationUnitSummary currentSummary:assignUnitSummaryList)
				this.addUnitSummaryToSummaryList(resultSummaryList, currentSummary);
		}
		if( (startUnitSummary = this.mjpa.getStartAsyncTaskUnitSummary(currentStmt, this.mAsyncFieldUnderAnalysis, unitList)) != null )
			this.addUnitSummaryToSummaryList(resultSummaryList, startUnitSummary);
		if( (cancelUnitSummary = this.mjpa.getCancelAsyncTaskUnitSummary(currentStmt, this.mAsyncFieldUnderAnalysis, unitList)) != null )
			this.addUnitSummaryToSummaryList(resultSummaryList, cancelUnitSummary);
		if( resultSummaryList.size() == 0)
			this.addUnitSummaryToSummaryList(resultSummaryList, new ActivityNullSummary(currentStmt));
		return resultSummaryList;
	}
	
	/**
	 * Judge whether method represented by theGraph contains AsyncTask Operation.
	 * If any statement contains variables whose type is AsyncTask or subclass of AsyncTask, we take it as a candidate method.
	 * 
	 * Methods that have many path conditions will lead to path explosion. 
	 * From our experience, such methods usually don't contain AsyncTask operation and can be omitted. 
	 * @param theGraph
	 */
	private void judgeMethodContainAsyncOperation(UnitGraph theGraph,List<Unit> operationUnits){
		Iterator<Unit> it = theGraph.iterator();
		boolean flag = false;
		while(it.hasNext()){
			Unit currentUnit = it.next();
			if( this.mjpa.isContainAsyncTaskOperation(currentUnit)){
				operationUnits.add(currentUnit);
				flag = true;
			}
		}
		AsyncTaskDetector.sSootMethodToIsContainAsyncOperation.put(this.mMethodUnderAnalysis, flag);
	}
	
	/**
	 * Update the summary of invoked method
	 * @param invokedMethodSummaryList
	 * @param resultSummaryList
	 * @param theExpr
	 * @param currentStmt
	 * @param unitList
	 */
	private void processInvokedMethodSummary(List<List<ActivityOperationUnitSummary>> invokedMethodSummaryList,
			List<List<ActivityOperationUnitSummary>> resultSummaryList,InvokeExpr theExpr,Stmt currentStmt,List<Unit> unitList){
		for(List<ActivityOperationUnitSummary> flowSummary:invokedMethodSummaryList){
			List<ActivityOperationUnitSummary> newFlowSummary = new ArrayList<ActivityOperationUnitSummary>();
			for(ActivityOperationUnitSummary unitSummary:flowSummary){
				if( !unitSummary.isComplete()){
					int argIndex = unitSummary.getAssignedArgIndex();
					if( unitSummary instanceof AssignAsyncTaskInstanceUnitSummary){
						List<ActivityOperationUnitSummary> newUnitSummaryList = this.mjpa.getNewAssignAsyncTaskUnitSummary(currentStmt,theExpr,unitList,argIndex);
						if( newUnitSummaryList == null )
							continue;
						for(ActivityOperationUnitSummary currentSummary:newUnitSummaryList){
							if(currentSummary == null ){
								continue;
							}
							currentSummary.setCurrentMethod(this.mMethodUnderAnalysis);
						}
						newFlowSummary.addAll(newUnitSummaryList);
					}
					else if(unitSummary instanceof StartAsyncTaskUnitSummary){
						StartAsyncTaskUnitSummary newUnitSummary = this.mjpa.getNewStartAsyncTaskUnitSummary((StartAsyncTaskUnitSummary)unitSummary,currentStmt, theExpr, this.mAsyncFieldUnderAnalysis, unitList, argIndex);
						if(newUnitSummary==null)
							continue;
						List<Unit> originalList = ((StartAsyncTaskUnitSummary) unitSummary).getUnitList();
						newUnitSummary.setUnitList(originalList);
						newUnitSummary.setTaintInstances(((StartAsyncTaskUnitSummary) unitSummary).getTaintInstances());
						newUnitSummary.setCurrentMethod(this.mMethodUnderAnalysis);
						newFlowSummary.add(newUnitSummary);
					}
					else if( unitSummary instanceof CancelAsyncTaskUnitSummary){
						CancelAsyncTaskUnitSummary newUnitSummary = this.mjpa.getNewCancelAsyncTaskUnitSummary(currentStmt, theExpr, this.mAsyncFieldUnderAnalysis, unitList, argIndex);
						if( newUnitSummary == null )
							continue;
						newUnitSummary.setCurrentMethod(this.mMethodUnderAnalysis);
						newFlowSummary.add(newUnitSummary);
					}
				}
				else{
					newFlowSummary.add(unitSummary);
				}
			}
			if(newFlowSummary.size() > 0)
				resultSummaryList.add(newFlowSummary);
		}
	}
	
	private void addUnitSummaryToSummaryList(List<List<ActivityOperationUnitSummary>> summaryList,ActivityOperationUnitSummary unitSummary){
		unitSummary.setCurrentMethod(this.mMethodUnderAnalysis);
		if( summaryList.size() > 0 ){
			for(List<ActivityOperationUnitSummary> controlFlowSummary:summaryList)
				controlFlowSummary.add(unitSummary);
		}
		else{
			List<ActivityOperationUnitSummary> controlFlowSummary = new ArrayList<ActivityOperationUnitSummary>();
			controlFlowSummary.add(unitSummary);
			summaryList.add(controlFlowSummary);
		}
	}
	
	
	
	
	/**
	 * Traverse the given unit with DFS, return the list of summary after this unit in CFG
	 * @param currentUnit
	 * @param node
	 * @param theGraph
	 * @return
	 */
	private List<List<ActivityOperationUnitSummary>> traverseUnit(Unit currentUnit,UnitGraph theGraph,Map<Unit,Boolean> unitToIsVisit,List<Unit> currentUnitList){
		assert(currentUnit.branches()||(!currentUnit.branches()&&theGraph.getSuccsOf(currentUnit).size()<=1));

		unitToIsVisit.put(currentUnit, true);
		currentUnitList.add(currentUnit);
		List<List<ActivityOperationUnitSummary>> resultList = new ArrayList<List<ActivityOperationUnitSummary>>();
		List<List<ActivityOperationUnitSummary>> summariesOfUnit = null;
		/**
		 * A unit and a control flow correspond to a unique unit summary
		 */
		String hashKey = String.valueOf(currentUnit.hashCode())+"-"+String.valueOf(currentUnitList.hashCode());
		if( this.mUnitToUnitSummary.containsKey(hashKey) )
			summariesOfUnit = this.mUnitToUnitSummary.get(hashKey);
		else{
			summariesOfUnit = this.processCurrentStmt((Stmt)currentUnit,currentUnitList);
			this.mUnitToUnitSummary.put(hashKey, summariesOfUnit);
		}
		List<Unit> succeedUnits = theGraph.getSuccsOf(currentUnit);
		for(Unit unit: succeedUnits){//Merge the summary of current Unit and its successive unit respectively
			Boolean isVisit = unitToIsVisit.get(unit);
			if( isVisit != null && isVisit.booleanValue() )
				continue;
			List<List<ActivityOperationUnitSummary>> summariesAfterUnit = traverseUnit(unit,theGraph,unitToIsVisit,currentUnitList);
			if( summariesAfterUnit.size() > 0 ){
				for(List<ActivityOperationUnitSummary> summaryAfterUnit: summariesAfterUnit){
					for(List<ActivityOperationUnitSummary> summaryOfUnit:summariesOfUnit){
						List<ActivityOperationUnitSummary> currentSummary = new ArrayList<ActivityOperationUnitSummary>();
						this.summaryMerge(currentSummary, summaryOfUnit);
						this.summaryMerge(currentSummary,summaryAfterUnit);
						if( currentSummary.size() > 0 )
							resultList.add(currentSummary);
					}
				}
			}
			else{
				for(List<ActivityOperationUnitSummary> summaryOfUnit:summariesOfUnit){
					List<ActivityOperationUnitSummary> currentSummary = new ArrayList<ActivityOperationUnitSummary>();
					this.summaryMerge(currentSummary, summaryOfUnit);
					if( currentSummary.size() > 0 )
						resultList.add(currentSummary);
				}
			}
		}
		unitToIsVisit.remove(currentUnit);
		currentUnitList.remove(currentUnitList.size()-1);
		return resultList;
	}
	
	/**
	 * Except NullActivitySummary, adding all unit summaries in subSummary to mainSummary
	 * @param mainSummary
	 * @param subSummary
	 */
	private void summaryMerge(List<ActivityOperationUnitSummary> mainSummary,List<ActivityOperationUnitSummary> subSummary){
		for(ActivityOperationUnitSummary unitSummary:subSummary){
			if( unitSummary instanceof ActivityNullSummary)
				continue;
			mainSummary.add(unitSummary);
		}
	}
}
