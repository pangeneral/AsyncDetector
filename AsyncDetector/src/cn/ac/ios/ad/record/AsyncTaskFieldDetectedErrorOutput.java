package cn.ac.ios.ad.record;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import soot.SootField;

public class AsyncTaskFieldDetectedErrorOutput extends AbstractSheetOutput {
	
	private Map<SootField, AsyncTaskRecord> sootFieldsMap = new HashMap<SootField, AsyncTaskRecord>();

	public AsyncTaskFieldDetectedErrorOutput(String apkName) {
		super(apkName);
	}

	private AsyncTaskRecord getAsyncTaskRecord(SootField sootField) {
		if (sootFieldsMap.containsKey(sootField)) {
			return sootFieldsMap.get(sootField);
		}
		AsyncTaskRecord record = new AsyncTaskRecord(sootField.getSignature());
		sootFieldsMap.put(sootField, record);
		return record;
	}

	public void addAsyncTaskRecordEarlyCancel(SootField sootField) {
		AsyncTaskRecord record = getAsyncTaskRecord(sootField);
		record.earlyCancelError = true;
	}

	public void addAsyncTaskRecordNotCancel(SootField sootField) {
		AsyncTaskRecord record = getAsyncTaskRecord(sootField);
		record.notCancelError = true;
	}

	public void addAsyncTaskRecordNotTerminate(SootField sootField) {
		AsyncTaskRecord record = getAsyncTaskRecord(sootField);
		record.notTerminateError = true;
	}

	public void addAsyncTaskRecordNullPoint(SootField sootField) {
		AsyncTaskRecord record = getAsyncTaskRecord(sootField);
		record.nullPointError = true;
	}

	public void addAsyncTaskRecordRepeatStart(SootField sootField) {
		AsyncTaskRecord record = getAsyncTaskRecord(sootField);
		record.repeatStartError = true;
	}

	public void addAsyncTaskRecordStrongReference(SootField sootField) {
		AsyncTaskRecord record = getAsyncTaskRecord(sootField);
		record.strongReferenceError = true;
	}
	
	@Override
	protected String getRecordFilePath() {
		return "error";
	}

	@Override
	protected String getApkContent() {

		StringBuffer sb = new StringBuffer();

		for (Entry<SootField, AsyncTaskRecord> entry : sootFieldsMap.entrySet()) {
			AsyncTaskRecord record = entry.getValue();

			sb.append(record.mAsyncTaskInstanceName);
			sb.append(";");
			sb.append(record.earlyCancelError ? "1" : "0");
			sb.append(";");
			sb.append(record.notCancelError ? "1" : "0");
			sb.append(";");
			sb.append(record.notTerminateError ? "1" : "0");
			sb.append(";");
			sb.append(record.nullPointError ? "1" : "0");
			sb.append(";");
			sb.append(record.repeatStartError ? "1" : "0");
			sb.append(";");
			sb.append(record.strongReferenceError ? "1" : "0");
			sb.append(";");
			sb.append(mApkName);
			sb.append("\r\n");
		}
		return sb.toString();

	}

	@Override
	protected String getHeaders() {
		StringBuffer sb = new StringBuffer();
		sb.append("AsyncTaskInstanceName;");
		sb.append("earlyCancelError;");
		sb.append("notCancelError;");
		sb.append("notTerminateError;");
		sb.append("nullPointError;");
		sb.append("repeatStartError;");
		sb.append("strongReferenceError;");
		sb.append("apkName\r\n");
		return sb.toString();
	}
	
	public static class AsyncTaskRecord {

		public String mAsyncTaskInstanceName = null;

		public boolean earlyCancelError = false;

		public boolean notCancelError = false;

		public boolean notTerminateError = false;

		public boolean nullPointError = false;

		public boolean repeatStartError = false;

		public boolean strongReferenceError = false;

		public AsyncTaskRecord(String asyncTaskInstanceName) {
			mAsyncTaskInstanceName = asyncTaskInstanceName;
		}
	}

}
