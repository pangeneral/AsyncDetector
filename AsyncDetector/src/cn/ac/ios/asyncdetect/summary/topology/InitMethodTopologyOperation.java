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

import java.util.HashSet;
import java.util.Set;

import cn.ac.ios.asyncdetect.summary.AbstractMethodSummary;
import cn.ac.ios.asyncdetect.summary.AsyncTaskInitMethodSummary;
import cn.ac.ios.asyncdetect.util.ClassInheritanceProcess;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

/**
 * 
 * @author Linjie Pan
 * @version 1.0
 */
public class InitMethodTopologyOperation extends TopologyOperation{
	private SootClass mAsyncTaskClass;
	
	/**
	 * The set of field of AsyncTask which could hold reference to outter Activity.
	 * The possible types of such fields include instance of Map, Collection, Activity or View. 
	 */
	private Set<SootField> mFieldsOfAsyncTask;
	
	public InitMethodTopologyOperation(SootMethod sourceMethod,SootClass asyncTaskClass) {
		super(sourceMethod);
		// TODO Auto-generated constructor stub
		this.mAsyncTaskClass = asyncTaskClass;
		this.mFieldsOfAsyncTask = new HashSet<SootField>();
		for(SootField field:this.mAsyncTaskClass.getFields()){
			//The field can hold the reference of Activity if it is the instance of android.view.View or collection of android.view.View
			SootClass fieldClass = Scene.v().getSootClass(field.getType().toString());
//			SootClass fieldClass = AsyncTaskDetector.classNameToSootClass.get(field.getType().toString());
			if( ClassInheritanceProcess.isInheritedFromView(fieldClass) || ClassInheritanceProcess.isInheritedFromMap(fieldClass) 
				|| ClassInheritanceProcess.isInheritedFromCollection(fieldClass) || ClassInheritanceProcess.isInheritedFromActivity(fieldClass))
				this.mFieldsOfAsyncTask.add(field);
		}
	}
	
	/**
	 * key=methodSignature+"-INIT_METHOD";
	 * @param theMethod
	 * @return
	 */
	public static String getInitMethodKey(SootMethod theMethod){
		return theMethod.getSignature()+"-INIT_METHOD";
	}

	@Override
	public AbstractMethodSummary getSourceMethodSummary() {
		// TODO Auto-generated method stub
		return TopologyOperation.sMethodKeyToSummary.get(getInitMethodKey(this.mSourceMethod));
	}

	@Override
	public String getKey(SootMethod theMethod) {
		// TODO Auto-generated method stub
		return getInitMethodKey(theMethod);
	}

	@Override
	public AbstractMethodSummary getMethodSummary(TopologyNode topNode) {
		// TODO Auto-generated method stub
		return new AsyncTaskInitMethodSummary(topNode.mMethod,this.mFieldsOfAsyncTask);
	}

	

}
