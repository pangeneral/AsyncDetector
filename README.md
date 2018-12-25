# AsyncDetector
A flow, context and object-sensitive inter-procedural static analysis tool for misuse detection of AsyncTask

# Obtaining AsyncDetector
You can download AsyncDetector from <https://github.com/pangeneral/AsyncDetector/asyncdetector.jar>

# How to run AsyncDetector
Before running AsyncDetector, we should set its running parameters in configuration file.

At first, we build an empty directory called `configure` under the same directory of `asyncdetector.jar`. Then we create a file `path_configuration.txt` under `configure`. 

In `path_configuration.txt`, we should set three parameters: the root directory of apk under analysis (`apkBasePath`), the root directory of jimple file generated by our tool (`jimpleBasePath`) and the android platform path (`androidPath`).

An example of `path_configuration.txt` is as following:

    apkBasePath test-apk  
    jimpleBasePath jimple test-apk
    androidPath D:\\download\\soot-path\\android-platforms-master\\

Obviously, the root directory of apk under analysis is `test-apk`, the root directory of jimple file is `jimple\test-apk` and the android platform path is `D:\\download\\soot-path\\android-platforms-master\\`.

After configuration, we can apply AsyncDetector by a simple command line instruction:

>java -jar asyncdetector.jar APK_NAME

`APK_NAME` is the apke that you want to analyze.

