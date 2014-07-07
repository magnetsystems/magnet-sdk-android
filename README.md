## Magnet Mobile SDK for Android

The Magnet Mobile Server SDK for Android is licensed under the terms of the [Magnet Software License Agreement](http://www.magnet.com/resources/tos.html). See [LICENSE](https://github.com/magnetsystems/magnet-sdk-android/blob/master/LICENSE) file for full details.


This directory contains the Magnet Mobile SDK for Android which includes the library to build your Android app for a Mobile App project.

Refer to  developer.android.com for IDE requirements and details about including an Android library project in your app. Below is a quick-start summary for using Eclipse and Apache Ant.

### Supported Android Platforms:
Android API level 16 and up.


### Prerequisites
1. Mobile App Builder tool (Installation instructions can be found [here](https://factory.magnet.com/get-started/#gs-step1).
2. [Android SDK](http://developer.android.com/tools/index.html), minimally Android 4.1.2, API Level 16.

### Build Android Apps with Magnet library using ANT

Add the Magnet library as a dependent library to the ANT xml file, project.properties of your Android project. Assuming the library is at the same level as your Android app project directory, run this from your Android project directory using relative path to the library directory:

    $ android update project --path . --library ../magnet-sdk-android/libproject/magnetlib-2.3.0 --target <target-id>

For Windows:

    $ android.bat update project --path . --library ..\magnet-sdk-android\libproject\magnetlib-2.3.0 --target <target-id>

Note: "--target" option is required for "android" command for new projects. For more details, refer to Android Developer Guide, [Managing Project from Command Line] (http://developer.android.com/tools/projects/projects-cmdline.html).


Build your app with ANT:

    $ ant clean debug


### Build Android Apps with Magnet library using Eclipse

Create the Magnet library as an "Android Library" project and include it in your Android app as a dependency:

1. File->New Project->Android->Android Project from Existing Code
2. Browse to the unzipped Magnet library project directory and select "magnetlib-2.3.0" as the "Root Directory".
A new Android project will be created with the name "magnetlib-2.3.0."
4. Select the newly created library project and right click Properties, under "Android", "is library" must be checked.
5. From your main Android application project, add the library project under Project Properties->"Android", select the "magnetlib-2.3.0" library project and add it.
6. Build your main Android application project using Eclipse.


### Running the App

Before running the Android app, you will need to build the Mobile backend. You can find detailed instructions for building the Mobile Backend server from scratch using the Mobile App Builder tool [here](https://factory.magnet.com/get-started/#gs-step2).

### Integrating Magnet generated API assets to the Android project


In order to use additional custom controller APIs, the generated assets from the Mobile App Builder tool must be copied to your Android project. The assets are created using "api-generate -wfc android" mab command. Using "jumpstart" as the example backend project:

1. Generate Assets for Android from mab tool:
		
		mab> api-generate -wfc android
		
2. Generated source for controllers to project source directory:

        mab> exec cp -R ~/MABProjects/jumpstart/mobile/apis/assets/android/com/magnetapi/ </path/to/MyAndroidProject/src/com/magnetapi>

        mab> exec cp -R ~/MABProjects/jumpstart/mobile/apis/assets/android/com/magnet/ </path/to/MyAndroidProject/src/com/magnet>


3. Generated java library jar to "libs" directory:

		mab> exec mkdir -p </path/to/MyAndroidProject/libs>
		mab> exec cp ~/MABProjects/jumpstart/mobile/apis/assets/android/beans.jar </path/to/MyAndroidProject/libs/>

4. Generated config files to "res/xml" directory:
		
		mab> exec mkdir -p </path/to/MyAndroidProject/res/xml>
		mab> exec cp ~/MABProjects/jumpstart/mobile/apis/assets/android/magnet_type_mapper.xml </path/to/MyAndroidProject/res/xml/>

		mab> exec cp ~/MABProjects/jumpstart/mobile/apis/assets/android/magnet_app_default.xml </path/to/MyAndroidProject/res/xml/>

5. Start using the generated controller classes in your Android code!



