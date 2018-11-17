package cn.ac.ios.ad.summary.topology;

import cn.ac.ios.ad.summary.AbstractMethodSummary;
import cn.ac.ios.ad.summary.DoInBackgroundMethodSummary;
import soot.SootMethod;

public class DoInBackgroundTopologyOperation extends TopologyOperation {

	public DoInBackgroundTopologyOperation(SootMethod sourceMethod) {
		super(sourceMethod);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void printTopologyGraph() {
		// TODO Auto-generated method stub
	}
	
	/**
	 * key=methodSignature+"-DO_IN_BACKGROUND";
	 * @param theMethod
	 * @return
	 */
	public static String getDoInBackgroundKey(SootMethod theMethod){
		return theMethod.getSignature()+"-DO_IN_BACKGROUND";
	}
	
	
	@Override
	public AbstractMethodSummary getSourceMethodSummary() {
		// TODO Auto-generated method stub
		return TopologyOperation.methodKeyToSummary.get(getDoInBackgroundKey(this.sourceMethod));
	}

	@Override
	public String getKey(SootMethod theMethod) {
		// TODO Auto-generated method stub
		return getDoInBackgroundKey(theMethod);
	}

	@Override
	public AbstractMethodSummary getMethodSummary(TopologyNode topNode) {
		// TODO Auto-generated method stub
		return new DoInBackgroundMethodSummary(topNode.method);
	}
	
	

}
