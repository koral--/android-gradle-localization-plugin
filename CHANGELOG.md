### 1.0.12
- `handleEmptyTranslationsAsDefault` config parameter added - [#36](https://github.com/koral--/android-gradle-localization-plugin/pull/36)

### 1.0.11
- multisheet support added - [#33](https://github.com/koral--/android-gradle-localization-plugin/pull/33)
- dependencies versions bump
- treating CDATA sections like tags in terms of `tagEscapingStrategy` so they are not escaped in `IF_TAGS_ABSENT` mode

### 1.0.10
- `nameColumnIndex` extension property added #23
- dependencies versions bump
- `tagEscapingStrategyColumnName` added

### 1.0.9
- task description added
- header parsing fixed

### 1.0.8
- XLS(X) input support added
- non-translatable plurals support added
- `tools:locale` support added
- ability to skip invalid and/or duplicated key names added
- `formatted` attribute support added
- ability to specify all custom column names added

Changes also contain documentation updates, typofixes, and trivial code clean-ups.
