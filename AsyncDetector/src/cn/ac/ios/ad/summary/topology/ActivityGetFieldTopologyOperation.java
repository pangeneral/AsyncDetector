package cn.ac.ios.ad.summary.topology;

import cn.ac.ios.ad.summary.AbstractMethodSummary;
import cn.ac.ios.ad.summary.ActivityGetFieldMethodSummary;
import soot.SootMethod;

public class ActivityGetFieldTopologyOperation extends TopologyOperation{
	
	public ActivityGetFieldTopologyOperation(SootMethod sourceMethod) {
		super(sourceMethod);
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractMethodSummary getSourceMethodSummary() {
		// TODO Auto-generated method stub
		return TopologyOperation.methodKeyToSummary.get(getGetFieldKey(this.sourceMethod));
	}
	
	/**
	 * key=methodSignature+"-GET_FIELD";
	 * @param theMethod
	 * @return
	 */
	public static String getGetFieldKey(SootMethod theMethod){
		return theMethod.getSignature()+"-GET_FIELD";
	}

	@Override
	public String getKey(SootMethod theMethod) {
		// TODO Auto-generated method stub
		return getGetFieldKey(theMethod);
	}

	@Override
	public AbstractMethodSummary getMethodSummary(TopologyNode topNode) {
		// TODO Auto-generated method stub
		return new ActivityGetFieldMethodSummary(topNode.method);
	}
}
