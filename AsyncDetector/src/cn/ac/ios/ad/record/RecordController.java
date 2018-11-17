package cn.ac.ios.ad.record;

import cn.ac.ios.ad.constant.Configuration;
import cn.ac.ios.ad.util.Log;

public class RecordController {

	private String filePath = null;

	private static RecordController sInstance = null;

	public String mApkName = null;

	private AsyncTaskFieldDetectedErrorOutput mAsyncTaskFieldDetectedErrorOutput = null;

	private TimeOutput mTimeOutput = null;

	private AsyncTaskMethodOutput mAsyncTaskMethodOutput = null;

	private AsyncTaskActivityLifeCycleMethodOutput mAsyncTaskActivityLifeCycleMethodOutput = null;

	private AsyncTaskListenerOutput mAsyncTaskListenerOutput = null;
	
	private LifeCycleMethodLoopOutput mLifeCycleMethodLoopOutput = null;
	
	private AsyncTaskFieldOutput mAsyncTaskFieldOutput = null;

	private RecordController() {
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

	public void newSheetOutput() {
		mAsyncTaskFieldDetectedErrorOutput = new AsyncTaskFieldDetectedErrorOutput(
				mApkName);
		mTimeOutput = new TimeOutput(mApkName);
		mAsyncTaskListenerOutput = new AsyncTaskListenerOutput(mApkName);
		mAsyncTaskActivityLifeCycleMethodOutput = new AsyncTaskActivityLifeCycleMethodOutput(
				mApkName);
		mAsyncTaskMethodOutput = new AsyncTaskMethodOutput(mApkName);
		mLifeCycleMethodLoopOutput = new LifeCycleMethodLoopOutput(mApkName);
		mAsyncTaskFieldOutput = new AsyncTaskFieldOutput(mApkName);
	}

	public void output() {
		filePath = Configuration.XLS_OUTPUT_FOLDER
				+ Configuration.XML_RESULT_FILE_NAME;
//		File file = new File(filePath);
//		if (!file.exists()) {
//			try {
//				file.createNewFile();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		mAsyncTaskFieldDetectedErrorOutput.output(filePath);
		mTimeOutput.output(filePath);
		//mAsyncTaskMethodOutput.output(filePath);
//		mAsyncTaskActivityLifeCycleMethodOutput.output(filePath);
//		mAsyncTaskListenerOutput.output(filePath);
//		mLifeCycleMethodLoopOutput.output(filePath);
//		mAsyncTaskFieldOutput.output(filePath);
		Log.e("-----------record is finished --------  ");
	}
	
	public void onExit(){
		
	}
}
