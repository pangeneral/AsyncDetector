package cn.ac.ios.ad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.ac.ios.ad.constant.MethodSignature;
import cn.ac.ios.ad.constant.ListenerSignature;
import cn.ac.ios.ad.lifecycle.LifeCycleSummaryProcess;
import cn.ac.ios.ad.record.RecordController;
import cn.ac.ios.ad.summary.ActivityAsyncOperationMethodSummary;
import cn.ac.ios.ad.summary.topology.ActivityGetFieldTopologyOperation;
import cn.ac.ios.ad.summary.topology.ActivityTopologyOperation;
import cn.ac.ios.ad.summary.topology.TopologyOperation;
import cn.ac.ios.ad.util.ClassInheritanceProcess;
import soot.Body;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.ClassConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

public class AsyncTaskDetector{
//	public final static Map<String,SootClass> classNameToSootClass = new HashMap<String,SootClass>();
	public final static Map<String,Body> methodSignatureToBody = new HashMap<String,Body>();
//	public final static Map<SootClass,List<SootClass>> activityToListeners = new HashMap<SootClass,List<SootClass>>();
	
	/**
	 * The set of async class in the APK
	 */
//	public final static Set<SootClass> asyncTaskClasses = new HashSet<SootClass>();
	
	/**
	 * The set of listeners in the APK
	 * Currently, we only consider the class which implements View.OnClickListener and is the inner class of Activity
	 */
	public final static Set<SootClass> listenerClasses = new HashSet<SootClass>();
	
	public static CallGraph cg;
	
	private PointsToAnalysis pta;
	
	public void setCallGraph(CallGraph cg){
		AsyncTaskDetector.cg = cg;
	}

	public PointsToAnalysis getPointsToAnalysis(){
		return this.pta;
	}
	
	public void setPointsToAnalysis(PointsToAnalysis pta){
		this.pta = pta;
	}
	
	public void addEdgeToCG(Edge edge){
		cg.addEdge(edge);
	}
	
	public static CallGraph getCallGraph(){
		return AsyncTaskDetector.cg;
	}
	
	
//	public void addAsyncTaskClasses(SootClass asyncClass){
//		asyncTaskClasses.add(asyncClass);
//	}
	
	/**
	 * update the call graph of detector 
	 * @param b 
	 */
	private void updateCallGraph(Body b){
		UnitGraph theGraph = new BriefUnitGraph(b);
		Iterator<Unit> it = theGraph.iterator();
		//According to the grammar of jimple, InvokeExpr could appear in InvokeStmt or AssignStmt.
		while( it.hasNext() ){
			Stmt currentStmt = (Stmt)it.next();
						
			if( currentStmt instanceof InvokeStmt && AsyncTaskDetector.methodSignatureToBody.get(currentStmt.getInvokeExpr().getMethod().getSignature()) != null){
				Edge e = new Edge(b.getMethod(),currentStmt,currentStmt.getInvokeExpr().getMethod());
				AsyncTaskDetector.cg.addEdge(e);
			}
			else if( currentStmt instanceof AssignStmt && ((AssignStmt)currentStmt).getRightOp() instanceof InvokeExpr && 
					AsyncTaskDetector.methodSignatureToBody.get(((InvokeExpr)((AssignStmt)currentStmt).getRightOp()).getMethod().getSignature()) != null){
				Edge e = new Edge(b.getMethod(),currentStmt,((InvokeExpr)((AssignStmt)currentStmt).getRightOp()).getMethod());
				AsyncTaskDetector.cg.addEdge(e);
			}
		}
	}
	
	public void constructCallGraph(){
		AsyncTaskDetector.cg = new CallGraph();
		for(SootClass currentClass: Scene.v().getApplicationClasses()){
			List<SootMethod> methods = currentClass.getMethods();
			for(int i=0;i<methods.size();i++){
				SootMethod currentMethod = methods.get(i);
				Body b = AsyncTaskDetector.methodSignatureToBody.get(currentMethod.getSignature());
				if( b != null)
					this.updateCallGraph(b);
			}
		}
	}
	
	public boolean detectAsyncTask(){
		boolean correctUse=true;
		this.initClassMessage();
		this.constructCallGraph();//construct the call graph before detection
//		for(SootClass asyncClass:asyncTaskClasses){
//			if(asyncClass.isAbstract()){
//				continue;
//			}
//			for(SootMethod method:asyncClass.getMethods()){
//				if( method.getName().equals("<init>")){
//					TopologyOperation to = new InitMethodTopologyOperation(method, asyncClass);
//					to.constructMainSummary(this.cg);
//					//TODO Sum
//					//TODO ((AsyncTaskInitMethodSummary)to.getSourceMethodSummary()).getDirtyArgIndexToTaintedField()
//					RecordController.getInstance().getAsyncTaskMethodOutput().addAsyncTaskInitMethodRecord(method.getDeclaringClass(), ((AsyncTaskInitMethodSummary)to.getSourceMethodSummary()).getDirtyArgIndexToTaintedField().size());
//				}
//			}
//			SootMethod doInBackgroundMethod = MethodUtil.getMethod(asyncClass, "doInBackground");
//			if( doInBackgroundMethod!=null){
//				TopologyOperation to = new DoInBackgroundTopologyOperation(doInBackgroundMethod);
//				to.constructMainSummary(this.cg);
//				//TODO Sum
//				//((DoInBackgroundMethodSummary)to.getSourceMethodSummary()).getLoopStartUnits()
//				RecordController.getInstance().getAsyncTaskMethodOutput().addAsyncTaskDoInBackgroundMethodRecord(doInBackgroundMethod.getDeclaringClass(), ((DoInBackgroundMethodSummary)to.getSourceMethodSummary()).getLoopStartUnits().size());
//			}
//		}
		for(SootClass currentClass:Scene.v().getApplicationClasses()){
			//Traverse each method of current class, generate the summary of their return value
			for(SootMethod currentMethod:currentClass.getMethods()){
				if( !ClassInheritanceProcess.isReturnTypeAsyncTask(currentMethod) || AsyncTaskDetector.methodSignatureToBody.get(currentMethod.getSignature()) == null )
					continue;
				TopologyOperation operation = new ActivityGetFieldTopologyOperation(currentMethod);
				operation.constructMainSummary(AsyncTaskDetector.cg);
			}
			
			if( !ClassInheritanceProcess.isInheritedFromActivity(currentClass))//We only consider AsyncTask instances which are used in Activity
				continue;
			
			//TODO Map改成List
			Map<SootClass,List<SootClass>> activityToListeners = new HashMap<SootClass,List<SootClass>>();
			activityToListeners.put(currentClass, new ArrayList<SootClass>());
			for( SootClass listener:listenerClasses )//Processing listeners declared in current Activity 
				if( listener.getOuterClass() == currentClass )
					activityToListeners.get(currentClass).add(listener);
			
			if( ClassInheritanceProcess.isInheritedFromOnClickListener(currentClass))
				activityToListeners.get(currentClass).add(currentClass);
			
			//NOTE: Currently, we only traverse field of Activity.
			//However, Activity's field could contain a AsyncTask field. It should be considered later.
			//Besides, field array should also be considered
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
		System.out.println("--------------------");
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
			lifeCycleOperationArray[i].constructMainSummary(AsyncTaskDetector.cg);
			//We only consider the life cycle method whose summary is not null
			boolean isNull = ((ActivityAsyncOperationMethodSummary) lifeCycleOperationArray[i].getSourceMethodSummary()).isNull();
			if( !isNull ){
				signatureToSummary.put(MethodSignature.signatureArray[i],(ActivityAsyncOperationMethodSummary)lifeCycleOperationArray[i].getSourceMethodSummary());
				//TODO Sum
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
			listenerOperationArray[i].constructMainSummary(AsyncTaskDetector.cg);
			boolean isNull = ((ActivityAsyncOperationMethodSummary) listenerOperationArray[i].getSourceMethodSummary()).isNull();
			if( !isNull ){
				//TODO Sum	
				RecordController.getInstance().getAsyncTaskListenerOutput().add(asyncTaskField, onClickMethod.getSignature(), ((ActivityAsyncOperationMethodSummary) listenerOperationArray[i].getSourceMethodSummary()).getControlFlowSummaryList());
				RecordController.getInstance().getAsyncTaskListenerOutput().addPurified(asyncTaskField, onClickMethod.getSignature(), ((ActivityAsyncOperationMethodSummary) listenerOperationArray[i].getSourceMethodSummary()).getPurifiedControlFlowSummaryList());
			}
			RecordController.getInstance().getLifeCycleMethodLoopOutput().add(asyncTaskField, onClickMethod,listenerOperationArray[i].hasLoop(), isNull?0:1);
		}
		LifeCycleSummaryProcess summaryProcess = new LifeCycleSummaryProcess(asyncTaskField);
		summaryProcess.dummyMethodDetection(signatureToSummary, listenerOperationArray);
		return true;
	}
	
	public void printPointsToAnalysis(SootField sf){
		PointsToSet pts = this.pta.reachingObjects(sf);
		System.out.println("*****************************************");
		System.out.println("Current field is "+sf.getSignature());
		System.out.println("isEmpty(): "+pts.isEmpty());
		System.out.print("PossibleTypes(): ");
		Set<Type> typeList = pts.possibleTypes();
		for(Type t: typeList){
			System.out.print(t.toString()+" ");
		}
		System.out.print("\n");
		Set<ClassConstant> constants = pts.possibleClassConstants();
		System.out.print("possibleClassConstants(): ");
		if( constants != null){
			for(ClassConstant cc:constants){
				System.out.print(cc.toString()+" ");
				System.out.print(cc.getValue()+" ");
			}
		}
		System.out.print("\n");
		System.out.println("*****************************************");
	}

	private void initClassMessage() {
		// TODO Auto-generated method stub
//		for(SootClass currentClass: Scene.v().getClasses()){
//			AsyncTaskDetector.classNameToSootClass.put(currentClass.getName(), currentClass);
//		}
	}
}