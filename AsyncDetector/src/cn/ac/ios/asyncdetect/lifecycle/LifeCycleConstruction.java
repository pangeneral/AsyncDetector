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

package cn.ac.ios.asyncdetect.lifecycle;

import java.util.ArrayList;
import java.util.List;

import cn.ac.ios.asyncdetect.constant.MethodSignature;
import cn.ac.ios.asyncdetect.util.Log;

/**
 * Combine the lifecyle methods of Activity and generate dummy method 
 * @author Linjie Pan
 * @version 1.0
 */
public class LifeCycleConstruction {
	private List<List<String>> mLifeCycleOrderList;
	
	public List<List<String>> getLifeCycleOrderList() {
		return mLifeCycleOrderList;
	}

	public void setLifeCycleOrderList(List<List<String>> lifeCycleOrderList) {
		this.mLifeCycleOrderList = lifeCycleOrderList;
	}

	public LifeCycleConstruction(){
		this.initLifeCycleOrderList();
	}
	
	public void initLifeCycleOrderList(){
		this.mLifeCycleOrderList = new ArrayList<List<String>>();
		
		List<String> onePassOrder = new ArrayList<String>();
		onePassOrder.add(MethodSignature.INIT);
		this.addEntireLifeCycle(onePassOrder);
		onePassOrder.add(MethodSignature.ON_DESTROY);
		this.mLifeCycleOrderList.add(onePassOrder);
		
		List<String> onRestartOrder = new ArrayList<String>();
		onRestartOrder.add(MethodSignature.INIT);
		this.addEntireLifeCycle(onRestartOrder);
		onRestartOrder.add(MethodSignature.ON_RESTART);
		this.addVisibleLifeCycle(onRestartOrder);
		onRestartOrder.add(MethodSignature.ON_DESTROY);
		this.mLifeCycleOrderList.add(onRestartOrder);
		
		List<String> onRestartDoubleOrder = new ArrayList<String>();
		onRestartDoubleOrder.add(MethodSignature.INIT);
		this.addEntireLifeCycle(onRestartDoubleOrder);
		onRestartDoubleOrder.add(MethodSignature.ON_RESTART);
		this.addVisibleLifeCycle(onRestartDoubleOrder);
		onRestartDoubleOrder.add(MethodSignature.ON_RESTART);
		this.addVisibleLifeCycle(onRestartDoubleOrder);
		onRestartDoubleOrder.add(MethodSignature.ON_DESTROY);
		this.mLifeCycleOrderList.add(onRestartDoubleOrder);
		
		List<String> onPauseToOnResumeOrder = new ArrayList<String>();
		onPauseToOnResumeOrder.add(MethodSignature.INIT);
		this.addEntireLifeCycle(onPauseToOnResumeOrder);
		onPauseToOnResumeOrder.remove(onPauseToOnResumeOrder.size()-1);
		this.addForeGroundLifeCycle(onPauseToOnResumeOrder);
		onPauseToOnResumeOrder.add(MethodSignature.ON_STOP);
		onPauseToOnResumeOrder.add(MethodSignature.ON_DESTROY);
		this.mLifeCycleOrderList.add(onPauseToOnResumeOrder);
		
		List<String> onPauseToOnResumeDoubleOrder = new ArrayList<String>();
		onPauseToOnResumeDoubleOrder.add(MethodSignature.INIT);
		this.addEntireLifeCycle(onPauseToOnResumeDoubleOrder);
		onPauseToOnResumeDoubleOrder.remove(onPauseToOnResumeDoubleOrder.size()-1);
		this.addForeGroundLifeCycle(onPauseToOnResumeDoubleOrder);
		this.addForeGroundLifeCycle(onPauseToOnResumeDoubleOrder);
		onPauseToOnResumeDoubleOrder.add(MethodSignature.ON_STOP);
		onPauseToOnResumeDoubleOrder.add(MethodSignature.ON_DESTROY);
		this.mLifeCycleOrderList.add(onPauseToOnResumeDoubleOrder);
		
		List<String> onRestartPlusOnPauseToOnResumeOrder = new ArrayList<String>();
		onRestartPlusOnPauseToOnResumeOrder.add(MethodSignature.INIT);
		this.addEntireLifeCycle(onRestartPlusOnPauseToOnResumeOrder);
		onRestartPlusOnPauseToOnResumeOrder.add(MethodSignature.ON_RESTART);
		this.addVisibleLifeCycle(onRestartPlusOnPauseToOnResumeOrder);
		onRestartPlusOnPauseToOnResumeOrder.remove(onRestartPlusOnPauseToOnResumeOrder.size()-1);
		this.addForeGroundLifeCycle(onRestartPlusOnPauseToOnResumeOrder);
		onRestartPlusOnPauseToOnResumeOrder.add(MethodSignature.ON_STOP);
		onRestartPlusOnPauseToOnResumeOrder.add(MethodSignature.ON_DESTROY);
		this.mLifeCycleOrderList.add(onRestartPlusOnPauseToOnResumeOrder);
		
		List<String> onPauseToOnResumePlusOnRestartOrder = new ArrayList<String>();
		onPauseToOnResumePlusOnRestartOrder.add(MethodSignature.INIT);
		this.addEntireLifeCycle(onPauseToOnResumePlusOnRestartOrder);
		onPauseToOnResumePlusOnRestartOrder.remove(onPauseToOnResumePlusOnRestartOrder.size()-1);
		this.addForeGroundLifeCycle(onPauseToOnResumePlusOnRestartOrder);
		onPauseToOnResumePlusOnRestartOrder.add(MethodSignature.ON_STOP);
		onPauseToOnResumePlusOnRestartOrder.add(MethodSignature.ON_RESTART);
		this.addVisibleLifeCycle(onPauseToOnResumePlusOnRestartOrder);
		onPauseToOnResumePlusOnRestartOrder.add(MethodSignature.ON_DESTROY);
		this.mLifeCycleOrderList.add(onPauseToOnResumePlusOnRestartOrder);
	}
	
	@SuppressWarnings("unused")
	private void printOrder(List<String> theOrder){
		for(String currentMethod: theOrder)
			Log.i(currentMethod);
	}
	
	/**
	 * Entire life cycle is from onCreate to onStop
	 * @param orderString
	 */
	private void addEntireLifeCycle(List<String> orderString){
		orderString.add(MethodSignature.ON_CREATE);
		orderString.add(MethodSignature.ON_START);
		orderString.add(MethodSignature.ON_RESUME);
		orderString.add(MethodSignature.ON_PAUSE);
		orderString.add(MethodSignature.ON_STOP);
	}
	
	/**
	 * Visisble life cycle is from onStart to onStop
	 * @param orderString
	 */
	private void addVisibleLifeCycle(List<String> orderString){
		orderString.add(MethodSignature.ON_START);
		orderString.add(MethodSignature.ON_RESUME);
		orderString.add(MethodSignature.ON_PAUSE);
		orderString.add(MethodSignature.ON_STOP);
	}
	
	/**
	 * Fore ground life cycle is from onResume to onPause
	 * @param orderString
	 */
	private void addForeGroundLifeCycle(List<String> orderString){
		orderString.add(MethodSignature.ON_RESUME);
		orderString.add(MethodSignature.ON_PAUSE);
	}
}
