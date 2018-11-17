package cn.ac.ios.ad.lifecycle;

import java.util.ArrayList;
import java.util.List;

import cn.ac.ios.ad.summary.ActivityAsyncOperationMethodSummary;
import cn.ac.ios.ad.summary.alphabet.activity.ActivityOperationUnitSummary;

public class LifeCycleDummyMethod {
	private List<String> lifeCycleOrder;
	private List<ActivityAsyncOperationMethodSummary> methodSummaryList;
	private List<List<ActivityOperationUnitSummary>> controlFlowSummaryList;
	
	public List<List<ActivityOperationUnitSummary>> getControlFlowSummaryList() {
		return controlFlowSummaryList;
	}

	public void setControlFlowSummaryList(
			List<List<ActivityOperationUnitSummary>> controlFlowSummaryList) {
		this.controlFlowSummaryList = controlFlowSummaryList;
	}

	public void constructControlFlowSummaryList(){
		this.controlFlowSummaryList = new ArrayList<List<ActivityOperationUnitSummary>>();
		for(int i=0;i < this.methodSummaryList.size(); i++){
			if( this.controlFlowSummaryList.size() == 0 )
				this.controlFlowSummaryList.addAll(this.methodSummaryList.get(i).getPurifiedControlFlowSummaryList());
			else{
				for( List<ActivityOperationUnitSummary> controlFlowSummary:this.controlFlowSummaryList )
					for( List<ActivityOperationUnitSummary> currentControlFlowSummary:this.methodSummaryList.get(i).getPurifiedControlFlowSummaryList() )
						controlFlowSummary.addAll(currentControlFlowSummary);
			}
		}
	}
	
	public List<ActivityAsyncOperationMethodSummary> getMethodSummaryList() {
		return methodSummaryList;
	}

	public void setMethodSummaryList(List<ActivityAsyncOperationMethodSummary> methodSummary) {
		this.methodSummaryList = methodSummary;
	}

	public List<String> getLifeCycleOrder() {
		return lifeCycleOrder;
	}

	public void setLifeCycleOrder(List<String> lifeCycleOrder) {
		this.lifeCycleOrder = lifeCycleOrder;
	}

	public LifeCycleDummyMethod(List<String> lifeCycleOrder,List<ActivityAsyncOperationMethodSummary> theSummary){
		this.lifeCycleOrder = lifeCycleOrder;
		this.methodSummaryList = theSummary;
		this.constructControlFlowSummaryList();
	}
}
