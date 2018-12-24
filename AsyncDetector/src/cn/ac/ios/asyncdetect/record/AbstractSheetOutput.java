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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import cn.ac.ios.asyncdetect.constant.Configuration;

/**
 * 
 * @author Baoquan Cui
 * @version 1.0
 */
public abstract class AbstractSheetOutput {

	protected String mApkName = Configuration.getCurrentApkName();

	public final void output() {
		File file = new File(Configuration.OUTPUT_FOLDER + Configuration.getDataSet() + "_" + getRecordFilePath()
				+ ".txt");
		try {
			StringBuffer sb = new StringBuffer();
			if (!file.exists()) {
				file.createNewFile();
				sb.append(getHeaders());
			}
			sb.append(getApkContent());
			BufferedWriter out = null;
			try {
				out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(file, true)));// true,进行追加写。
				out.write(sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract String getRecordFilePath();

	protected abstract String getApkContent();

	protected abstract String getHeaders();

}
