package cn.ac.ios.ad.record;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

import cn.ac.ios.ad.constant.Configuration;

public class LongTimeException {
	public static void saveException(String dataSet,String apkFilePath) {
		String filePath = Configuration.XLS_OUTPUT_FOLDER + dataSet
				+ Configuration.LONG_TIME_FILE_NAME;
		File file = new File(filePath);

		BufferedWriter out = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true)));// true,进行追加写。
			out.write(new Date(System.currentTimeMillis()).toString() + "\r\n");
			out.write(dataSet + "\r\n");
			out.write(apkFilePath + "\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
