#!/bin/bash
#
# Magnet SDK Install script
#
# Usage:  [magnet-sdk-relative-dir] [api-lib-dir] -clean
#
if [ "$1" == "help" -o $# -eq 0 -o $# -gt 3 ]; then
    echo
    echo 'This tool installs Magnet generated api and Mobile Server libraries to your Android project. It generates required Magnet service mapping jar. Run this script from the root directory of your Android application project'
    echo
    echo "Usage:"
    echo "sh install-libs.sh [magnet-lib-project-dir] [api-lib-dir] | -clean"
    echo ' [magnet-lib-project-dir]   : directory to the Magnet library Android project'
    echo ' [api-lib-dir]              : directory to the api library jars. Generated service mapping jar goes here'
    echo ' -clean                     : remove generated files'
    echo
    echo ' Example: ../magnet-sdk-android/magnet-tools/install-libs.sh ../magnet-sdk-android/libproject/2.2.0 ./libs'
    echo
    exit 1
fi

SDKDIR=$1
APILIBDIR=$2

MYDIR=`pwd`

echo "Application directory: ${MYDIR}"
echo "Magnet SDK directory: ${SDKDIR}"
echo "API direcotry: ${APILIBDIR}"

if [ ! -d ${SDKDIR} ]; then
echo "Directory ${SDKDIR} does not exist; ${SDKDIR} is invalid"
exit 2
fi

# read version
VERSION=$(echo `cat ${SDKDIR}/version.txt | tr -d '\n'`)

#MYSDKDIR="${MYDIR}/${SDKDIR}"
MYSDKDIR="${SDKDIR}"

LIBDIR="${APILIBDIR}"
OUTDIR="${APILIBDIR}"
GENDIR="${MYDIR}/magnet-gen"
GENSRCDIR="${GENDIR}/src"
GENCLASSDIR="${GENDIR}/classes"

if [ "$3" == "-clean" ]; then
echo "Cleaning up all files"
rm -rf ${MYSDKDIR}/libs/config.dir
if [ -d ${OUTDIR} ]; then
  rm -rf ${OUTDIR}/magnet-service-mappings.jar
  rm -rf ${OUTDIR}/config.dir
fi
rm -rf ${GENDIR}/*
exit 0
fi

if [ ! -d ${APILIBDIR} ]; then
echo "Directory ${APILIBDIR} does not exist; ${APILIBDIR} is invalid"
exit 2
fi

echo "Generating service map..."

MERGEOPTION="-mergedir ${MYSDKDIR}/libs"

# first, clean out generated directory
echo "Remove existing service mappings files"
rm -rf ${OUTDIR}/magnet-service-mappings.jar
rm -rf ${GENDIR}/*

# Run the tool
RUN_OPTS="-libdir ${LIBDIR} -outdir ${OUTDIR} ${MERGEOPTION} -gensrcdir ${GENSRCDIR} -genclassdir ${GENCLASSDIR} -clean"

echo "Generating service registry... ${RUN_OPTS}"

java -jar ${MYSDKDIR}/tools-lib/magnet-core-mobile-android-build-tool-${VERSION}-shaded.jar ${RUN_OPTS}

echo "Removing unneeded generated files..."
rm -rf ${MYSDKDIR}/libs/config.dir

# echo "Copy required assets..."
# if [ ! -d ${MYDIR}/assets/config.dir ]; then
# mkdir -p ${MYDIR}/assets/config.dir
# fi
#cp -n ${SDKDIR}/assets/config.dir/* ${MYDIR}/assets/config.dir
