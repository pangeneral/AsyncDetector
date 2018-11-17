package cn.ac.ios.ad.record;

import java.util.HashSet;
import java.util.Set;

import soot.SootField;

public class AsyncTaskFieldOutput extends AbstractSheetOutput {

	private Set<SootField> mFields = null;

	public AsyncTaskFieldOutput(String apkName) {
		super(apkName);
		mFields = new HashSet<>();
	}

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
