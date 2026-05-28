@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements. See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership. The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License. You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied. See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@setlocal

@REM Set script directory
set "WRAPPER_DIR=%~dp0"

@REM Wrapper settings
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"
set "WRAPPER_JAR=%WRAPPER_DIR%.mvn\wrapper\maven-wrapper.jar"
set "DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

@REM Download wrapper jar if missing
if exist "%WRAPPER_JAR%" goto execute
echo Downloading Maven Wrapper...

powershell -Command "&{" ^
  "$webclient = New-Object System.Net.WebClient;" ^
  "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12;" ^
  "$webclient.DownloadFile('%DOWNLOAD_URL%', '%WRAPPER_JAR%')" ^
  "}"

if not exist "%WRAPPER_JAR%" (
    echo ERROR: Failed to download Maven Wrapper.
    goto error
)

:execute

@REM CLI args
set MAVEN_CMD_LINE_ARGS=%*

@REM Find Java
if defined JAVA_HOME goto findJavaFromJavaHome

set "JAVA_EXE=java.exe"
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%"=="0" goto run

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo Please set the JAVA_HOME variable in your environment to match the location of your Java installation.
echo.
goto error

:findJavaFromJavaHome
set "JAVA_HOME=%JAVA_HOME:"=%"
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

if exist "%JAVA_EXE%" goto run

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo Please set the JAVA_HOME variable in your environment to match the location of your Java installation.
echo.
goto error

:run
set "WRAPPER_DIR_NOSLASH=%WRAPPER_DIR:~0,-1%"
"%JAVA_EXE%" -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%WRAPPER_DIR_NOSLASH%" org.apache.maven.wrapper.MavenWrapperMain %MAVEN_CMD_LINE_ARGS%


if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%

if not "%MAVEN_SKIP_RC%"=="" goto skipRcPost

if exist "%USERPROFILE%\mavenrc_post.bat" call "%USERPROFILE%\mavenrc_post.bat"
if exist "%USERPROFILE%\mavenrc_post.cmd" call "%USERPROFILE%\mavenrc_post.cmd"

:skipRcPost

if "%MAVEN_BATCH_PAUSE%"=="on" pause

if "%MAVEN_TERMINATE_CMD%"=="on" exit %ERROR_CODE%

cmd /C exit /B %ERROR_CODE%