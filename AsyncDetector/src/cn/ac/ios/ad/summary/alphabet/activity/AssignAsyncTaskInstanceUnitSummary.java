package cn.ac.ios.ad.summary.alphabet.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.ac.ios.ad.AsyncTaskDetector;
import cn.ac.ios.ad.record.RecordController;
import cn.ac.ios.ad.summary.AsyncTaskInitMethodSummary;
import cn.ac.ios.ad.summary.DoInBackgroundMethodSummary;
import cn.ac.ios.ad.summary.analysis.ActivityAsyncOperationJimpleAnalysis;
import cn.ac.ios.ad.summary.topology.DoInBackgroundTopologyOperation;
import cn.ac.ios.ad.summary.topology.InitMethodTopologyOperation;
import cn.ac.ios.ad.summary.topology.TopologyOperation;
import cn.ac.ios.ad.util.MethodUtil;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Expr;
import soot.jimple.InvokeExpr;

/**
 * Assign value to the AsyncTask field of Activity.
 * For example, $r0.<com.example.listenertest.MainActivity: android.os.AsyncTask at> = $r2;
 * @author panlj
 */
public class AssignAsyncTaskInstanceUnitSummary extends ActivityOperationUnitSummary implements HoldingReference{
	protected SootMethod initMethod;
	protected Expr initExpr;
	protected List<Unit> unitList;
	protected SootMethod doInBackgroundMethod;

	List<TaintInstance> taintInstances;

	private void setDoInBackgroundMethod() {
		if( this.initMethod == null  )
			this.doInBackgroundMethod = null;
		else{
			SootClass asyncClass = this.initMethod.getDeclaringClass();
			System.out.println("-------asyncClass------------------");
			System.out.println(asyncClass.getName());
			this.doInBackgroundMethod = MethodUtil.getMethod(asyncClass, "doInBackground");
			String key = DoInBackgroundTopologyOperation.getDoInBackgroundKey(this.doInBackgroundMethod);
			DoInBackgroundMethodSummary doInBackgroundSummary = (DoInBackgroundMethodSummary) TopologyOperation.methodKeyToSummary.get(key);  
			if( doInBackgroundSummary == null ){
				TopologyOperation to = new DoInBackgroundTopologyOperation(this.doInBackgroundMethod);
				to.constructMainSummary(AsyncTaskDetector.cg);

				RecordController.getInstance().getAsyncTaskMethodOutput().addAsyncTaskDoInBackgroundMethodRecord(this.doInBackgroundMethod.getDeclaringClass(), ((DoInBackgroundMethodSummary)to.getSourceMethodSummary()).getLoopStartUnits().size());
			}
		}
	}
	
	public List<TaintInstance> getTaintInstances() {
		return taintInstances;
	}

	public void setTaintInstances(List<TaintInstance> taintInstances) {
		this.taintInstances = taintInstances;
	}

	public SootMethod getInitMethod(){
		return initMethod;
	}

	public void setInitMethod(SootMethod initMethod){
		this.initMethod = initMethod;
		String key = InitMethodTopologyOperation.getInitMethodKey(this.initMethod);
		AsyncTaskInitMethodSummary ims = (AsyncTaskInitMethodSummary)TopologyOperation.methodKeyToSummary.get(key);
		if( ims == null ){
			TopologyOperation to = new InitMethodTopologyOperation(this.initMethod, this.initMethod.getDeclaringClass());
			to.constructMainSummary(AsyncTaskDetector.cg);
			
			RecordController.getInstance().getAsyncTaskMethodOutput().addAsyncTaskInitMethodRecord(this.initMethod.getDeclaringClass(), ((AsyncTaskInitMethodSummary)to.getSourceMethodSummary()).getDirtyArgIndexToTaintedField().size());
		}
	}

	public Expr getInitExpr() {
		return initExpr;
	}

	public void setInitExpr(Expr initExpr) {
		this.initExpr = initExpr;
	}

	public AssignAsyncTaskInstanceUnitSummary(Unit currentUnit,int argIndex){
		super(currentUnit,argIndex);
	}
	
	public AssignAsyncTaskInstanceUnitSummary(Unit currentUnit,Expr initExpr,List<Unit> unitList){
		super(currentUnit);
		this.setInitExpr(initExpr);
		this.setInitMethod(((InvokeExpr)initExpr).getMethod());
		this.setDoInBackgroundMethod();
		this.unitList = unitList;
		this.initTaintInstances();
	}
	
	public SootMethod getDoInBackgroundMethod(){
		return this.doInBackgroundMethod;
	}
	
	/**
	 * The parameter of init method contains the field of Activity and such parameter is assigned to the field of AsyncTask
	 * Then the AsyncTask holds the reference to the Activity
	 * @param unitList
	 * @return
	 */
	public void initTaintInstances(){
		//specialinvoke $r5.<com.example.fieldsensitivitytest.MainActivity$InnerTask: void <init>(com.example.fieldsensitivitytest.MainActivity)>($r0);
		//$r0.<com.example.fieldsensitivitytest.MainActivity: android.os.AsyncTask at> = $r5;
		this.taintInstances = new ArrayList<TaintInstance>();
		if( initMethod.getParameterCount() == 0 )
			return;
		List<Value> args = ((InvokeExpr)initExpr).getArgs();
		
		String key = InitMethodTopologyOperation.getInitMethodKey(this.initMethod);
		AsyncTaskInitMethodSummary ims = (AsyncTaskInitMethodSummary)TopologyOperation.methodKeyToSummary.get(key);
		if( ims == null )
			return;
		Map<Integer,SootField> argIndexToField = ims.getDirtyArgIndexToTaintedField();
		ActivityAsyncOperationJimpleAnalysis activityAnalysis = new ActivityAsyncOperationJimpleAnalysis();
		
		for(int i=0;i < args.size(); i++){
			if( !argIndexToField.containsKey(i) )
				continue;
			Unit source = activityAnalysis.isValueReferToViewElement(args.get(i), this.unitList);
			this.taintInstances.add(new TaintInstance(argIndexToField.get(i),i,source));
//			if(source != null )
//				this.taintInstances.add(new TaintInstance(argIndexToField.get(i),i,source));
		}
		return;
	}

	@Override
	public String getSummary() {
		// TODO Auto-generated method stub
		return SummaryAlphabet.ASSIGN_ASYNC_INSTANCE;
	}
}
