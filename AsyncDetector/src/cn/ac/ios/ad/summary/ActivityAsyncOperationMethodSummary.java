package cn.ac.ios.ad.summary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.ac.ios.ad.AsyncTaskDetector;
import cn.ac.ios.ad.summary.alphabet.activity.ActivityOperationUnitSummary;
import cn.ac.ios.ad.summary.alphabet.activity.AssignAsyncTaskInstanceUnitSummary;
import cn.ac.ios.ad.summary.alphabet.activity.CancelAsyncTaskUnitSummary;
import cn.ac.ios.ad.summary.alphabet.activity.ActivityNullSummary;
import cn.ac.ios.ad.summary.alphabet.activity.StartAsyncTaskUnitSummary;
import cn.ac.ios.ad.summary.analysis.ActivityAsyncOperationJimpleAnalysis;
import cn.ac.ios.ad.summary.topology.ActivityTopologyOperation;
import cn.ac.ios.ad.summary.topology.TopologyOperation;
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
 * A method and a AsyncTask field corresponds to one method summary
 * @author panlj
 */
public class ActivityAsyncOperationMethodSummary extends AbstractMethodSummary{
	private SootField asyncFieldUnderAnalysis;
	private SootClass activityClass;

	/**
	 * Key = hash code of current unit+"-"+the hash code of current control flow which is in the form of List<Unit>
	 * Value is the summary of current unit
	 */
	private Map<String,List<List<ActivityOperationUnitSummary>>> unitToUnitSummary;
	
	/**
	 * The summary of a method is a list, each element in the list represents the summary of a control flow path
	 * The summary of a control flow path is a list, each element in the list represents the summary of an available statement
	 * Here, available statement means statements that related to AsyncTask operation 
	 */
	private List<List<ActivityOperationUnitSummary>> controlFlowSummaryList;

	public List<List<ActivityOperationUnitSummary>> getControlFlowSummaryList() {
		return controlFlowSummaryList;
	}

	public void setMethodSummaryList(List<List<ActivityOperationUnitSummary>> controlFlowSummaryList) {
		this.controlFlowSummaryList = controlFlowSummaryList;
	}

	public ActivityAsyncOperationMethodSummary(SootMethod methodUnderAnalysis,SootField fieldUnderAnalysis,SootClass activityClass){
		super(methodUnderAnalysis);
		this.asyncFieldUnderAnalysis = fieldUnderAnalysis;
		this.activityClass = activityClass;
		this.controlFlowSummaryList= new ArrayList<List<ActivityOperationUnitSummary>>();
		this.unitToUnitSummary = new HashMap<String,List<List<ActivityOperationUnitSummary>>>();
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
		for(List<ActivityOperationUnitSummary> controlFlowSummary:this.controlFlowSummaryList){
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
			System.out.println(summary.getUnit().toString()+" "+summary.getSummary());
		}
	}
	
	@Override
	public void printMethodSummary(){
		System.out.println("Activity async operation method is: "+this.methodUnderAnalysis.getSignature());
		for(List<ActivityOperationUnitSummary> currentSummaryInList:this.controlFlowSummaryList){
			System.out.println(this.getSummarySequence(currentSummaryInList)+" ");
		}
	}
	
	public boolean isNull(){
		return this.controlFlowSummaryList.size()==0;
	}
	
	public boolean isParameterAssignedToField(int argIndex){
		for(List<ActivityOperationUnitSummary> controlFlowSummary: this.controlFlowSummaryList){
			for(ActivityOperationUnitSummary unitSummary:controlFlowSummary){
				if( unitSummary.getAssignedArgIndex() == argIndex)
					return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param currentStmt, current unit
	 * @param theGraph
	 * @param unitList, current path from the head unit to current unit 
	 * @return
	 */
	protected List<List<ActivityOperationUnitSummary>> processCurrentStmt(Stmt currentStmt,List<Unit> unitList){
		ActivityAsyncOperationJimpleAnalysis ja = new ActivityAsyncOperationJimpleAnalysis();
		List<ActivityOperationUnitSummary> assignUnitSummaryList = null;
		CancelAsyncTaskUnitSummary cancelUnitSummary = null;
		StartAsyncTaskUnitSummary startUnitSummary = null;
		
		List<List<ActivityOperationUnitSummary>> resultSummaryList = new ArrayList<List<ActivityOperationUnitSummary>>();
		
		InvokeExpr theExpr = ja.getInvokeExprOfCurrentStmt(currentStmt);
		//If current unit invokes a method which belongs to the topology graph, then the summary of current unit is a list of summary
		if( theExpr != null && AsyncTaskDetector.methodSignatureToBody.get(theExpr.getMethod().getSignature()) != null){
			String key = ActivityTopologyOperation.getActivityOperationKey(this.asyncFieldUnderAnalysis,theExpr.getMethod());
			ActivityAsyncOperationMethodSummary currentMethodSummary = (ActivityAsyncOperationMethodSummary)TopologyOperation.methodKeyToSummary.get(key);
			if( currentMethodSummary != null ){
				List<List<ActivityOperationUnitSummary>> invokedMethodSummaryList = currentMethodSummary.controlFlowSummaryList;
				this.processInvokedMethodSummary(invokedMethodSummaryList, resultSummaryList,theExpr, currentStmt, unitList);
			}
		}
		if( (assignUnitSummaryList = ja.getAssignAsyncTaskUnitSummary(currentStmt, this.asyncFieldUnderAnalysis,unitList,this.activityClass)) != null ){
			for(ActivityOperationUnitSummary currentSummary:assignUnitSummaryList)
				this.addUnitSummaryToSummaryList(resultSummaryList, currentSummary);
		}
//		if( (judgeNullUnitSummary = ja.getJudgeNullAsyncTaskUnitSummary(currentStmt,this.asyncFieldUnderAnalysis,unitList)) != null)
//			this.addUnitSummaryToSummaryList(resultSummaryList, judgeNullUnitSummary);
		if( (startUnitSummary = ja.getStartAsyncTaskUnitSummary(currentStmt, this.asyncFieldUnderAnalysis, unitList)) != null )
			this.addUnitSummaryToSummaryList(resultSummaryList, startUnitSummary);
		if( (cancelUnitSummary = ja.getCancelAsyncTaskUnitSummary(currentStmt, this.asyncFieldUnderAnalysis, unitList)) != null )
			this.addUnitSummaryToSummaryList(resultSummaryList, cancelUnitSummary);
		if( resultSummaryList.size() == 0)
			this.addUnitSummaryToSummaryList(resultSummaryList, new ActivityNullSummary(currentStmt));
		return resultSummaryList;
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
		ActivityAsyncOperationJimpleAnalysis ja = new ActivityAsyncOperationJimpleAnalysis();
		for(List<ActivityOperationUnitSummary> flowSummary:invokedMethodSummaryList){
			List<ActivityOperationUnitSummary> newFlowSummary = new ArrayList<ActivityOperationUnitSummary>();
			for(ActivityOperationUnitSummary unitSummary:flowSummary){
				if( !unitSummary.isComplete()){
					int argIndex = unitSummary.getAssignedArgIndex();
					if( unitSummary instanceof AssignAsyncTaskInstanceUnitSummary){
						List<ActivityOperationUnitSummary> newUnitSummaryList = ja.getNewAssignAsyncTaskUnitSummary(currentStmt,theExpr,unitList,argIndex);
						if( newUnitSummaryList == null )
							continue;
						for(ActivityOperationUnitSummary currentSummary:newUnitSummaryList){
							if(currentSummary == null ){
								continue;
							}
							currentSummary.setCurrentMethod(this.methodUnderAnalysis);
						}
						newFlowSummary.addAll(newUnitSummaryList);
					}
					else if(unitSummary instanceof StartAsyncTaskUnitSummary){
						StartAsyncTaskUnitSummary newUnitSummary = ja.getNewStartAsyncTaskUnitSummary((StartAsyncTaskUnitSummary)unitSummary,currentStmt, theExpr, this.asyncFieldUnderAnalysis, unitList, argIndex);
						if(newUnitSummary==null)
							continue;
						List<Unit> originalList = ((StartAsyncTaskUnitSummary) unitSummary).getUnitList();
						newUnitSummary.setUnitList(originalList);
						newUnitSummary.setTaintInstances(((StartAsyncTaskUnitSummary) unitSummary).getTaintInstances());
						newUnitSummary.setCurrentMethod(this.methodUnderAnalysis);
						newFlowSummary.add(newUnitSummary);
					}
					else if( unitSummary instanceof CancelAsyncTaskUnitSummary){
						CancelAsyncTaskUnitSummary newUnitSummary = ja.getNewCancelAsyncTaskUnitSummary(currentStmt, theExpr, this.asyncFieldUnderAnalysis, unitList, argIndex);
						if( newUnitSummary == null )
							continue;
						newUnitSummary.setCurrentMethod(this.methodUnderAnalysis);
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
		unitSummary.setCurrentMethod(this.methodUnderAnalysis);
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
	
	@Override
	protected void generation(UnitGraph theGraph) {
		// TODO Auto-generated method stub
		List<Unit> headList = theGraph.getHeads();
		assert(headList.size()<=1);
		for(Unit currentUnit: headList){
			List<List<ActivityOperationUnitSummary>> currentSummary = traverseUnit(currentUnit,theGraph,new HashMap<Unit,Boolean>(),new ArrayList<Unit>());
			if( currentSummary != null)
				this.controlFlowSummaryList.addAll(currentSummary);
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
		if( this.unitToUnitSummary.containsKey(hashKey) )
			summariesOfUnit = this.unitToUnitSummary.get(hashKey);
		else{
			summariesOfUnit = this.processCurrentStmt((Stmt)currentUnit,currentUnitList);
			this.unitToUnitSummary.put(hashKey, summariesOfUnit);
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
