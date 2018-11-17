package cn.ac.ios.ad.summary.analysis;

import java.util.List;

import cn.ac.ios.ad.summary.ActivityGetFieldMethodSummary;
import cn.ac.ios.ad.summary.topology.ActivityGetFieldTopologyOperation;
import cn.ac.ios.ad.summary.topology.TopologyOperation;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.SpecialInvokeExpr;

public class ActivityGetFieldJimpleAnalysis extends CommonJimpleAnalysis{
	
	
	public boolean setGetFieldMethodSummary(ActivityGetFieldMethodSummary thisSummary,Value originalValue,List<Unit> unitList){
		Value newValue = this.getReferedValue(originalValue);
		if( newValue == null)
			return false;
		for(int i=unitList.size()-2; i >= 0; i--){
			Unit unit = unitList.get(i);
			if( unit instanceof InvokeStmt){//1.Instance of the AsyncTask
				InvokeExpr ie = ((InvokeStmt)unit).getInvokeExpr();
				if( ie instanceof SpecialInvokeExpr && ((SpecialInvokeExpr)ie).getBase() == newValue && ie.getMethod().getName().equals("<init>") ){
					thisSummary.setReturnValue(ie);
					thisSummary.setInitUnitList(unitList.subList(0, i+1));
					return true;
				}
			}
			else if(unit instanceof IdentityStmt && ((IdentityStmt)unit).getLeftOp() == newValue && ((IdentityStmt) unit).getRightOp() instanceof ParameterRef){
				// 3.Parameter of current method
				ParameterRef pr = (ParameterRef) ((IdentityStmt) unit).getRightOp();
				thisSummary.setReturnValue(pr);
				return true;
			}
			else if( unit instanceof AssignStmt && this.isValueEqual(((AssignStmt) unit).getLeftOp(),newValue)){
				Value rightOp = ((AssignStmt) unit).getRightOp();
				/**
				 * If rightOp is an invokeExpr, then we discuss the three conditions of the returnValue of the method invoked by the invokeExpr
				 */
				if( rightOp instanceof InvokeExpr){
					InvokeExpr theExpr = (InvokeExpr)rightOp;
					SootMethod theMethod = theExpr.getMethod();
					String key = ActivityGetFieldTopologyOperation.getGetFieldKey(theMethod);
					ActivityGetFieldMethodSummary subMethodSummary = (ActivityGetFieldMethodSummary) TopologyOperation.methodKeyToSummary.get(key);
					if(subMethodSummary == null){
						continue;
					}
					Value returnValue = subMethodSummary.getReturnValue();
					assert(returnValue != null);
					if( subMethodSummary.getReferedFields().size() > 0 )
						thisSummary.getReferedFields().addAll(subMethodSummary.getReferedFields());
					if( returnValue instanceof ParameterRef){
						int argIndex = ((ParameterRef) returnValue).getIndex();
						return this.setGetFieldMethodSummary(thisSummary,theExpr.getArg(argIndex),unitList.subList(0, i+1));
					}
					else if( returnValue instanceof FieldRef){
						thisSummary.setReturnValue(returnValue);
						return this.setGetFieldMethodSummary(thisSummary, returnValue, unitList.subList(0, i+1));
					}
					else if( returnValue instanceof InvokeExpr){
						thisSummary.setReturnValue(returnValue);
						thisSummary.setInitUnitList(subMethodSummary.getInitUnitList());
						return true;
					}
				}
				else if(rightOp instanceof FieldRef){
					//2.Field of Activity
					thisSummary.addReferedFields(((FieldRef) rightOp).getField());
					thisSummary.setReturnValue(rightOp);
					return this.setGetFieldMethodSummary(thisSummary, rightOp, unitList.subList(0, i+1));
				}
				else if( rightOp instanceof NullConstant)//4.NullConstant
					thisSummary.setReturnValue(rightOp);
				else
					return this.setGetFieldMethodSummary(thisSummary,rightOp, unitList.subList(0, i+1));
			}
		}
		return false;
	}
	
}
