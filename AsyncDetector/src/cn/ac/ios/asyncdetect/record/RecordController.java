/* AsyncDetecotr - an Android async component misuse detection tool
 * Copyright (C) 2018 Baoquan Cui
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

package cn.ac.ios.asyncdetect.record;

import cn.ac.ios.asyncdetect.util.Log;

/**
 * 
 * @author Baoquan Cui
 * @version 1.0
 */
public class RecordController {

	private static RecordController sInstance = null;
	
	private AsyncTaskFieldDetectedErrorOutput mAsyncTaskFieldDetectedErrorOutput = null;

	private TimeOutput mTimeOutput = null;

	private AsyncTaskMethodOutput mAsyncTaskMethodOutput = null;

	private AsyncTaskActivityLifeCycleMethodOutput mAsyncTaskActivityLifeCycleMethodOutput = null;

	private AsyncTaskListenerOutput mAsyncTaskListenerOutput = null;
	
	private LifeCycleMethodLoopOutput mLifeCycleMethodLoopOutput = null;
	
	private AsyncTaskFieldOutput mAsyncTaskFieldOutput = null;

	private RecordController() {
		
		newOutputInstance();
		
	}

	public static RecordController getInstance() {
		if (sInstance == null) {
			sInstance = new RecordController();
		}
		return sInstance;
	}

	public AsyncTaskFieldDetectedErrorOutput getAsyncTaskFieldDetectedErrorOutput() {
		return mAsyncTaskFieldDetectedErrorOutput;
	}

	public TimeOutput getTimeOutput() {
		return mTimeOutput;
	}

	public AsyncTaskMethodOutput getAsyncTaskMethodOutput() {
		return mAsyncTaskMethodOutput;
	}

	public AsyncTaskActivityLifeCycleMethodOutput getAsyncTaskActivityLifeCycleMethodOutput() {
		return mAsyncTaskActivityLifeCycleMethodOutput;
	}

	public AsyncTaskListenerOutput getAsyncTaskListenerOutput() {
		return mAsyncTaskListenerOutput;
	}

	public LifeCycleMethodLoopOutput getLifeCycleMethodLoopOutput() {
		return mLifeCycleMethodLoopOutput;
	}
	
	public AsyncTaskFieldOutput getAsyncTaskFieldOutput(){
		return mAsyncTaskFieldOutput;
	}

	private void newOutputInstance() {
		mAsyncTaskFieldDetectedErrorOutput = new AsyncTaskFieldDetectedErrorOutput();
		mTimeOutput = new TimeOutput();
		mAsyncTaskListenerOutput = new AsyncTaskListenerOutput();
		mAsyncTaskActivityLifeCycleMethodOutput = new AsyncTaskActivityLifeCycleMethodOutput();
		mAsyncTaskMethodOutput = new AsyncTaskMethodOutput();
		mLifeCycleMethodLoopOutput = new LifeCycleMethodLoopOutput();
		mAsyncTaskFieldOutput = new AsyncTaskFieldOutput();
	}

	public void output() {
		mAsyncTaskFieldDetectedErrorOutput.output();
		mTimeOutput.output();
		mAsyncTaskMethodOutput.output();
		mAsyncTaskActivityLifeCycleMethodOutput.output();
		mAsyncTaskListenerOutput.output();
		mLifeCycleMethodLoopOutput.output();
		mAsyncTaskFieldOutput.output();
		Log.i("-----------record has been finished --------  ");
	}
	
	public void onExit(){
		
	}
}
