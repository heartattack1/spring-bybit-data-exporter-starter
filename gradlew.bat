@echo off
set APP_HOME=%~dp0
if defined JAVA_HOME (
  set JAVA_EXEC=%JAVA_HOME%\bin\java
) else (
  set JAVA_EXEC=java
)
"%JAVA_EXEC%" -classpath "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
