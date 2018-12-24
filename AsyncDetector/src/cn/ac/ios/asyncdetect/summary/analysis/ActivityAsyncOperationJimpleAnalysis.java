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

package cn.ac.ios.asyncdetect.summary.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.ac.ios.asyncdetect.AsyncTaskDetector;
import cn.ac.ios.asyncdetect.constant.MethodSignature;
import cn.ac.ios.asyncdetect.summary.ActivityAsyncOperationMethodSummary;
import cn.ac.ios.asyncdetect.summary.ActivityGetFieldMethodSummary;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.ActivityOperationUnitSummary;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.AssignAsyncTaskInstanceUnitSummary;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.AssignNullAsyncTaskUnitSummary;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.CancelAsyncTaskUnitSummary;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.StartAsyncTaskUnitSummary;
import cn.ac.ios.asyncdetect.summary.topology.ActivityGetFieldTopologyOperation;
import cn.ac.ios.asyncdetect.summary.topology.ActivityTopologyOperation;
import cn.ac.ios.asyncdetect.summary.topology.TopologyOperation;
import cn.ac.ios.asyncdetect.util.ClassInheritanceProcess;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.Expr;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.ThisRef;
import soot.jimple.VirtualInvokeExpr;

/**
 * 
 * @author Linjie Pan
 * @version 1.0
 */
public class ActivityAsyncOperationJimpleAnalysis extends CommonJimpleAnalysis{
	
	private static final int IS_REFERENCE=-1;
	private static final int NOT_REFERENCE=-2;
	
	public List<ActivityOperationUnitSummary> getNewAssignAsyncTaskUnitSummary(Unit currentUnit,InvokeExpr invokeExpr,List<Unit> unitList,int argIndex){
		Value theValue = invokeExpr.getArg(argIndex);
		if( theValue instanceof NullConstant){
			List<ActivityOperationUnitSummary> summaryList = new ArrayList<ActivityOperationUnitSummary>();
			summaryList.add(new AssignNullAsyncTaskUnitSummary(currentUnit));
			return summaryList;
		}
		else
			return this.getAssignAsyncTask(currentUnit, theValue, unitList);
	}
	
	public StartAsyncTaskUnitSummary getNewStartAsyncTaskUnitSummary(StartAsyncTaskUnitSummary formerSummary,Unit currentUnit,InvokeExpr invokeExpr,SootField fieldUnderAnalysis,List<Unit> unitList,int argIndex){
		Value originalValue = invokeExpr.getArg(argIndex);
		int result=Integer.MIN_VALUE;
		if( formerSummary.isBeforeAssignment())
			result = this.isValueReferToInstance(originalValue, unitList);
		else
			result = this.isValueReferToGivenField(originalValue, fieldUnderAnalysis, unitList,true);
		return this.getStartAsyncTaskAccordingToResult(currentUnit, unitList, result,formerSummary.isBeforeAssignment());
	}
	
	public CancelAsyncTaskUnitSummary getNewCancelAsyncTaskUnitSummary(Unit currentUnit,InvokeExpr invokeExpr,SootField fieldUnderAnalysis,List<Unit> unitList,int argIndex){
		Value originalValue = invokeExpr.getArg(argIndex);
		int result = this.isValueReferToGivenField(originalValue, fieldUnderAnalysis, unitList,true);
		return this.getCancelAsyncTaskAccordingToResult(currentUnit, result);
	}
	
	/**
	 * An valid JudgeNullAsyncTaskUnitSummary contains two types of condition expression.
	 * 1.EqExpr or NeExpr. For example, this.asyncTask == null or this.asyncTask != null
	 * 2.GtExpr, GeExpr, LtExpr or LeExpr. For example, boolean result = (this.asyncTask == null); result >= 1;
	 * @param currentUnit
	 * @param fieldUnderAnalysis
	 * @param unitList
	 * @return
	 */
//	public JudgeNullAsyncTaskUnitSummary getJudgeNullAsyncTaskUnitSummary(Unit currentUnit,SootField fieldUnderAnalysis,List<Unit> unitList){
//		if( currentUnit instanceof IfStmt){
//			ConditionExpr condition = (ConditionExpr) ((IfStmt) currentUnit).getCondition();
//			return this.getJudgeNullFromCondition(currentUnit, fieldUnderAnalysis, unitList, condition);
//		}
//		return null;
//	}
//	
//	private JudgeNullAsyncTaskUnitSummary getJudgeNullFromCondition(Unit currentUnit,SootField fieldUnderAnalysis,List<Unit> unitList,ConditionExpr condition){
//		ConditionExpr newCondition = null;
//		//condition = eq_expr | ge_expr | le_expr | lt_expr | ne_expr | gt_expr;
//		if( condition instanceof EqExpr || condition instanceof NeExpr)
//			newCondition = condition;
//		else
//			newCondition = ConditionProcess.isConditionReducedToEqOrNeq(condition);
//		if( newCondition == null)
//			return null;
//		Value leftOp = newCondition.getOp1();
//		Value rightOp = newCondition.getOp2();
//		if( rightOp instanceof NullConstant )
//			return this.getJudgeNullFromOperator(leftOp, fieldUnderAnalysis, unitList, newCondition, currentUnit);
//		else if( leftOp instanceof NullConstant )
//			return this.getJudgeNullFromOperator(rightOp, fieldUnderAnalysis, unitList, newCondition, currentUnit);
//		else{//One operator is boolean constant and the other is EqExpr which judges whether fieldUnderAnalysis is null or not 
//			if( leftOp instanceof IntConstant )
//				return this.isValueReferToJudgeFieldNullExpr(currentUnit,rightOp, fieldUnderAnalysis, unitList,newCondition.getSymbol(),((IntConstant)leftOp).value);
//			else if(rightOp instanceof IntConstant)
//				return this.isValueReferToJudgeFieldNullExpr(currentUnit,leftOp, fieldUnderAnalysis, unitList, newCondition.getSymbol(), ((IntConstant)rightOp).value);
//		}
//		return null;
//	}
//	
//	private JudgeNullAsyncTaskUnitSummary getJudgeNullFromOperator(Value originalValue,SootField fieldUnderAnalysis,List<Unit> unitList,ConditionExpr condition,Unit currentUnit){
//		int result = this.isValueReferToGivenField(originalValue, fieldUnderAnalysis, unitList, false); 
//		boolean isNull = (condition instanceof EqExpr)?true:false;
//		if( result == ActivityAsyncOperationJimpleAnalysis.IsReference)
//			return new JudgeNullAsyncTaskUnitSummary(currentUnit,condition,isNull);
//		else if( result == ActivityAsyncOperationJimpleAnalysis.NotReference)
//			return null;
//		else
//			return new JudgeNullAsyncTaskUnitSummary(currentUnit,result,condition,isNull);
//	}
//	
//	private JudgeNullAsyncTaskUnitSummary isValueReferToJudgeFieldNullExpr(Unit currentUnit,Value originalValue,SootField fieldUnderAnalysis,List<Unit> unitList,String symbol,int contantValue){
//		Value newValue = this.getReferedValue(originalValue);
//		if( newValue == null)
//			return null;
//		for(int i=unitList.size()-2; i >= 0; i--){
//			Unit unit = unitList.get(i);
//			if( unit instanceof AssignStmt && this.isValueEqual(((AssignStmt) unit).getLeftOp(), newValue) ){
//				Value rightOp = ((AssignStmt) unit).getRightOp();
//				JudgeNullAsyncTaskUnitSummary summary = null;
//				if( rightOp instanceof InvokeExpr )
//					summary =  this.isValueReferToJudgeFieldNullExprFromInvokeExpr(currentUnit,fieldUnderAnalysis, unitList,(InvokeExpr)rightOp);
//				else if( rightOp instanceof ConditionExpr)
//					summary = this.getJudgeNullFromCondition(currentUnit, fieldUnderAnalysis, unitList.subList(0, i+1),(ConditionExpr)rightOp);
//				else 
//					summary = this.isValueReferToJudgeFieldNullExpr(currentUnit,rightOp, fieldUnderAnalysis, unitList.subList(0, i+1), symbol, contantValue);
//				if( summary != null )
//					summary.process(symbol, contantValue);
//				return summary;
//			}
//		}
//		return null;
//	}
//	
//	public JudgeNullAsyncTaskUnitSummary isValueReferToJudgeFieldNullExprFromInvokeExpr(Unit currentUnit,SootField fieldUnderAnalysis,List<Unit> unitList,InvokeExpr invokeExpr){
//		Body b = AsyncTaskDetector.methodSignatureToBody.get(invokeExpr.getMethod().getSignature());
//		return null;
//	}
	
	/**
	 * If current value refer to instance of Activity or view element of Activity, return true.
	 * @param originalValue
	 * @param unitList
	 * @return
	 */
	public Unit isValueReferToViewElement(Value originalValue,List<Unit> unitList){
		Value newValue = this.getReferedValue(originalValue);
		if(newValue == null)
			return null;
		for(int i=unitList.size()-1; i>=0; i--){
			Unit unit = unitList.get(i);
			//In jimple grammar,
			//the left operator could be array_ref | instance_field_ref | static_field_ref | local
			//the right operator could be array_ref | constant | expr | instance_field_ref | local | next_next_stmt_address | static_field_ref.
			//expr = binop_expr | cast_expr | instance_of_expr | invoke_expr | new_array_expr |new_expr | new_multi_array_expr | unop_expr;
			if( unit instanceof AssignStmt && this.isValueEqual(((AssignStmt) unit).getLeftOp(),newValue)){
				Value rightOp = ((AssignStmt)unit).getRightOp();
				if( rightOp instanceof InvokeExpr ){
					if( ((InvokeExpr)rightOp).getMethod().getName().equals("findViewById") )
						return unit;
					else//other invokeExprs lead to false
						return null;
				}
				else
					return this.isValueReferToViewElement(rightOp,unitList.subList(0, i));
			}
			//identity_stmt = local ":=" identity_value;
			//identity_value = caught_exception_ref | parameter_ref | this_ref;
			else if( unit instanceof IdentityStmt && ((IdentityStmt) unit).getLeftOp() == newValue){
				Value rightOp = ((IdentityStmt) unit).getRightOp();
				if( rightOp instanceof ThisRef && this.isInstanceOfActivity(((ThisRef)rightOp).getType().toString()) )
					return unit;
				else
					return null;
			}
		}
		return null;
	}
	
	private boolean isInstanceOfActivity(String className){
		SootClass currentClass = Scene.v().getSootClass(className);
//		SootClass currentClass = AsyncTaskDetector.classNameToSootClass.get(className);
		if(ClassInheritanceProcess.isInheritedFromActivity(currentClass))
			return true;
		else
			return false;
	}
	
	/**
	 * Judge Whether current unit cancel fieldUnderAnalysis 
	 * @param currentUnit
	 * @param fieldUnderAnalysis
	 * @param theGraph
	 * @return
	 */
	public CancelAsyncTaskUnitSummary getCancelAsyncTaskUnitSummary(Unit currentUnit,SootField fieldUnderAnalysis,List<Unit> unitList){
		int result = this.isCallingMethod(currentUnit, fieldUnderAnalysis, unitList, MethodSignature.CANCEL_SIGNATURE); 
		return this.getCancelAsyncTaskAccordingToResult(currentUnit, result);
	}
	
	private CancelAsyncTaskUnitSummary getCancelAsyncTaskAccordingToResult(Unit currentUnit,int result){
		if( result == ActivityAsyncOperationJimpleAnalysis.NOT_REFERENCE )
			return null;
		else if(result == ActivityAsyncOperationJimpleAnalysis.IS_REFERENCE )
			return new CancelAsyncTaskUnitSummary(currentUnit);
		else//refer to parameter of current method
			return new CancelAsyncTaskUnitSummary(currentUnit,result);
	}
	
	private StartAsyncTaskUnitSummary getStartAsyncTaskAccordingToResult(Unit currentUnit,List<Unit> unitList,int result,boolean isBeforeAssignment){
		if( result == ActivityAsyncOperationJimpleAnalysis.NOT_REFERENCE)
			return null;
		else if( result == ActivityAsyncOperationJimpleAnalysis.IS_REFERENCE)
			return new StartAsyncTaskUnitSummary(currentUnit,unitList,isBeforeAssignment);
		else
			return new StartAsyncTaskUnitSummary(currentUnit,unitList,result,isBeforeAssignment);
	}
	
	public boolean isContainAsyncTaskOperation(Unit currentUnit){
		if( currentUnit instanceof InvokeStmt){
			InvokeExpr ie = ((InvokeStmt) currentUnit).getInvokeExpr();
			if( this.isInvokeExpressionContainAsyncTaskOperation(ie) )
				return true;
		}
		else if( currentUnit instanceof AssignStmt){
			Type leftType = ((AssignStmt) currentUnit).getLeftOp().getType();
			Value rightOp = ((AssignStmt) currentUnit).getRightOp();
			if( leftType instanceof RefType && ClassInheritanceProcess.isInheritedFromAsyncTask(((RefType) leftType).getSootClass()) )
				return true;
			if( !(rightOp instanceof Expr) && rightOp.getType() instanceof RefType && ClassInheritanceProcess.isInheritedFromAsyncTask(((RefType) rightOp.getType()).getSootClass()) )
				return true;
			if( rightOp instanceof InvokeExpr && this.isInvokeExpressionContainAsyncTaskOperation((InvokeExpr) rightOp))
				return true;
		}
		return false;
	}
	
	private boolean isInvokeExpressionContainAsyncTaskOperation(InvokeExpr ie){
		if( ie.getMethod().getSignature().equals(MethodSignature.EXECUTE_SIGNATURE) || ie.getMethod().getSignature().equals(MethodSignature.CANCEL_SIGNATURE))
			return true;
		if( AsyncTaskDetector.sSootMethodToIsContainAsyncOperation.getOrDefault(ie.getMethod(), false) )
			return true;
		return false;
	}
	
	/**
	 * Judge whether current unit start fieldUnderAnalysis
	 * @param currentUnit
	 * @param fieldUnderAnalysis
	 * @return
	 */
	public StartAsyncTaskUnitSummary getStartAsyncTaskUnitSummary(Unit currentUnit,SootField fieldUnderAnalysis,List<Unit> unitList){
		int result =  this.isCallingMethod(currentUnit, fieldUnderAnalysis, unitList, MethodSignature.EXECUTE_SIGNATURE);
		return this.getStartAsyncTaskAccordingToResult(currentUnit, unitList, result,false);
	}
	
	/**
	 * Judge whether current unit calls the method with given method name
	 * Both execute and cancel method of AsyncTask have return value which means they can appear in InvokeStmt or AssignStmt
	 * @param currentUnit
	 * @param fieldUnderAnalysis
	 * @param unitList
	 * @param methodSignature
	 * @return
	 */
	private int isCallingMethod(Unit currentUnit,SootField fieldUnderAnalysis,List<Unit> unitList,String methodSignature){
		InvokeExpr ie = null;
		if( currentUnit instanceof InvokeStmt)
			ie = ((InvokeStmt) currentUnit).getInvokeExpr();
		else if( currentUnit instanceof AssignStmt && ((AssignStmt)currentUnit).getRightOp() instanceof InvokeExpr)
			ie = (InvokeExpr) ((AssignStmt)currentUnit).getRightOp();
		if( ie != null && ie instanceof VirtualInvokeExpr ){
			//$r1 = $r0.<com.example.fieldsensitivitytest.MainActivity: android.os.AsyncTask at>;
			//virtualinvoke $r1.<android.os.AsyncTask: android.os.AsyncTask execute(java.lang.Object[])>($r2);
			if(ie.getMethod().getSignature().equals(methodSignature)){
				return this.isValueReferToGivenField(((VirtualInvokeExpr) ie).getBase(), fieldUnderAnalysis,unitList,true);
			}
		}
		return ActivityAsyncOperationJimpleAnalysis.NOT_REFERENCE;
	}
	
	private int isValueReferToInstance(Value originalValue,List<Unit> unitList){
		Value newValue = this.getReferedValue(originalValue);
		if(newValue == null)
			return ActivityAsyncOperationJimpleAnalysis.NOT_REFERENCE;
		for(int i=unitList.size()-2; i >= 0 ; i--){
			Unit unit = unitList.get(i);
			if( unit instanceof AssignStmt && this.isValueEqual(((AssignStmt)unit).getLeftOp(),newValue)){
				Value rightOp = ((AssignStmt) unit).getRightOp();
				if( rightOp instanceof InvokeExpr){
					InvokeExpr theExpr = (InvokeExpr)rightOp;
					return this.isValueReferToInstanceFromInvokeExpr(theExpr,unitList.subList(0, i+1));
				}
				else
					return this.isValueReferToInstance(rightOp,unitList.subList(0, i+1));
			}
			else if(unit instanceof IdentityStmt && ((IdentityStmt) unit).getLeftOp() == newValue && ((IdentityStmt) unit).getRightOp() instanceof ParameterRef){
				return ((ParameterRef)((IdentityStmt) unit).getRightOp()).getIndex();
			}
			else if( unit instanceof InvokeStmt ){
				InvokeExpr ie = ((InvokeStmt) unit).getInvokeExpr();
				if( ie instanceof SpecialInvokeExpr && ((SpecialInvokeExpr)ie).getBase() == newValue && ie.getMethod().getName().equals("<init>"))
					return ActivityAsyncOperationJimpleAnalysis.IS_REFERENCE;
			}
		}
		return ActivityAsyncOperationJimpleAnalysis.NOT_REFERENCE;
	}
	
	private int isValueReferToInstanceFromInvokeExpr(InvokeExpr theExpr,List<Unit> unitList){
		SootMethod theMethod = theExpr.getMethod();
		String key = ActivityGetFieldTopologyOperation.getGetFieldKey(theMethod);
		ActivityGetFieldMethodSummary getFieldSummary = (ActivityGetFieldMethodSummary) TopologyOperation.getsMethodKeyToSummary().get(key);
		if( getFieldSummary == null)
			return ActivityAsyncOperationJimpleAnalysis.NOT_REFERENCE;
		Value returnValue = getFieldSummary.getReturnValue();
		if(returnValue instanceof ParameterRef )
			return this.isValueReferToInstance(theExpr.getArg(((ParameterRef) returnValue).getIndex()),unitList);
		else if( returnValue instanceof InvokeExpr)
			return ActivityAsyncOperationJimpleAnalysis.IS_REFERENCE;
		else if(returnValue instanceof FieldRef)
			return this.isValueReferToInstance(returnValue, unitList);
		else
			return ActivityAsyncOperationJimpleAnalysis.NOT_REFERENCE;
	}
	
	
	/**
	 * Backwardly analyze along the current path to judge whether local variable l refer to given field.
	 * Note that there are three kinds of valid variable that l can finally refer to.They are:
	 * 1.fieldUnderAnalysis
	 * 2.return value of a certain method
	 * 3.parameter of current method
	 * If l does not refer to any of the three kind of variable above, then return NotReferToField.
	 * If l refers to fieldUnderAnalysis, then return ReferToField.
	 * If l refers to a parameter of current method, then return the index of the parameter. 
	 * @param l
	 * @param fieldUnderAnalysis
	 * @param unitList
	 * @param isValueCallingMethod 
	 * @return
	 */
	private int isValueReferToGivenField(Value originalValue,SootField fieldUnderAnalysis,List<Unit> unitList,boolean isValueCallingMethod){
		Value newValue = this.getReferedValue(originalValue);
		if(newValue == null)
			return ActivityAsyncOperationJimpleAnalysis.NOT_REFERENCE;
		for(int i=unitList.size()-2; i >= 0 ; i--){
			Unit unit = unitList.get(i);
			if( unit instanceof AssignStmt){
				Value leftOp = ((AssignStmt)unit).getLeftOp();
				Value rightOp = ((AssignStmt) unit).getRightOp();
				if(this.isValueEqual(leftOp,newValue)){
					if( rightOp instanceof FieldRef){
						if( ((FieldRef) rightOp).getField() == fieldUnderAnalysis)
							return ActivityAsyncOperationJimpleAnalysis.IS_REFERENCE;
						else
							return ActivityAsyncOperationJimpleAnalysis.NOT_REFERENCE;
					}
					else if( rightOp instanceof InvokeExpr){
						InvokeExpr theExpr = (InvokeExpr)rightOp;
						return this.isValueReferToGivenFieldFromInvokeExpr(theExpr, fieldUnderAnalysis, unitList.subList(0, i+1),isValueCallingMethod);
					}
					else
						return this.isValueReferToGivenField(rightOp, fieldUnderAnalysis, unitList.subList(0, i+1),isValueCallingMethod);
				}
				/**
				 * Right operator refers to the AsyncTask field which is the left operator only when right operate invokes "execute" or "cancel" method. 
				 * For example, this.at = firstLocal;
				 * firstLocal.cancel(true);
				 */
				else if(isValueCallingMethod && this.isValueEqual(rightOp, newValue) && leftOp instanceof FieldRef && ((FieldRef) leftOp).getField() == fieldUnderAnalysis){ 
					return this.isValueReferToInstance(rightOp, unitList.subList(0, i+1));
				}
			}
			else if(unit instanceof IdentityStmt && ((IdentityStmt) unit).getLeftOp() == newValue && ((IdentityStmt) unit).getRightOp() instanceof ParameterRef){
				return ((ParameterRef)((IdentityStmt) unit).getRightOp()).getIndex();
			}
			/**
			 * newValue is the parameter of Invoked method and is assigned to fieldUnderAnalysis in the method.
			 * For example,
			 * staticinvoke <com.example.listenertest.MainActivity: void access$0(com.example.listenertest.MainActivity,android.os.AsyncTask)>($r3, $r2);
			 * $z0 = virtualinvoke $r2.<android.os.AsyncTask: boolean cancel(boolean)>(1);
			 */
			else if( isValueCallingMethod && unit instanceof InvokeStmt ){
				InvokeExpr ie = ((InvokeStmt)unit).getInvokeExpr();
				int argIndex = -1;
				for(int j=0;j < ie.getArgCount(); j++)
					if( ie.getArg(j) == newValue ){
						argIndex = j;
						break;
					}
				if( argIndex != -1){
					String key = ActivityTopologyOperation.getActivityOperationKey(fieldUnderAnalysis, ie.getMethod());
					ActivityAsyncOperationMethodSummary methodSummary = (ActivityAsyncOperationMethodSummary)TopologyOperation.getsMethodKeyToSummary().get(key);
					if( methodSummary != null && methodSummary.isParameterAssignedToField(argIndex) )
						return ActivityAsyncOperationJimpleAnalysis.IS_REFERENCE;
				}
			}
		}
		return ActivityAsyncOperationJimpleAnalysis.NOT_REFERENCE;
	}
	
	private int isValueReferToGivenFieldFromInvokeExpr(InvokeExpr theExpr,SootField fieldUnderAnalysis,List<Unit> unitList,boolean isValueCallingMethod){
		SootMethod theMethod = theExpr.getMethod();
		String key = ActivityGetFieldTopologyOperation.getGetFieldKey(theMethod);
		ActivityGetFieldMethodSummary getFieldSummary = (ActivityGetFieldMethodSummary) TopologyOperation.getsMethodKeyToSummary().get(key);
		if( getFieldSummary == null ){
			return ActivityAsyncOperationJimpleAnalysis.NOT_REFERENCE;
		}
		Set<SootField> referedFields = getFieldSummary.getReferedFields();
		if( referedFields.contains(fieldUnderAnalysis))
			return ActivityAsyncOperationJimpleAnalysis.IS_REFERENCE;
		else if( referedFields.size() == 0 ){
			Value returnValue = getFieldSummary.getReturnValue();
			if(returnValue == null)
				return ActivityAsyncOperationJimpleAnalysis.NOT_REFERENCE;
			else if(returnValue instanceof ParameterRef )
				return this.isValueReferToGivenField(theExpr.getArg(((ParameterRef) returnValue).getIndex()), fieldUnderAnalysis, unitList,isValueCallingMethod);
			else
				return ActivityAsyncOperationJimpleAnalysis.NOT_REFERENCE;
		}
		else{
			Value returnValue = getFieldSummary.getReturnValue();
			if( returnValue instanceof ParameterRef)
				return this.isValueReferToGivenField(theExpr.getArg(((ParameterRef) returnValue).getIndex()), fieldUnderAnalysis, unitList,isValueCallingMethod);
			else
				return ActivityAsyncOperationJimpleAnalysis.NOT_REFERENCE;
		}
	}
	
	/**
	 * Current statement is an assignment statement, the left operator is the field under analysis.
	 * Then the right operator can be used to construct an instance of AssignAsyncTaskSummary
	 * @param currentUnit
	 * @param fieldUnderAnalysis
	 * @return
	 */
	public List<ActivityOperationUnitSummary> getAssignAsyncTaskUnitSummary(Unit currentUnit,SootField fieldUnderAnalysis,List<Unit> unitList,SootClass activityClass){
		if( currentUnit instanceof AssignStmt ){//directly assign value to fieldUnderAnalysis
			//specialinvoke $r3.<com.example.fieldsensitivitytest.AsyncTasker: void <init>()>();
			//$r0.<com.example.fieldsensitivitytest.MainActivity: android.os.AsyncTask at> = $r3;
			AssignStmt as = (AssignStmt)currentUnit;
			if( as.getLeftOp() instanceof FieldRef && ((FieldRef)as.getLeftOp()).getField() == fieldUnderAnalysis)
				return this.getAssignAsyncTask(currentUnit, unitList);
		}
		return null;
	}
	
	/**
	 * The right operator is an invokeExpr which should be handled specially. 
	 * @param assignUnit
	 * @param theExpr
	 * @param unitList
	 * @return
	 */
	private List<ActivityOperationUnitSummary> getAssignAsyncTaskSummaryFromInvokeExpr(Unit assignUnit,InvokeExpr theExpr,List<Unit> unitList){
		SootMethod theMethod = theExpr.getMethod();
		List<ActivityOperationUnitSummary> summaryList = null;
		if(theMethod.getSignature().equals(MethodSignature.EXECUTE_SIGNATURE)){
			//$r5 = virtualinvoke $r2.<android.os.AsyncTask: android.os.AsyncTask execute(java.lang.Object[])>($r4);
			//staticinvoke <com.example.listenertest.MainActivity: void access$0(com.example.listenertest.MainActivity,android.os.AsyncTask)>($r3, $r5);
			Value originalValue = ((InstanceInvokeExpr) theExpr).getBase();
			List<ActivityOperationUnitSummary> currentList = this.getAssignAsyncTask(assignUnit,originalValue,unitList);
			if( currentList != null){
				int result = this.isValueReferToInstance(originalValue, unitList);
				currentList.add(this.getStartAsyncTaskAccordingToResult(unitList.get(unitList.size()-1), unitList, result, true));
			}
			return currentList;
		}
			
		String key = ActivityGetFieldTopologyOperation.getGetFieldKey(theMethod);
		ActivityGetFieldMethodSummary getFieldSummary = (ActivityGetFieldMethodSummary) TopologyOperation.getsMethodKeyToSummary().get(key);
		if( getFieldSummary == null )
			return null;
		Value returnValue = getFieldSummary.getReturnValue();
		/**
		 * 1.Instance of the AsyncTask
		 * 2.Field of Activity
 		 * 3.Parameter of current method
		 */
		if( returnValue instanceof SpecialInvokeExpr){
			summaryList = new ArrayList<ActivityOperationUnitSummary>();
			summaryList.add(new AssignAsyncTaskInstanceUnitSummary(assignUnit,(SpecialInvokeExpr)returnValue,getFieldSummary.getInitUnitList()));
			return summaryList;
		}
		else if(returnValue instanceof ParameterRef ){
			int argIndex = ((ParameterRef) returnValue).getIndex();
			Value originalValue = theExpr.getArg(argIndex);
			return this.getAssignAsyncTask(assignUnit, originalValue, unitList);
		}
		else if( returnValue instanceof FieldRef)
			return this.getAssignAsyncTask(assignUnit, returnValue, unitList);
		else if( returnValue instanceof NullConstant){
			summaryList = new ArrayList<ActivityOperationUnitSummary>();
			summaryList.add(new AssignNullAsyncTaskUnitSummary(assignUnit));
			return summaryList;
		}
		return null;
	}
	
	/**
	 * 
	 * @param assignUnit
	 * @param unitList
	 * @return
	 */
	private List<ActivityOperationUnitSummary> getAssignAsyncTask(Unit assignUnit,List<Unit> unitList){
		AssignStmt as = (AssignStmt)assignUnit;
		if( as.getRightOp() instanceof InvokeExpr){
			InvokeExpr theExpr = (InvokeExpr) as.getRightOp();
			return this.getAssignAsyncTaskSummaryFromInvokeExpr(assignUnit, theExpr, unitList);
		}
		else if( as.getRightOp() instanceof NullConstant){
			List<ActivityOperationUnitSummary> summaryList = new ArrayList<ActivityOperationUnitSummary>();
			summaryList.add(new AssignNullAsyncTaskUnitSummary(assignUnit));
			return summaryList;
		}
		else
			return this.getAssignAsyncTask(assignUnit,as.getRightOp(),unitList);
	}
	
	/**
	 * @param originalValue
	 * @param unitList
	 * @return
	 */
	private List<ActivityOperationUnitSummary> getAssignAsyncTask(Unit assignUnit,Value originalValue,List<Unit> unitList){
		List<ActivityOperationUnitSummary> unitSummaryList = null;
		Value newValue = this.getReferedValue(originalValue);
		if( newValue == null)
			return null;
		for(int i=unitList.size()-2; i >= 0; i--){
			Unit unit = unitList.get(i);
			if( unit instanceof InvokeStmt && ((InvokeStmt)unit).containsInvokeExpr()){//Current unit is a invokeStmt which calls init method
				InvokeExpr ie = ((InvokeStmt)unit).getInvokeExpr();
				if( ie instanceof SpecialInvokeExpr && ((SpecialInvokeExpr)ie).getBase() == newValue && ie.getMethod().getName().equals("<init>") ){
					unitSummaryList = new ArrayList<ActivityOperationUnitSummary>();
					unitSummaryList.add(new AssignAsyncTaskInstanceUnitSummary(assignUnit,ie,unitList.subList(0,i+1)));
					return unitSummaryList;
				}
			}
			else if(unit instanceof IdentityStmt && ((IdentityStmt) unit).getLeftOp() == newValue && ((IdentityStmt) unit).getRightOp() instanceof ParameterRef){
				ParameterRef pr = (ParameterRef) ((IdentityStmt) unit).getRightOp();
				unitSummaryList = new ArrayList<ActivityOperationUnitSummary>();
				unitSummaryList.add(new AssignAsyncTaskInstanceUnitSummary(assignUnit,pr.getIndex()));
				return unitSummaryList;
			}
			else if( unit instanceof AssignStmt && this.isValueEqual(((AssignStmt) unit).getLeftOp(),newValue)){
				Value rightOp = ((AssignStmt) unit).getRightOp();
				if( rightOp instanceof InvokeExpr)
					return this.getAssignAsyncTaskSummaryFromInvokeExpr(assignUnit, (InvokeExpr)rightOp, unitList.subList(0, i+1));
				else if( rightOp instanceof NullConstant){
					List<ActivityOperationUnitSummary> summaryList = new ArrayList<ActivityOperationUnitSummary>();
					summaryList.add(new AssignNullAsyncTaskUnitSummary(assignUnit));
					return summaryList;
				}
				else
					return this.getAssignAsyncTask(assignUnit,rightOp ,unitList.subList(0, i+1));
			}
		}
		return null;
	}
}
