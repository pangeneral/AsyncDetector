package cn.ac.ios.ad.record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.ac.ios.ad.constant.MethodSignature;
import cn.ac.ios.ad.summary.alphabet.activity.ActivityOperationUnitSummary;
import soot.SootField;

public class AsyncTaskActivityLifeCycleMethodOutput extends AbstractSheetOutput {

	private Map<SootField, Map<String, List<List<ActivityOperationUnitSummary>>>> mAsyncTaskLifeCycleMethodPurifiedMap = null;

	private Map<SootField, Map<String, List<List<ActivityOperationUnitSummary>>>> mAsyncTaskLifeCycleMethodMap2 = null;

	public AsyncTaskActivityLifeCycleMethodOutput(String apkName) {
		super(apkName);
		mAsyncTaskLifeCycleMethodMap2 = new HashMap<>();
		mAsyncTaskLifeCycleMethodPurifiedMap = new HashMap<>();
	}

	public void add(SootField sootField, String signature,
			List<List<ActivityOperationUnitSummary>> summaryList) {
		Map<String, List<List<ActivityOperationUnitSummary>>> map = null;
		if (mAsyncTaskLifeCycleMethodMap2.containsKey(sootField)) {
			map = mAsyncTaskLifeCycleMethodMap2.get(sootField);
		} else {
			map = new HashMap<>();
			mAsyncTaskLifeCycleMethodMap2.put(sootField, map);
		}
		map.put(signature, summaryList);
	}

	public void addPurified(SootField sootField, String signature,
			List<List<ActivityOperationUnitSummary>> purifiedSummaryList) {
		Map<String, List<List<ActivityOperationUnitSummary>>> map = null;
		if (mAsyncTaskLifeCycleMethodPurifiedMap.containsKey(sootField)) {
			map = mAsyncTaskLifeCycleMethodPurifiedMap.get(sootField);
		} else {
			map = new HashMap<>();
			mAsyncTaskLifeCycleMethodPurifiedMap.put(sootField, map);
		}
		map.put(signature, purifiedSummaryList);
	}
	
	@Override
	protected String getApkContent(){
		StringBuffer sb = new StringBuffer();
		for (int row = 0; row < mAsyncTaskLifeCycleMethodMap2.size(); row++) {
			Entry<SootField, Map<String, List<List<ActivityOperationUnitSummary>>>> entry = mAsyncTaskLifeCycleMethodMap2
					.entrySet().iterator().next();	
			sb.append(entry.getKey().getSignature());
			sb.append(";");
			appendOperationUnitSummaryList(sb, entry);
			entry = mAsyncTaskLifeCycleMethodPurifiedMap
					.entrySet().iterator().next();
			appendOperationUnitSummaryList(sb, entry);
			sb.append(mApkName);
			sb.append("\r\n");		
		}
		return sb.toString();
	}
	
	private void appendOperationUnitSummaryList(StringBuffer sb,Entry<SootField, Map<String, List<List<ActivityOperationUnitSummary>>>> entry){
		List<List<List<ActivityOperationUnitSummary>>> linearList = new ArrayList<>();
		for (String signature : MethodSignature.signatureArray) {
			if (entry.getValue().containsKey(signature)) {
				linearList.add(entry.getValue().get(signature));
			} else {
				linearList.add(null);
			}
		}
		for (List<List<ActivityOperationUnitSummary>> lists : linearList) {
			sb.append(lists == null || lists.isEmpty()?"N/A":lists.size());
			sb.append(";");
			if (lists != null &&! lists.isEmpty()){
				for (List<ActivityOperationUnitSummary> list : lists) {
					sb.append(list.size());
					sb.append(",");
				}
			}
			sb.append(";");
		}
	}

	

	@Override
	protected String getRecordFilePath() {
		return "activity";
	}
	
	@Override
	protected String getHeaders() {

		StringBuffer sb = new StringBuffer();

		sb.append("AsyncTaskField;");
		for (String string : MethodSignature.signatureArray) {
			sb.append(string);
			sb.append(";");
			sb.append(string);
			sb.append("-sub");
			sb.append(";");
		}

		for (String string : MethodSignature.signatureArray) {
			sb.append(string);
			sb.append("-purify");
			sb.append(";");
			sb.append(string);
			sb.append("-purify-sub");
			sb.append(";");
		}
		sb.append("ApkName\r\n");
		return sb.toString();
	}

}
