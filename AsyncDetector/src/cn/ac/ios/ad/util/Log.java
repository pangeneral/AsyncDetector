package cn.ac.ios.ad.util;

public class Log {

	private static final boolean DEBUG = true;

	public static void i(Object... objects) {
		if (DEBUG) {
			StringBuffer sb = new StringBuffer();
			if (objects == null) {
				sb.append("null");
			} else {
				for (Object obj : objects) {
					sb.append(obj);
				}
			}
			System.out.println(sb.toString());
		}
	}

	public static void e(Object... objects) {
		if (DEBUG) {
			StringBuffer sb = new StringBuffer();
			if (objects == null) {
				sb.append("null");
			} else {
				for (Object obj : objects) {
					sb.append(obj);
				}
			}
			System.err.println(sb.toString());
		}
	}
}
