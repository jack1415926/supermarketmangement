@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------
@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Windows startup script (must be ASCII/GBK, NOT UTF-8!)
@REM Usage:
@REM   mvnw.cmd compile
@REM   mvnw.cmd spring-boot:run
@REM   mvnw.cmd test
@REM   mvnw.cmd clean package
@REM ----------------------------------------------------------------------------

@setlocal

@if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: MAVEN_BATCH_PAUSE is set to 'on', cannot find maven wrapper properties.
goto end

:execute
@REM Setup Maven home directories
@set "MAVEN_HOME=%~dp0"
@set "MAVEN_USER_HOME=%MAVEN_HOME%"

@REM Locate wrapper properties file
@set "WRAPPER_PROPERTIES_PATH=%MAVEN_HOME%.mvn\wrapper\maven-wrapper.properties"

@REM Extract wrapperUrl from properties file
@for /f "usebackq tokens=1,2 delims==" %%A in ("%WRAPPER_PROPERTIES_PATH%") do (
    @if "%%A" == "wrapperUrl" set "WRAPPER_URL=%%B"
)

@REM Extract distributionUrl from properties file
@for /f "usebackq tokens=1,2 delims==" %%A in ("%WRAPPER_PROPERTIES_PATH%") do (
    @if "%%A" == "distributionUrl" set "DISTRIBUTION_URL=%%B"
)

@REM Set project base directory
@set "MAVEN_PROJECTBASEDIR=%MAVEN_HOME%"
@set "MAVEN_BASEDIR=%MAVEN_HOME%"

@REM Find Java installation — check if JAVA_HOME actually points to a valid Java
@set "JAVA_EXEC=java"
@if defined JAVA_HOME (
    @if exist "%JAVA_HOME%\bin\java.exe" (
        @set "JAVA_EXEC=%JAVA_HOME%\bin\java.exe"
    )
)

@REM Set JVM options
@if not defined MAVEN_OPTS (
    @set "MAVEN_OPTS=-Xmx1024m -XX:MaxMetaspaceSize=256m"
)

@REM Execute Maven via the wrapper JAR
@set "CLASSWORLDS_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"
@set "WRAPPER_JAR=%MAVEN_HOME%.mvn\wrapper\maven-wrapper.jar"

"%JAVA_EXEC%" ^
  %MAVEN_OPTS% ^
  -classpath "%WRAPPER_JAR%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %CLASSWORLDS_LAUNCHER% ^
  %*

:end
@endlocal

exit /b %ERRORLEVEL%
