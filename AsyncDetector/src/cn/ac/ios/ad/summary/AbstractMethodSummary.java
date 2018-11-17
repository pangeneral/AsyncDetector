package cn.ac.ios.ad.summary;

import cn.ac.ios.ad.AsyncTaskDetector;
import cn.ac.ios.ad.util.Log;
import soot.Body;
import soot.SootMethod;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

/**
 * Base class of method summary
 * @author panlj
 *
 */
public abstract class AbstractMethodSummary {
	protected SootMethod methodUnderAnalysis;
	
	public SootMethod getMethodUnderAnalysis() {
		return methodUnderAnalysis;
	}

	public void setMethodUnderAnalysis(SootMethod methodUnderAnalysis) {
		this.methodUnderAnalysis = methodUnderAnalysis;
	}

	public void generateMethodSummary(){
		Body b = AsyncTaskDetector.methodSignatureToBody.get(this.methodUnderAnalysis.getSignature());
		System.out.println(this.methodUnderAnalysis.getSignature());
		UnitGraph theGraph = new BriefUnitGraph(b);
		generation(theGraph);
	}
	
	public abstract void printMethodSummary();
	
	/**
	 * Abstract method to generate method summary.
	 * @param theGraph
	 */
	protected abstract void generation(UnitGraph theGraph);
	
	public AbstractMethodSummary(SootMethod methodUnderAnalysis){
		this.methodUnderAnalysis = methodUnderAnalysis;
	}
}
