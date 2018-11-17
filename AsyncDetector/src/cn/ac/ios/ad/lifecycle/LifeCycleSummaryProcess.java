package cn.ac.ios.ad.lifecycle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.SootField;
import soot.SootMethod;
import cn.ac.ios.ad.constant.MethodSignature;
import cn.ac.ios.ad.summary.ActivityAsyncOperationMethodSummary;
import cn.ac.ios.ad.summary.alphabet.activity.ActivityOperationUnitSummary;
import cn.ac.ios.ad.summary.topology.ActivityTopologyOperation;
import cn.ac.ios.ad.summary.topology.TopologyOperation;
import cn.ac.ios.ad.util.ActivityAsyncOperationChecker;

public class LifeCycleSummaryProcess {
	private SootField aysncTaskField;
	/**
	 * Each element in this list is a possible execution order of life cycle methods
	 */
	private static List<List<String>> lifeCycleOrderList;
	private List<LifeCycleDummyMethod> dummyMethodList;
	private Set<Integer> methodOrderHashSet;
	
	private void initLifeCycleOrderList(){
		LifeCycleConstruction construction = new LifeCycleConstruction();
		lifeCycleOrderList = construction.getLifeCycleOrderList();
	}
	
	public LifeCycleSummaryProcess(SootField asyncTaskField){
		if( lifeCycleOrderList == null)
			this.initLifeCycleOrderList();
		this.dummyMethodList = new ArrayList<LifeCycleDummyMethod>();
		this.methodOrderHashSet = new HashSet<Integer>();
		this.aysncTaskField = asyncTaskField;
	}
	
	
	private void dummyMethodListGeneration(Map<String,ActivityAsyncOperationMethodSummary> lifeCycleSignatureToMethodSummary,ActivityTopologyOperation listenerOperationArray[]){
		for(int i=0;i < lifeCycleOrderList.size(); i++){
			List<ActivityAsyncOperationMethodSummary> operationList = new ArrayList<ActivityAsyncOperationMethodSummary>();
			List<String> methodOrder = new ArrayList<String>();
			this.dummyMethodListGenerationFromCurrentOrder(lifeCycleSignatureToMethodSummary, listenerOperationArray, i, 0, operationList, methodOrder);
		}
	}
	
	private void dummyMethodListGenerationFromCurrentOrder(Map<String,ActivityAsyncOperationMethodSummary> lifeCycleSignatureToMethodSummary,ActivityTopologyOperation listenerOperationArray[],
			int i,int currentIndex,List<ActivityAsyncOperationMethodSummary> currentMethodSummaryList,List<String> methodOrder){
		List<String> currentLifeCycleOrder = lifeCycleOrderList.get(i);
		for(int j=currentIndex;j < currentLifeCycleOrder.size(); j++){
			ActivityAsyncOperationMethodSummary currentMethodSummary = lifeCycleSignatureToMethodSummary.get(currentLifeCycleOrder.get(j));
			if( currentMethodSummary != null && !currentMethodSummary.isNull()){
				currentMethodSummaryList.add(currentMethodSummary);
				methodOrder.add(currentLifeCycleOrder.get(j));
			}
			if( currentLifeCycleOrder.get(j).equals(MethodSignature.ON_RESUME)){//Listener should be processed independently
				for(int k=0;k < listenerOperationArray.length; k++){
					if( listenerOperationArray[k] == null )
						continue;
					ActivityAsyncOperationMethodSummary listenerSummary = (ActivityAsyncOperationMethodSummary) listenerOperationArray[k].getSourceMethodSummary();
					if( listenerSummary.getControlFlowSummaryList().size() == 0 )
						continue;
					List<ActivityAsyncOperationMethodSummary> newList = new ArrayList<ActivityAsyncOperationMethodSummary>();
					List<String> newOrder = new ArrayList<String>();
					newOrder.addAll(methodOrder);
					newOrder.add(listenerOperationArray[k].getSourceMethod().getSignature());
					newList.addAll(currentMethodSummaryList);
					newList.add((ActivityAsyncOperationMethodSummary) listenerOperationArray[k].getSourceMethodSummary());
					this.dummyMethodListGenerationFromCurrentOrder(lifeCycleSignatureToMethodSummary, listenerOperationArray, i, j+1, newList,newOrder);
				}
			}
		}
		if( !this.methodOrderHashSet.contains(methodOrder.hashCode())){
			this.methodOrderHashSet.add(methodOrder.hashCode());
			LifeCycleDummyMethod dummyMethod = new LifeCycleDummyMethod(methodOrder,currentMethodSummaryList);
			this.dummyMethodList.add(dummyMethod);
		}
	}
	
	public void dummyMethodDetection(Map<String,ActivityAsyncOperationMethodSummary> lifeCycleSignatureToMethodSummary,ActivityTopologyOperation listenerOperationArray[]){
		this.dummyMethodListGeneration(lifeCycleSignatureToMethodSummary, listenerOperationArray);
		
		for(LifeCycleDummyMethod currentDummyMethod:this.dummyMethodList){//traverse each dummy method
			for(List<ActivityOperationUnitSummary> controlFlowSummary:currentDummyMethod.getControlFlowSummaryList())
				ActivityAsyncOperationChecker.check(controlFlowSummary, this.aysncTaskField);
		}
	}
	
	public void printDummyMethod(){
		for(LifeCycleDummyMethod currentDummyMethod: this.dummyMethodList){
			System.out.println("======================================");
			for(String name:currentDummyMethod.getLifeCycleOrder()){
				System.out.println(name);
			}
		}
	}
}