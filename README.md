# Smart Fit: Smart Fitting Room Assistance System
Smart Fit is a IoT product designed and built by [Chi Zhang](https://www.linkedin.com/in/zhang-chi/), [Jiajie He](https://www.linkedin.com/in/jiajie-he/), [Jang Won Suh](https://www.linkedin.com/in/jang-won-suh-371123b8/) and [Supanath Juthacharoenwong](https://www.linkedin.com/in/supanath-juthacharoenwong-02754353/). It is also a team project for Mechaphonics: Smart Phone-Enabled Mechatronic Systems[(ME202)](http://explorecourses.stanford.edu/search?view=catalog&filter-coursestatus-Active=on&page=0&catalog=&academicYear=&q=Mechaphonics&collapse=) at Stanford University, Spring 2017.

:warning: this page serves as software description, please refer to the project [website](http://me202smartfit.weebly.com/) for more details on mechanical and eletrical systems design.

## Requirements <img src="https://lh6.ggpht.com/ydol6v1uv6PCQdVZU3D0HucU6fqbYbQctmOqkwJ56QD65h3OaNam5cELB3FgnZpMCII=w300" width="50" height="50" />
### <img src="https://img.clippp.com/6b0c1fedd5eee052a5776c3f5e921f34_android-logo-logotype-all-logos-emblems-brands-pictures-gallery-android-logo_5100-1180.png" width="80" height="20" />
- [Android 6.0 Marshmallow](https://www.android.com/versions/marshmallow-6-0/) with API 23
- [Android SDK 25.3.0](https://developer.android.com/studio/releases/sdk-tools.html)
- [Android Bluetooth Low Energy Service](https://developer.android.com/guide/topics/connectivity/bluetooth-le.html)
### <img src="https://www.arduino.cc/arduino_logo.png" width="60" height="40" />
- [Arduino 1.69](https://www.arduino.cc/) with ES Framework by [J. Edward Carryer](https://profiles.stanford.edu/j-edward-carryer)
### Third-party Services and Libraries
- [Google Firebase](https://firebase.google.com/)
- [Butter Knife](http://jakewharton.github.io/butterknife/)
- [Picasso](http://square.github.io/picasso/)
- [Android GIF drawable](https://github.com/koral--/android-gif-drawable)
- [Android Circular Progress Button](https://github.com/dmytrodanylyk/circular-progress-button)
- [Adafruit BluefruitLE nRF51](https://learn.adafruit.com/introducing-the-adafruit-bluefruit-le-uart-friend/software)
- [Adafruit PN532 RFID](https://github.com/adafruit/Adafruit-PN532)

## Android Dependencies
### app level
```groovy
dependencies {
    compile 'com.android.support:appcompat-v7:25.3.1'
    // Access to Android Design library
    compile 'com.android.support:design:25.3.1'
    // Constraint layout
    compile 'com.android.support.constraint:constraint-layout:1.0.2'
    // Recycler view
    compile 'com.android.support:recyclerview-v7:25.3.1'
    // Image processing (Picasso)
    compile 'com.squareup.picasso:picasso:2.5.2'
    // View injector (Butter Knife)
    compile 'com.jakewharton:butterknife:8.5.1'
    // Gif views
    compile 'pl.droidsonroids.gif:android-gif-drawable:1.2.7'
    // Progress button
    compile 'com.github.dmytrodanylyk.circular-progress-button:library:1.1.3'
    testCompile 'junit:junit:4.12'
    annotationProcessor 'com.jakewharton:butterknife-compiler:8.5.1'
    // Firebase
    compile 'com.google.firebase:firebase-core:10.2.6'
    compile 'com.google.firebase:firebase-auth:10.2.6'
    compile 'com.google.firebase:firebase-database:10.2.6'
    compile 'com.google.firebase:firebase-storage:10.2.6'
}
```
For use of Google Services, you also need
```groovy
apply plugin: 'com.google.gms.google-services'
```
on the bottom of `build.gradle` in `app` folder.
### build level
```groovy
repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.3.2'
        // Google Services
        classpath 'com.google.gms:google-services:3.1.0'
    }
```

## Android Workflow

## Android GIF Demo
![customer](http://www.reactiongifs.us/wp-content/uploads/2013/10/nuh_uh_conan_obrien.gif)
![merchant](http://www.reactiongifs.us/wp-content/uploads/2013/10/nuh_uh_conan_obrien.gif)
## Arduino State Machine

## Copyright :copyright:

