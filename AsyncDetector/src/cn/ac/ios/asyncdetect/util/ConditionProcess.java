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

package cn.ac.ios.asyncdetect.util;

import org.junit.Test;

import soot.Value;
import soot.jimple.ConditionExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.IntConstant;
import soot.jimple.LeExpr;
import soot.jimple.LongConstant;
import soot.jimple.LtExpr;
import soot.jimple.NumericConstant;
import soot.jimple.StringConstant;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JGtExpr;
import soot.jimple.internal.JLeExpr;
import soot.jimple.internal.JLtExpr;

/**
 * 
 * @author Linjie Pan
 * @version 1.0
 */
public class ConditionProcess {
	
	/**
	 * In jimple, condition = eq_expr | ge_expr | le_expr | lt_expr | ne_expr | gt_expr;
	 * We only consider ge, le, lt and gt expr and judge whether they can be reduced to eq or ne expr. 
	 * @param condition
	 * @return
	 */
	public static ConditionExpr isConditionReducedToEqOrNeq(ConditionExpr condition){
		Value leftOp = condition.getOp1();
		Value rightOp = condition.getOp2();
		if(condition instanceof GtExpr ){
			if( leftOp instanceof NumericConstant )
				return processLtExpr(rightOp,leftOp);
			else if( rightOp instanceof NumericConstant)
				return processGtExpr(leftOp,rightOp);
			else 
				return null;
		}
		else if( condition instanceof GeExpr){
			if( leftOp instanceof NumericConstant)
				return processLeExpr(rightOp,leftOp);
			else if( rightOp instanceof NumericConstant)
				return processGeExpr(leftOp,rightOp);
			else
				return null;
		}
		else if( condition instanceof LtExpr){
			if( leftOp instanceof NumericConstant)
				return processGtExpr(rightOp,leftOp);
			else if( rightOp instanceof NumericConstant)
				return processLtExpr(leftOp,rightOp);
			else
				return null;
		}
		else if( condition instanceof LeExpr){
			if( leftOp instanceof NumericConstant)
				return processGeExpr(rightOp,leftOp);
			else if( rightOp instanceof NumericConstant)
				return processLeExpr(leftOp,rightOp);
			else
				return null;
		}
		else
			return null;
	}
	
	private static ConditionExpr processGtExpr(Value leftOp,Value rightOp){
		double theValue = getNumericValue((NumericConstant) rightOp);
		if( theValue >= 0 && theValue < 1)
			return new JEqExpr(leftOp,IntConstant.v(1));
		else
			return null;
	}
	
	private static ConditionExpr processGeExpr(Value leftOp,Value rightOp){
		double theValue = getNumericValue((NumericConstant) rightOp);
		if( theValue > 0 && theValue <= 1)
			return new JEqExpr(leftOp,IntConstant.v(1));
		else
			return null;
	}
	
	private static ConditionExpr processLtExpr(Value leftOp,Value rightOp){
		double theValue = getNumericValue((NumericConstant) rightOp);
		if( theValue > 0 && theValue <= 1)
			return new JEqExpr(leftOp,IntConstant.v(0));
		else
			return null;
	}
	
	private static ConditionExpr processLeExpr(Value leftOp,Value rightOp){
		double theValue = getNumericValue((NumericConstant) rightOp);
		if( theValue >= 0 && theValue < 1)
			return new JEqExpr(leftOp,IntConstant.v(0));
		else
			return null;
	}
	
	private static double getNumericValue(NumericConstant nc){
		if( nc instanceof IntConstant)
			return ((IntConstant) nc).value;
		else if( nc instanceof DoubleConstant)
			return ((DoubleConstant) nc).value;
		else if( nc instanceof FloatConstant)
			return ((FloatConstant) nc).value;
		else 
			return ((LongConstant) nc).value;
	}
	
	@Test
	public void test(){
		ConditionExpr gt[] = new ConditionExpr[100];
		gt[0] = new JGtExpr(StringConstant.v("abc"),IntConstant.v(1));
		gt[1] = new JGtExpr(IntConstant.v(1),StringConstant.v("abc"));
		gt[2] = new JGtExpr(IntConstant.v(0),StringConstant.v("abc"));
		gt[3] = new JGtExpr(DoubleConstant.v(0.5),StringConstant.v("abc"));
		gt[4] = new JGtExpr(DoubleConstant.v(1.0),StringConstant.v("abc"));
		gt[5] = new JGtExpr(DoubleConstant.v(0.0),StringConstant.v("abc"));
		
		gt[6] = new JGeExpr(StringConstant.v("abc"),IntConstant.v(1));
		gt[7] = new JGeExpr(IntConstant.v(1),StringConstant.v("abc"));
		gt[8] = new JGeExpr(IntConstant.v(0),StringConstant.v("abc"));
		gt[9] = new JGeExpr(DoubleConstant.v(0.5),StringConstant.v("abc"));
		gt[10] = new JGeExpr(DoubleConstant.v(1.0),StringConstant.v("abc"));
		gt[11] = new JGeExpr(DoubleConstant.v(0.0),StringConstant.v("abc"));
		
		gt[12] = new JLeExpr(StringConstant.v("abc"),IntConstant.v(1));
		gt[13] = new JLeExpr(IntConstant.v(1),StringConstant.v("abc"));
		gt[14] = new JLeExpr(IntConstant.v(0),StringConstant.v("abc"));
		gt[15] = new JLeExpr(DoubleConstant.v(0.5),StringConstant.v("abc"));
		gt[16] = new JLeExpr(DoubleConstant.v(1.0),StringConstant.v("abc"));
		gt[17] = new JLeExpr(DoubleConstant.v(0.0),StringConstant.v("abc"));
		
		gt[18] = new JLtExpr(StringConstant.v("abc"),IntConstant.v(1));
		gt[19] = new JLtExpr(IntConstant.v(1),StringConstant.v("abc"));
		gt[20] = new JLtExpr(IntConstant.v(0),StringConstant.v("abc"));
		gt[21] = new JLtExpr(DoubleConstant.v(0.5),StringConstant.v("abc"));
		gt[22] = new JLtExpr(DoubleConstant.v(1.0),StringConstant.v("abc"));
		gt[23] = new JLtExpr(DoubleConstant.v(0.0),StringConstant.v("abc"));
		for(int i=0;i <= 23; i++){
			ConditionExpr newExpr = isConditionReducedToEqOrNeq(gt[i]);
			if(newExpr == null)
				Log.i("gt["+i+"] "+gt[i].toString() + " is null");
			else
				Log.i("gt["+i+"] "+gt[i].toString()+ " is "+newExpr.toString());
		}
	}
}
