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
import java.util.List;

import soot.SootField;
import soot.SootMethod;

/**
 * 
 * @author Baoquan Cui
 * @version 1.0
 */
public class LifeCycleMethodLoopOutput extends AbstractSheetOutput {

	private List<SummaryInfo> summaryInfos = new ArrayList<>();

	public void add(SootField sootField, SootMethod sootMethod,
			boolean hasLoop, int size) {
		SummaryInfo info = new SummaryInfo();
		info.signature = sootField.getSignature() + sootMethod.getSignature();
		info.hasLoop = hasLoop;
		info.summarySize = size;
		summaryInfos.add(info);
	}


	class SummaryInfo {
		String signature = null;
		boolean hasLoop = false;
		int summarySize = 0;
	}

	@Override
	protected String getRecordFilePath() {
		return "LifeCycleLoop";
	}

	@Override
	protected String getApkContent() {
		StringBuffer sb = new StringBuffer();
		for (SummaryInfo info : summaryInfos) {
			sb.append(info.signature);
			sb.append(";");
			sb.append(info.hasLoop ? "1"
					: "0");
			sb.append(";");
			sb.append(info.summarySize);
			sb.append(";");
			sb.append(mApkName);
			sb.append("\r\n");
		}
		return sb.toString();
	}

	@Override
	protected String getHeaders() {
		StringBuffer sb = new StringBuffer();
		sb.append("signature;");
		sb.append("value;");
		sb.append("summarySize;");
		sb.append("ApkName\r\n");
		return sb.toString();
	}

}
