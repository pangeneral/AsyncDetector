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

package cn.ac.ios.asyncdetect.summary.alphabet.activity;

import java.util.ArrayList;
import java.util.List;

import cn.ac.ios.asyncdetect.summary.analysis.ActivityAsyncOperationJimpleAnalysis;
import cn.ac.ios.asyncdetect.util.ClassInheritanceProcess;
import soot.Scene;
import soot.SootClass;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NullConstant;

/**
 * Start a AsyncTask by executing the "execute()" method of AsyncTask class
 * The field executeExpr represents the expr which executes the "execute" method 
 * @author Linjie Pan
 * @version 1.0
 */
public class StartAsyncTaskUnitSummary extends ActivityOperationUnitSummary implements HoldingReference{
	private List<Unit> mUnitList;
	private InvokeExpr mExecuteExpr;
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
		return mUnitList;
	}

	public void setUnitList(List<Unit> unitList) {
		this.mUnitList = unitList;
	}

	public InvokeExpr getExecuteExpr() {
		return mExecuteExpr;
	}

	public void setExecuteExpr(Unit unit) {
		if( unit instanceof InvokeStmt )
			this.mExecuteExpr = ((InvokeStmt) unit).getInvokeExpr();
		else if (unit instanceof AssignStmt)
			this.mExecuteExpr = (InvokeExpr) ((AssignStmt) unit).getRightOp();
	}

	public StartAsyncTaskUnitSummary(Unit unit,List<Unit> unitList,boolean isBeforeAssignment){
		super(unit);
		this.setExecuteExpr(unit);
		this.mUnitList = unitList;
		this.isBeforeAssignment = isBeforeAssignment;
		this.initTaintInstances();
	}
	
	public StartAsyncTaskUnitSummary(Unit unit,List<Unit> unitList,int argIndex,boolean isBeforeAssignment){
		super(unit,argIndex);
		this.setExecuteExpr(unit);
		this.mUnitList = unitList;
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
		if( this.mExecuteExpr.getArgCount() == 0 )
			return;
		List<Value> args = this.mExecuteExpr.getArgs();
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
			Unit unit = activityAnalysis.isValueReferToViewElement(args.get(i), this.mUnitList);
			this.taintInstances.add(new TaintInstance(null,i,unit));
//			if( unit != null )
//				this.taintInstances.add(new TaintInstance(null,i,unit));
		}
	}
	
}
