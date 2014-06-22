android-gradle-localization-plugin
==================================

Gradle plugin for generating localized string resources

## Overview
This plugin generates Android string resource XML files from CSV file.
Generation has to be invoked as additional gradle task.
 
##Supported features
 * non-translatable resources - `translatable="false"` XML attribute
 * auto-escaping double quotes, apostrophes and newlines
 * auto-quoting leading and trailing spaces
 * syntax validation - duplicated, empty, invalid names detection
 * comments
  
## Usage
1. Add maven snapshot repository and dependency to the __top-level__ `build.gradle` file.
 Your file should look like this:
 ```gradle
 buildscript {
     repositories {
         mavenLocal()
         mavenCentral()
         maven{ url('https://oss.sonatype.org/content/repositories/snapshots')}
     }
     dependencies {
         classpath 'com.android.tools.build:gradle:0.11.+'
         classpath 'pl.droidsonroids.gradle.localization:android-gradle-localization-plugin:1.0.+'
     }
 }
 ```
2. Apply plugin and add configuration to `build.gradle` of the application, eg:
 ```gradle
 apply plugin: 'localization'
 localization
     {
         csvFilePath='translations.csv'
     }
 ```
 
3. Invoke `localization` gradle task. Task may be invoked from commandline or from Android Studio GUI.
 * from commandline: `./gradlew localization` (or `gradlew.bat localization` on Windows)
 * from GUI: menu `View->Tool Windows->Gradle` and double click `localization`<br>
 
 Non existent folders will be created. __WARNING__ existing XML files will be overwritten.

##Example
The following CSV file:
```csv
name,default    ,pl       ,comment   ,translatable
file,File       ,"Plik"   ,file label,
app ,Application,,,false
```
will produce 2 XML files:
* `values/strings.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools">
  <string name="file">File</string><!-- file label -->
  <string name="app" translatable="false">Application</string>
</resources>
```
* `values-pl/strings.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools">
  <string name="file">Plik</string><!-- file label -->
</resources>
```

##Configuration
`localization` extension in `build.gradle` can contain several configuration options. All of them 
except CSV file location are optional and has reasonable default values.<br>
CSV file location. __Exactly one of them__ must be specified:
* `csvFilePath` - path to CSV file (relative or absolute)
* `csvFileURI` - CSV file URI

CSV format:
* `defaultColumnName` - default='default', column name which corresponds to default localization 
(`values` folder)

The following options turn off some auto-escaping, can be useful if you have it already escaped in CSV:
* `escapeApostrophes` - default=true, if set to false apostrophes (`'`) won't be escaped
* `escapeQuotes` - default=true, if set to false double quotes (`"`)  won't be escaped
* `escapeNewLines` - default=true, if set to false newlines won't be escaped
* `escapeBoundarySpaces` - default=true, if set to false and value have leading or trailing spaces
then they won't be escaped so they will be effectively removed

Advanced options:
* `allowNonTranslatableTranslation` - default=false, if set to true resources marked 
non-translatable but translated are permitted
* `allowEmptyTranslations` - default=false, if set to true then empty values are permitted
 
##License

MIT License<br>
See [LICENSE](LICENSE) file.