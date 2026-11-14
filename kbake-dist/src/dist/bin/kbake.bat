@echo off
setlocal
set "APP_HOME=%~dp0.."
if defined JAVA_HOME (
  set "JAVA=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA=java"
)

"%JAVA%" -XX:MaxDirectMemorySize=512m -Djna.nosys=true --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --enable-native-access=ALL-UNNAMED -jar "%APP_HOME%\lib\kbake.jar" %*
exit /b %ERRORLEVEL%
