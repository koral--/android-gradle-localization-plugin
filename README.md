android-gradle-localization-plugin
==================================
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/pl.droidsonroids.gradle.localization/android-gradle-localization-plugin/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/pl.droidsonroids.gradle.localization/android-gradle-localization-plugin)[ ![Bintray](https://api.bintray.com/packages/koral/maven/android-gradle-localization-plugin/images/download.svg) ](https://bintray.com/koral/maven/android-gradle-localization-plugin/_latestVersion) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-android--gradle--localization--plugin-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/902)  [![Build Status](https://travis-ci.org/koral--/android-gradle-localization-plugin.svg?branch=master)](https://travis-ci.org/koral--/android-gradle-localization-plugin)

Gradle plugin for generating localized string resources

## Overview
This plugin generates Android string resource XML files from CSV or XLS(X) file.
Generation has to be invoked as additional gradle task. Java 1.8 is required.
 
##Supported features
 * string arrays
 * plurals
 * non-translatable resources - `translatable` XML attribute
 * auto-escaping double quotes, apostrophes and newlines
 * auto-quoting leading and trailing spaces
 * syntax validation - duplicated, empty, invalid names detection
 * comments
 * formatted strings - `formatted` XML attribute
 * default locale specification - `tools:locale`
  
## Applying plugin
### Gradle 2.1+
In whichever `build.gradle` file.
```
plugins {
  id 'pl.droidsonroids.localization' version '1.0.8'
}
```
Note: exact version number must be specified, `+` cannot be used as wildcard.

### All versions of Gradle
1. Add dependency to the __top-level__ `build.gradle` file.

 ```
  buildscript {
     repositories {
         mavenCentral()
         jcenter()
     }
     dependencies {
         classpath 'com.android.tools.build:gradle:1.2.3'
         classpath 'pl.droidsonroids.gradle.localization:android-gradle-localization-plugin:1.0.8'
     }
 }
 ```
 Note: `mavenCentral()` and/or `jcenter()` repository can be specified, `+` can be used as wildcard in version number.
2. Apply plugin and add configuration to `build.gradle` of the application, eg:
 ```
 apply plugin: 'pl.droidsonroids.localization'
 ```
 
## Usage
Invoke `localization` gradle task. Task may be invoked from commandline or from Android Studio GUI.
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
Add `localization` extension in `build.gradle` of particular module.
 ```
 localization
     {
         csvFile=file('translations.csv')
         OR
         csvFileURI='https://docs.google.com/spreadsheets/d/<key>/export?format=csv'
         OR
         csvGenerationCommand='/usr/bin/xlsx2csv translation.xlsx'
         OR
         xlsFile=file('translations.xlsx')
         OR
         xlsFileURI='https://docs.google.com/spreadsheets/d/<key>/export?format=xlsx'
     }
 ```
* `csvFileURI` and `xlsFileURI` can be any valid URI, not necessarily Google Docs' one
* `xlsFile` and `xlsFileURI` accepts both XLSX and XLS files. If filename ends with `xls` file will
    be treated as XLS, XLSX otherwise

Sources,  __exactly one of them__ must be specified:
* `csvFile`, `xlsFile` - CSV/XLS(X) file, Gradle's `file()` can be used to retrieve files by path relative to module location or absolute
* `csvFileURI`, `xlsFileURI` - CSV/XLS(X) file URI
* `csvGenerationCommand` - shell command which writes CSV as text to standard output.
Command string should be specified like for [Runtime#exec()](http://docs.oracle.com/javase/8/docs/api/java/lang/Runtime.html#exec-java.lang.String-).
Standard error of the command is redirected to the standard error of the process executing gradle,
so it could be seen in the gradle console.

#### Spreadsheet format:
* `defaultColumnName` - default=`'default'`, name of the column which corresponds to default localization
(`values` folder)
* `nameColumnName` - default=`'name'`, name of the column containing key names (source for the `name`
XML attribute)
* `translatableColumnName` - default=`'translatable'`, name of the column containing translatable flags
(source for the `translatable` XML attribute)
* `commentColumnName` - default=`'comment'`, name of the column containing comments
* `formattedColumnName` - default=`'formatted'`, name of the column formatted flags
                                                                    (source for the `formatted` XML attribute)

The following options turn off some character escaping and substitutions, can be useful if you have 
something already escaped in CSV:
* `escapeApostrophes` - default=`true`, if set to false apostrophes (`'`) won't be escaped
* `escapeQuotes` - default=`true`, if set to false double quotes (`"`)  won't be escaped
* `escapeNewLines` - default=`true`, if set to false newline characters won't be escaped
* `convertTripleDotsToHorizontalEllipsis` - default=`true`, if set to false triple dots (`...`) won't be converted to ellipsis entity `&#8230`
* `escapeSlashes` - default=`true`, if set to false slashes (`\`) won't be escaped
* `normalizationForm` - default=[Normalizer.Form.NFC](http://docs.oracle.com/javase/8/docs/api/java/text/Normalizer.Form.html#NFC) if set to `null` Unicode normalization won't be performed, see [javadoc of Normalizer](http://docs.oracle.com/javase/8/docs/api/java/text/Normalizer.Form.html#NFC)
for more details
* `tagEscapingStrategy` - default=`IF_TAGS_ABSENT`, defines X(H)TML tag brackets (&lt; and &gt;) escaping strategy
possible values:
 * `ALWAYS` - brackets are always escaped. Eg. "&lt;" in source becomes "&amp;lt;" in output XML
 * `NEVER` - brackets are never escaped. Eg. "&lt;" in source is passed without change to output XML
 * `IF_TAGS_ABSENT` - Brackets aren't escaped if text contains tags. Eg.
   &lt;b&gt;bold&lt;/b&gt;} will be passed without change, but "if x&lt;4 then…" becomes "if x&amp;lt;4 then…".
   See [JSoup](http://jsoup.org) - library used to detect tags

#### CSV format:
* `csvStrategy` - default=`null` (library default strategy, equivalent of
[CSVStrategy.DEFAULT_STRATEGY](https://lucene.apache.org/solr/4_0_0/solr-core/org/apache/solr/internal/csv/CSVStrategy.html#DEFAULT_STRATEGY))
 - see [CSVStrategy javadoc](https://lucene.apache.org/solr/4_0_0/solr-core/org/apache/solr/internal/csv/CSVStrategy.html),
 and [sources](http://grepcode.com/file/repo1.maven.org/maven2/org.apache.solr/solr-core/4.8.0/org/apache/solr/internal/csv/CSVStrategy.java#CSVStrategy)
 since documentation is quite incomplete

#### XLS(X) format:
* `sheetName` - default=`<name of the first sheet>`, name of the sheet to be parsed, only one can be specified

#### Advanced options:
* `ignorableColumns` - default=`[]`, columns from that list will be ignored during parsing. List should
contain column names eg. `['Section', 'Notes']`
* `allowNonTranslatableTranslation` - default=`false`, if set to true resources marked
non-translatable but translated are permitted
* `allowEmptyTranslations` - default=`false`, if set to true then empty values are permitted
* `outputFileName` - default=`strings.xml`, XML file name (with extension) which should be generated as an output
* `outputIndent` - default=`  `(two spaces), character(s) used to indent each line in output XML files
* `skipInvalidName` - default=`false`, if set to true then rows with invalid key names will be ignored instead
of throwing an exception
* `skipDuplicatedName` - default=`false`, if set to true then rows with duplicated key names will be ignored instead
of throwing an exception. First rows with given key will be taken into account.
* `defaultLocaleQualifier` - language (eg. `es`) and optionally region (eg. `es_US`) ISO codes of default translations.
 Default=`null`(unset) which effectively means English `en`, if set then value will be placed in `tools:locale`
 XML attribute. See [Tools Attributes](http://tools.android.com/tech-docs/tools-attributes#TOC-tools:locale)
 for more information.

#### Migration from versions <1.0.7:
Versions older than 1.0.7 provided `escapeBoundarySpaces` option, which defaulted to true. Currently
strings are always escaped when corresponding **parsed**  ceil contains leading or trailing spaces,
but such spaces are stripped by default CSV strategy. So effectively strings are trimmed by default.
If you want to include mentioned spaces in output set appropriate `csvStrategy`.

##License

MIT License<br>
See [LICENSE](LICENSE) file.
