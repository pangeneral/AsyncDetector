package cn.ac.ios.ad.summary.topology;

import java.util.HashSet;
import java.util.Set;

import cn.ac.ios.ad.AsyncTaskDetector;
import cn.ac.ios.ad.constant.BasicType;
import cn.ac.ios.ad.summary.AbstractMethodSummary;
import cn.ac.ios.ad.summary.ActivityAsyncOperationMethodSummary;
import cn.ac.ios.ad.summary.AsyncTaskInitMethodSummary;
import cn.ac.ios.ad.util.ClassInheritanceProcess;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

public class InitMethodTopologyOperation extends TopologyOperation{
	private SootClass asyncTaskClass;
	
	/**
	 * The set of field of AsyncTask which could hold reference to outter Activity.
	 * The possible types of such fields include instance of Map, Collection, Activity or View. 
	 */
	private Set<SootField> fieldsOfAsyncTask;
	
	public InitMethodTopologyOperation(SootMethod sourceMethod,SootClass asyncTaskClass) {
		super(sourceMethod);
		// TODO Auto-generated constructor stub
		this.asyncTaskClass = asyncTaskClass;
		this.fieldsOfAsyncTask = new HashSet<SootField>();
		for(SootField field:this.asyncTaskClass.getFields()){
			//The field can hold the reference of Activity if it is the instance of android.view.View or collection of android.view.View
			SootClass fieldClass = Scene.v().getSootClass(field.getType().toString());
//			SootClass fieldClass = AsyncTaskDetector.classNameToSootClass.get(field.getType().toString());
			if( ClassInheritanceProcess.isInheritedFromView(fieldClass) || ClassInheritanceProcess.isInheritedFromMap(fieldClass) 
				|| ClassInheritanceProcess.isInheritedFromCollection(fieldClass) || ClassInheritanceProcess.isInheritedFromActivity(fieldClass))
				this.fieldsOfAsyncTask.add(field);
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
		return TopologyOperation.methodKeyToSummary.get(getInitMethodKey(this.sourceMethod));
	}

	@Override
	public String getKey(SootMethod theMethod) {
		// TODO Auto-generated method stub
		return getInitMethodKey(theMethod);
	}

	@Override
	public AbstractMethodSummary getMethodSummary(TopologyNode topNode) {
		// TODO Auto-generated method stub
		return new AsyncTaskInitMethodSummary(topNode.method,this.fieldsOfAsyncTask);
	}

	

}
