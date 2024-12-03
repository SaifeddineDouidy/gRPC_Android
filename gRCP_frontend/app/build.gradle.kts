plugins {
    id("com.android.application")
    id("com.google.protobuf")
}

android {
    namespace = "ma.ensaj.gRPC"
    compileSdk = 34

    defaultConfig {
        applicationId = "ma.ensaj.gRPC"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Existing dependencies
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    // gRPC dependencies
    implementation("io.grpc:grpc-okhttp:1.56.1")
    implementation("io.grpc:grpc-protobuf:1.56.1") // Full protobuf for gRPC
    implementation("io.grpc:grpc-stub:1.56.1")
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}


protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.29.0"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.56.1"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                java {}
            }
            task.plugins {
                create("grpc") {}
            }
        }
    }
}
