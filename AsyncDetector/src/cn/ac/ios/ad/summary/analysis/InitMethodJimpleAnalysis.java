package cn.ac.ios.ad.summary.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ParameterRef;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import soot.jimple.VirtualInvokeExpr;
import cn.ac.ios.ad.AsyncTaskDetector;
import cn.ac.ios.ad.summary.ActivityGetFieldMethodSummary;
import cn.ac.ios.ad.summary.alphabet.activity.AssignAsyncTaskInstanceUnitSummary;
import cn.ac.ios.ad.summary.topology.ActivityGetFieldTopologyOperation;
import cn.ac.ios.ad.summary.topology.TopologyOperation;
import cn.ac.ios.ad.util.ClassInheritanceProcess;

public class InitMethodJimpleAnalysis extends CommonJimpleAnalysis{
	
	/**
	 * Current stmt changes the value of a field of AsyncTask. There could be several scenes.
	 * Scene 1: Current stmt is a assign stmt, the left op is the field of AsyncTask and is the instance of android.view.View
	 * Scene 2: Current stmt is a invoke stmt, the left op is the field of AsyncTask and is the instance of java.util.Collection or java.util.Map or array
	 * If the field holds reference to the parameter of current method where current stmt belongs to,
	 * return the map from the field of AsyncTask to the index of the parameter.
	 * @param currentStmt
	 * @param unitList
	 * @param taintedFields the set of field that has already been analyzed
	 * @param fieldsOfAsyncTask the set of all possible field that could be tainted
	 * @return
	 */
	public Map<SootField,Integer> getTaintedFieldToDirtyArgIndex(Stmt currentStmt,List<Unit> unitList,Set<SootField> taintedFields,Set<SootField> fieldsOfAsyncTask){
		//In jimple grammar,
		//the left operator could be array_ref | instance_field_ref | static_field_ref | local
		//the right operator could be array_ref | constant | expr | instance_field_ref | local | next_next_stmt_address | static_field_ref.
		if( currentStmt instanceof AssignStmt){
			Value leftOp = ((AssignStmt) currentStmt).getLeftOp();
			Value dirtyOp = ((AssignStmt) currentStmt).getRightOp();
			if( leftOp instanceof FieldRef){
				SootField taintedField = ((FieldRef) leftOp).getField();
				if( fieldsOfAsyncTask.contains(taintedField) && !taintedFields.contains(taintedField))
					return this.getAssignedFieldToMethodArg(unitList, dirtyOp, taintedField);
			}
			else if(leftOp instanceof ArrayRef){//$r3 = $r0.<com.example.fieldsensitivitytest.AsyncTasker: android.widget.Button[] buttonArray>; $r3[0] = $r1;
				Value arrayBase = ((ArrayRef) leftOp).getBase();
				SootField taintedField = this.getValueReferToField(arrayBase, unitList);
				if( taintedField != null )
					return this.getAssignedFieldToMethodArg(unitList, dirtyOp, taintedField);
			}
		}
		else if( currentStmt instanceof InvokeStmt){//interfaceinvoke $r4.<java.util.List: boolean add(java.lang.Object)>($r1);
			return this.getCollectionTaintedFieldToArgIndex(currentStmt.getInvokeExpr(), unitList);
		}
		return new HashMap<SootField,Integer>();
	}

	/**
	 * 
	 * @param invokeExpr
	 * @param unitList
	 * @return
	 */
	private Map<SootField,Integer> getCollectionTaintedFieldToArgIndex(InvokeExpr invokeExpr,List<Unit> unitList){
		//$r4 = $r0.<com.example.fieldsensitivitytest.AsyncTasker: java.util.List buttonList>;
        //interfaceinvoke $r4.<java.util.List: boolean add(java.lang.Object)>($r1);
		Value base=null; SootField taintedField=null;
		if( invokeExpr instanceof InterfaceInvokeExpr )
			base = ((InterfaceInvokeExpr) invokeExpr).getBase();
		else if( invokeExpr instanceof VirtualInvokeExpr )
			base = ((VirtualInvokeExpr) invokeExpr).getBase();
		if( base != null && base instanceof Local)
			taintedField = this.getValueReferToField(base, unitList);
		if( taintedField != null){
			String className = taintedField.getType().toString();
			SootClass fieldClass = Scene.v().getSootClass(className);
//			SootClass fieldClass = AsyncTaskDetector.classNameToSootClass.get(className);
			
			//Tainted field is an instance of Collection
			if( ClassInheritanceProcess.isInheritedFromCollection(fieldClass) && invokeExpr.getMethod().getName().equals("add") ){
				assert(invokeExpr.getArgCount()==1);
				Value dirtyOp = invokeExpr.getArg(0);
				return this.getAssignedFieldToMethodArg(unitList, dirtyOp, taintedField);
			}
			//Tainted field is an instance of Map
			else if( ClassInheritanceProcess.isInheritedFromMap(fieldClass) && invokeExpr.getMethod().getName().equals("put")){
				assert(invokeExpr.getArgCount()==2);
				Value firstDirtyOp = invokeExpr.getArg(0);
				Map<SootField,Integer> argToTaintedField = this.getAssignedFieldToMethodArg(unitList, firstDirtyOp, taintedField);
				Value secondDirtyOp = invokeExpr.getArg(1);
				argToTaintedField.putAll(this.getAssignedFieldToMethodArg(unitList, secondDirtyOp, taintedField));
				return argToTaintedField;
			}
		}
		return new HashMap<SootField,Integer>();
	}
	
	/**
	 * If value v refer to the parameter of current method, return the index of the parameter. 
	 * For example,Given method A(int i1,int i2,int i3), int m = i2, then m refer to parameter i2, return 1 as 1 the index of i2 in the parameter list.
	 * @param originalValue
	 * @param unitList
	 * @return
	 */
	public int getValueReferToArgIndex(Value originalValue,SootField assignedField,List<Unit> unitList){
		Value newValue = this.getReferedValue(originalValue);
		if( newValue == null)
			return -1;
		for(int i=unitList.size()-2; i >= 0 ; i--){
			Unit unit = unitList.get(i);
			if( unit instanceof AssignStmt && this.isValueEqual(((AssignStmt)unit).getLeftOp(),newValue) ){
				Value rightOp = ((AssignStmt)unit).getRightOp();
				if( rightOp instanceof InvokeExpr)
					return this.getValueReferToArgIndexFromInvokeExpr((InvokeExpr)rightOp, assignedField, unitList.subList(0, i+1));
				else
					return this.getValueReferToArgIndex(((AssignStmt)unit).getRightOp(),assignedField,unitList.subList(0, i+1));
			}
			else if( unit instanceof IdentityStmt && ((IdentityStmt)unit).getLeftOp() == newValue ){
				//The following is the grammar of identity_stmt
				//identity_stmt = local ":=" identity_value;
				//identity_value = caught_exception_ref | parameter_ref | this_ref;
				//Apparently, the right operator must be parameter_ref so that l refers to parameter of method
				if( ((IdentityStmt)unit).getRightOp() instanceof ParameterRef){
					ParameterRef pr = (ParameterRef)((IdentityStmt)unit).getRightOp();
					return pr.getIndex();
				}
				else
					return -1;
			}
		}
		return -1;
	}
	
	/**
	 * The right operator is an invokeExpr which should be handled specially. 
	 * @param assignUnit
	 * @param theExpr
	 * @param unitList
	 * @return
	 */
	private int getValueReferToArgIndexFromInvokeExpr(InvokeExpr theExpr,SootField assignedField,List<Unit> unitList){
		SootMethod theMethod = theExpr.getMethod();
		String key = ActivityGetFieldTopologyOperation.getGetFieldKey(theMethod);
		ActivityGetFieldMethodSummary getFieldSummary = (ActivityGetFieldMethodSummary) TopologyOperation.methodKeyToSummary.get(key);
		if( getFieldSummary == null ){
			if(AsyncTaskDetector.methodSignatureToBody.get(theMethod.getSignature()) != null){
				TopologyOperation operation = new ActivityGetFieldTopologyOperation(theMethod);
				operation.constructMainSummary(AsyncTaskDetector.getCallGraph());
				getFieldSummary = (ActivityGetFieldMethodSummary) TopologyOperation.methodKeyToSummary.get(key);
				if( getFieldSummary == null )
					return -1;
			}
			else
				return -1;
		}	
		Value returnValue = getFieldSummary.getReturnValue();
		
		if( returnValue instanceof SpecialInvokeExpr )
			return -1;
		else if(returnValue instanceof ParameterRef ){
			Value originalValue = theExpr.getArg(((ParameterRef) returnValue).getIndex());
			return this.getValueReferToArgIndex(originalValue, assignedField,unitList);
		}
		else if( returnValue instanceof FieldRef )
			return this.getValueReferToArgIndex(returnValue,assignedField, unitList);
		else
			return -1;
	}
	
	
	
	/**
	 * It is known that rightOp is assigned to assignedField, if rightOp refers to a parameter of current method,
	 * then return the map from assignedField to the index of the parameter. 
	 * @param unitList
	 * @param rightOp
	 * @param assignedField
	 * @return
	 */
	protected Map<SootField,Integer> getAssignedFieldToMethodArg(List<Unit> unitList, Value rightOp,SootField assignedField) {
		Map<SootField,Integer> assignedFieldToMethodArg = new HashMap<SootField,Integer>();
		if( rightOp == null || assignedField == null)
			return assignedFieldToMethodArg;
		int argIndex=-1;
		if( rightOp instanceof InvokeExpr)
			argIndex = this.getValueReferToArgIndexFromInvokeExpr((InvokeExpr)rightOp, assignedField, unitList);
		else
			argIndex = this.getValueReferToArgIndex(rightOp, assignedField,unitList);
		if( argIndex != -1)
			assignedFieldToMethodArg.put(assignedField,argIndex);
		return assignedFieldToMethodArg;
	}	
}