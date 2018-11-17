package cn.ac.ios.ad.summary.topology;

import java.util.Map.Entry;

import cn.ac.ios.ad.AsyncTaskDetector;
import cn.ac.ios.ad.summary.AbstractMethodSummary;
import cn.ac.ios.ad.summary.ActivityAsyncOperationMethodSummary;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.Stmt;

public class ActivityTopologyOperation extends TopologyOperation{
	private SootField fieldUnderAnalysis;
	private SootClass activityClass;
	
	public SootField getFieldUnderAnalysis() {
		return fieldUnderAnalysis;
	}

	public void setFieldUnderAnalysis(SootField fieldUnderAnalysis) {
		this.fieldUnderAnalysis = fieldUnderAnalysis;
	}

	public SootClass getActivityClass() {
		return activityClass;
	}

	public void setActivityClass(SootClass activityClass) {
		this.activityClass = activityClass;
	}

	public ActivityTopologyOperation(SootMethod sourceMethod,
			SootField fieldUnderAnalysis,SootClass activityClass) {
		super(sourceMethod);
		// TODO Auto-generated constructor stub
		this.fieldUnderAnalysis = fieldUnderAnalysis;
		this.activityClass = activityClass;
	}
	
	/**
	 * key=fieldSignature+"-"+methodSignature
	 * @param fieldUnderAnalysis
	 * @param methodUnderAnalysis
	 * @return
	 */
	public static String getActivityOperationKey(SootField fieldUnderAnalysis,SootMethod methodUnderAnalysis){
		return fieldUnderAnalysis.getSignature()+"-"+methodUnderAnalysis.getSignature();
	}
	
	@Override
	public String getKey(SootMethod theMethod) {
		// TODO Auto-generated method stub
		return getActivityOperationKey(fieldUnderAnalysis,theMethod);
	}
	
	protected void printTopologyGraph(){
		System.out.print("Field under analysis is " + this.fieldUnderAnalysis + " ");
		super.printTopologyGraph();
	}

	@Override
	public AbstractMethodSummary getSourceMethodSummary() {
		// TODO Auto-generated method stub
		return TopologyOperation.methodKeyToSummary.get(ActivityTopologyOperation.getActivityOperationKey(this.fieldUnderAnalysis,this.sourceMethod));
	}

	@Override
	public AbstractMethodSummary getMethodSummary(TopologyNode topNode) {
		// TODO Auto-generated method stub
		return new ActivityAsyncOperationMethodSummary(topNode.method,this.fieldUnderAnalysis,this.activityClass);
	}

	

}
