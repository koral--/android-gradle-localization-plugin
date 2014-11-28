android-gradle-localization-plugin
==================================
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/pl.droidsonroids.gradle.localization/android-gradle-localization-plugin/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/pl.droidsonroids.gradle.localization/android-gradle-localization-plugin) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-android--gradle--localization--plugin-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/902)  [![Build Status](https://travis-ci.org/koral--/android-gradle-localization-plugin.svg?branch=master)](https://travis-ci.org/koral--/android-gradle-localization-plugin)

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
1. Add dependency to the __top-level__ `build.gradle` file.
 Your file should look like this:
 ```
 
  buildscript {
     repositories {
         mavenCentral()
     }
     dependencies {
         classpath 'com.android.tools.build:gradle:0.14.+'
         classpath 'pl.droidsonroids.gradle.localization:android-gradle-localization-plugin:1.0.+'
     }
 }
 ```
2. Apply plugin and add configuration to `build.gradle` of the application, eg:
 ```
 apply plugin: 'pl.droidsonroids.localization'
 localization
     {
         csvFile=file('translations.csv')
         OR
         csvFileURI='https://docs.google.com/spreadsheets/d/<key>/export?format=csv'
         OR
         csvGenerationCommand='/usr/bin/xlsx2csv translation.xlsx'
     }
 ```
 `csvFileURI` can be any valid URI, not necessarily Google Docs' one 
 
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
except CSV source are optional and has reasonable default values.<br>
CSV source. __Exactly one of them__ must be specified:
* `csvFile` - CSV File, Gradle's `file()` can be used to retrieve files by path relative to module location or absolute   
* `csvFileURI` - CSV file URI
* `csvGenerationCommand` - shell command which writes CSV as text to standard output.
Command string should be specified like for [Runtime#exec()](http://docs.oracle.com/javase/8/docs/api/java/lang/Runtime.html#exec-java.lang.String-).
Standard error of the command is redirected to the standard error of the process executing gradle,
so it could be seen in the gradle console.

CSV format:
* `defaultColumnName` - default=`'default'`, column name which corresponds to default localization
(`values` folder)
* `csvStrategy` - default=`null` (library default strategy, equivalent of 
[CSVStrategy.DEFAULT_STRATEGY](https://lucene.apache.org/solr/4_0_0/solr-core/org/apache/solr/internal/csv/CSVStrategy.html#DEFAULT_STRATEGY))
 - see [CSVStrategy javadoc](https://lucene.apache.org/solr/4_0_0/solr-core/org/apache/solr/internal/csv/CSVStrategy.html),
 and [sources](http://grepcode.com/file/repo1.maven.org/maven2/org.apache.solr/solr-core/4.8.0/org/apache/solr/internal/csv/CSVStrategy.java#CSVStrategy)
 since documentation is quite incomplete

The following options turn off some character escaping and substitutions, can be useful if you have 
something already escaped in CSV:
* `escapeApostrophes` - default=`true`, if set to false apostrophes (`'`) won't be escaped
* `escapeQuotes` - default=`true`, if set to false double quotes (`"`)  won't be escaped
* `escapeNewLines` - default=`true`, if set to false newline characters won't be escaped
* `convertTripleDotsToHorizontalEllipsis` - default=`true`, if set to false triple dots (`...`) won't be converted to ellipsis entity `&#8230`
* `escapeSlashes` - default=`true`, if set to false slashes (`\`) won't be escaped
* `normalizationForm` - default=[Normalizer.Form.NFC](http://docs.oracle.com/javase/8/docs/api/java/text/Normalizer.Form.html#NFC)
if set to `null` Unicode normalization won't be performed, see (javadoc of Normalizer)[http://docs.oracle.com/javase/8/docs/api/java/text/Normalizer.Form.html#NFC]
for more details
* `tagEscapingStrategy` - default=`IF_TAGS_ABSENT`, defines X(H)TML tag brackets (&lt; and &gt;) escaping strategy
possible values:
 * `ALWAYS` - brackets are always escaped. Eg. "&lt;" in source becomes "&amp;lt;" in output XML
 * `NEVER` - brackets are never escaped. Eg. "&lt;" in source is passed without change to output XML
 * `IF_TAGS_ABSENT` - Brackets aren't escaped if text contains tags. Eg.
   &lt;b&gt;bold&lt;/b&gt;} will be passed without change, but "if x&lt;4 then…" becomes "if x&amp;lt;4 then…".
   See [JSoup](http://jsoup.org) - library used to detect tags

Advanced options:
* `ignorableColumns` - default=`[]`, columns from that list will be ignored during parsing. List should
contain column names eg. `['Section', 'Notes']`
* `allowNonTranslatableTranslation` - default=`false`, if set to true resources marked
non-translatable but translated are permitted
* `allowEmptyTranslations` - default=`false`, if set to true then empty values are permitted
* `outputFileName` - default=`strings.xml`, XML file name (with extension) which should be generated as an output
* `outputIndent` - default=`  `(two spaces), character(s) used to indent each line in output XML files

##License

MIT License<br>
See [LICENSE](LICENSE) file.
