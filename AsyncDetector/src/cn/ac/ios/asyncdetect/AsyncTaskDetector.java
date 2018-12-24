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

package cn.ac.ios.asyncdetect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.ac.ios.asyncdetect.constant.ListenerSignature;
import cn.ac.ios.asyncdetect.constant.MethodSignature;
import cn.ac.ios.asyncdetect.lifecycle.LifeCycleSummaryProcess;
import cn.ac.ios.asyncdetect.record.RecordController;
import cn.ac.ios.asyncdetect.summary.ActivityAsyncOperationMethodSummary;
import cn.ac.ios.asyncdetect.summary.topology.ActivityGetFieldTopologyOperation;
import cn.ac.ios.asyncdetect.summary.topology.ActivityTopologyOperation;
import cn.ac.ios.asyncdetect.summary.topology.TopologyOperation;
import cn.ac.ios.asyncdetect.util.ClassInheritanceProcess;
import cn.ac.ios.asyncdetect.util.Log;
import soot.Body;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

/**
 * 
 * @author Linjie Pan
 * @version 1.0 
 */
public class AsyncTaskDetector{
	
	public static Map<SootMethod,Boolean> sSootMethodToIsContainAsyncOperation = new HashMap<SootMethod,Boolean>();
	
	public static Map<String,Body> sMethodSignatureToBody = new HashMap<String,Body>();
	
	/**
	 * The set of listeners in the APK
	 * Currently, we only consider the class which implements View.OnClickListener and is the inner class of Activity
	 */
	public static Set<SootClass> sListenerClasses = new HashSet<SootClass>();
	
	private static CallGraph sCg;
	
	public void setCallGraph(CallGraph cg){
		AsyncTaskDetector.sCg = cg;
	}
	
	public void addEdgeToCG(Edge edge){
		sCg.addEdge(edge);
	}
	
	public static CallGraph getCallGraph(){
		return AsyncTaskDetector.sCg;
	}
	
	/**
	 * update the call graph of the apk under analysis 
	 * @param b 
	 */
	private void updateCallGraph(Body b){
		UnitGraph theGraph = new BriefUnitGraph(b);
		Iterator<Unit> it = theGraph.iterator();
		
		while( it.hasNext() ){
			Stmt currentStmt = (Stmt)it.next();
			
			//According to the grammar of jimple, InvokeExpr could appear in InvokeStmt or AssignStmt.
			if( currentStmt instanceof InvokeStmt && 
					sMethodSignatureToBody.get(currentStmt.getInvokeExpr().getMethod().getSignature()) != null){
				Edge e = new Edge(b.getMethod(),currentStmt,currentStmt.getInvokeExpr().getMethod());
				AsyncTaskDetector.sCg.addEdge(e);
			}
			else if( currentStmt instanceof AssignStmt && 
					((AssignStmt)currentStmt).getRightOp() instanceof InvokeExpr && 
					sMethodSignatureToBody.get(((InvokeExpr)((AssignStmt)currentStmt).getRightOp()).getMethod().getSignature()) != null){
				Edge e = new Edge(b.getMethod(),currentStmt,((InvokeExpr)((AssignStmt)currentStmt).getRightOp()).getMethod());
				AsyncTaskDetector.sCg.addEdge(e);
			}
		}
	}
	
	/**
	 * Construct the call graph of the apk under analysis
	 */
	public void constructCallGraph(){
		AsyncTaskDetector.sCg = new CallGraph();
		for(SootClass currentClass: Scene.v().getApplicationClasses()){
			List<SootMethod> methods = currentClass.getMethods();
			for(int i=0;i<methods.size();i++){
				SootMethod currentMethod = methods.get(i);
				Body b = AsyncTaskDetector.sMethodSignatureToBody.get(currentMethod.getSignature());
				if( b != null)
					this.updateCallGraph(b);
			}
		}
	}
	
	public boolean detectAsyncTask(){
		boolean correctUse=true;
		this.constructCallGraph();//construct the call graph before detection
		for(SootClass currentClass:Scene.v().getApplicationClasses()){
			
			//Traverse each method of current class, generate the summary of their return value
			for(SootMethod currentMethod:currentClass.getMethods()){
				if( !ClassInheritanceProcess.isReturnTypeAsyncTask(currentMethod) || 
						AsyncTaskDetector.sMethodSignatureToBody.get(currentMethod.getSignature()) == null )
					continue;
				TopologyOperation operation = new ActivityGetFieldTopologyOperation(currentMethod);
				operation.constructMainSummary(AsyncTaskDetector.sCg);
			}
			
			if( !ClassInheritanceProcess.isInheritedFromActivity(currentClass))//We only consider AsyncTask instances which are used in Activity
				continue;
			
			Map<SootClass,List<SootClass>> activityToListeners = new HashMap<SootClass,List<SootClass>>();
			activityToListeners.put(currentClass, new ArrayList<SootClass>());
			for( SootClass listener:sListenerClasses )//Processing listeners declared in current Activity 
				if( listener.getOuterClass() == currentClass )
					activityToListeners.get(currentClass).add(listener);
			
			if( ClassInheritanceProcess.isInheritedFromOnClickListener(currentClass))
				activityToListeners.get(currentClass).add(currentClass);
			
			Chain<SootField> fieldList = currentClass.getFields();
			for(SootField currentField:fieldList){
				if( currentField.getType() instanceof RefType){
					SootClass currentFieldClass = ((RefType)currentField.getType()).getSootClass();
					if( ClassInheritanceProcess.isInheritedFromAsyncTask(currentFieldClass)){
						RecordController.getInstance().getAsyncTaskFieldOutput().add(currentField);
						this.isFieldCorrectlyUse(currentField,activityToListeners);
					}
				}
			}
		}
		return correctUse;
	}
	
	private boolean isFieldCorrectlyUse(SootField asyncTaskField,Map<SootClass,List<SootClass>> activityToListeners){
		Log.i("--------------------");
		SootClass activityClass = asyncTaskField.getDeclaringClass();
		SootMethod lifeCycleMethodArray[] = new SootMethod[MethodSignature.LIFE_CYCLE_NUMBER];
		for(int i=0;i < lifeCycleMethodArray.length; i++)
			lifeCycleMethodArray[i] = activityClass.getMethodUnsafe(MethodSignature.signatureArray[i]);
		
		Map<String,ActivityAsyncOperationMethodSummary> signatureToSummary = new HashMap<String, ActivityAsyncOperationMethodSummary>();
		ActivityTopologyOperation lifeCycleOperationArray[] = new ActivityTopologyOperation[MethodSignature.LIFE_CYCLE_NUMBER];
		for(int i=0;i < lifeCycleMethodArray.length; i++){
			if( lifeCycleMethodArray[i] == null){
				continue;
			}
			lifeCycleOperationArray[i] = new ActivityTopologyOperation(lifeCycleMethodArray[i],asyncTaskField,activityClass);
			lifeCycleOperationArray[i].constructMainSummary(AsyncTaskDetector.sCg);
			
			//We only consider the life cycle method whose summary is not null
			boolean isNull = ((ActivityAsyncOperationMethodSummary) lifeCycleOperationArray[i].getSourceMethodSummary()).isNull();
			if( !isNull ){
				signatureToSummary.put(MethodSignature.signatureArray[i],(ActivityAsyncOperationMethodSummary)lifeCycleOperationArray[i].getSourceMethodSummary());
				RecordController.getInstance().getAsyncTaskActivityLifeCycleMethodOutput().add(asyncTaskField,MethodSignature.signatureArray[i],((ActivityAsyncOperationMethodSummary)lifeCycleOperationArray[i].getSourceMethodSummary()).getControlFlowSummaryList());
				RecordController.getInstance().getAsyncTaskActivityLifeCycleMethodOutput().addPurified(asyncTaskField,MethodSignature.signatureArray[i],((ActivityAsyncOperationMethodSummary)lifeCycleOperationArray[i].getSourceMethodSummary()).getPurifiedControlFlowSummaryList());
			}
			RecordController.getInstance().getLifeCycleMethodLoopOutput().add(asyncTaskField, lifeCycleMethodArray[i],lifeCycleOperationArray[i].hasLoop(), isNull?0:1);
		}
		
		List<SootClass> listenerOperationList = activityToListeners.get(asyncTaskField.getDeclaringClass());
		ActivityTopologyOperation listenerOperationArray[] = new ActivityTopologyOperation[listenerOperationList.size()];
		for(int i=0;i < listenerOperationList.size(); i++){
			SootMethod onClickMethod = listenerOperationList.get(i).getMethodUnsafe(ListenerSignature.ON_CLICK);
			if( onClickMethod == null )
				continue;
			listenerOperationArray[i] = new ActivityTopologyOperation(onClickMethod,asyncTaskField,activityClass);
			listenerOperationArray[i].constructMainSummary(AsyncTaskDetector.sCg);
			boolean isNull = ((ActivityAsyncOperationMethodSummary) listenerOperationArray[i].getSourceMethodSummary()).isNull();
			if( !isNull ){
				RecordController.getInstance().getAsyncTaskListenerOutput().add(asyncTaskField, onClickMethod.getSignature(), ((ActivityAsyncOperationMethodSummary) listenerOperationArray[i].getSourceMethodSummary()).getControlFlowSummaryList());
				RecordController.getInstance().getAsyncTaskListenerOutput().addPurified(asyncTaskField, onClickMethod.getSignature(), ((ActivityAsyncOperationMethodSummary) listenerOperationArray[i].getSourceMethodSummary()).getPurifiedControlFlowSummaryList());
			}
			RecordController.getInstance().getLifeCycleMethodLoopOutput().add(asyncTaskField, onClickMethod,listenerOperationArray[i].hasLoop(), isNull?0:1);
		}
		LifeCycleSummaryProcess summaryProcess = new LifeCycleSummaryProcess(asyncTaskField);
		summaryProcess.dummyMethodDetection(signatureToSummary, listenerOperationArray);
		return true;
	}
}