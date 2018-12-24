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
import java.util.Map;

import cn.ac.ios.asyncdetect.AsyncTaskDetector;
import cn.ac.ios.asyncdetect.record.RecordController;
import cn.ac.ios.asyncdetect.summary.AsyncTaskInitMethodSummary;
import cn.ac.ios.asyncdetect.summary.DoInBackgroundMethodSummary;
import cn.ac.ios.asyncdetect.summary.analysis.ActivityAsyncOperationJimpleAnalysis;
import cn.ac.ios.asyncdetect.summary.topology.DoInBackgroundTopologyOperation;
import cn.ac.ios.asyncdetect.summary.topology.InitMethodTopologyOperation;
import cn.ac.ios.asyncdetect.summary.topology.TopologyOperation;
import cn.ac.ios.asyncdetect.util.Log;
import cn.ac.ios.asyncdetect.util.MethodUtil;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Expr;
import soot.jimple.InvokeExpr;

/**
 * Assign value to the AsyncTask field of Activity.
 * For example, $r0.<com.example.listenertest.MainActivity: android.os.AsyncTask at> = $r2;
 * @author Linjie Pan
 * @version 1.0
 */
public class AssignAsyncTaskInstanceUnitSummary extends ActivityOperationUnitSummary implements HoldingReference{
	protected SootMethod mInitMethod;
	protected Expr mInitExpr;
	protected List<Unit> mUnitList;
	protected SootMethod mDoInBackgroundMethod;

	List<TaintInstance> mTaintInstances;

	private void setDoInBackgroundMethod() {
		if( this.mInitMethod == null  )
			this.mDoInBackgroundMethod = null;
		else{
			SootClass asyncClass = this.mInitMethod.getDeclaringClass();
			Log.i("-------asyncClass.getName() = ",asyncClass.getName());
			this.mDoInBackgroundMethod = MethodUtil.getMethod(asyncClass, "doInBackground");
			String key = DoInBackgroundTopologyOperation.getDoInBackgroundKey(this.mDoInBackgroundMethod);
			DoInBackgroundMethodSummary doInBackgroundSummary = (DoInBackgroundMethodSummary) TopologyOperation.getsMethodKeyToSummary().get(key);  
			if( doInBackgroundSummary == null ){
				TopologyOperation to = new DoInBackgroundTopologyOperation(this.mDoInBackgroundMethod);
				to.constructMainSummary(AsyncTaskDetector.getCallGraph());

				RecordController.getInstance().getAsyncTaskMethodOutput().addAsyncTaskDoInBackgroundMethodRecord(this.mDoInBackgroundMethod.getDeclaringClass(), ((DoInBackgroundMethodSummary)to.getSourceMethodSummary()).getLoopStartUnits().size());
			}
		}
	}
	
	public List<TaintInstance> getTaintInstances() {
		return mTaintInstances;
	}

	public void setTaintInstances(List<TaintInstance> taintInstances) {
		this.mTaintInstances = taintInstances;
	}

	public SootMethod getInitMethod(){
		return mInitMethod;
	}

	public void setInitMethod(SootMethod initMethod){
		this.mInitMethod = initMethod;
		String key = InitMethodTopologyOperation.getInitMethodKey(this.mInitMethod);
		AsyncTaskInitMethodSummary ims = (AsyncTaskInitMethodSummary)TopologyOperation.getsMethodKeyToSummary().get(key);
		if( ims == null ){
			TopologyOperation to = new InitMethodTopologyOperation(this.mInitMethod, this.mInitMethod.getDeclaringClass());
			to.constructMainSummary(AsyncTaskDetector.getCallGraph());
			
			RecordController.getInstance().getAsyncTaskMethodOutput().addAsyncTaskInitMethodRecord(this.mInitMethod.getDeclaringClass(), ((AsyncTaskInitMethodSummary)to.getSourceMethodSummary()).getDirtyArgIndexToTaintedField().size());
		}
	}

	public Expr getInitExpr() {
		return mInitExpr;
	}

	public void setInitExpr(Expr initExpr) {
		this.mInitExpr = initExpr;
	}

	public AssignAsyncTaskInstanceUnitSummary(Unit currentUnit,int argIndex){
		super(currentUnit,argIndex);
	}
	
	public AssignAsyncTaskInstanceUnitSummary(Unit currentUnit,Expr initExpr,List<Unit> unitList){
		super(currentUnit);
		this.setInitExpr(initExpr);
		this.setInitMethod(((InvokeExpr)initExpr).getMethod());
		this.setDoInBackgroundMethod();
		this.mUnitList = unitList;
		this.initTaintInstances();
	}
	
	public SootMethod getDoInBackgroundMethod(){
		return this.mDoInBackgroundMethod;
	}
	
	/**
	 * The parameter of init method contains the field of Activity and such parameter is assigned to the field of AsyncTask
	 * Then the AsyncTask holds the reference to the Activity
	 * @param mUnitList
	 * @return
	 */
	public void initTaintInstances(){
		//specialinvoke $r5.<com.example.fieldsensitivitytest.MainActivity$InnerTask: void <init>(com.example.fieldsensitivitytest.MainActivity)>($r0);
		//$r0.<com.example.fieldsensitivitytest.MainActivity: android.os.AsyncTask at> = $r5;
		this.mTaintInstances = new ArrayList<TaintInstance>();
		if( mInitMethod.getParameterCount() == 0 )
			return;
		List<Value> args = ((InvokeExpr)mInitExpr).getArgs();
		
		String key = InitMethodTopologyOperation.getInitMethodKey(this.mInitMethod);
		AsyncTaskInitMethodSummary ims = (AsyncTaskInitMethodSummary)TopologyOperation.getsMethodKeyToSummary().get(key);
		if( ims == null )
			return;
		Map<Integer,SootField> argIndexToField = ims.getDirtyArgIndexToTaintedField();
		ActivityAsyncOperationJimpleAnalysis activityAnalysis = new ActivityAsyncOperationJimpleAnalysis();
		
		for(int i=0;i < args.size(); i++){
			if( !argIndexToField.containsKey(i) )
				continue;
			Unit source = activityAnalysis.isValueReferToViewElement(args.get(i), this.mUnitList);
			this.mTaintInstances.add(new TaintInstance(argIndexToField.get(i),i,source));
		}
		return;
	}

	@Override
	public String getSummary() {
		return SummaryAlphabet.ASSIGN_ASYNC_INSTANCE;
	}
}
