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

package cn.ac.ios.asyncdetect.summary.topology;

import cn.ac.ios.asyncdetect.summary.AbstractMethodSummary;
import cn.ac.ios.asyncdetect.summary.ActivityAsyncOperationMethodSummary;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

/**
 * 
 * @author Linjie Pan
 * @version 1.0
 */
public class ActivityTopologyOperation extends TopologyOperation{
	private SootField mFieldUnderAnalysis;
	private SootClass mActivityClass;
	
	public SootField getFieldUnderAnalysis() {
		return mFieldUnderAnalysis;
	}

	public void setFieldUnderAnalysis(SootField fieldUnderAnalysis) {
		this.mFieldUnderAnalysis = fieldUnderAnalysis;
	}

	public SootClass getActivityClass() {
		return mActivityClass;
	}

	public void setActivityClass(SootClass activityClass) {
		this.mActivityClass = activityClass;
	}

	public ActivityTopologyOperation(SootMethod sourceMethod,
			SootField fieldUnderAnalysis,SootClass activityClass) {
		super(sourceMethod);
		// TODO Auto-generated constructor stub
		this.mFieldUnderAnalysis = fieldUnderAnalysis;
		this.mActivityClass = activityClass;
	}
	
	/**
	 * key=fieldSignature+"-"+methodSignature
	 * @param fieldUnderAnalysis
	 * @param methodUnderAnalysis
	 * @return
	 */
	public static String getActivityOperationKey(SootField fieldUnderAnalysis,SootMethod methodUnderAnalysis){
		return fieldUnderAnalysis.getSignature()+"-"+methodUnderAnalysis.getSignature();
	}
	
	@Override
	public String getKey(SootMethod theMethod) {
		// TODO Auto-generated method stub
		return getActivityOperationKey(mFieldUnderAnalysis,theMethod);
	}
	
	protected void printTopologyGraph(){
		System.out.print("Field under analysis is " + this.mFieldUnderAnalysis + " ");
		super.printTopologyGraph();
	}

	@Override
	public AbstractMethodSummary getSourceMethodSummary() {
		// TODO Auto-generated method stub
		return TopologyOperation.sMethodKeyToSummary.get(ActivityTopologyOperation.getActivityOperationKey(this.mFieldUnderAnalysis,this.mSourceMethod));
	}

	@Override
	public AbstractMethodSummary getMethodSummary(TopologyNode topNode) {
		// TODO Auto-generated method stub
		return new ActivityAsyncOperationMethodSummary(topNode.mMethod,this.mFieldUnderAnalysis,this.mActivityClass);
	}

	

}
