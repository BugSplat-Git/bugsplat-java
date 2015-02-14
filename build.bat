:
: Batch file to build the BugSplat Java Library
:

set classpath=.;C:\Program Files\Java\jdk1.7.0_51\lib
set classpath=%classpath%;C:\www\src\BugSplat\BugSplatJava\lib\soap.jar
set classpath=%classpath%;C:\www\src\BugSplat\BugSplatJava\lib\mailapi.jar
set classpath=%classpath%;C:\www\src\BugSplat\BugSplatJava\lib\activation.jar
set classpath=%classpath%;C:\www\src\BugSplat\BugSplatJava\lib\BrowserLauncher2-1_3.jar

:
: Delete the old byte code and archives
:

del com\bugsplatsoftware\client\gui\BugSplatLabel.class
del com\bugsplatsoftware\client\gui\BugSplatImageCanvas.class
del com\bugsplatsoftware\client\gui\BugSplatViewDetails.class
del com\bugsplatsoftware\client\gui\BugSplatDialog.class
del com\bugsplatsoftware\client\gui\BugSplatProgress.class
del com\bugsplatsoftware\client\gui\BugSplatDetails.class

del com\bugsplatsoftware\client\util\BugSplatFormPost.class
del com\bugsplatsoftware\client\util\BugSplatTrustManager.class
del com\bugsplatsoftware\client\util\BugSplatReport.class
del com\bugsplatsoftware\client\util\BugSplatThreadGroup.class
del com\bugsplatsoftware\client\util\BugSplatThread.class

del com\bugsplatsoftware\client\BugSplat.class

del lib\bugsplat.jar

:
: Build the library classes
:

javac -deprecation com\bugsplatsoftware\client\gui\BugSplatLabel.java
javac -deprecation com\bugsplatsoftware\client\gui\BugSplatImageCanvas.java
javac -deprecation com\bugsplatsoftware\client\gui\BugSplatViewDetails.java
javac -deprecation com\bugsplatsoftware\client\gui\BugSplatDialog.java
javac -deprecation com\bugsplatsoftware\client\gui\BugSplatProgress.java
javac -deprecation com\bugsplatsoftware\client\gui\BugSplatDetails.java

javac -deprecation com\bugsplatsoftware\client\util\BugSplatFormPost.java
javac -deprecation com\bugsplatsoftware\client\util\BugSplatTrustManager.java
javac -deprecation com\bugsplatsoftware\client\util\BugSplatReport.java
javac -deprecation com\bugsplatsoftware\client\util\BugSplatThreadGroup.java
javac -deprecation com\bugsplatsoftware\client\util\BugSplatThread.java

javac -deprecation com\bugsplatsoftware\client\BugSplat.java

:
: Archive the library
:
jar cvf bugsplat.jar com\bugsplatsoftware\client\*.class com\bugsplatsoftware\client\gui\*.class com\bugsplatsoftware\client\gui\images\*.gif com\bugsplatsoftware\client\util\*.class

:
: Sign the jar
: This is causing problems at runtime - for now, do not sign
:
: jarsigner -keystore BugSplatKeys -storepass DeerRun BugSplat.jar BugSplat 

:
: Display the jar contents
:
jar tf bugsplat.jar


:
: Move the jar to the lib folder
:
move bugsplat.jar lib

:
: Delete the old byte code and archives
:
del *.class -y
del *.jar -y

: Generate documentation
javadoc -d doc com\bugsplatsoftware\client\BugSplat.java
 


:
: Build the demo application class
:
javac -deprecation MyJavaCrasher.java

:
: Archive the demo application
: The security manager will not allow the app to access network resource
:
:jar cvfm BugSplatDemo.jar manifest.txt *.class resources\*.gif

:
: Sign the jar
: This is causing problems at runtime - for now, do not sign
:
:jarsigner -keystore BugSplatKeys -storepass DeerRun BugSplatDemo.jar BugSplat 

:
: Build the demo console class
:
javac -deprecation MyJavaCrasherConsole.java

:
: Build the demo applet class
: The security manager will not allow the applet to perform IO
:
:javac -deprecation MyJavaCrasherApplet.java
