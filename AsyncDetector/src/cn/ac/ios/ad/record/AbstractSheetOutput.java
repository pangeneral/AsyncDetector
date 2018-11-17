package cn.ac.ios.ad.record;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import cn.ac.ios.ad.constant.Configuration;

public abstract class AbstractSheetOutput {

	protected String mApkName = null;

	public AbstractSheetOutput(String apkName ) {
		mApkName = apkName;
	}

	public final void output(String  filePath) {
		File file = new File(Configuration.XLS_OUTPUT_FOLDER+getRecordFilePath()+".txt");
		try {
			StringBuffer sb = new StringBuffer();
			if(!file.exists()){
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
