package cn.ac.ios.ad.lifecycle;

import java.util.ArrayList;
import java.util.List;

import cn.ac.ios.ad.constant.MethodSignature;

public class LifeCycleConstruction {
	private List<List<String>> lifeCycleOrderList;
	
	public List<List<String>> getLifeCycleOrderList() {
		return lifeCycleOrderList;
	}

	public void setLifeCycleOrderList(List<List<String>> lifeCycleOrderList) {
		this.lifeCycleOrderList = lifeCycleOrderList;
	}

	public LifeCycleConstruction(){
		this.initLifeCycleOrderList();
	}
	
	public void initLifeCycleOrderList(){
		this.lifeCycleOrderList = new ArrayList<List<String>>();
		
		List<String> onePassOrder = new ArrayList<String>();
		onePassOrder.add(MethodSignature.INIT);
		this.addEntireLifeCycle(onePassOrder);
		onePassOrder.add(MethodSignature.ON_DESTROY);
		this.lifeCycleOrderList.add(onePassOrder);
		
		List<String> onRestartOrder = new ArrayList<String>();
		onRestartOrder.add(MethodSignature.INIT);
		this.addEntireLifeCycle(onRestartOrder);
		onRestartOrder.add(MethodSignature.ON_RESTART);
		this.addVisibleLifeCycle(onRestartOrder);
		onRestartOrder.add(MethodSignature.ON_DESTROY);
		this.lifeCycleOrderList.add(onRestartOrder);
		
		List<String> onRestartDoubleOrder = new ArrayList<String>();
		onRestartDoubleOrder.add(MethodSignature.INIT);
		this.addEntireLifeCycle(onRestartDoubleOrder);
		onRestartDoubleOrder.add(MethodSignature.ON_RESTART);
		this.addVisibleLifeCycle(onRestartDoubleOrder);
		onRestartDoubleOrder.add(MethodSignature.ON_RESTART);
		this.addVisibleLifeCycle(onRestartDoubleOrder);
		onRestartDoubleOrder.add(MethodSignature.ON_DESTROY);
		this.lifeCycleOrderList.add(onRestartDoubleOrder);
		
		List<String> onPauseToOnResumeOrder = new ArrayList<String>();
		onPauseToOnResumeOrder.add(MethodSignature.INIT);
		this.addEntireLifeCycle(onPauseToOnResumeOrder);
		onPauseToOnResumeOrder.remove(onPauseToOnResumeOrder.size()-1);
		this.addForeGroundLifeCycle(onPauseToOnResumeOrder);
		onPauseToOnResumeOrder.add(MethodSignature.ON_STOP);
		onPauseToOnResumeOrder.add(MethodSignature.ON_DESTROY);
		this.lifeCycleOrderList.add(onPauseToOnResumeOrder);
		
		List<String> onPauseToOnResumeDoubleOrder = new ArrayList<String>();
		onPauseToOnResumeDoubleOrder.add(MethodSignature.INIT);
		this.addEntireLifeCycle(onPauseToOnResumeDoubleOrder);
		onPauseToOnResumeDoubleOrder.remove(onPauseToOnResumeDoubleOrder.size()-1);
		this.addForeGroundLifeCycle(onPauseToOnResumeDoubleOrder);
		this.addForeGroundLifeCycle(onPauseToOnResumeDoubleOrder);
		onPauseToOnResumeDoubleOrder.add(MethodSignature.ON_STOP);
		onPauseToOnResumeDoubleOrder.add(MethodSignature.ON_DESTROY);
		this.lifeCycleOrderList.add(onPauseToOnResumeDoubleOrder);
		
		List<String> onRestartPlusOnPauseToOnResumeOrder = new ArrayList<String>();
		onRestartPlusOnPauseToOnResumeOrder.add(MethodSignature.INIT);
		this.addEntireLifeCycle(onRestartPlusOnPauseToOnResumeOrder);
		onRestartPlusOnPauseToOnResumeOrder.add(MethodSignature.ON_RESTART);
		this.addVisibleLifeCycle(onRestartPlusOnPauseToOnResumeOrder);
		onRestartPlusOnPauseToOnResumeOrder.remove(onRestartPlusOnPauseToOnResumeOrder.size()-1);
		this.addForeGroundLifeCycle(onRestartPlusOnPauseToOnResumeOrder);
		onRestartPlusOnPauseToOnResumeOrder.add(MethodSignature.ON_STOP);
		onRestartPlusOnPauseToOnResumeOrder.add(MethodSignature.ON_DESTROY);
		this.lifeCycleOrderList.add(onRestartPlusOnPauseToOnResumeOrder);
		
		List<String> onPauseToOnResumePlusOnRestartOrder = new ArrayList<String>();
		onPauseToOnResumePlusOnRestartOrder.add(MethodSignature.INIT);
		this.addEntireLifeCycle(onPauseToOnResumePlusOnRestartOrder);
		onPauseToOnResumePlusOnRestartOrder.remove(onPauseToOnResumePlusOnRestartOrder.size()-1);
		this.addForeGroundLifeCycle(onPauseToOnResumePlusOnRestartOrder);
		onPauseToOnResumePlusOnRestartOrder.add(MethodSignature.ON_STOP);
		onPauseToOnResumePlusOnRestartOrder.add(MethodSignature.ON_RESTART);
		this.addVisibleLifeCycle(onPauseToOnResumePlusOnRestartOrder);
		onPauseToOnResumePlusOnRestartOrder.add(MethodSignature.ON_DESTROY);
		this.lifeCycleOrderList.add(onPauseToOnResumePlusOnRestartOrder);
		
//		List<String> onPauseToOnCreateOrder = new ArrayList<String>();
//		onPauseToOnCreateOrder.add(MethodSignature.INIT);
//		this.addEntireLifeCycle(onPauseToOnCreateOrder);
//		onPauseToOnCreateOrder.remove(onPauseToOnCreateOrder.size()-1);
//		this.addEntireLifeCycle(onPauseToOnCreateOrder);
//		onPauseToOnCreateOrder.add(MethodSignature.ON_DESTROY);
//		this.lifeCycleOrderList.add(onPauseToOnCreateOrder);
//		
//		List<String> onStopToOnCreateOrder = new ArrayList<String>();
//		onStopToOnCreateOrder.add(MethodSignature.INIT);
//		this.addEntireLifeCycle(onStopToOnCreateOrder);
//		this.addEntireLifeCycle(onStopToOnCreateOrder);
//		onStopToOnCreateOrder.add(MethodSignature.ON_DESTROY);
//		this.lifeCycleOrderList.add(onStopToOnCreateOrder);
	}
	
	private void printOrder(List<String> theOrder){
		for(String currentMethod: theOrder)
			System.out.println(currentMethod);
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
