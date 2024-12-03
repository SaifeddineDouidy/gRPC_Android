// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.1" apply false
    id("com.google.protobuf") version "0.9.4" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.1")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
