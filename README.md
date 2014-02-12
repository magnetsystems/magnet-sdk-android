## Magnet Mobile Server SDK for Android

The Magnet Mobile Server SDK for Android is licensed under the terms of the [Magnet Software License Agreement](http://www.magnet.com/resources/tos.html).  Please see [LICENSE](LICENSE) file for full details.


This directory contains the Magnet Mobile Server SDK for Android which includes tools and the library to build your Android app for a Mobile App Server project.

Refer to  developer.android.com for IDE requirements and details about including an Android library project in your app. Below is a quick-start summary for using Eclipse and Apache Ant.

### Supported Platforms:
Android API level 15 and up.

## REQUIRED: Install the Magnet library

From directory of your Android application project, install the Magnet library to your application using relative path to the SDK directory. For example, assuming the SDK directory is one level above the application project directory named "magnet-sdk-android":

    $ sh ../magnet-sdk-android/magnet-tools/install-libs.sh ../magnet-sdk-android/libproject/2.2.0 ./libs

General Usage:

    install-libs.sh <magnet-libproject-dir> <app-library-dir>
    <magnet-library-dir> : path to Magnet Mobile Server library project
    <app-library-dir>    : path to application specific dependent libraries, typically "libs"

For Windows:

    $ ..\magnet-sdk-android\magnet-tools\install-libs.bat ..\magnet-sdk-android\libproject\2.2.0 .\libs

Enable manifest merging by adding this line to project.properties:

    manifestmerger.enabled=true

## Optional: Google Play Services Client library

Certains features of the Magnet library is dependent on the Google Play Services client library such as geo constraints and offline requests. Please follow instructions on [Google's Google Play Services website] (http://developer.android.com/google/play-services/setup.html) on how to add this library to your Android project. Below is a short summary of the instructions.


### Google Play Services using Ant

To add the Google Play Client Services library using Ant, follow these steps:

1. Use the Android SDK Manager to install Google Play Services.
2. Copy the directory located at extras/google/google_play_services/libproject/google-play-services_lib to another directory.
3. Build the copied Google play services library project with ant:

        $ cd google-play-services_lib
        $ android update project -p .
        $ ant debug

4. Add Google Play Service library project as a dependent library to your project. From directory of your project:

        $ android update project --path . --library <relative-path>/google-play-services_lib --target <target-id>


### Google Play Services using Eclipse

Follow instructions on how to add an [Android library to an Eclipse project] (http://developer.android.com/tools/projects/projects-eclipse.html#ReferencingLibraryProject).

### Build Android Apps with Magnet library using ANT

Add the Magnet library as a dependent library to the ANT xml file project.properties. Use a relative path. For example, run this from your Android project directory:

    $ android update project --path . --library ../magnet-sdk-android/libproject/2.2.0 --target <target-id>

For Windows:

    $ android.bat update project --path . --library ..\magnet-sdk-android\libproject\2.2.0 --target <target-id>

Note: "--target" option is required for "android" command for new projects. For more details, refer to Android Developer Guide, [Managing Projecting from Command Line] (http://developer.android.com/tools/projects/projects-cmdline.html)


Build your application with ANT:

    $ ant clean debug


### Build Android Apps with Magnet library using Eclipse

Create Magnet library as an "Android Library" project and include it as a library in your Android app:

1. File->New Project->Android->Android Project from Existing Code
2. Browse to the unzipped Magnet library project directory and select "libproject/2.2.0" as the "Root Directory".
3. A new Android project will now be created with the name "OAuthFlowActivity".
4. Select the newly created library project and right click->Properties, under "Android", "is library" should be checked.
5. From the main Android application project, add the library project under Project Properties->"Android", select the library and add it.
6. Build your main Android application project using Eclipse.


### Integrating Magnet generated API libraries to Android projects

This step is REQUIRED before building your Android app.

In order to use custom controller APIs, the library must be integrated to your application build using "install-libs" script. The API library is typically generated the Magnet Mobile App Builder tool, provided by your server team.

First, copy custom Magnet controller api library jar files to the directory where all dependent libraries are stored, typically "libs". Then, regenerate Magnet platform service mapping library using the script, "install-libs.sh" as described above.

    $ sh ../magnet-sdk-android/magnet-tools/install-libs.sh ../magnet-sdk-android/libproject/2.2.0 ./libs

For Windows:

    $ ..\magnet-sdk-android\magnet-tools\install-libs.bat ..\magnet-sdk-android\libproject\2.2.0 .\libs

##### After running "install-libs", if using Eclipse, make sure to do "Refresh" on "libs" directory of your Android project before building it.

IMPORTANT: It's highly recommended that you make a backup copy of the original "./libs" directory before introducing a new api library jar to "libs" directory.


