package cn.ac.ios.ad.record;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.ac.ios.ad.summary.alphabet.activity.ActivityOperationUnitSummary;
import soot.SootField;

public class AsyncTaskListenerOutput extends AbstractSheetOutput {

	private Map<String, List<List<ActivityOperationUnitSummary>>> mAsyncTaskListenerMap = null;

	private Map<String, List<List<ActivityOperationUnitSummary>>> mAsyncTaskListenerPurifiedMap = null;

	public AsyncTaskListenerOutput(String apkName) {
		super(apkName);
		mAsyncTaskListenerMap = new HashMap<>();
		mAsyncTaskListenerPurifiedMap = new HashMap<>();
	}

	public void add(SootField sootField, String methodSignature,
			List<List<ActivityOperationUnitSummary>> list) {
		mAsyncTaskListenerMap.put(sootField.getSignature() + methodSignature,
				list);
	}

	public void addPurified(SootField sootField, String methodSignature,
			List<List<ActivityOperationUnitSummary>> list) {
		mAsyncTaskListenerPurifiedMap.put(sootField.getSignature()
				+ methodSignature, list);
	}
	
	@Override
	protected String getRecordFilePath() {
		return "listener";
	}

	@Override
	protected String getApkContent() {
		StringBuffer sb = new StringBuffer();
		Iterator<Entry<String, List<List<ActivityOperationUnitSummary>>>> iterator = mAsyncTaskListenerMap.entrySet().iterator();
		Iterator<Entry<String, List<List<ActivityOperationUnitSummary>>>> iterator2 = mAsyncTaskListenerPurifiedMap.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, List<List<ActivityOperationUnitSummary>>> entry = iterator.next();
			int size = 0;
			sb.append(entry.getKey());
			sb.append(";");
			if (entry.getValue() == null || entry.getValue().isEmpty()) {
//				sb.append("0");
			} else {
				size = entry.getValue().size();
			}
			sb.append(size);
			sb.append(";");
			if(size>0){
				for (List<ActivityOperationUnitSummary> list : entry.getValue()) {
					sb.append(list.size());
					sb.append(",");
				}
			}else{
				sb.append("N/A");
			}
			sb.append(";");
			entry = iterator2.next();
			size = 0;
			if (entry.getValue() == null || entry.getValue().isEmpty()) {
//				sb.append("0");
			} else {
				size = entry.getValue().size();
			}
			sb.append(size);
			sb.append(";");
			if(size>0){
				for (List<ActivityOperationUnitSummary> list : entry.getValue()) {
					sb.append(list.size());
					sb.append(",");
				}
			}else{
				sb.append("N/A");
			}
			sb.append(";");
			sb.append(mApkName);
			sb.append("\r\n");
		}
		return sb.toString();
	}

	@Override
	protected String getHeaders() {
		StringBuffer sb = new StringBuffer();
		sb.append("AsyncTaskClass+Listener;");
		sb.append("MapSize;");
		sb.append("MapSize-sub;");
		sb.append("MapSize-purified;");
		sb.append("MapSize-purified-sub;");
		sb.append("ApkName\r\n");
		return sb.toString();
	}

}
