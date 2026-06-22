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
@REM Maven Wrapper Windows 启动脚本 (mvnw.cmd)
@REM 作用：在没有全局安装 Maven 的情况下，自动下载并使用指定版本的 Maven 来构建项目。
@REM 类似 C++ 的 FetchContent —— 自动拉取工具链，无需手动安装。
@REM
@REM 使用方法：
@REM   mvnw.cmd compile        - 编译项目
@REM   mvnw.cmd spring-boot:run - 启动 Spring Boot
@REM   mvnw.cmd test           - 运行测试
@REM   mvnw.cmd clean package  - 打包
@REM ----------------------------------------------------------------------------

@REM 开始本地化作用域，避免环境变量污染
@setlocal

@REM 设置错误处理：遇到错误立即退出
@if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: MAVEN_BATCH_PAUSE is set to 'on', cannot find maven wrapper properties.
goto end

:execute
@REM 查找 Maven 主目录（MAVEN_HOME）和用户目录（MAVEN_USER_HOME）
@REM Setup Anything...
@set "MAVEN_HOME=%~dp0"
@set "MAVEN_USER_HOME=%MAVEN_HOME%"

@REM 如果是 Maven Wrapper 下载的 Maven（存放于 wrapper/dists），使用解压目录结构
@set "DIST_DIR=%MAVEN_HOME%wrapper\dists"

@REM ==================== 解析命令行参数 ====================

@REM 查找 wrapper properties 文件
@set "WRAPPER_PROPERTIES_PATH=%MAVEN_HOME%.mvn\wrapper\maven-wrapper.properties"

@REM 读取 wrapper.url（Maven Wrapper JAR 的下载地址）
@for /f "usebackq tokens=1,2 delims==" %%A in ("%WRAPPER_PROPERTIES_PATH%") do (
    @if "%%A" == "wrapperUrl" set "WRAPPER_URL=%%B"
)

@REM 读取 distributionUrl（Maven 分发包的下载地址）
@for /f "usebackq tokens=1,2 delims==" %%A in ("%WRAPPER_PROPERTIES_PATH%") do (
    @if "%%A" == "distributionUrl" set "DISTRIBUTION_URL=%%B"
)

@REM 生成 MAVEN_PROJECTBASEDIR（项目根目录）
@set "MAVEN_PROJECTBASEDIR=%MAVEN_HOME%"
@set "MAVEN_BASEDIR=%MAVEN_HOME%"

@REM ==================== 查找 Java 安装路径 ====================
@if defined JAVA_HOME (
    @set "JAVA_EXEC=%JAVA_HOME%\bin\java.exe"
) else (
    @set "JAVA_EXEC=java"
)

@REM ==================== 设置 JVM 配置 ====================
@if not defined MAVEN_OPTS (
    @set "MAVEN_OPTS=-Xmx1024m -XX:MaxMetaspaceSize=256m"
)

@REM ==================== 执行 Maven ====================
@REM 使用 Maven Wrapper JAR 作为入口，它会：
@REM 1. 检查本地是否已有指定版本的 Maven
@REM 2. 如果没有，自动从 distributionUrl 下载
@REM 3. 用下载好的 Maven 执行用户指定的命令（compile、test 等）

@set "CLASSWORLDS_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"
@set "WRAPPER_JAR=%MAVEN_HOME%.mvn\wrapper\maven-wrapper.jar"

"%JAVA_EXEC%" ^
  %MAVEN_OPTS% ^
  -classpath "%WRAPPER_JAR%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %CLASSWORLDS_LAUNCHER% ^
  %*

:end
@REM 结束本地化作用域
@endlocal

@REM 退出码
exit /b %ERRORLEVEL%
