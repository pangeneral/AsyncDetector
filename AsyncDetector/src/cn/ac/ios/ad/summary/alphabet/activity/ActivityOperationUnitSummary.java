package cn.ac.ios.ad.summary.alphabet.activity;

import cn.ac.ios.ad.summary.alphabet.AbstractUnitSummary;
import soot.Unit;

/**
 * Base class for any operation towards AsyncTask within Activity
 * @author panlj
 *
 */
public abstract class ActivityOperationUnitSummary extends AbstractUnitSummary{

	/**
	 * Whatever the operation toward AsyncTask is assign, execute or cancel, 
	 * we need to backwardly traverse the control flow to find the instance of AsyncTask that the operation really points to.
	 * If the operation points to a parameter of current method, then "assignedArgIndex" refers to the index of the parameter. 
	 * For example, in situation below, assignedArgIndex is 1
	 * $r0 := @parameter0: com.example.asynctasktest.DialogActivity;
     * $r1 := @parameter1: com.example.asynctasktest.DialogActivity$InnerStaticAsyncTask;
     * $r0.<com.example.asynctasktest.DialogActivity: com.example.asynctasktest.DialogActivity$InnerStaticAsyncTask currentTask> = $r1;
	 */
	protected int assignedArgIndex;
	
	/**
	 * If we don't find the init method of AsyncTask, then isComplete is false
	 */
	protected boolean isComplete;
	
	public ActivityOperationUnitSummary(Unit currentUnit) {
		super(currentUnit);
		// TODO Auto-generated constructor stub
		this.setComplete(true);
	}
	
	public ActivityOperationUnitSummary(Unit currentUnit,int argIndex){
		super(currentUnit);
		this.setAssignedArgIndex(argIndex);
		this.setComplete(false);
	}
	
	public boolean isComplete() {
		return isComplete;
	}

	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}
	
	/**
	 * this method can be invoked iff isComplete is false
	 * @return
	 */
	public int getAssignedArgIndex(){
		return assignedArgIndex;
	}

	public void setAssignedArgIndex(int assignedArgIndex) {
		this.assignedArgIndex = assignedArgIndex;
	}
}
