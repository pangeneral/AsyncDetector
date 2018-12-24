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

package cn.ac.ios.asyncdetect.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.SootField;
import cn.ac.ios.asyncdetect.constant.Configuration;
import cn.ac.ios.asyncdetect.record.RecordController;
import cn.ac.ios.asyncdetect.summary.DoInBackgroundMethodSummary;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.ActivityOperationUnitSummary;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.AssignAsyncTaskInstanceUnitSummary;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.CancelAsyncTaskUnitSummary;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.StartAsyncTaskUnitSummary;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.SummaryAlphabet;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.TaintInstance;
import cn.ac.ios.asyncdetect.summary.topology.DoInBackgroundTopologyOperation;
import cn.ac.ios.asyncdetect.summary.topology.TopologyOperation;

/**
 * Detect whether AsyncTask misuse exists in value flow via an automaton
 * @author Linjie Pan
 * @version 1.0
 */
public class ActivityAsyncOperationChecker {	
	
	enum Status{
		Null,Pending,Running,Wrong
	}
	
	private static Map<Status,Map<String,Status>> sTransitionMatrix;
	
	private static void initTransitionMatrix(){ 
		sTransitionMatrix = new HashMap<Status,Map<String,Status>>();
		
		Map<String,Status> nullTransition = new HashMap<String,Status>();
		nullTransition.put(SummaryAlphabet.ASSIGN_ASYNC_INSTANCE, Status.Pending);
		nullTransition.put(SummaryAlphabet.ASSIGN_NULL_ASYNC, Status.Null);
		nullTransition.put(SummaryAlphabet.START_ASYNC, Status.Wrong);
		nullTransition.put(SummaryAlphabet.CANCEL_ASYNC, Status.Wrong);
		sTransitionMatrix.put(Status.Null, nullTransition);
		
		Map<String,Status> pendingTransition = new HashMap<String,Status>();
		pendingTransition.put(SummaryAlphabet.ASSIGN_ASYNC_INSTANCE, Status.Pending);
		pendingTransition.put(SummaryAlphabet.ASSIGN_NULL_ASYNC, Status.Null);
		pendingTransition.put(SummaryAlphabet.START_ASYNC, Status.Running);
		pendingTransition.put(SummaryAlphabet.CANCEL_ASYNC, Status.Wrong);
		sTransitionMatrix.put(Status.Pending, pendingTransition);
		
		Map<String,Status> runningTransition = new HashMap<String,Status>();
		runningTransition.put(SummaryAlphabet.ASSIGN_ASYNC_INSTANCE, Status.Pending);
		runningTransition.put(SummaryAlphabet.ASSIGN_NULL_ASYNC, Status.Null);
		runningTransition.put(SummaryAlphabet.START_ASYNC, Status.Wrong);
		runningTransition.put(SummaryAlphabet.CANCEL_ASYNC, Status.Running);
		sTransitionMatrix.put(Status.Running, runningTransition);
	}
	
	/**
	 * This method detect whether a value flow contains misuse of AsyncTask field under analysis 
	 * @param controlFlowSummary is the current value flow 
	 * @param asyncTaskField is the AsyncTask field under analysis
	 * @return
	 */
	public static boolean check(List<ActivityOperationUnitSummary> controlFlowSummary,SootField asyncTaskField){
		if( sTransitionMatrix == null)
			initTransitionMatrix();
		Status currentStatus = Status.Null;
		AssignAsyncTaskInstanceUnitSummary currentAssignSummary = null;
		boolean isRunning = false,isCancel = false,isRight=true;
		for(int i=0;i < controlFlowSummary.size(); i++){
			ActivityOperationUnitSummary unitSummary = controlFlowSummary.get(i);
			
			Status transitionStatus = sTransitionMatrix.get(currentStatus).get(unitSummary.getSummary());
			if( transitionStatus == Status.Null ){
				currentAssignSummary = null;
				currentStatus = transitionStatus;
				isCancel = false;
				isRunning = false;
			}
			else if( transitionStatus == Status.Pending){
				currentAssignSummary = (AssignAsyncTaskInstanceUnitSummary) unitSummary;//save the init method of AsyncTask
				currentStatus = transitionStatus;
				isCancel = false;
				isRunning = false;
			}
			else if( transitionStatus == Status.Running ){
				if( unitSummary instanceof CancelAsyncTaskUnitSummary )
					isCancel = true;
				else if( unitSummary instanceof StartAsyncTaskUnitSummary){
					isRunning = true;
					List<TaintInstance> initTaintInstances = currentAssignSummary.getTaintInstances();
					List<TaintInstance> executeTaintInstances = ((StartAsyncTaskUnitSummary)unitSummary).getTaintInstances();
					if( initTaintInstances.size() > 0 || executeTaintInstances.size() > 0)		/*StrongReference*/
						recordHoldingStrongReference(controlFlowSummary.subList(0,i+1),asyncTaskField,initTaintInstances,executeTaintInstances,
							currentAssignSummary,(StartAsyncTaskUnitSummary)unitSummary);
					
					String key = DoInBackgroundTopologyOperation.getDoInBackgroundKey(currentAssignSummary.getDoInBackgroundMethod());
					DoInBackgroundMethodSummary doInBackgroundSummary = (DoInBackgroundMethodSummary) TopologyOperation.getsMethodKeyToSummary().get(key);  
					if(doInBackgroundSummary!=null && !doInBackgroundSummary.isAllLoopCancelled())		/*NotTerminate*/
						recordNotTerminateAsyncTask(controlFlowSummary.subList(0, i+1),asyncTaskField);
				}
				currentStatus = transitionStatus;
			}
			else{
				if( currentStatus == Status.Null ){		/*NullReference*/		
					recordNullPointerReference(controlFlowSummary.subList(0, i+1),asyncTaskField);
					isRight = false;
					break;
				}
				else if( currentStatus == Status.Running ){		/*RepeatStart*/
					recordRestartAsyncTask(controlFlowSummary.subList(0, i+1),asyncTaskField);
					isRight = false;
					break;
				}
				else if( currentStatus == Status.Pending ){		/*EarlyCancel*/
					recordEarlyCancelAsyncTask(controlFlowSummary.subList(0, i+1),asyncTaskField);
					isRight = false;
					break;
				}
			}
		}
		if( isRunning && !isCancel && isRight )		/*NotCancel*/
			recordNotCancelAsyncTask(controlFlowSummary,asyncTaskField);
		return false;
	}
	
	private static String getErrorBasePath(){
		return Configuration.getJimpleFolder()+File.separator+Configuration.ERROR_FOLDER;
	}
	
	private static void recordExecutionPath(FileWriter writer,List<ActivityOperationUnitSummary> controlFlowSummary) throws IOException{
		for(int i=0;i < controlFlowSummary.size(); i++){
			writer.write(i+1+" "+controlFlowSummary.get(i).getSummary()+" "+controlFlowSummary.get(i).getCurrentMethod().getSignature()+" "+controlFlowSummary.get(i).getUnit().toString()+"\n");
		}		
	}
	
	private static void recordNotTerminateAsyncTask(List<ActivityOperationUnitSummary> controlFlowSummary,SootField asyncTaskField){
		RecordController.getInstance().getAsyncTaskFieldDetectedErrorOutput().addAsyncTaskRecordNotTerminate(asyncTaskField);
		try {
			FileWriter sumWriter = new FileWriter(getErrorBasePath()+File.separator+"not-terminate-sum.txt",true);
			sumWriter.write(asyncTaskField.getSignature()+"\n");
			sumWriter.close();
			
			FileWriter writer=new FileWriter(getErrorBasePath()+File.separator+"not-terminate.txt",true);
			writer.write("NotTerminate "+asyncTaskField.getSignature()+"\n");
			recordExecutionPath(writer,controlFlowSummary);
			writer.write("\n");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void recordEarlyCancelAsyncTask(List<ActivityOperationUnitSummary> controlFlowSummary,SootField asyncTaskField){
		RecordController.getInstance().getAsyncTaskFieldDetectedErrorOutput().addAsyncTaskRecordEarlyCancel(asyncTaskField);
		try {
			FileWriter sumWriter = new FileWriter(getErrorBasePath()+File.separator+"early-cancel-sum.txt",true);
			sumWriter.write(asyncTaskField.getSignature()+"\n");
			sumWriter.close();
			
			FileWriter writer=new FileWriter(getErrorBasePath()+File.separator+"early-cancel.txt",true);
			writer.write("EarlyCancel "+asyncTaskField.getSignature()+"\n");
			recordExecutionPath(writer,controlFlowSummary);
			writer.write("\n");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void recordNotCancelAsyncTask(List<ActivityOperationUnitSummary> controlFlowSummary,SootField asyncTaskField){
		RecordController.getInstance().getAsyncTaskFieldDetectedErrorOutput().addAsyncTaskRecordNotCancel(asyncTaskField);
		try {
			FileWriter sumWriter = new FileWriter(getErrorBasePath()+File.separator+"not-cancel-async-tast-sum.txt",true);
			sumWriter.write(asyncTaskField.getSignature()+"\n");
			sumWriter.close();
			
			
			FileWriter writer=new FileWriter(getErrorBasePath()+File.separator+"not-cancel-async-task.txt",true);
			writer.write("Not cancel "+asyncTaskField.getSignature()+"\n");
			recordExecutionPath(writer,controlFlowSummary);
			writer.write("\n");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void recordHoldingStrongReference(List<ActivityOperationUnitSummary> controlFlowSummary,SootField asyncTaskField,List<TaintInstance> initTaintInstances,
		List<TaintInstance> executeTaintInstances,AssignAsyncTaskInstanceUnitSummary assignSummary,StartAsyncTaskUnitSummary startSummary){
		RecordController.getInstance().getAsyncTaskFieldDetectedErrorOutput().addAsyncTaskRecordStrongReference(asyncTaskField);
		try {
			FileWriter sumWriter = new FileWriter(getErrorBasePath()+File.separator+"holding-strong-reference-sum.txt",true);
			sumWriter.write(asyncTaskField.getSignature()+"\n");
			sumWriter.close();
			
			FileWriter writer = new FileWriter(getErrorBasePath()+File.separator+"holding-strong-reference.txt",true);
			writer.write(asyncTaskField.getSignature()+" holding strong reference to Activity "+asyncTaskField.getDeclaringClass().getName()+"\n");
			writer.write("Execution path:\n");
			recordExecutionPath(writer,controlFlowSummary);
			if( initTaintInstances.size() > 0 ){
				writer.write("Holding reference through Init method "+assignSummary.getInitExpr().toString()+"\n");
				for(TaintInstance currentInstance: initTaintInstances){
					if( currentInstance.getSource() != null)
						writer.write("source: "+currentInstance.getSource().toString()+"	sink: "+currentInstance.getTaintedField().getSignature()+"\n");
					else
						writer.write("sink: "+currentInstance.getTaintedField().getSignature()+"\n");
				}
			}
			if( executeTaintInstances.size() > 0 ){
				writer.write("Holding reference through execute method\n");
				for(TaintInstance currentInstance: executeTaintInstances){
					writer.write("execute method is: "+startSummary.getUnit()+"\n");
					if( currentInstance.getSource() != null)
						writer.write("source: "+currentInstance.getSource().toString()+"	sink: "+currentInstance.getArgIndex()+"\n");
					else
						writer.write("sink: "+currentInstance.getArgIndex()+"\n");
				}
					
			}
			writer.write("\n");
			writer.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void recordNullPointerReference(List<ActivityOperationUnitSummary> controlFlowSummary,SootField asyncTaskField){
		RecordController.getInstance().getAsyncTaskFieldDetectedErrorOutput().addAsyncTaskRecordNullPoint(asyncTaskField);
		try {
			FileWriter sumWriter = new FileWriter(getErrorBasePath()+File.separator+"null-pointer-reference-sum.txt",true);
			sumWriter.write(asyncTaskField.getSignature()+"\n");
			sumWriter.close();
			
			FileWriter writer=new FileWriter(getErrorBasePath()+File.separator+"null-pointer-reference.txt",true);
			writer.write("Null pointer reference of "+asyncTaskField.getSignature()+"\n");
			recordExecutionPath(writer,controlFlowSummary);
			writer.write("\n");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void recordRestartAsyncTask(List<ActivityOperationUnitSummary> controlFlowSummary,SootField asyncTaskField){
		RecordController.getInstance().getAsyncTaskFieldDetectedErrorOutput().addAsyncTaskRecordRepeatStart(asyncTaskField);
		try {
			FileWriter sumWriter = new FileWriter(getErrorBasePath()+File.separator+"restart-async-task-sum.txt",true);
			sumWriter.write(asyncTaskField.getSignature()+"\n");
			sumWriter.close();
			
			FileWriter writer = new FileWriter(getErrorBasePath()+File.separator+"restart-async-task.txt",true);
			writer.write(asyncTaskField.getSignature()+" has already been executed\n");
			recordExecutionPath(writer,controlFlowSummary);
			writer.write("\n");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
