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
import java.util.List;

import cn.ac.ios.asyncdetect.summary.ActivityAsyncOperationMethodSummary;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.ActivityOperationUnitSummary;

/**
 * Life cycle 
 * @author Linjie Pan
 * @version 1.0
 */
public class LifeCycleDummyMethod {
	private List<String> mLifeCycleOrder;
	private List<ActivityAsyncOperationMethodSummary> mMethodSummaryList;
	private List<List<ActivityOperationUnitSummary>> mControlFlowSummaryList;
	
	public List<List<ActivityOperationUnitSummary>> getControlFlowSummaryList() {
		return mControlFlowSummaryList;
	}

	public void setControlFlowSummaryList(
			List<List<ActivityOperationUnitSummary>> controlFlowSummaryList) {
		this.mControlFlowSummaryList = controlFlowSummaryList;
	}

	public void constructControlFlowSummaryList(){
		this.mControlFlowSummaryList = new ArrayList<List<ActivityOperationUnitSummary>>();
		for(int i=0;i < this.mMethodSummaryList.size(); i++){
			if( this.mControlFlowSummaryList.size() == 0 )
				this.mControlFlowSummaryList.addAll(this.mMethodSummaryList.get(i).getPurifiedControlFlowSummaryList());
			else{
				for( List<ActivityOperationUnitSummary> controlFlowSummary:this.mControlFlowSummaryList )
					for( List<ActivityOperationUnitSummary> currentControlFlowSummary:this.mMethodSummaryList.get(i).getPurifiedControlFlowSummaryList() )
						controlFlowSummary.addAll(currentControlFlowSummary);
			}
		}
	}
	
	public List<ActivityAsyncOperationMethodSummary> getMethodSummaryList() {
		return mMethodSummaryList;
	}

	public void setMethodSummaryList(List<ActivityAsyncOperationMethodSummary> methodSummary) {
		this.mMethodSummaryList = methodSummary;
	}

	public List<String> getLifeCycleOrder() {
		return mLifeCycleOrder;
	}

	public void setLifeCycleOrder(List<String> lifeCycleOrder) {
		this.mLifeCycleOrder = lifeCycleOrder;
	}

	public LifeCycleDummyMethod(List<String> lifeCycleOrder,List<ActivityAsyncOperationMethodSummary> theSummary){
		this.mLifeCycleOrder = lifeCycleOrder;
		this.mMethodSummaryList = theSummary;
		this.constructControlFlowSummaryList();
	}
}
