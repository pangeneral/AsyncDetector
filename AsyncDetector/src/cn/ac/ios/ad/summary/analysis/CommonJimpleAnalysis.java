package cn.ac.ios.ad.summary.analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.ac.ios.ad.AsyncTaskDetector;
import cn.ac.ios.ad.util.ClassInheritanceProcess;
import soot.Local;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import soot.jimple.VirtualInvokeExpr;
import soot.toolkits.graph.UnitGraph;

/**
 * This class includes some common operations to analysis jimple code
 * @author panlj
 *
 */
public class CommonJimpleAnalysis {	
	/**
	 * Backwardly analyze along the current path to judge whether local variable l refer to "this" object
	 * @param l
	 * @param unitList
	 * @return
	 */
	protected boolean isValueReferToThis(Value v,List<Unit> unitList){
		for(int i=unitList.size()-2; i >= 0 ; i--){
			Unit unit = unitList.get(i);
			if( unit instanceof AssignStmt && ((AssignStmt)unit).getLeftOp() == v)
				//In jimple grammar, the right operator could be 
				//array_ref | constant | expr | instance_field_ref | local | next_next_stmt_address | static_field_ref.
				return isValueReferToThis(((AssignStmt)unit).getRightOp(),unitList.subList(0, i));
			else if( unit instanceof IdentityStmt && ((IdentityStmt)unit).getLeftOp() == v ){
				//The following is the grammar of identity_stmt
				//identity_stmt = local ":=" identity_value;
				//identity_value = caught_exception_ref | parameter_ref | this_ref;
				//Apparently, the right operator must be this_ref so that l refers to "this" object
				if( ((IdentityStmt)unit).getRightOp() instanceof ThisRef)
					return true;
				else
					return false;
			}
		}
		return false;
	}
	
	
	
	
	/**
	 * Judge whether currentStmt contains method invoke statement
	 * Here, the method must have a body which means it is included in the Topology graph
	 * @param currentStmt
	 * @param node
	 * @return
	 */
	public InvokeExpr getInvokeExprOfCurrentStmt(Stmt currentStmt){
		if(currentStmt instanceof InvokeStmt)
			return currentStmt.getInvokeExpr();
		else if( currentStmt instanceof AssignStmt && ((AssignStmt) currentStmt).getRightOp() instanceof InvokeExpr)
			return (InvokeExpr) ((AssignStmt) currentStmt).getRightOp();
		else
			return null;
	}
	
	
	
	/**
	 * Get the field that v refers to, if v doesn't refer to any field, return null.
	 * For example, v is $r3 and $r3 = $r0.<com.example.fieldsensitivitytest.AsyncTasker: android.widget.Button[] buttonArray>,
	 * then return <com.example.fieldsensitivitytest.AsyncTasker: android.widget.Button[] buttonArray>.
	 * @param v
	 * @param unitList
	 * @return
	 */
	protected SootField getValueReferToField(Value v,List<Unit> unitList){
		Value newValue = this.getReferedValue(v);
		if(newValue == null)
			return null;
		for(int i=unitList.size()-1;i>=0;i--){
			Unit unit = unitList.get(i);
			//$r3 = $r0.<com.example.fieldsensitivitytest.AsyncTasker: android.widget.Button[] buttonArray>
			if( unit instanceof AssignStmt && this.isValueEqual(((AssignStmt)unit).getLeftOp(),newValue) ){
				Value rightOp = ((AssignStmt) unit).getRightOp();
				if( rightOp instanceof InstanceFieldRef ){
					InstanceFieldRef ifr = (InstanceFieldRef)rightOp;
					if( this.isValueReferToThis(ifr.getBase(),unitList.subList(0, i)) )
						return ifr.getField();
					else
						return null;
				}
				else if( rightOp instanceof StaticFieldRef )
					return ((StaticFieldRef) rightOp).getField();
//				else if( rightOp instanceof InvokeExpr)
				else
					return this.getValueReferToField(rightOp, unitList.subList(0, i));//NOTE: need to be modified
			}
		}
		return null;
	}
	
	protected boolean isValueEqual(Value firstValue,Value secondValue){
		boolean isFirstFieldRef = firstValue instanceof FieldRef;
		boolean isSecondFieldRef = secondValue instanceof FieldRef;
		if( isFirstFieldRef && isSecondFieldRef){
			if( ((FieldRef) firstValue).getField() == ((FieldRef)secondValue).getField() )
				return true;
			else
				return false;
		}
		else if( !isFirstFieldRef && !isSecondFieldRef){
			if( firstValue == secondValue )
				return true;
			else
				return false;
		}
		else
			return false;
	}
	
	protected Value getReferedValue(Value originalValue){
		//In jimple grammar, 
		//the left operator could be array_ref | instance_field_ref | static_field_ref | local
		//the right operator could be array_ref | constant | expr | instance_field_ref | local | next_next_stmt_address | static_field_ref.
		//expr = binop_expr | cast_expr | instance_of_expr | invoke_expr | new_array_expr |new_expr | new_multi_array_expr | unop_expr;
		//Currently, we don't process InstanceFieldRef and StaticFieldRef as we may not find where they are initialized through backward traversing
		if( originalValue instanceof Local || originalValue instanceof FieldRef || originalValue instanceof ArrayRef)
			return originalValue;
		else if(originalValue instanceof CastExpr)
			return ((CastExpr) originalValue).getOp();
		else if( originalValue instanceof NullConstant )
			return NullConstant.v();
		else
			return null;
	}
	
	/**
	 * CurrentStmt contains invoke expression, get the actual parameters of the invoked method
	 * @param currentStmt
	 * @return
	 */
	public List<Value> getActualParameter(Stmt currentStmt){
		if(currentStmt instanceof InvokeStmt){
			InvokeStmt is = (InvokeStmt)currentStmt;
			InvokeExpr ie = is.getInvokeExpr();
			SootMethod invokedMethod = ie.getMethod();
			if( AsyncTaskDetector.methodSignatureToBody.get(invokedMethod.getSignature()) != null)
				return ie.getArgs();
		}
		else if( currentStmt instanceof AssignStmt){
			AssignStmt as = (AssignStmt)currentStmt;
			Value ro = as.getRightOp();
			if( ro instanceof InvokeExpr){
				SootMethod invokedMethod = ((InvokeExpr) ro).getMethod();
				if( AsyncTaskDetector.methodSignatureToBody.get(invokedMethod.getSignature()) != null)
					return ((InvokeExpr) ro).getArgs();
			}
		}
		return null;
	}
	
	
	public void recordExprMessage(Unit unit,BufferedWriter bw){
		try {
			bw.write(unit.toString()+"\n");
			
			List<UnitBox> unitBoxes = unit.getBoxesPointingToThis();
			for(UnitBox box:unitBoxes){
				bw.write("boxes pointing to this: "+box.getUnit().toString()+"\n");
			}
			
			List<ValueBox> valueBoxes = unit.getUseBoxes();
			for(ValueBox box:valueBoxes){
				bw.write("use boxes: "+box.getValue().toString()+"\n");
			}
			
			List<ValueBox> defineBoxes = unit.getDefBoxes();
			for(ValueBox box:defineBoxes){
				bw.write("define boxes: "+box.getValue().toString()+"\n");
			}
			bw.write("========================================\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Forwardly analyze along the unit graph to judge whether there is a virtual invoke statement whose base is the local variable l after the given unit
	 * @param unitList
	 * @param theGraph
	 * @param l
	 * @param methodName
	 * @return
	 */
	@SuppressWarnings("unused")
	private InvokeExpr getInvokeExpr(List<Unit> unitList,UnitGraph theGraph,Local l,String methodName){
		if(unitList.size() != 1 )
			return null;
		Unit unit = unitList.get(0); 
		if( unit instanceof InvokeStmt ){
			InvokeExpr ie = ((InvokeStmt)unit).getInvokeExpr();
			if( ie instanceof VirtualInvokeExpr && ((VirtualInvokeExpr) ie).getBase() == l){
				if(ie.getMethod().getName().equals(methodName) )
					return ie;
				else
					return null;
			}
		}
		return getInvokeExpr(theGraph.getSuccsOf(unit),theGraph,l,methodName);
	}
}
