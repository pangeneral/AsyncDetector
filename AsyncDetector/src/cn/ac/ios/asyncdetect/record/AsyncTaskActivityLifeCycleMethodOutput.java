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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.ac.ios.asyncdetect.constant.MethodSignature;
import cn.ac.ios.asyncdetect.summary.alphabet.activity.ActivityOperationUnitSummary;
import soot.SootField;

/**
 * 
 * @author Baoquan Cui
 * @version 1.0
 */
public class AsyncTaskActivityLifeCycleMethodOutput extends AbstractSheetOutput {

	private Map<SootField, Map<String, List<List<ActivityOperationUnitSummary>>>> mAsyncTaskLifeCycleMethodPurifiedMap = new HashMap<>();

	private Map<SootField, Map<String, List<List<ActivityOperationUnitSummary>>>> mAsyncTaskLifeCycleMethodMap2 = new HashMap<>();

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
