
```groovy
static  def getAppVersionName() {
    def cmd = 'git describe --tags --abbrev=0'
    cmd.execute().text.trim()
}

static  def getAppVersionCode() {
    def cmd = 'git rev-list HEAD --first-parent --count'
    cmd.execute().text.trim().toInteger()
}


android {

    compileSdk 31

    defaultConfig {
        applicationId "com.newegg.logistics"
        minSdk 26
        targetSdk 31
        versionCode 1
        versionName "1.0"

        versionCode getAppVersionCode()
        versionName getAppVersionName()

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("Integer", "LOG_SHOW", "2")
            buildConfigField("Boolean", "DEBUG", "true")
            buildConfigField("String", "SERVICE_URL", "\"http://gqc-services.newegg.space/c6762c53/\"")
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }

        release {
            buildConfigField("Integer", "LOG_SHOW", "5")
            buildConfigField("Boolean", "DEBUG", "false")
            buildConfigField("String", "SERVICE_URL", "\"http://gdev-services.newegg.space/c6762c53/\"")
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }


        gdev {
            buildConfigField("Integer", "LOG_SHOW", "2")
            buildConfigField("Boolean", "DEBUG", "true")
            buildConfigField("String", "SERVICE_URL", "\"http://gdev-services.newegg.space/c6762c53/\"")
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }

        gqc {
            buildConfigField("Integer", "LOG_SHOW", "2")
            buildConfigField("Boolean", "DEBUG", "true")
            buildConfigField("String", "SERVICE_URL", "\"http://gqc-services.newegg.space/c6762c53/\"")
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }

        pre {
            buildConfigField("Integer", "LOG_SHOW", "2")
            buildConfigField("Boolean", "DEBUG", "true")
            buildConfigField("String", "SERVICE_URL", "\"http://10.16.45.151:8080/\"")
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }

        prd {
            buildConfigField("Integer", "LOG_SHOW", "2")
            buildConfigField("Boolean", "DEBUG", "true")
            buildConfigField("String", "SERVICE_URL", "\"http://10.16.45.152:8080/\"")
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }


    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    buildFeatures {
        dataBinding true
        viewBinding true
    }

    testOptions {
        unitTests.returnDefaultValues = true

    }

    applicationVariants.all { variant ->
        if (variant.buildType.name == 'gdev') {
            variant.outputs.each { output ->
                output.versionNameOverride = getAppVersionName() + "-gdev"
            }
        } else if (variant.buildType.name == 'gqc') {
            variant.outputs.each { output ->
                output.versionNameOverride = getAppVersionName() + "-gqc"
            }
        }else if (variant.buildType.name == 'pre') {
            variant.outputs.each { output ->
                output.versionNameOverride = getAppVersionName() + "-pre"
            }
        }else if (variant.buildType.name == 'prd') {
            variant.outputs.each { output ->
                output.versionNameOverride = getAppVersionName() + "-prd"
            }
        }
    }

}

```
