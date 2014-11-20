package pl.droidsonroids.gradle.localization

import groovy.xml.MarkupBuilder
import groovy.xml.MarkupBuilderHelper
import org.apache.commons.csv.CSVParser
import org.jsoup.Jsoup

import java.text.Normalizer
import java.util.regex.Pattern

import static pl.droidsonroids.gradle.localization.ResourceType.ARRAY
import static pl.droidsonroids.gradle.localization.ResourceType.PLURAL
import static pl.droidsonroids.gradle.localization.ResourceType.STRING

/**
 * Class containing CSV parser logic
 */
class Parser {
    private static
    final Pattern JAVA_IDENTIFIER_REGEX = Pattern.compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*");
    private static final String NAME = "name", TRANSLATABLE = "translatable", COMMENT = "comment"
    private static final int BUFFER_SIZE = 128 * 1024
    private final CSVParser mParser
    private final ConfigExtension mConfig
    private final File mResDir
    private final Reader mReader

    Parser(ConfigExtension config, File resDir) {
        def csvSources = [config.csvFileURI, config.csvFile, config.csvGenerationCommand] as Set
        csvSources.remove(null)
        if (csvSources.size() != 1)
            throw new IllegalArgumentException("Exactly one source must be defined")
        Reader reader
        if (config.csvGenerationCommand != null) {
            def split = config.csvGenerationCommand.split('\\s+')
            def redirect = ProcessBuilder.Redirect.INHERIT
            def process = new ProcessBuilder(split).redirectError(redirect).start()
            reader = new InputStreamReader(process.getInputStream())
        } else if (config.csvFile != null) {
            reader = new FileReader(config.csvFile)
        } else { // if (config.csvFileURI!=null)
            reader = new InputStreamReader(new URL(config.csvFileURI).openStream())
        }

        mReader = reader
        mResDir = resDir

        def parser = new CSVParser(reader, config.csvStrategy)
        mParser = config.csvStrategy ? parser : new CSVParser(reader)
        mConfig = config
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

    void parseCSV() throws IOException {
        mReader.withReader {
            parseCells(parseHeader(mParser))
        }
    }


    private parseCells(final SourceInfo sourceInfo) throws IOException {
        String[][] cells = mParser.getAllValues()
        def stringAttrs = new LinkedHashMap<>(2)

        for (j in 0..sourceInfo.mBuilders.length - 1) {
            XMLBuilder builder = sourceInfo.mBuilders[j]
            if (builder == null) {
                continue
            }
            def keys = new HashSet(cells.length)
            builder.addResource({
                def pluralsMap = new HashMap<TranslatableNode, List<PluralItem>>()
                def arrays = new HashMap<TranslatableNode, List<StringArrayItem>>()
                for (i in 0..cells.length - 1) {
                    String[] row = cells[i]
                    if (row.length < sourceInfo.mColumnsCount) {
                        String[] extendedRow = new String[sourceInfo.mColumnsCount]
                        System.arraycopy(row, 0, extendedRow, 0, row.length)
                        for (k in row.length..sourceInfo.mColumnsCount - 1)
                            extendedRow[k] = ''
                        row = extendedRow
                    }
                    String name = row[sourceInfo.mNameIdx]
                    String value = row[j]
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
                    } else {
                        resourceType = STRING
                        indexValue = null
                    }

                    def translatable = true
                    if (resourceType == STRING || resourceType == ARRAY) {
                        if (sourceInfo.mTranslatableIdx >= 0) {
                            translatable = !row[sourceInfo.mTranslatableIdx].equalsIgnoreCase('false')
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
                        stringAttrs['name']= name
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
                    if (mConfig.normalizationForm)
                        value = Normalizer.normalize(value, mConfig.normalizationForm)

                    if (resourceType == PLURAL || resourceType == ARRAY) {
                        name = name.substring(0, indexOfOpeningBrace)
                        if (!JAVA_IDENTIFIER_REGEX.matcher(name).matches()) {
                            throw new IOException(name + " is not valid name, row #" + (i + 2))
                        }

                        def translatableNode = new TranslatableNode(name, translatable) //TODO or all translatable values
                        if (resourceType == ARRAY) {
                            List<StringArrayItem> stringList = arrays.get(translatableNode, [])
                            stringList += new StringArrayItem(value, comment)
                            arrays[translatableNode] = stringList
                        } else {
                            Quantity pluralQuantity = Quantity.valueOf(indexValue)
                            //                        if (!Quantity.values().contains(pluralQuantity))
                            //                            throw new IOException(pluralQuantity + " is not valid quantity, row #" + (i + 2))
                            List<PluralItem> quantitiesList = pluralsMap.get(translatableNode, [])
                            if (!value.isEmpty())
                                quantitiesList += new PluralItem(pluralQuantity, value, comment)
                            pluralsMap[translatableNode] = quantitiesList
                        }
                        continue
                    } else if (!JAVA_IDENTIFIER_REGEX.matcher(name).matches())
                        throw new IOException(name + " is not valid name, row #" + (i + 2))
                    if (!keys.add(name))
                        throw new IOException(name + " is duplicated in row #" + (i + 2))

                    string(stringAttrs) {
                        yieldValue(mkp, value)
                    }
                    if (comment) {
                        mkp.comment(comment)
                    }
                }
                for (Map.Entry<TranslatableNode, List<PluralItem>> entry : pluralsMap) {
                    plurals([name: entry.key.name, translatable: entry.key.translatable ? null : 'false']) {
                        if (entry.value.isEmpty())
                            throw new IOException("At least one quantity string must be defined for key: "
                                    + entry.key.name + ", qualifier " + builder.mQualifier)
                        for (PluralItem quantityEntry : entry.value) {
                            item(quantity: quantityEntry.quantity) {
                                yieldValue(mkp, quantityEntry.value)
                            }
                            if (quantityEntry.comment)
                                mkp.comment(quantityEntry.comment)
                        }
                    }
                }
                for (Map.Entry<TranslatableNode, List<StringArrayItem>> entry : arrays) {
                    'string-array'([name: entry.key.name, translatable: entry.key.translatable ? null : 'false']) {
                        for (StringArrayItem stringArrayItem : entry.value) {
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

    private void yieldValue(MarkupBuilderHelper mkp, String value) {
        if (mConfig.tagEscapingStrategy == TagEscapingStrategy.ALWAYS ||
                (mConfig.tagEscapingStrategy == TagEscapingStrategy.IF_TAGS_ABSENT &&
                        Jsoup.parse(value).body().children().isEmpty()))
            mkp.yield(value)
        else
            mkp.yieldUnescaped(value)
    }

    private SourceInfo parseHeader(CSVParser mParser) throws IOException {
        def headerLine = mParser.getLine()
        if (headerLine == null || headerLine.size() < 2)
            throw new IOException("Invalid CSV header: " + headerLine)
        List<String> header = Arrays.asList(headerLine)
        def keyIdx = header.indexOf(NAME)
        if (keyIdx == -1)
            throw new IOException("'name' column not present")
        if (header.indexOf(mConfig.defaultColumnName) == -1)
            throw new IOException("Default locale column not present")
        def builders = new XMLBuilder[header.size()]

        def reservedColumns = [NAME, COMMENT, TRANSLATABLE]
        reservedColumns.addAll(mConfig.ignorableColumns)
        def i = 0
        for (columnName in header) {
            if (!(columnName in reservedColumns)) {
                builders[i] = new XMLBuilder(columnName)
            }
            i++
        }

        def translatableIdx = header.indexOf(TRANSLATABLE)
        def commentIdx = header.indexOf(COMMENT)
        new SourceInfo(builders, keyIdx, translatableIdx, commentIdx, header.size())
    }
}
