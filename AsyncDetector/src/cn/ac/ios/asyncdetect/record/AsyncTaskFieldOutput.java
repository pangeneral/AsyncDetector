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

import java.util.HashSet;
import java.util.Set;

import soot.SootField;

/**
 * 
 * @author Baoquan Cui
 * @version 1.0
 */
public class AsyncTaskFieldOutput extends AbstractSheetOutput {

	private Set<SootField> mFields = new HashSet<>();

	public void add(SootField sootField) {
		mFields.add(sootField);
	} 

	@Override
	protected String getRecordFilePath() {
		return "asynTaskField";
	}

	@Override
	protected String getApkContent() {
		StringBuffer sb = new StringBuffer();
		sb.append(mApkName);
		sb.append(";");
		sb.append(mFields.size());
		sb.append("\r\n");
		return sb.toString();
	}

	@Override
	protected String getHeaders() {
		StringBuffer sb = new StringBuffer();
		sb.append("ApkName;");
		sb.append("asynTaskField#");
		sb.append("\r\n");
		return sb.toString();
	}

}
