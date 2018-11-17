package cn.ac.ios.ad.record;

public class TimeOutput extends AbstractSheetOutput {

	public long sootTime = 0l;

	public long detecorTime = 0l;

	public TimeOutput(String apkName) {
		super(apkName);
	}

	@Override
	protected String getRecordFilePath() {
		return "time";
	}

	@Override
	protected String getApkContent() {
		StringBuffer sb = new StringBuffer();
		sb.append(mApkName);
		sb.append(";");
		sb.append(sootTime);
		sb.append(";");
		sb.append(detecorTime);
		sb.append("\r\n");
		return sb.toString();
	}

	@Override
	protected String getHeaders() {
		StringBuffer sb = new StringBuffer();
		sb.append("ApkName;");
		sb.append("SootTime;");
		sb.append("DetectorTime\r\n");
		return sb.toString();
	}

}
