package cn.ac.ios.ad;

import java.util.Map;

import cn.ac.ios.ad.util.ClassInheritanceProcess;
import soot.Body;
import soot.BodyTransformer;
import soot.SootClass;

/**
 * @author panlj
 */
public class AsyncTaskTransformer extends BodyTransformer{
	
	@Override
	protected void internalTransform(Body b, String phaseName,
			Map<String, String> options) {
		SootClass theClass = b.getMethod().getDeclaringClass();
		synchronized (AsyncTaskDetector.methodSignatureToBody) {
			AsyncTaskDetector.methodSignatureToBody.put(b.getMethod().getSignature(),b);
			boolean isListener = ClassInheritanceProcess.isInheritedFromOnClickListener(theClass);
			synchronized(AsyncTaskDetector.listenerClasses){
				if( isListener && theClass.isInnerClass())//We only consider inner class
					AsyncTaskDetector.listenerClasses.add(theClass);
			}
		}
	}
}
