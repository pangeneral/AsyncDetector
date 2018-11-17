package cn.ac.ios.ad.record;

import java.util.ArrayList;
import java.util.List;

import soot.SootField;
import soot.SootMethod;

public class LifeCycleMethodLoopOutput extends AbstractSheetOutput {

	List<SummaryInfo> summaryInfos = null;

	public LifeCycleMethodLoopOutput(String apkName) {
		super(apkName);
		summaryInfos = new ArrayList<>();
	}

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
