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
import java.util.Map;
import java.util.Map.Entry;

import soot.SootClass;

/**
 * 
 * @author Baoquan Cui
 * @version 1.0
 */
public class AsyncTaskMethodOutput extends AbstractSheetOutput {
	
	private Map<SootClass, AsyncTaskInitDoInBackground> mAsyncTaskMethod = new HashMap<>();
	
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
