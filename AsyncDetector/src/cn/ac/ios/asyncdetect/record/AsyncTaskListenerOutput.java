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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.ac.ios.asyncdetect.summary.alphabet.activity.ActivityOperationUnitSummary;
import soot.SootField;

/**
 * 
 * @author Baoquan Cui
 * @version 1.0
 */
public class AsyncTaskListenerOutput extends AbstractSheetOutput {

	private Map<String, List<List<ActivityOperationUnitSummary>>> mAsyncTaskListenerMap = new HashMap<>();

	private Map<String, List<List<ActivityOperationUnitSummary>>> mAsyncTaskListenerPurifiedMap = new HashMap<>();

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
