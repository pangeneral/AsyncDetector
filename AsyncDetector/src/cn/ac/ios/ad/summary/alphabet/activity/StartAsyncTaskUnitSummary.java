package cn.ac.ios.ad.summary.alphabet.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.ac.ios.ad.AsyncTaskDetector;
import cn.ac.ios.ad.summary.AsyncTaskInitMethodSummary;
import cn.ac.ios.ad.summary.alphabet.AbstractUnitSummary;
import cn.ac.ios.ad.summary.analysis.ActivityAsyncOperationJimpleAnalysis;
import cn.ac.ios.ad.summary.topology.TopologyOperation;
import cn.ac.ios.ad.util.ClassInheritanceProcess;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NullConstant;

/**
 * Start a AsyncTask by executing the "execute()" method of AsyncTask class
 * The field executeExpr represents the expr which executes the "execute" method 
 * @author panlj
 *
 */
public class StartAsyncTaskUnitSummary extends ActivityOperationUnitSummary implements HoldingReference{
	private List<Unit> unitList;
	private InvokeExpr executeExpr;
	/**
	 * If AsyncTask invoke "execute" method before it is assigned to field, then isBeforeAssignment is ture.
	 * For example,
	 * $r4 = virtualinvoke $r2.<android.os.AsyncTask: android.os.AsyncTask execute(java.lang.Object[])>($r3);
     * $r0.<com.example.listenertest.MainActivity: android.os.AsyncTask at> = $r4;
	 */
	private boolean isBeforeAssignment;
	
	private List<TaintInstance> taintInstances;
	
	public List<TaintInstance> getTaintInstances() {
		return taintInstances;
	}

	public void setTaintInstances(List<TaintInstance> taintInstances) {
		this.taintInstances = taintInstances;
	}

	public boolean isBeforeAssignment() {
		return isBeforeAssignment;
	}

	public void setBeforeAssignment(boolean isBeforeAssignment) {
		this.isBeforeAssignment = isBeforeAssignment;
	}

	public List<Unit> getUnitList() {
		return unitList;
	}

	public void setUnitList(List<Unit> unitList) {
		this.unitList = unitList;
	}

	public InvokeExpr getExecuteExpr() {
		return executeExpr;
	}

	public void setExecuteExpr(Unit unit) {
		if( unit instanceof InvokeStmt )
			this.executeExpr = ((InvokeStmt) unit).getInvokeExpr();
		else if (unit instanceof AssignStmt)
			this.executeExpr = (InvokeExpr) ((AssignStmt) unit).getRightOp();
	}

	public StartAsyncTaskUnitSummary(Unit unit,List<Unit> unitList,boolean isBeforeAssignment){
		super(unit);
		this.setExecuteExpr(unit);
		this.unitList = unitList;
		this.isBeforeAssignment = isBeforeAssignment;
		this.initTaintInstances();
	}
	
	public StartAsyncTaskUnitSummary(Unit unit,List<Unit> unitList,int argIndex,boolean isBeforeAssignment){
		super(unit,argIndex);
		this.setExecuteExpr(unit);
		this.unitList = unitList;
		this.isBeforeAssignment = isBeforeAssignment;
		this.initTaintInstances();
	}
	
	@Override
	public String getSummary() {
		// TODO Auto-generated method stub
		return SummaryAlphabet.START_ASYNC;
	}

	@Override
	public void initTaintInstances() {
		// TODO Auto-generated method stub
		//specialinvoke $r5.<com.example.fieldsensitivitytest.MainActivity$InnerTask: void <init>(com.example.fieldsensitivitytest.MainActivity)>($r0);
		//$r0.<com.example.fieldsensitivitytest.MainActivity: android.os.AsyncTask at> = $r5;
		this.taintInstances = new ArrayList<TaintInstance>();
		if( this.executeExpr.getArgCount() == 0 )
			return;
		List<Value> args = this.executeExpr.getArgs();
		ActivityAsyncOperationJimpleAnalysis activityAnalysis = new ActivityAsyncOperationJimpleAnalysis();
		for(int i=0;i < args.size(); i++){
			if( args.get(i) == NullConstant.v() )
				continue;
			SootClass fieldClass = Scene.v().getSootClass(args.get(i).getType().toString());
//			SootClass fieldClass = AsyncTaskDetector.classNameToSootClass.get(args.get(i).getType().toString());
//			if(!ClassInheritanceProcess.isInheritedFromView(fieldClass) && !ClassInheritanceProcess.isInheritedFromActivity(fieldClass) &&
//					!ClassInheritanceProcess.isInheritedFromMap(fieldClass) && !ClassInheritanceProcess.isInheritedFromCollection(fieldClass))
			if(!ClassInheritanceProcess.isInheritedFromView(fieldClass) && !ClassInheritanceProcess.isInheritedFromActivity(fieldClass) )
				continue;
			Unit unit = activityAnalysis.isValueReferToViewElement(args.get(i), this.unitList);
			this.taintInstances.add(new TaintInstance(null,i,unit));
//			if( unit != null )
//				this.taintInstances.add(new TaintInstance(null,i,unit));
		}
	}
	
}
