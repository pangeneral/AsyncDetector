package cn.ac.ios.ad.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import cn.ac.ios.ad.AsyncTaskDetector;
import cn.ac.ios.ad.constant.AsyncClass;
import cn.ac.ios.ad.constant.BasicType;

/**
 * Processing class inheritance relationship 
 * @author panlj
 *
 */
public class ClassInheritanceProcess {
	enum MatchType{
		equal,regular
	}
	
	public static boolean isInheritedFromAsyncTask(SootClass theClass){
//		if( theClass.getType().toString().equals(AsyncClass.ASYNC_TASK))
//			return false;
//		else
		return isInheritedFromGivenClass(theClass, AsyncClass.ASYNC_TASK,MatchType.equal);
	}
	
	public static boolean isReturnTypeAsyncTask(SootMethod theMethod){
		Type returnType = theMethod.getReturnType();
		SootClass returnClass = Scene.v().getSootClass(returnType.toString());
//		SootClass returnClass = AsyncTaskDetector.classNameToSootClass.get(returnType.toString());
		if(!returnType.toString().equals(AsyncClass.ASYNC_TASK) && !isInheritedFromAsyncTask(returnClass))
			return false;
		else
			return true;
	}
	
	
	public static boolean isInheritedFromActivity(SootClass theClass){
		return isInheritedFromGivenClass(theClass, BasicType.ACTIVITY,MatchType.equal);
	}
	
	public static boolean isInheritedFromView(SootClass theClass){
		return isInheritedFromGivenClass(theClass, BasicType.VIEW,MatchType.equal);
	}
	
	public static boolean isInheritedFromMap(SootClass theClass){
		return isInheritedFromGivenClass(theClass, BasicType.MAP,MatchType.equal);
	}
	
	public static boolean isInheritedFromCollection(SootClass theClass){
		return isInheritedFromGivenClass(theClass, BasicType.COLLECTION,MatchType.equal);
	}
	
	public static boolean isInheritedFromOnClickListener(SootClass theClass){
		return isInheritedFromGivenClass(theClass, BasicType.ON_CLICK_LISTENER, MatchType.equal);
	}
	
	public static boolean isInheritedFromListener(SootClass theClass){
		return isInheritedFromGivenClass(theClass, BasicType.LISTENER, MatchType.regular);
	}
	
	
	
	private static boolean isInheritedFromGivenClass(SootClass theClass,String classNameUnderMatch,MatchType matchType){
		if( theClass == null || theClass.getType().toString().equals(BasicType.OBJECT))
			return false;
		if( isTypeMatch(theClass,classNameUnderMatch,matchType))
			return true;
		for(SootClass interfaceClass:theClass.getInterfaces())
			if( isInheritedFromGivenClass(interfaceClass, classNameUnderMatch,matchType))
				return true;
		SootClass superClass = theClass.getSuperclass();
		while(superClass != null && !superClass.getName().equals(BasicType.OBJECT)){
			for(SootClass interfaceClass:superClass.getInterfaces())
				if(isInheritedFromGivenClass(interfaceClass, classNameUnderMatch,matchType))
					return true;
			if( isTypeMatch(superClass, classNameUnderMatch, matchType))
				return true;
			superClass = superClass.getSuperclass();
		}
		return false;
	}
	
	
	private static boolean isTypeMatch(SootClass currentClass,String classNameUnderMatch,MatchType matchType){
		if( matchType == MatchType.equal && currentClass.getType().toString().equals(classNameUnderMatch))
			return true;
		else if( matchType == MatchType.regular && isRegularMatch(currentClass.getType().toString(), classNameUnderMatch))
			return true;
		else
			return false;
	}
	
	
	private static boolean isRegularMatch(String targetStr,String regularStr){
		Pattern p=Pattern.compile(regularStr);
		Matcher m=p.matcher(targetStr);
		return m.matches();
	}
}
