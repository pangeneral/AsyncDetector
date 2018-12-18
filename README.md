# AsyncDetector
A flow, context and object-sensitive inter-procedural static analysis tool for misuse detection of AsyncTask

# Part I
```
IDE:eclipse-kepler
complier: jdk 1.7
Runtime Environment: jdk 1.8
```

# Part II
configuration:
./configure/path_configuration.txt
```
apkBasePath test-apk 
```
## "test-apk" is your apk dictionary path


```
jimpleBasePath jimple test-apk  
```
## "jimple test-apk " is your jimple file output folder , that is ".\jimple\test-apk\"



```
androidPath /yourAndroidJarPath 
```
##  androidPath is needed by Soot. If you set it in "src\cn\ac\ios\ad\constant\Configuration.java" , here will not be necessary.


# Part III
Command line
```
apkname.apk ./test-apk
```
The First arg is the apk u want to detect, and the second one is its path.
