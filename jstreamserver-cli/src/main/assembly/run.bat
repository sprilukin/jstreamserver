@echo off
if not "%JAVA_HOME%" == "" goto ok
if not "%JRE_NOME%" == "" goto setupJavaHomeFromJREHome
@echo The JAVA_HOME or JRE_HOME environment variable is not defined correctly
@echo One of these environment variables are mandatory to run this program
exit /B 1
:setupJavaHomeFromJREHome
SET JAVA_HOME=%JRE_HOME%
:ok

SET JAVA_OPTS=-Dfile.encoding=UTF-8 -Xmx256m %JAVA_OPTS%
SET JAVA="%JAVA_HOME%\bin\java.exe"
SET CONSOLE_CLIENT_JAR=jstreamserver-cli-@VERSION@.jar
SET EXECUTE_COMMAND=%JAVA% -jar -Drootdir.c="c:\" -Drootdir.d="d:\" %JAVA_OPTS% %CONSOLE_CLIENT_JAR%
call %EXECUTE_COMMAND%
EXIT /B 0