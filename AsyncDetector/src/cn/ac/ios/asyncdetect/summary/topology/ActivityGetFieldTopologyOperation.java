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
import cn.ac.ios.asyncdetect.summary.ActivityGetFieldMethodSummary;
import soot.SootMethod;

/**
 * 
 * @author Linjie Pan
 * @version 1.0
 */
public class ActivityGetFieldTopologyOperation extends TopologyOperation{
	
	public ActivityGetFieldTopologyOperation(SootMethod sourceMethod) {
		super(sourceMethod);
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractMethodSummary getSourceMethodSummary() {
		// TODO Auto-generated method stub
		return TopologyOperation.sMethodKeyToSummary.get(getGetFieldKey(this.mSourceMethod));
	}
	
	/**
	 * key=methodSignature+"-GET_FIELD";
	 * @param theMethod
	 * @return
	 */
	public static String getGetFieldKey(SootMethod theMethod){
		return theMethod.getSignature()+"-GET_FIELD";
	}

	@Override
	public String getKey(SootMethod theMethod) {
		// TODO Auto-generated method stub
		return getGetFieldKey(theMethod);
	}

	@Override
	public AbstractMethodSummary getMethodSummary(TopologyNode topNode) {
		// TODO Auto-generated method stub
		return new ActivityGetFieldMethodSummary(topNode.mMethod);
	}
}