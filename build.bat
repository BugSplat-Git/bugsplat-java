:
: Batch file to build the BugSplat Java Library
:

set classpath=.;C:\Program Files\Java\jdk1.7.0_51\lib
set classpath=%classpath%;C:\www\bugsplat-java\lib\soap.jar
set classpath=%classpath%;C:\www\bugsplat-java\lib\mailapi.jar
set classpath=%classpath%;C:\www\bugsplat-java\lib\activation.jar
set classpath=%classpath%;C:\www\bugsplat-java\lib\BrowserLauncher2-1_3.jar

:
: Delete the old byte code and archives
:

del src\main\java\com\bugsplat\client\gui\BugSplatLabel.class
del src\main\java\com\bugsplat\client\gui\BugSplatImageCanvas.class
del src\main\java\com\bugsplat\client\gui\BugSplatViewDetails.class
del src\main\java\com\bugsplat\client\gui\BugSplatDialog.class
del src\main\java\com\bugsplat\client\gui\BugSplatProgress.class
del src\main\java\com\bugsplat\client\gui\BugSplatDetails.class

del src\main\java\com\bugsplat\client\util\BugSplatFormPost.class
del src\main\java\com\bugsplat\client\util\BugSplatTrustManager.class
del src\main\java\com\bugsplat\client\util\BugSplatReport.class
del src\main\java\com\bugsplat\client\util\BugSplatThreadGroup.class
del src\main\java\com\bugsplat\client\util\BugSplatThread.class

del src\main\java\com\bugsplat\client\BugSplat.class

del lib\bugsplat.jar

:
: Build the library classes
:

javac -deprecation -classpath src\main\java src\main\java\com\bugsplat\client\gui\*.java
javac -deprecation -classpath src\main\java src\main\java\com\bugsplat\client\util\*.java
javac -deprecation -classpath src\main\java src\main\java\com\bugsplat\client\BugSplat.java

:
: Archive the library
:
cd src\main\java
jar cvf ..\..\..\bugsplat.jar com\bugsplat\client\*.class com\bugsplat\client\gui\*.class com\bugsplat\client\gui\images\*.gif com\bugsplat\client\util\*.class
cd ..\..\..

:
: Sign the jar
: This is causing problems at runtime - for now, do not sign
:
: jarsigner -keystore BugSplatKeys -storepass ****** BugSplat.jar BugSplat 

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
javadoc -classpath src/main/java -d doc src\main\java\com\bugsplat\client\BugSplat.java
 
:
: Build the demo application class
:
javac -deprecation -classpath lib/bugsplat.jar MyJavaCrasher.java

:
: Archive the demo application
: The security manager will not allow the app to access network resource
:
jar cvfm BugSplatDemo.jar MyJavaCrasher.mf *.class resources\*.gif

:
: Sign the jar
: This is causing problems at runtime - for now, do not sign
:
: jarsigner -keystore BugSplatKeys -storepass ***** BugSplatDemo.jar BugSplat 

:
: Build the demo console class
:
javac -deprecation -classpath target/bugsplat-java-0.0.4.jar MyJavaCrasherConsole.java

:
: Build the demo applet class
: The security manager will not allow the applet to perform IO
:
javac -deprecation -classpath lib/bugsplat.jar MyJavaCrasherApplet.java
