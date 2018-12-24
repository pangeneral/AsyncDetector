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

package cn.ac.ios.asyncdetect.lifecycle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.SootField;
import cn.ac.ios.asyncdetect.constant.MethodSignature;
import cn.ac.ios.asyncdetect.summary.ActivityAsyncOperationMethodSummary;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.ActivityOperationUnitSummary;
import cn.ac.ios.asyncdetect.summary.topology.ActivityTopologyOperation;
import cn.ac.ios.asyncdetect.util.ActivityAsyncOperationChecker;
import cn.ac.ios.asyncdetect.util.Log;

/**
 * 
 * @author Linjie Pan
 * @version 1.0
 */
public class LifeCycleSummaryProcess {
	private SootField mAysncTaskField;
	/**
	 * Each element in this list is a possible execution order of life cycle methods
	 */
	private static List<List<String>> sLifeCycleOrderList;
	private List<LifeCycleDummyMethod> mDummyMethodList;
	private Set<Integer> mMethodOrderHashSet;
	
	private void initLifeCycleOrderList(){
		LifeCycleConstruction construction = new LifeCycleConstruction();
		sLifeCycleOrderList = construction.getLifeCycleOrderList();
	}
	
	public LifeCycleSummaryProcess(SootField asyncTaskField){
		if( sLifeCycleOrderList == null)
			this.initLifeCycleOrderList();
		this.mDummyMethodList = new ArrayList<LifeCycleDummyMethod>();
		this.mMethodOrderHashSet = new HashSet<Integer>();
		this.mAysncTaskField = asyncTaskField;
	}
	
	
	private void dummyMethodListGeneration(Map<String,ActivityAsyncOperationMethodSummary> lifeCycleSignatureToMethodSummary,ActivityTopologyOperation listenerOperationArray[]){
		for(int i=0;i < sLifeCycleOrderList.size(); i++){
			List<ActivityAsyncOperationMethodSummary> operationList = new ArrayList<ActivityAsyncOperationMethodSummary>();
			List<String> methodOrder = new ArrayList<String>();
			this.dummyMethodListGenerationFromCurrentOrder(lifeCycleSignatureToMethodSummary, listenerOperationArray, i, 0, operationList, methodOrder);
		}
	}
	
	private void dummyMethodListGenerationFromCurrentOrder(Map<String,ActivityAsyncOperationMethodSummary> lifeCycleSignatureToMethodSummary,ActivityTopologyOperation listenerOperationArray[],
			int i,int currentIndex,List<ActivityAsyncOperationMethodSummary> currentMethodSummaryList,List<String> methodOrder){
		List<String> currentLifeCycleOrder = sLifeCycleOrderList.get(i);
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
		if( !this.mMethodOrderHashSet.contains(methodOrder.hashCode())){
			this.mMethodOrderHashSet.add(methodOrder.hashCode());
			LifeCycleDummyMethod dummyMethod = new LifeCycleDummyMethod(methodOrder,currentMethodSummaryList);
			this.mDummyMethodList.add(dummyMethod);
		}
	}
	
	public void dummyMethodDetection(Map<String,ActivityAsyncOperationMethodSummary> lifeCycleSignatureToMethodSummary,ActivityTopologyOperation listenerOperationArray[]){
		this.dummyMethodListGeneration(lifeCycleSignatureToMethodSummary, listenerOperationArray);
		
		for(LifeCycleDummyMethod currentDummyMethod:this.mDummyMethodList){//traverse each dummy method
			for(List<ActivityOperationUnitSummary> controlFlowSummary:currentDummyMethod.getControlFlowSummaryList())
				ActivityAsyncOperationChecker.check(controlFlowSummary, this.mAysncTaskField);
		}
	}
	
	public void printDummyMethod(){
		for(LifeCycleDummyMethod currentDummyMethod: this.mDummyMethodList){
			Log.i("======================================");
			for(String name:currentDummyMethod.getLifeCycleOrder()){
				Log.i(name);
			}
		}
	}
}