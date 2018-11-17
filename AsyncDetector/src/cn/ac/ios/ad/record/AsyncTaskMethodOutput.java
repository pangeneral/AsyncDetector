package cn.ac.ios.ad.record;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import soot.SootClass;

public class AsyncTaskMethodOutput extends AbstractSheetOutput {
	
	private Map<SootClass, AsyncTaskInitDoInBackground> mAsyncTaskMethod = new HashMap<>();

	public AsyncTaskMethodOutput(String apkName) {
		super(apkName);
	}
	
	private AsyncTaskInitDoInBackground getAsyncTaskInitDoInBackground(
			SootClass sootClass) {
		if (mAsyncTaskMethod.containsKey(sootClass)) {
			return mAsyncTaskMethod.get(sootClass);
		}
		AsyncTaskInitDoInBackground record = new AsyncTaskInitDoInBackground();
		mAsyncTaskMethod.put(sootClass, record);
		return record;
	}

	public void addAsyncTaskInitMethodRecord(SootClass sootClass, int size) {
		AsyncTaskInitDoInBackground record = getAsyncTaskInitDoInBackground(sootClass);
		record.initMethodMapSize = size;
	}

	public void addAsyncTaskDoInBackgroundMethodRecord(SootClass sootClass,
			int size) {
		AsyncTaskInitDoInBackground record = getAsyncTaskInitDoInBackground(sootClass);
		record.doInBackgroundMethodMapSize = size;
	}

	
	public static class AsyncTaskInitDoInBackground {

		public int initMethodMapSize = 0;
		public int doInBackgroundMethodMapSize = 0;

	}

	@Override
	protected String getRecordFilePath() {
		return "asynctask";
	}

	@Override
	protected String getApkContent() {
		StringBuffer sb = new StringBuffer();
		for (Entry<SootClass, AsyncTaskInitDoInBackground> entry : mAsyncTaskMethod
				.entrySet()) {
			sb.append(entry.getKey().getName());
			sb.append(";");
			sb.append(entry.getValue().initMethodMapSize);
			sb.append(";");
			sb.append(entry.getValue().doInBackgroundMethodMapSize);
			sb.append(";");
			sb.append(mApkName);
			sb.append("\r\n");
		}
		return sb.toString();
	}

	@Override
	protected String getHeaders() {
		StringBuffer sb = new StringBuffer();
		sb.append("AsyncTaskClassName;");
		sb.append("InitMethodMapSize;");
		sb.append("DoInBackgroundMethodMapSize;");
		sb.append("ApkName\r\n");
		return sb.toString();
	}


}
