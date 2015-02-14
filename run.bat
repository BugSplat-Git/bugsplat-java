:
: Batch file to run the BugSplat Java application
:
@echo off

if "%1"=="" goto GUI
java MyJavaCrasherConsole

goto end

:GUI
java MyJavaCrasher

:end

