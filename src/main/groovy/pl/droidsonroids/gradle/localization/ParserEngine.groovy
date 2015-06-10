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

    enum SourceType {
        CSV, XLS, XLSX
    }

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
            mCloseableInput = wrapReader(new InputStreamReader(process.getInputStream()))
            sourceType = SourceType.CSV
        } else if (config.csvFile != null) {
            mCloseableInput = wrapReader(new FileReader(config.csvFile))
            sourceType = SourceType.CSV
        } else if (config.csvFileURI != null) {
            mCloseableInput = wrapReader(new InputStreamReader(new URL(config.csvFileURI).openStream()))
            sourceType = SourceType.CSV
        } else if (config.xlsFileURI != null) {
            final url = new URL(config.xlsFileURI)
            mCloseableInput = wrapInputStream(url.openStream())
            sourceType = url.getPath().endsWith("xls") ? SourceType.XLS : SourceType.XLSX
        } else if (config.xlsFile != null) {
            mCloseableInput = wrapInputStream(new FileInputStream(config.xlsFile))
            sourceType = config.xlsFile.getAbsolutePath().endsWith("xls") ? SourceType.XLS : SourceType.XLSX
        } else {
            throw new IllegalStateException()
        }

        if (sourceType == SourceType.CSV) {
            mParser = config.csvStrategy ? new CSVParser((Reader) mCloseableInput, config.csvStrategy) : new CSVParser((Reader) mCloseableInput)
        } else {
            mParser = new XLSXParser((InputStream) mCloseableInput, sourceType == SourceType.XLS, config.sheetName)
        }
    }

    private static BufferedReader wrapReader(Reader reader) {
        new BufferedReader(reader, BUFFER_SIZE)
    }

    private static BufferedInputStream wrapInputStream(InputStream inputStream) {
        new BufferedInputStream(inputStream, BUFFER_SIZE)
    }

    static class SourceInfo {
        private final XMLBuilder[] mBuilders
        private final int mCommentIdx
        private final int mTranslatableIdx
        private final int mFormattedIdx
        private final int mNameIdx
        private final int mColumnsCount

        SourceInfo(XMLBuilder[] builders, nameIdx, translatableIdx, commentIdx, formattedIdx, columnsCount) {
            mBuilders = builders
            mNameIdx = nameIdx
            mTranslatableIdx = translatableIdx
            mCommentIdx = commentIdx
            mFormattedIdx = formattedIdx
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

        def addResource(body) {
            if (mQualifier == mConfig.defaultColumnName && mConfig.defaultLocaleQualifier != null)
                mBuilder.resources(body, 'xmlns:tools': 'http://schemas.android.com/tools', 'tools:locale': mConfig.defaultLocaleQualifier)
            else
                mBuilder.resources(body)
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
        HashMap<String, Boolean> translatableArrays = new HashMap<String, Boolean>()
        for (j in 1..sourceInfo.mBuilders.length - 1) {
            def builder = sourceInfo.mBuilders[j]
            if (builder == null) {
                continue
            }
            //the key indicate all language string
            def keys = new HashSet(cells.length)
            builder.addResource({
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
                    stringAttrs['name'] = name
                    if (sourceInfo.mTranslatableIdx >= 0) {
                        translatable = !row[sourceInfo.mTranslatableIdx].equalsIgnoreCase('false')
                        if (resourceType == ARRAY) {
                            translatable &= translatableArrays.get(name, true)
                            translatableArrays[name] = translatable
                        } else
                            stringAttrs['translatable'] = translatable ? null : 'false'
                    }
                    if (sourceInfo.mFormattedIdx >= 0) {
                        def formatted = !row[sourceInfo.mFormattedIdx].equalsIgnoreCase('false')
                        stringAttrs['formatted'] = formatted ? null : 'false'
                    }
                    if (value.isEmpty()) {
                        if (!translatable && builder.mQualifier != mConfig.defaultColumnName)
                            continue
                        if (!mConfig.allowEmptyTranslations)
                            throw new IOException(name + " is not translated to locale " + builder.mQualifier + ", row #" + (i + 1))
                    } else {
                        if (!translatable && !mConfig.allowNonTranslatableTranslation && builder.mQualifier != mConfig.defaultColumnName)
                            throw new IOException(name + " is translated but marked translatable='false', row #" + (i + 1))
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
                        throw new IOException(name + " is not valid name, row #" + (i + 1))
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
                                throw new IOException(pluralQuantity.name() + " is not valid quantity, row #" + (i + 1))
                            HashSet<PluralItem> quantitiesSet = pluralsMap.get(name, [] as HashSet)
                            if (!value.isEmpty()) {
                                if (!quantitiesSet.add(new PluralItem(pluralQuantity, value, comment)))
                                    throw new IOException(name + " is duplicated in row #" + (i + 1))
                            }
                        }
                        continue
                    }
                    if (!keys.add(name)) {
                        if (mConfig.skipDuplicatedName)
                            continue
                        throw new IOException(name + " is duplicated in row #" + (i + 1))
                    }
                    string(stringAttrs) {
                        yieldValue(mkp, value)
                    }
                    if (comment) {
                        mkp.comment(comment)
                    }
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

        def reservedColumns = [mConfig.nameColumnName, mConfig.commentColumnName, mConfig.translatableColumnName, mConfig.formattedColumnName]
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
        def formattedIdx = header.indexOf(mConfig.formattedColumnName)
        new SourceInfo(builders, keyIdx, translatableIdx, commentIdx, formattedIdx, header.size())
    }
}
