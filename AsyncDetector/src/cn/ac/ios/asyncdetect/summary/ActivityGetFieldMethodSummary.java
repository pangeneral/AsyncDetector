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

package cn.ac.ios.asyncdetect.summary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cn.ac.ios.asyncdetect.summary.analysis.ActivityGetFieldJimpleAnalysis;
import cn.ac.ios.asyncdetect.util.Log;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.toolkits.graph.UnitGraph;

/**
 * The summary of method whose return type is AsyncTask or subclass of AsyncTask
 * @author Linjie Pan
 * @version 1.0
 */
public class ActivityGetFieldMethodSummary extends AbstractMethodSummary{
	
	/**
	 * The return value of current method can be one of four kinds below:
	 * 1.Instance of AsyncTask
	 * 2.AsyncTask field of Activity
	 * 3.Parameter of current method
	 * 4.Null Constant
	 */
	private Value mReturnValue;
	
	/**
	 * If return value refers to instance of AsyncTask, 
	 * initUnitList is the list of unit from the beginning of the method to where init method of AsyncTask is called 
	 */
	private List<Unit> mInitUnitList;
	
	private Set<SootField> mReferedFields = new HashSet<SootField>();
	

	public void setReferedFields(Set<SootField> referedFields) {
		this.mReferedFields = referedFields;
	}

	public Set<SootField> getReferedFields() {
		return mReferedFields;
	}

	public void addReferedFields(SootField referedField) {
		this.mReferedFields.add(referedField);
	}

	public List<Unit> getInitUnitList() {
		return mInitUnitList;
	}

	public void setInitUnitList(List<Unit> initUnitList) {
		this.mInitUnitList = initUnitList;
	}

	public void setReturnValue(Value returnValue) {
		this.mReturnValue = returnValue;
	}

	public Value getReturnValue() {
		return mReturnValue;
	}


	public ActivityGetFieldMethodSummary(SootMethod methodUnderAnalysis) {
		super(methodUnderAnalysis);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void printMethodSummary() {
		// TODO Auto-generated method stub
		if( this.mReturnValue == null)
			return;
		Log.i("Get field method is "+this.mMethodUnderAnalysis.getSignature());
		System.out.print("refered fields are: ");
		for(SootField currentField:this.mReferedFields)
			System.out.print(currentField.getSignature()+" ");
		System.out.print("\n");
		if(this.mReturnValue instanceof ParameterRef )
			Log.i("parameter index is "+((ParameterRef)this.mReturnValue).getIndex());
		else if( this.mReturnValue instanceof FieldRef )
			Log.i("Return field is "+((FieldRef)this.mReturnValue).getField().getSignature());
		else if( this.mReturnValue instanceof InvokeExpr )
			Log.i("Return expr is "+this.mReturnValue);
		Log.i("");
	}

	@Override
	protected void generation(UnitGraph theGraph) {
		// TODO Auto-generated method stub
		if( this.isMethodBranch(theGraph))
			return;
		
		List<Unit> unitList = this.getUnitList(theGraph);
		List<Unit> tails = theGraph.getTails();
		assert(tails.size()==1);
		ActivityGetFieldJimpleAnalysis getFieldAnalysis = new ActivityGetFieldJimpleAnalysis();
		Value originalValue = null;
		if( tails.get(0) instanceof ReturnStmt)
			originalValue = ((ReturnStmt)(tails.get(0))).getOp();
		if(originalValue instanceof NullConstant)
			this.setReturnValue(originalValue);
		else
			getFieldAnalysis.setGetFieldMethodSummary(this,originalValue,unitList);
	}
	
	private List<Unit> getUnitList(UnitGraph theGraph){
		List<Unit> unitList = new ArrayList<Unit>();
		Iterator<Unit> it = theGraph.iterator();
		while(it.hasNext()){
			Unit currentUnit = it.next();
			unitList.add(currentUnit);
		}
		return unitList;
	}
	
	/**
	 * If methodUnderAnalysis has branches, then return true
	 * @return
	 */
	private boolean isMethodBranch(UnitGraph theGraph){
		Iterator<Unit> it = theGraph.iterator();
		while(it.hasNext()){
			Unit currentUnit = it.next();
			if( currentUnit.branches())
				return true;
		}
		return false;
	}
}
