REM
REM Magnet SDK Install script
REM
REM Usage:  [magnet-lib-project-dir] [api-lib-dir] -clean
REM

if "%1"=="help" GOTO SHOWHELP

set SDKDIR=%1
set APILIBDIR=%2

set MYDIR=%CD%
echo "Application directory: %MYDIR%"
echo "Magnet SDK directory: %SDKDIR%"
echo "API direcotry: %APILIBDIR%"

if EXIST %SDKDIR% GOTO SETVARS
echo "Directory %SDKDIR% does not exist; %SDKDIR% is invalid"
GOTO DONE

:SETVARS
REM set MYSDKDIR=%MYDIR%\%SDKDIR%
set MYSDKDIR=%SDKDIR%
set LIBDIR=%APILIBDIR%
set OUTDIR=%APILIBDIR%
set GENDIR=%MYDIR%\magnet-gen
set GENSRCDIR=%GENDIR%\src
set GENCLASSDIR=%GENDIR%\classes
set /p VERSION=<%MYSDKDIR%\version.txt

if "%3"=="-clean" GOTO CLEANUP

if EXIST %APILIBDIR% GOTO GENERATE
echo "Directory %APILIBDIR% does not exist; %APILIBDIR% is invalid"
GOTO DONE

:GENERATE
echo "Generating service map..."

set MERGEOPTION=-mergedir %MYSDKDIR%\libs

REM first, clean out generated directory
echo "Remove existing service mappings files"
del/f/q %OUTDIR%\magnet-service-mappings.jar
del/f/q %GENDIR%\*

REM Run the tool
set "RUN_OPTS=-libdir %LIBDIR% -outdir %OUTDIR% %MERGEOPTION% -gensrcdir %GENSRCDIR% -genclassdir %GENCLASSDIR% -clean"

echo "Generating service registry... %RUN_OPTS%"
java -jar %MYSDKDIR%\tools-lib\magnet-core-mobile-android-build-tool-%VERSION%-shaded.jar %RUN_OPTS%

echo "Removing unneeded generated files..."
rmdir/s/q %MYSDKDIR%\libs\config.dir

GOTO DONE

:CLEANUP
echo "Cleaning up all files"
del/f/q %OUTDIR%\magnet-service-mappings.jar

rmdir/s/q %MYSDKDIR%\libs\config.dir
rmdir/s/q %OUTDIR%\config.dir

rmdir/s/q %GENDIR%\*
GOTO DONE

:SHOWHELP
echo.
echo "This tool installs Magnet SDK library to your Android project. It generates required Magnet service mapping jar. Run this script from the root directory of your Android application project"
echo.
echo "Usage:"
echo "install-lib.bat [magnet-lib-project-dir] [api-lib-absolute-dir] -clean"
echo ' [magnet-lib-project-dir]  : directory to the Magnet library Android project'
echo ' [api-lib-dir]             : directory to the api library jars'
echo '  -clean                   : remove generated files'
echo.
echo ' Example: ..\magnet-sdk-dir\magnet-tools\install-lib.bat ..\magnet-sdk-dir\libproject/2.2.0 .\libs'
echo.

:DONE
