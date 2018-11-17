package cn.ac.ios.ad.summary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cn.ac.ios.ad.summary.analysis.ActivityGetFieldJimpleAnalysis;
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

public class ActivityGetFieldMethodSummary extends AbstractMethodSummary{
	
	/**
	 * The return value of current method can be one of four kinds below:
	 * 1.Instance of AsyncTask
	 * 2.AsyncTask field of Activity
	 * 3.Parameter of current method
	 * 4.Null Constant
	 */
	private Value returnValue;
	
	/**
	 * If return value refers to instance of AsyncTask, 
	 * initUnitList is the list of unit from the beginning of the method to where init method of AsyncTask is called 
	 */
	private List<Unit> initUnitList;
	
	private Set<SootField> referedFields = new HashSet<SootField>();
	

	public void setReferedFields(Set<SootField> referedFields) {
		this.referedFields = referedFields;
	}

	public Set<SootField> getReferedFields() {
		return referedFields;
	}

	public void addReferedFields(SootField referedField) {
		this.referedFields.add(referedField);
	}

	public List<Unit> getInitUnitList() {
		return initUnitList;
	}

	public void setInitUnitList(List<Unit> initUnitList) {
		this.initUnitList = initUnitList;
	}

	public void setReturnValue(Value returnValue) {
		this.returnValue = returnValue;
	}

	public Value getReturnValue() {
		return returnValue;
	}


	public ActivityGetFieldMethodSummary(SootMethod methodUnderAnalysis) {
		super(methodUnderAnalysis);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void printMethodSummary() {
		// TODO Auto-generated method stub
		if( this.returnValue == null)
			return;
		System.out.println("Get field method is "+this.methodUnderAnalysis.getSignature());
		System.out.print("refered fields are: ");
		for(SootField currentField:this.referedFields)
			System.out.print(currentField.getSignature()+" ");
		System.out.print("\n");
		if(this.returnValue instanceof ParameterRef )
			System.out.println("parameter index is "+((ParameterRef)this.returnValue).getIndex());
		else if( this.returnValue instanceof FieldRef )
			System.out.println("Return field is "+((FieldRef)this.returnValue).getField().getSignature());
		else if( this.returnValue instanceof InvokeExpr )
			System.out.println("Return expr is "+this.returnValue);
		System.out.println("");
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
