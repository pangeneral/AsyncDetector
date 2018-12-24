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

package cn.ac.ios.asyncdetect.summary.topology;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;

import cn.ac.ios.asyncdetect.summary.AbstractMethodSummary;
import cn.ac.ios.asyncdetect.util.Log;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

/**
 * Construct the summary of source method through topological sort.
 * During the construction process, the summaries of methods that invoked by source method are also constructed. 
 * @author Linjie Pan
 * @version 1.0
 */
public abstract class TopologyOperation {
	protected Map<SootMethod,TopologyNode> mMethodToNode;
	protected int mRingNumber;
	protected boolean hasLoop = false;
	protected SootMethod mSourceMethod;
	
	/**
	 * The value of methodKey depends on the type of method summary
	 */
	protected static Map<String,AbstractMethodSummary> sMethodKeyToSummary= new HashMap<String,AbstractMethodSummary>();
	
	public static Map<String, AbstractMethodSummary> getsMethodKeyToSummary() {
		return sMethodKeyToSummary;
	}

	public SootMethod getSourceMethod() {
		return mSourceMethod;
	}

	public void setSourceMethod(SootMethod sourceMethod) {
		this.mSourceMethod = sourceMethod;
	}
	
	public TopologyOperation(SootMethod sourceMethod){
		this.mSourceMethod = sourceMethod;
		this.mRingNumber = 0;
	}
	
	public abstract String getKey(SootMethod theMethod);
	
	public abstract AbstractMethodSummary getSourceMethodSummary();
	
	public abstract AbstractMethodSummary getMethodSummary(TopologyNode topNode);
	
	
	public boolean hasLoop(){
		return hasLoop;
	}
	
	protected void printTopologyGraph(){
		Log.i("Source method is " + this.mSourceMethod.getSignature() + " "+ this.getClass().getName());
		for(Entry<SootMethod, TopologyNode> entry: this.mMethodToNode.entrySet()){
			if( entry.getValue().mOutDegree == 0 )
				continue;
			Log.i("Outdegree of "+entry.getValue().mMethod.getSignature()+" is "+entry.getValue().mOutDegree);
			for(TopologyNode currentNode:entry.getValue().mPointingNodes){
				Log.i(entry.getValue().mMethod.getSignature()+"==>"+currentNode.mMethod.getSignature());
			}
			Log.i("==================================================");
		}
	}
	
	
	
	
	/**
	 * Main Summary is the summary of method which is the top method, i.e., life cycle method of activity or listener, in a call graph 
	 * @param cg
	 * @param fieldUnderAnalysis
	 */
	public void constructMainSummary(CallGraph cg){
		this.mMethodToNode = new HashMap<SootMethod,TopologyNode>();
		TopologyNode sourceNode = new TopologyNode(mSourceMethod,null);
		this.mMethodToNode.put(mSourceMethod, sourceNode);
		this.constructTopologyGraph(cg,this.mSourceMethod);//init the call graph for topology sort
		
		Stack<TopologyNode> topStack = new Stack<TopologyNode>();//stack for top sort
		this.printTopologyGraph();
		for(Map.Entry<SootMethod,TopologyNode> entry:this.mMethodToNode.entrySet()){//node whose out degree is zero will be pushed into the stack
			TopologyNode node = entry.getValue();
			if( node.mOutDegree == 0){
				topStack.add(node);
//				Log.i(node.method.getName()+" push");
				node.mEverInStack=true;
			}
		}
		while( !topStack.empty()){
			TopologyNode topNode = topStack.pop();
//			Log.i(topNode.method.getName()+" pop");
//			this.printNodeMessage(topNode);
			assert(topNode.mMethod != null);
			String key = this.getKey(topNode.mMethod);
			if( TopologyOperation.sMethodKeyToSummary.get(key) == null){
				AbstractMethodSummary currentMethodSummary = this.getMethodSummary(topNode);
				currentMethodSummary.generateMethodSummary();
//				currentMethodSummary.printMethodSummary();
				TopologyOperation.sMethodKeyToSummary.put(key,currentMethodSummary);
			}
			for(Map.Entry<SootMethod,TopologyNode> entry:this.mMethodToNode.entrySet()){//update the out degree of nodes influenced by the top node in the stack
				TopologyNode node = entry.getValue();
				if( node.mEverInStack )
					continue;
				if( node.mPointingNodes.contains(topNode))
					node.mOutDegree--;
				if( node.mOutDegree == 0){
//					Log.i(node.method.getName()+" push");
					topStack.add(node);
					node.mEverInStack=true;
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void printNodeMessage(TopologyNode node){
		Log.i("current method is: "+node.mMethod);
		Log.i("outDegree is: "+node.mOutDegree);
		for(TopologyNode currentNode:node.mPointingNodes){
			if( currentNode.mStmt != null)
				Log.i(node.mStmt.toString()+" "+node.mMethod.getName()+"==>"+currentNode.mStmt.toString()+" "+currentNode.mMethod.getName());
			else
				Log.i(node.mMethod.getName()+"==>"+currentNode.mStmt.toString()+" "+currentNode.mMethod.getName());
		}
	}
	
	private void constructTopologyGraph(CallGraph cg,SootMethod sourceMethod){		
		TopologyNode node = this.mMethodToNode.get(sourceMethod);
		Iterator<Edge> it = cg.edgesOutOf(sourceMethod);
		while( it.hasNext() ){
			Edge e = it.next();
			Stmt invokedStmt = e.srcStmt();
			TopologyNode nextNode = this.mMethodToNode.get(e.tgt());
			if( !this.isPathExist(nextNode, node) && !node.mPointingNodes.contains(nextNode) ){//avoid ring in the topology graph
				node.mOutDegree++;
				if( nextNode == null){
					nextNode = new TopologyNode(e.tgt(),invokedStmt);
					this.mMethodToNode.put(e.tgt(), nextNode);
				}
				node.mPointingNodes.add(nextNode);
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
		if( start.mMethod == end.mMethod ){
			hasLoop = true;
			return true;
		}
		List<TopologyNode> nodes = start.mPointingNodes;
		for(TopologyNode node:nodes){
			if( this.isPathExist(this.mMethodToNode.get(node.mMethod),end)){
				return true;
			}
				
		}
		return false;
	}
}
