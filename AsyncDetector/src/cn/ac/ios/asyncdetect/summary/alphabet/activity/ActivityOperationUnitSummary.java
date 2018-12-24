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

package cn.ac.ios.asyncdetect.summary.alphabet.activity;

import cn.ac.ios.asyncdetect.summary.alphabet.AbstractUnitSummary;
import soot.Unit;

/**
 * Base class for any external AsyncTask operation
 * @author Linjie Pan
 * @version 1.0
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
	protected int mAssignedArgIndex;
	
	/**
	 * If we don't find the init method of AsyncTask, then isComplete is false
	 */
	protected boolean mIsComplete;
	
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
		return mIsComplete;
	}

	public void setComplete(boolean isComplete) {
		this.mIsComplete = isComplete;
	}
	
	/**
	 * this method can be invoked iff isComplete is false
	 * @return
	 */
	public int getAssignedArgIndex(){
		return mAssignedArgIndex;
	}

	public void setAssignedArgIndex(int assignedArgIndex) {
		this.mAssignedArgIndex = assignedArgIndex;
	}
}
