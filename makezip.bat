@echo off

attrib -r BugSplatJava.zip > NUL
del .BugSplatJava.zip > NUL

echo Creating BugSplat Java binaries zip
\www\src\bin\7z a -tzip BugSplatJava.zip lib\soap.jar
\www\src\bin\7z a -tzip BugSplatJava.zip lib\mailapi.jar
\www\src\bin\7z a -tzip BugSplatJava.zip lib\BrowserLauncher2-1_3.jar
\www\src\bin\7z a -tzip BugSplatJava.zip lib\activation.jar
\www\src\bin\7z a -tzip BugSplatJava.zip lib\bugsplat.jar
\www\src\bin\7z a -tzip BugSplatJava.zip license\soap.license.txt
\www\src\bin\7z a -tzip BugSplatJava.zip license\javamail.license.txt
\www\src\bin\7z a -tzip BugSplatJava.zip license\activation.license.txt
\www\src\bin\7z a -tzip BugSplatJava.zip license\BrowserLauncher.license.txt
\www\src\bin\7z a -tzip BugSplatJava.zip license\COPYING.txt
\www\src\bin\7z a -tzip BugSplatJava.zip MyJavaCrasher.java
\www\src\bin\7z a -tzip BugSplatJava.zip MyJavaCrasherConsole.java
\www\src\bin\7z a -tzip BugSplatJava.zip additional.txt
\www\src\bin\7z a -tzip BugSplatJava.zip doc
