package cn.ac.ios.ad.summary.topology;

import java.util.ArrayList;
import java.util.List;
import soot.SootMethod;
import soot.jimple.Stmt;

/**
 * Graph node for top sort
 * @author panlj
 */
public class TopologyNode{
	SootMethod method;//the SootMethod represented by the node
	Stmt stmt;//the statement that invokes the SootMethod
	
	int outDegree;
	boolean everInStack;
	List<TopologyNode> pointingNodes;
	
	public List<TopologyNode> getPointingNodes(){
		return pointingNodes;
	}
	
	public TopologyNode(SootMethod method,Stmt stmt){
		this.method = method;
		this.stmt = stmt;
		this.outDegree = 0;
		this.everInStack = false;
		pointingNodes = new ArrayList<TopologyNode>();
	}
}
