plugins {
    kotlin("jvm") version "1.4.32"
    id("com.utopia-rise.godot-kotlin-jvm") version "0.2.0-3.3.2"
}

repositories {
    mavenCentral()
}

godot {
    isAndroidExportEnabled.set(true)
    dxToolPath.set("${System.getenv("ANDROID_SDK_ROOT")}/build-tools/30.0.3/dx")
}
