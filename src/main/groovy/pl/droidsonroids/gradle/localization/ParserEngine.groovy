package pl.droidsonroids.gradle.localization

import groovy.xml.MarkupBuilderHelper

import java.text.Normalizer
import java.util.regex.Pattern

import static pl.droidsonroids.gradle.localization.ResourceType.ARRAY
import static pl.droidsonroids.gradle.localization.ResourceType.PLURAL
import static pl.droidsonroids.gradle.localization.TagEscapingStrategy.ALWAYS
import static pl.droidsonroids.gradle.localization.TagEscapingStrategy.IF_TAGS_ABSENT

class ParserEngine {
    private static
    final Pattern JAVA_IDENTIFIER_REGEX = Pattern.compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*")
    private final Parser mParser
    private final ConfigExtension mConfig
    private final File mResDir
    private final Closeable mCloseableInput

    ParserEngine(ConfigExtension config, File resDir) {
        def csvSources = [config.csvFileURI, config.csvFile, config.csvGenerationCommand,
                          config.xlsFile, config.xlsFileURI] as Set
        csvSources.remove(null)
        if (csvSources.size() != 1) {
            throw new IllegalArgumentException("Exactly one source must be defined")
        }
        mResDir = resDir
        mConfig = config

        final SourceType sourceType

        if (config.csvGenerationCommand != null) {
            def shellCommand = config.csvGenerationCommand.split('\\s+')
            def redirect = ProcessBuilder.Redirect.INHERIT
            def process = new ProcessBuilder(shellCommand).redirectError(redirect).start()
            mCloseableInput = Utils.wrapReader(new InputStreamReader(process.inputStream))
            sourceType = SourceType.CSV
        } else if (config.csvFile != null) {
            mCloseableInput = Utils.wrapReader(new FileReader(config.csvFile))
            sourceType = SourceType.CSV
        } else if (config.csvFileURI != null) {
            mCloseableInput = Utils.wrapReader(new InputStreamReader(new URL(config.csvFileURI).openStream()))
            sourceType = SourceType.CSV
        } else if (config.xlsFileURI != null) {
            final url = new URL(config.xlsFileURI)
            mCloseableInput = Utils.wrapInputStream(url.openStream())
            sourceType = url.path.endsWith("xls") ? SourceType.XLS : SourceType.XLSX
        } else if (config.xlsFile != null) {
            mCloseableInput = Utils.wrapInputStream(new FileInputStream(config.xlsFile))
            sourceType = config.xlsFile.absolutePath.endsWith("xls") ? SourceType.XLS : SourceType.XLSX
        } else {
            throw new IllegalStateException()
        }

        if (sourceType == SourceType.CSV) {
            def reader = (Reader) mCloseableInput
            mParser = config.csvStrategy ? new CSVInnerParser(reader, config.csvStrategy) : new CSVInnerParser(reader)
        } else {
            def isXLS = sourceType == SourceType.XLS
            mParser = new XLSXParser((InputStream) mCloseableInput, isXLS, config.sheetName, config.useAllSheets)
        }
    }

    void parseSpreadsheet() {
        mCloseableInput.withCloseable {
            Map<String, String[][]> sheets = mParser.getResult()
            for (String sheetName: sheets.keySet()) {
                String[][] allCells = sheets.get(sheetName)
                String outputFileName
                if (sheetName != null) {
                    outputFileName = sheetName + ".xml"
                } else {
                    outputFileName = mConfig.outputFileName
                }
                def header = new SourceInfo(allCells[0], mConfig, mResDir, outputFileName)
                parseCells(header, allCells)
            }
        }
    }

    private parseCells(final SourceInfo sourceInfo, String[][] cells) {
        HashMap<String, Boolean> translatableArrays = new HashMap<String, Boolean>()
        for (j in 1..sourceInfo.mBuilders.length - 1) {
            def builder = sourceInfo.mBuilders[j]
            if (builder == null) {
                continue
            }

            def keys = new HashSet(cells.length)
            builder.addResource({
                if (cells.length <= 1)
                    return
                def stringAttrs = new LinkedHashMap<>(2)
                def pluralsMap = new HashMap<String, HashSet<PluralItem>>()
                def arrays = new HashMap<String, List<StringArrayItem>>()
                for (i in 1..cells.length - 1) {
                    String[] row = cells[i]
                    if (row == null) {
                        continue
                    }
                    if (row.length < sourceInfo.mColumnsCount) {
                        String[] extendedRow = new String[sourceInfo.mColumnsCount]
                        System.arraycopy(row, 0, extendedRow, 0, row.length)
                        for (int k in row.length..sourceInfo.mColumnsCount - 1)
                            extendedRow[k] = ''
                        row = extendedRow
                    }

                    String name = row[sourceInfo.mNameIdx]
                    def value = row[j]

                    String comment = null
                    if (sourceInfo.mCommentIndex >= 0 && !row[sourceInfo.mCommentIndex].empty) {
                        comment = row[sourceInfo.mCommentIndex]
                    }
                    def indexOfOpeningBrace = name.indexOf('[')
                    def indexOfClosingBrace = name.indexOf(']')
                    String indexValue
                    ResourceType resourceType
                    if (indexOfOpeningBrace > 0 && indexOfClosingBrace == name.length() - 1) {
                        indexValue = name.substring(indexOfOpeningBrace + 1, indexOfClosingBrace)
                        resourceType = indexValue.empty ? ARRAY : PLURAL
                        name = name.substring(0, indexOfOpeningBrace)
                    } else {
                        resourceType = ResourceType.STRING
                        indexValue = null
                    }

                    def translatable = true
                    stringAttrs['name'] = name
                    if (sourceInfo.mTranslatableIndex >= 0) {
                        translatable = !row[sourceInfo.mTranslatableIndex].equalsIgnoreCase('false')
                        if (resourceType == ARRAY) {
                            translatable &= translatableArrays.get(name, true)
                            translatableArrays[name] = translatable
                        } else
                            stringAttrs['translatable'] = translatable ? null : 'false'
                    }
                    if (sourceInfo.mFormattedIndex >= 0) {
                        def formatted = !row[sourceInfo.mFormattedIndex].equalsIgnoreCase('false')
                        stringAttrs['formatted'] = formatted ? null : 'false'
                    }
                    if (value.empty) {
                        if (!translatable && builder.mQualifier != mConfig.defaultColumnName)
                            continue
                        if (!mConfig.allowEmptyTranslations)
                            throw new IllegalArgumentException("$name is not translated to locale $builder.mQualifier, row #${i + 1}")
                    } else {
                        if (!translatable && !mConfig.allowNonTranslatableTranslation && builder.mQualifier != mConfig.defaultColumnName)
                            throw new IllegalArgumentException("$name is translated but marked translatable='false', row #${i + 1}")
                    }
                    if (mConfig.escapeSlashes)
                        value = value.replace("\\", "\\\\")
                    if (mConfig.escapeApostrophes)
                        value = value.replace("'", "\\'")
                    if (mConfig.escapeQuotes) //TODO don't escape tag attribute values
                        value = value.replace("\"", "\\\"")
                    if (mConfig.escapeNewLines)
                        value = value.replace("\n", "\\n")
                    if (value.startsWith(' ') || value.endsWith(' '))
                        value = '"' + value + '"'
                    if (mConfig.convertTripleDotsToHorizontalEllipsis)
                        value = value.replace("...", "…")
                    value = value.replace("?", "\\?")
                    if (mConfig.normalizationForm)
                        value = Normalizer.normalize(value, mConfig.normalizationForm)

                    if (!JAVA_IDENTIFIER_REGEX.matcher(name).matches()) {
                        if (mConfig.skipInvalidName)
                            continue
                        throw new IllegalArgumentException("$name is not valid name, row #${i + 1}")
                    }
                    if (resourceType == PLURAL || resourceType == ARRAY) {
                        //TODO require only one translatable value for all list?
                        if (resourceType == ARRAY) {
                            def stringList = arrays.get(name, [])
                            stringList += new StringArrayItem(value, comment)
                            arrays[name] = stringList
                        } else {
                            Quantity pluralQuantity = Quantity.valueOf(indexValue)
                            if (!Quantity.values().contains(pluralQuantity))
                                throw new IllegalArgumentException("${pluralQuantity.name()} is not valid quantity, row #${i + 1}")
                            HashSet<PluralItem> quantitiesSet = pluralsMap.get(name, [] as HashSet)
                            if (!value.empty) {
                                if (!quantitiesSet.add(new PluralItem(pluralQuantity, value, comment)))
                                    throw new IllegalArgumentException("$name is duplicated in row #${i + 1}")
                            }
                        }
                        continue
                    }
                    if (!keys.add(name)) {
                        //TODO support case when first occurrence is marked non-translatable
                        if (mConfig.skipDuplicatedName)
                            continue
                        throw new IllegalArgumentException("$name is duplicated in row #${i + 1}")
                    }
                    TagEscapingStrategy strategy = mConfig.tagEscapingStrategy
                    if (sourceInfo.mTagEscapingStrategyIndex >= 0) {
                        def strategyName = row[sourceInfo.mTagEscapingStrategyIndex]
                        if (strategyName) {
                            strategy = TagEscapingStrategy.valueOf(strategyName)
                        }
                    }
                    string(stringAttrs) {
                        yieldValue(mkp, value, strategy)
                    }
                    if (comment) {
                        mkp.comment(comment)
                    }
                }
                pluralsMap.each { key, value ->
                    plurals([name: key]) {
                        if (value.empty)
                            throw new IllegalArgumentException("At least one quantity string must be defined for key: $key, qualifier $builder.mQualifier")
                        value.each { quantityEntry ->
                            item(quantity: quantityEntry.quantity) {
                                yieldValue(mkp, quantityEntry.value)
                            }
                            if (quantityEntry.comment)
                                mkp.comment(quantityEntry.comment)
                        }
                    }
                }
                arrays.each { key, value ->
                    'string-array'([name: key, translatable: translatableArrays[key] ? null : 'false']) {
                        value.each { stringArrayItem ->
                            item {
                                yieldValue(mkp, stringArrayItem.value)
                            }
                            if (stringArrayItem.comment)
                                mkp.comment(stringArrayItem.comment)
                        }
                    }
                }
            })
        }
    }

    private void yieldValue(MarkupBuilderHelper mkp, String value, TagEscapingStrategy strategy = mConfig.tagEscapingStrategy) {
        if (strategy == ALWAYS || (strategy == IF_TAGS_ABSENT && Utils.containsHTML(value)))
            mkp.yield(value)
        else
            mkp.yieldUnescaped(value)
    }
}
