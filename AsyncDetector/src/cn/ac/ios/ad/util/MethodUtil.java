package cn.ac.ios.ad.util;

import soot.SootClass;
import soot.SootMethod;

public class MethodUtil {

	/**
	 * 目前只有doInBackgound方法有此
	 * 
	 * @param sootClass
	 * @param methodName
	 * @return
	 */
	public static SootMethod getMethod(SootClass sootClass, String methodName) {
		if (sootClass == null) {
			return null;
		}
		SootMethod resultMethod = null;
		for (SootMethod sootMethod : sootClass.getMethods()) {

			if (sootMethod.getName().equals(methodName)
					&& sootMethod.getDeclaration().contains("transient")
					&& !sootMethod.getDeclaration().contains("volatile")) {
				return sootMethod;
			}
			if (sootMethod.getName().equals(methodName)
					&& sootMethod.getDeclaration().contains("transient")) {
				resultMethod = sootMethod;
				continue;
			}
			if (sootMethod.getName().equals(methodName) && resultMethod == null) {
				resultMethod = sootMethod;
			}
		}

		if (resultMethod == null && sootClass.hasSuperclass()) {
			SootClass superclass = sootClass.getSuperclass();
			return getMethod(superclass, methodName);
		}

		return resultMethod;
	}
}
