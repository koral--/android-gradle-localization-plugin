package pl.droidsonroids.gradle.localization

import groovy.xml.MarkupBuilder
import org.apache.commons.csv.CSVParser

import java.util.regex.Pattern

class Parser {
    private static final Pattern JAVA_IDENTIFIER_REGEX=Pattern.compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*");
    private static final String NAME ="name", TRANSLATABLE="translatable", COMMENT="comment"
    private final CSVParser mParser
    private final XMLBuilder[] mBuilders
    private final int mCommentIdx
    private final int mTranslatableIdx
    private final int mNameIdx
    private final int mColumnsCount
    private final ConfigExtension mConfig
    private final File mResDir

    public Parser(ConfigExtension config, File resDir) {
        Reader reader
        if (!(config.csvFile != null ^ config.csvFileURI != null))
            throw new IllegalArgumentException("Exactly one of properties: 'csvFile' or 'csvFileURI' must be defined")
        else if (config.csvFile != null)
            reader = new FileReader(config.csvFile)
        else// if (config.csvFileURI!=null)
            reader=new InputStreamReader(new URL(config.csvFileURI).openStream())

        mResDir = resDir
        mParser = config.csvStrategy?new CSVParser(reader,config.csvStrategy):new CSVParser(reader)
        mConfig = config

        (mBuilders,mNameIdx,mTranslatableIdx,mCommentIdx,mColumnsCount) = parseHeader(mParser)
    }

    class XMLBuilder
    {
        final String mQualifier
        final MarkupBuilder mBuilder
        XMLBuilder(String qualifier)
        {
            String valuesDirName=qualifier==mConfig.defaultColumnName?'values':'values-'+qualifier
            File valuesDir=new File(mResDir,valuesDirName)
            if (!valuesDir.isDirectory())
                valuesDir.mkdirs()
            File valuesFile=new File(valuesDir,'strings.xml')
            mBuilder=new MarkupBuilder(new FileWriter(valuesFile))
            mBuilder.setDoubleQuotes(true)
            mBuilder.setOmitNullAttributes(true)
            mQualifier=qualifier
            mBuilder.getMkp().xmlDeclaration(version:'1.0', encoding:'utf-8')
        }
       def addResource(body)
       {//TODO add support for tools:locale
           mBuilder.resources(body/*, 'xmlns:tools':'http://schemas.android.com/tools', 'tools:locale':mQualifier*/)
       }
    }

    public parseCells() throws IOException {
        String[][] cells = mParser.getAllValues()
        def attrs=new LinkedHashMap<>(2)
        for (j in 0..mBuilders.length - 1) {
            def builder = mBuilders[j]
            if (builder == null)
                continue
            def keys=new HashSet(cells.length)
            builder.addResource({
                for (i in 0..cells.length-1)
                {
                    def row=cells[i]
                    if (row.size()<mColumnsCount)
                        throw new InputParseException("Undersized row #"+(i+2))
                    def name=row[mNameIdx]
                    if (!JAVA_IDENTIFIER_REGEX.matcher(name).matches())
                        throw new InputParseException(name+" is not valid name, row #"+(i+2))
                    if (!keys.add(name))
                        throw new InputParseException(name+" is duplicated in row #"+(i+2))
                    attrs.put('name', name)
                    def translatable = true
                    if (mTranslatableIdx>=0) {
                        translatable = !row[mTranslatableIdx].equalsIgnoreCase('false')
                        attrs.put('translatable', translatable ? null : 'false')
                    }
                    def value=row[j]
                    if (value.isEmpty())
                    {
                        if (!translatable && builder.mQualifier != mConfig.defaultColumnName)
                            continue
                        if (!mConfig.allowEmptyTranslations)
                            throw new InputParseException(name+" is not translated to locale "+builder.mQualifier+", row #"+(i+2))
                    }
                    else
                    {
                        if (!translatable && !mConfig.allowNonTranslatableTranslation && builder.mQualifier != mConfig.defaultColumnName)
                            throw new InputParseException(name+" is translated but marked translatable='false', row #"+(i+2))
                    }
                    if (mConfig.escapeSlashes)
                        value = value.replace("\\", "\\\\")
                    if (mConfig.escapeApostrophes)
                        value=value.replace("'","\\'")
                    if (mConfig.escapeQuotes)
                        value=value.replace("\"","\\\"")
                    if (mConfig.escapeNewLines)
                        value=value.replace("\n","\\n")
                    if (mConfig.escapeBoundarySpaces&&(value.indexOf(' ')==0||value.lastIndexOf(' ')==value.length()-1))
                        value='"'+value+'"'
                    if (mConfig.convertTripleDotsToHorizontalEllipsis)
                        value=value.replace("...","…")
                    string(attrs) { mkp.yield(value) }
                    if (mCommentIdx>=0&&!row[mCommentIdx].isEmpty())
                        mkp.comment(row[mCommentIdx])
                }
            })
        }
    }

    private parseHeader(CSVParser mParser) throws IOException {
        List<String> header = Arrays.asList(mParser.getLine())
        if (header.isEmpty())
            throw new InputParseException("Empty header. Is data in CSV format?")
        def keyIdx=header.indexOf(NAME)
        if (keyIdx==-1)
            throw new InputParseException("'name' column not present")
        if (header.indexOf(mConfig.defaultColumnName) == -1)
            throw new InputParseException("Default locale column not present")
        if (header.size() < 2)
            throw new InputParseException("At least one qualifier column needed")
        def builders = new XMLBuilder[header.size()]

        def reservedColumns = [NAME, COMMENT, TRANSLATABLE]
        reservedColumns.addAll(mConfig.ignorableColumns)
        def i = 0
        for (columnName in header) {
            if (!(columnName in reservedColumns))
                builders[i] = new XMLBuilder(columnName)
            i++
        }
        [builders, keyIdx, header.indexOf(TRANSLATABLE), header.indexOf(COMMENT), header.size()]
    }
}
