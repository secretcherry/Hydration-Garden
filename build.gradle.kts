
buildscript {
    dependencies {
        classpath ("com.google.gms:google-services:4.4.0")
        classpath ("com.android.tools.build:gradle:8.2.0")
        classpath ("com.google.gms:google-services:4.4.0")
    }
}

plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false

}