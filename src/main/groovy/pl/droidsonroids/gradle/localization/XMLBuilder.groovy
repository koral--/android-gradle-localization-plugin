package pl.droidsonroids.gradle.localization

import groovy.xml.MarkupBuilder

class XMLBuilder {
        final String mQualifier
        final MarkupBuilder mBuilder
        final ConfigExtension mConfig

        XMLBuilder(String qualifier, ConfigExtension config, File mResDir) {
            mConfig = config
            def defaultValues = qualifier == mConfig.defaultColumnName
            String valuesDirName = defaultValues ? 'values' : 'values-' + qualifier
            File valuesDir = new File(mResDir, valuesDirName)
            if (!valuesDir.directory) {
                valuesDir.mkdirs()
            }
            File valuesFile = new File(valuesDir, mConfig.outputFileName)

            def outputStream = new BufferedOutputStream(new FileOutputStream(valuesFile), Utils.BUFFER_SIZE)
            def streamWriter = new OutputStreamWriter(outputStream, 'UTF-8')
            mBuilder = new MarkupBuilder(new IndentPrinter(streamWriter, mConfig.outputIndent))

            mBuilder.doubleQuotes = true
            mBuilder.omitNullAttributes = true
            mQualifier = qualifier
            mBuilder.mkp.xmlDeclaration(version: '1.0', encoding: 'UTF-8')
        }

        def addResource(body) {
            if (mQualifier == mConfig.defaultColumnName && mConfig.defaultLocaleQualifier != null)
                mBuilder.resources(body, 'xmlns:tools': 'http://schemas.android.com/tools', 'tools:locale': mConfig.defaultLocaleQualifier)
            else
                mBuilder.resources(body)
        }
    }