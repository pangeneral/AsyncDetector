package cn.ac.ios.ad.record;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

import cn.ac.ios.ad.constant.Configuration;

public class ExceptionRecord {

	public static void saveException(Exception exception, String apkName) {
		String filePath = Configuration.XLS_OUTPUT_FOLDER
				+ Configuration.EXCEPTION_FILE_NAME;
		File file = new File(filePath);
		
		exception.printStackTrace();

		BufferedWriter out = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true)));// 。
			out.write(new Date(System.currentTimeMillis()).toString() + "\r\n");
			out.write(apkName + "\r\n");
			out.write(exception + "\r\n");
			for(StackTraceElement element: exception.getStackTrace()){
				out.write("	"+ element + "\r\n");
			}
			
			out.write( "------------------------------\r\n\n");
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
