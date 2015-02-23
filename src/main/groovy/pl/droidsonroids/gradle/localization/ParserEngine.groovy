package pl.droidsonroids.gradle.localization

import groovy.xml.MarkupBuilder
import groovy.xml.MarkupBuilderHelper
import org.apache.commons.csv.CSVParser
import org.jsoup.Jsoup

import java.text.Normalizer
import java.util.regex.Pattern

import static pl.droidsonroids.gradle.localization.ResourceType.ARRAY
import static pl.droidsonroids.gradle.localization.ResourceType.PLURAL

class ParserEngine {
    private static
    final Pattern JAVA_IDENTIFIER_REGEX = Pattern.compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*");
    private static final int BUFFER_SIZE = 128 * 1024
    private final mParser
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

        final boolean isCsv

        if (config.csvGenerationCommand != null) {
            def shellCommand = config.csvGenerationCommand.split('\\s+')
            def redirect = ProcessBuilder.Redirect.INHERIT
            def process = new ProcessBuilder(shellCommand).redirectError(redirect).start()
            mCloseableInput = new InputStreamReader(process.getInputStream())
            isCsv = true
        } else if (config.csvFile != null) {
            mCloseableInput = new FileReader(config.csvFile)
            isCsv = true
        } else if (config.xlsFile != null) {
            mCloseableInput = new BufferedInputStream(new FileInputStream(config.xlsFile), BUFFER_SIZE)
            isCsv = false
        } else if (config.csvFileURI != null) {
            mCloseableInput = new InputStreamReader(new URL(config.csvFileURI).openStream())
            isCsv = true
        } else if (config.xlsFileURI != null) {
            mCloseableInput = new InputStreamReader(new URL(config.xlsFileURI).openStream())
            isCsv = false
        }

        if (isCsv) {
            mParser = config.csvStrategy ? new CSVParser(mCloseableInput, config.csvStrategy) : new CSVParser(mCloseableInput)
        } else {
            mParser = new XLSXParser(mCloseableInput, config.xlsFile.getAbsolutePath().endsWith("xls"), config.sheetName)
        }

    }

    static class SourceInfo {
        private final XMLBuilder[] mBuilders
        private final int mCommentIdx
        private final int mTranslatableIdx
        private final int mNameIdx
        private final int mColumnsCount

        SourceInfo(XMLBuilder[] builders, nameIdx, translatableIdx, commentIdx, columnsCount) {
            mBuilders = builders
            mNameIdx = nameIdx
            mTranslatableIdx = translatableIdx
            mCommentIdx = commentIdx
            mColumnsCount = columnsCount
        }
    }

    class XMLBuilder {
        final String mQualifier
        final MarkupBuilder mBuilder

        XMLBuilder(String qualifier) {
            def defaultValues = qualifier == mConfig.defaultColumnName
            String valuesDirName = defaultValues ? 'values' : 'values-' + qualifier
            File valuesDir = new File(mResDir, valuesDirName)
            if (!valuesDir.isDirectory()) {
                valuesDir.mkdirs()
            }
            File valuesFile = new File(valuesDir, mConfig.outputFileName)

            def outputStream = new BufferedOutputStream(new FileOutputStream(valuesFile), BUFFER_SIZE)
            def streamWriter = new OutputStreamWriter(outputStream, 'UTF-8')
            mBuilder = new MarkupBuilder(new IndentPrinter(streamWriter, mConfig.outputIndent))

            mBuilder.setDoubleQuotes(true)
            mBuilder.setOmitNullAttributes(true)
            mQualifier = qualifier
            mBuilder.getMkp().xmlDeclaration(version: '1.0', encoding: 'UTF-8')
        }

        def addResource(body) {//TODO add support for tools:locale
            mBuilder.resources(body/*, 'xmlns:tools':'http://schemas.android.com/tools', 'tools:locale':mQualifier*/)
        }
    }

    void parseSpreadsheet() throws IOException {
        mCloseableInput.withCloseable {
            String[][] allCells = mParser.getAllValues()
            def header = parseHeader(allCells[0])
            parseCells(header, allCells)
        }
    }

    private parseCells(final SourceInfo sourceInfo, String[][] cells) throws IOException {
        def attrs = new LinkedHashMap<>(2)
        def stringAttrs = new LinkedHashMap<>(2)
        HashMap<String, Boolean> translatableArrays = new HashMap<String, Boolean>()
        for (j in 1..sourceInfo.mBuilders.length - 1) {
            def builder = sourceInfo.mBuilders[j]
            if (builder == null) {
                continue
            }
            //the key indicate all language string
            def keys = new HashSet(cells.length)
            builder.addResource({
                def pluralsMap = new HashMap<String, HashSet<PluralItem>>()
                def arrays = new HashMap<String, List<StringArrayItem>>()
                for (i in 0..cells.length - 1) {
                    String[] row = cells[i]
                    if (row == null) {
                        continue
                    }
                    if (row.length < sourceInfo.mColumnsCount) {
                        String[] extendedRow = new String[sourceInfo.mColumnsCount]
                        System.arraycopy(row, 0, extendedRow, 0, row.length)
                        for (k in row.length..sourceInfo.mColumnsCount - 1)
                            extendedRow[k] = ''
                        row = extendedRow
                    }

                    String name = row[sourceInfo.mNameIdx]
                    def value = row[j]

                    String comment = null
                    if (sourceInfo.mCommentIdx >= 0 && !row[sourceInfo.mCommentIdx].isEmpty()) {
                        comment = row[sourceInfo.mCommentIdx]
                    }
                    def indexOfOpeningBrace = name.indexOf('[')
                    def indexOfClosingBrace = name.indexOf(']')
                    String indexValue
                    ResourceType resourceType
                    if (indexOfOpeningBrace > 0 && indexOfClosingBrace == name.length() - 1) {
                        indexValue = name.substring(indexOfOpeningBrace + 1, indexOfClosingBrace)
                        resourceType = indexValue.isEmpty() ? ARRAY : PLURAL
                        name = name.substring(0, indexOfOpeningBrace)
                    } else {
                        resourceType = ResourceType.STRING
                        indexValue = null
                    }

                    def translatable = true
                    if (resourceType == ResourceType.STRING || resourceType == ARRAY) {
                        stringAttrs['name'] = name //TODO not used by array, optimize?
                        if (sourceInfo.mTranslatableIdx >= 0) {
                            translatable = !row[sourceInfo.mTranslatableIdx].equalsIgnoreCase('false')
                            if (resourceType == ARRAY) {
                                translatable &= translatableArrays.get(name, true)
                                translatableArrays[name] = translatable
                            } else
                                stringAttrs['translatable'] = translatable ? null : 'false'
                        }
                        if (value.isEmpty()) {
                            if (!translatable && builder.mQualifier != mConfig.defaultColumnName)
                                continue
                            if (!mConfig.allowEmptyTranslations)
                                throw new IOException(name + " is not translated to locale " + builder.mQualifier + ", row #" + (i + 2))
                        } else {
                            if (!translatable && !mConfig.allowNonTranslatableTranslation && builder.mQualifier != mConfig.defaultColumnName)
                                throw new IOException(name + " is translated but marked translatable='false', row #" + (i + 2))
                        }
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
                        throw new IOException(name + " is not valid name, row #" + (i + 2))
                    }
                    if (resourceType == PLURAL || resourceType == ARRAY) {
                        //TODO require only one translatable value for all list?
                        if (resourceType == ARRAY) {
                            def stringList = arrays.get(name, [])
                            stringList += new StringArrayItem(value, comment)
                            arrays[name] = stringList
                        } else {
                            Quantity pluralQuantity = Quantity.valueOf(indexValue)
                            //                        if (!Quantity.values().contains(pluralQuantity))
                            //                            throw new IOException(pluralQuantity + " is not valid quantity, row #" + (i + 2))
                            HashSet<PluralItem> quantitiesSet = pluralsMap.get(name, [] as HashSet)
                            if (!value.isEmpty()) {
                                if (!quantitiesSet.add(new PluralItem(pluralQuantity, value, comment)))
                                    throw new IOException(name + " is duplicated in row #" + (i + 2))
                            }
                        }
                        continue
                    }
                    if (!keys.add(name)) {
                        if (mConfig.skipDuplicatedName)
                            continue
                        throw new IOException(name + " is duplicated in row #" + (i + 2))
                    }
                    string(stringAttrs) {
                        yieldValue(mkp, value)
                    }
                    if (comment) {
                        mkp.comment(comment)
                    }
                    for (Map.Entry<String, HashSet<PluralItem>> entry : pluralsMap) {
                        plurals([name: entry.key]) {
                            if (entry.value.isEmpty())
                                throw new IOException("At least one quantity string must be defined for key: "
                                        + entry.key + ", qualifier " + builder.mQualifier)
                            for (PluralItem quantityEntry : entry.value) {
                                item(quantity: quantityEntry.quantity) {
                                    yieldValue(mkp, quantityEntry.value)
                                }
                                if (quantityEntry.comment)
                                    mkp.comment(quantityEntry.comment)
                            }
                        }
                    }
                    for (Map.Entry<String, List<StringArrayItem>> entry : arrays) {
                        'string-array'([name: entry.key, translatable: translatableArrays[entry.key] ? null : 'false']) {
                            for (StringArrayItem stringArrayItem : entry.value) {
                                item {
                                    yieldValue(mkp, stringArrayItem.value)
                                }
                                if (stringArrayItem.comment)
                                    mkp.comment(stringArrayItem.comment)
                            }
                        }
                    }
                }
            });
        }
    }

    private void yieldValue(MarkupBuilderHelper mkp, String value) {
        if (mConfig.tagEscapingStrategy == TagEscapingStrategy.ALWAYS ||
                (mConfig.tagEscapingStrategy == TagEscapingStrategy.IF_TAGS_ABSENT &&
                        Jsoup.parse(value).body().children().isEmpty()))
            mkp.yield(value)
        else
            mkp.yieldUnescaped(value)
    }

    private SourceInfo parseHeader(String[] headerLine) throws IOException {
        if (headerLine == null || headerLine.size() < 2)
            throw new IOException("Invalid CSV header: " + headerLine)
        List<String> header = Arrays.asList(headerLine)
        def keyIdx = header.indexOf(mConfig.nameColumnName)
        if (keyIdx == -1) {
            throw new IOException("'name' column not present")
        }
        if (header.indexOf(mConfig.defaultColumnName) == -1) {
            throw new IOException("Default locale column not present")
        }
        def builders = new XMLBuilder[header.size()]

        def reservedColumns = [mConfig.nameColumnName, mConfig.commentColumnName, mConfig.translatableColumnName]
        reservedColumns.addAll(mConfig.ignorableColumns)
        def i = 0
        for (columnName in header) {
            if (!(columnName in reservedColumns)) {
                builders[i] = new XMLBuilder(columnName)
            }
            i++
        }

        def translatableIdx = header.indexOf(mConfig.translatableColumnName)
        def commentIdx = header.indexOf(mConfig.commentColumnName)
        new SourceInfo(builders, keyIdx, translatableIdx, commentIdx, header.size())
    }
}
