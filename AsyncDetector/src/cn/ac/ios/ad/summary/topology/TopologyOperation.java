package cn.ac.ios.ad.summary.topology;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;

import cn.ac.ios.ad.summary.AbstractMethodSummary;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

/**
 * Construct the summary of source method through topological sort.
 * During the construction process, the summaries of methods that invoked by source method are also constructed. 
 * @author panlj
 *
 */
public abstract class TopologyOperation {
//	protected Map<Stmt,TopologyNode> stmtToNode;//topology graph used for method summary construction
	protected Map<SootMethod,TopologyNode> methodToNode;
	protected int ringNumber;
	protected boolean hasLoop = false;
	protected SootMethod sourceMethod;
	
	public SootMethod getSourceMethod() {
		return sourceMethod;
	}

	public void setSourceMethod(SootMethod sourceMethod) {
		this.sourceMethod = sourceMethod;
	}

	/**
	 * The value of methodKey depends on the type of method summary
	 */
	public static Map<String,AbstractMethodSummary> methodKeyToSummary= new HashMap<String,AbstractMethodSummary>();
	
	public TopologyOperation(SootMethod sourceMethod){
		this.sourceMethod = sourceMethod;
		this.ringNumber = 0;
	}
	
	public abstract String getKey(SootMethod theMethod);
	
	public abstract AbstractMethodSummary getSourceMethodSummary();
	
	public abstract AbstractMethodSummary getMethodSummary(TopologyNode topNode);
	
	public boolean hasLoop(){
		return hasLoop;
	}
	
	protected void printTopologyGraph(){
		System.out.println("Source method is " + this.sourceMethod.getSignature());
		for(Entry<SootMethod, TopologyNode> entry: this.methodToNode.entrySet()){
			if( entry.getValue().outDegree == 0 )
				continue;
			System.out.println("Outdegree of "+entry.getValue().method.getSignature()+" is "+entry.getValue().outDegree);
			for(TopologyNode currentNode:entry.getValue().pointingNodes){
				System.out.println(entry.getValue().method.getSignature()+"==>"+currentNode.method.getSignature());
			}
			System.out.println("==================================================");
		}
	}
	
	
	/**
	 * Main Summary is the summary of method which is the top method, i.e., life cycle method of activity or listener, in a call graph 
	 * @param cg
	 * @param fieldUnderAnalysis
	 */
	public void constructMainSummary(CallGraph cg){
		this.methodToNode = new HashMap<SootMethod,TopologyNode>();
		TopologyNode sourceNode = new TopologyNode(sourceMethod,null);
		this.methodToNode.put(sourceMethod, sourceNode);
		this.constructTopologyGraph(cg,this.sourceMethod);//init the call graph for topology sort
		
		Stack<TopologyNode> topStack = new Stack<TopologyNode>();//stack for top sort
		this.printTopologyGraph();
		for(Map.Entry<SootMethod,TopologyNode> entry:this.methodToNode.entrySet()){//node whose out degree is zero will be pushed into the stack
			TopologyNode node = entry.getValue();
			if( node.outDegree == 0){
				topStack.add(node);
//				System.out.println(node.method.getName()+" push");
				node.everInStack=true;
			}
		}
		while( !topStack.empty()){
			TopologyNode topNode = topStack.pop();
//			System.out.println(topNode.method.getName()+" pop");
//			this.printNodeMessage(topNode);
			assert(topNode.method != null);
			String key = this.getKey(topNode.method);
			if( TopologyOperation.methodKeyToSummary.get(key) == null){
				AbstractMethodSummary currentMethodSummary = this.getMethodSummary(topNode);
				currentMethodSummary.generateMethodSummary();
//				currentMethodSummary.printMethodSummary();
				TopologyOperation.methodKeyToSummary.put(key,currentMethodSummary);
			}
			for(Map.Entry<SootMethod,TopologyNode> entry:this.methodToNode.entrySet()){//update the out degree of nodes influenced by the top node in the stack
				TopologyNode node = entry.getValue();
				if( node.everInStack )
					continue;
				if( node.pointingNodes.contains(topNode))
					node.outDegree--;
				if( node.outDegree == 0){
//					System.out.println(node.method.getName()+" push");
					topStack.add(node);
					node.everInStack=true;
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void printNodeMessage(TopologyNode node){
		System.out.println("current method is: "+node.method);
		System.out.println("outDegree is: "+node.outDegree);
		for(TopologyNode currentNode:node.pointingNodes){
			if( currentNode.stmt != null)
				System.out.println(node.stmt.toString()+" "+node.method.getName()+"==>"+currentNode.stmt.toString()+" "+currentNode.method.getName());
			else
				System.out.println(node.method.getName()+"==>"+currentNode.stmt.toString()+" "+currentNode.method.getName());
		}
	}
	
	/**
	 * The call graph offered by Soot is organized by edge, which is hard to carry out topology sort.
	 * This method construct a call graph organized by vertex through analyzing the original edge-based call graph
	 * During the construction of topology graph, potential cycle is considered and removed
	 * @param cg 
	 * @param sourceMethod
	 */
//	private void constructTopologyGraph(CallGraph cg,Stmt contextStmt,SootMethod sourceMethod){		
//		TopologyNode node = this.stmtToNode.get(contextStmt);
//		Iterator<Edge> it = cg.edgesOutOf(sourceMethod);
//		while( it.hasNext() ){
//			Edge e = it.next();
//			Stmt invokedStmt = e.srcStmt();
//			TopologyNode nextNode = this.stmtToNode.get(invokedStmt);
//			if( !this.isPathExist(nextNode, node)){//avoid ring in the topology graph
//				node.outDegree++;
//				if( nextNode == null){
//					nextNode = new TopologyNode(e.tgt(),invokedStmt);
//					this.stmtToNode.put(invokedStmt, nextNode);
//				}
//				node.pointingNodes.add(nextNode);
//				this.constructTopologyGraph(cg,invokedStmt,e.tgt());
//			}
//		}
//	}
	
	private void constructTopologyGraph(CallGraph cg,SootMethod sourceMethod){		
		TopologyNode node = this.methodToNode.get(sourceMethod);
		Iterator<Edge> it = cg.edgesOutOf(sourceMethod);
		while( it.hasNext() ){
			Edge e = it.next();
			Stmt invokedStmt = e.srcStmt();
			TopologyNode nextNode = this.methodToNode.get(e.tgt());
			if( !this.isPathExist(nextNode, node) && !node.pointingNodes.contains(nextNode) ){//avoid ring in the topology graph
				node.outDegree++;
				if( nextNode == null){
					nextNode = new TopologyNode(e.tgt(),invokedStmt);
					this.methodToNode.put(e.tgt(), nextNode);
				}
				node.pointingNodes.add(nextNode);
				this.constructTopologyGraph(cg,e.tgt());
			}
		}
	}
	
	/**
	 * Judge whether there is a path in the toplogy graph from node 'start' to node 'end' by dfs
	 * @param start
	 * @param end
	 * @return
	 */
	private boolean isPathExist(TopologyNode start,TopologyNode end){
		if( start == null )
			return false;
		if( start.method == end.method ){
			hasLoop = true;
			return true;
		}
		List<TopologyNode> nodes = start.pointingNodes;
		for(TopologyNode node:nodes){
			if( this.isPathExist(this.methodToNode.get(node.method),end)){
				return true;
			}
				
		}
		return false;
	}
}
