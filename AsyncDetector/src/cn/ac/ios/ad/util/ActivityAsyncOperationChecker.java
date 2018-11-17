package cn.ac.ios.ad.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.SootField;
import cn.ac.ios.ad.constant.Configuration;
import cn.ac.ios.ad.record.RecordController;
import cn.ac.ios.ad.summary.DoInBackgroundMethodSummary;
import cn.ac.ios.ad.summary.alphabet.activity.ActivityOperationUnitSummary;
import cn.ac.ios.ad.summary.alphabet.activity.AssignAsyncTaskInstanceUnitSummary;
import cn.ac.ios.ad.summary.alphabet.activity.CancelAsyncTaskUnitSummary;
import cn.ac.ios.ad.summary.alphabet.activity.StartAsyncTaskUnitSummary;
import cn.ac.ios.ad.summary.alphabet.activity.SummaryAlphabet;
import cn.ac.ios.ad.summary.alphabet.activity.TaintInstance;
import cn.ac.ios.ad.summary.topology.DoInBackgroundTopologyOperation;
import cn.ac.ios.ad.summary.topology.TopologyOperation;

public class ActivityAsyncOperationChecker {	
	
	enum Status{
		Null,Pending,Running,Wrong
	}
	
	private static Map<Status,Map<String,Status>> transitionMatrix;
	
	private static void initTransitionMatrix(){ 
		transitionMatrix = new HashMap<Status,Map<String,Status>>();
		
		Map<String,Status> nullTransition = new HashMap<String,Status>();
		nullTransition.put(SummaryAlphabet.ASSIGN_ASYNC_INSTANCE, Status.Pending);
		nullTransition.put(SummaryAlphabet.ASSIGN_NULL_ASYNC, Status.Null);
		nullTransition.put(SummaryAlphabet.START_ASYNC, Status.Wrong);
		nullTransition.put(SummaryAlphabet.CANCEL_ASYNC, Status.Wrong);
		transitionMatrix.put(Status.Null, nullTransition);
		
		Map<String,Status> pendingTransition = new HashMap<String,Status>();
		pendingTransition.put(SummaryAlphabet.ASSIGN_ASYNC_INSTANCE, Status.Pending);
		pendingTransition.put(SummaryAlphabet.ASSIGN_NULL_ASYNC, Status.Null);
		pendingTransition.put(SummaryAlphabet.START_ASYNC, Status.Running);
		pendingTransition.put(SummaryAlphabet.CANCEL_ASYNC, Status.Wrong);
		transitionMatrix.put(Status.Pending, pendingTransition);
		
		Map<String,Status> runningTransition = new HashMap<String,Status>();
		runningTransition.put(SummaryAlphabet.ASSIGN_ASYNC_INSTANCE, Status.Pending);
		runningTransition.put(SummaryAlphabet.ASSIGN_NULL_ASYNC, Status.Null);
		runningTransition.put(SummaryAlphabet.START_ASYNC, Status.Wrong);
		runningTransition.put(SummaryAlphabet.CANCEL_ASYNC, Status.Running);
		transitionMatrix.put(Status.Running, runningTransition);
	}
	
	public static boolean check(List<ActivityOperationUnitSummary> controlFlowSummary,SootField asyncTaskField){
		if( transitionMatrix == null)
			initTransitionMatrix();
		Status currentStatus = Status.Null;
		AssignAsyncTaskInstanceUnitSummary currentAssignSummary = null;
		boolean isRunning = false,isCancel = false,isRight=true;
		for(int i=0;i < controlFlowSummary.size(); i++){
			ActivityOperationUnitSummary unitSummary = controlFlowSummary.get(i);
			
			Status transitionStatus = transitionMatrix.get(currentStatus).get(unitSummary.getSummary());
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
					if( initTaintInstances.size() > 0 || executeTaintInstances.size() > 0)
						recordHoldingStrongReference(controlFlowSummary.subList(0,i+1),asyncTaskField,initTaintInstances,executeTaintInstances,
							currentAssignSummary,(StartAsyncTaskUnitSummary)unitSummary);
					
					String key = DoInBackgroundTopologyOperation.getDoInBackgroundKey(currentAssignSummary.getDoInBackgroundMethod());
					DoInBackgroundMethodSummary doInBackgroundSummary = (DoInBackgroundMethodSummary) TopologyOperation.methodKeyToSummary.get(key);  
					if(doInBackgroundSummary!=null && !doInBackgroundSummary.isAllLoopCancelled())
						recordNotTerminateAsyncTask(controlFlowSummary.subList(0, i+1),asyncTaskField);
				}
				currentStatus = transitionStatus;
			}
			else{
				if( currentStatus == Status.Null ){
					recordNullPointerReference(controlFlowSummary.subList(0, i+1),asyncTaskField);
					isRight = false;
					break;
				}
				else if( currentStatus == Status.Running ){
					recordRestartAsyncTask(controlFlowSummary.subList(0, i+1),asyncTaskField);
					isRight = false;
					break;
				}
				else if( currentStatus == Status.Pending ){
					recordEarlyCancelAsyncTask(controlFlowSummary.subList(0, i+1),asyncTaskField);
					isRight = false;
					break;
				}
			}
		}
		if( isRunning && !isCancel && isRight )
			recordNotCancelAsyncTask(controlFlowSummary,asyncTaskField);
		return false;
	}
	
	private static String getErrorBasePath(){
		return Configuration.JimpleBasePath+File.separator+Configuration.CurrentApkName.substring(0, Configuration.CurrentApkName.indexOf("."))
				+File.separator+Configuration.ErrorFolder;
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
