package pl.droidsonroids.gradle.localization

class SourceInfo {
    final XMLBuilder[] mBuilders
    final int mCommentIndex
    final int mTranslatableIndex
    final int mFormattedIndex
    final int mTagEscapingStrategyIndex
    final int mNameIdx
    final int mColumnsCount

    SourceInfo(String[] headerLine, ConfigExtension config, File resDir) {
        this(headerLine, config, resDir, config.outputFileName)
    }

    SourceInfo(String[] headerLine, ConfigExtension config, File resDir, String outputFileName) {
        if (headerLine == null || headerLine.length < 2) {
            throw new IllegalArgumentException("Invalid CSV header: $headerLine")
        }

        List<String> header = Arrays.asList(headerLine)
        mColumnsCount = headerLine.length
        mBuilders = new XMLBuilder[mColumnsCount]
        mTranslatableIndex = header.indexOf(config.translatableColumnName)
        mCommentIndex = header.indexOf(config.commentColumnName)
        mFormattedIndex = header.indexOf(config.formattedColumnName)
        mTagEscapingStrategyIndex = header.indexOf(config.tagEscapingStrategyColumnName)

        if (config.nameColumnIndex != null) {
            if (config.nameColumnName != null) {
                throw new IllegalArgumentException("Only one of nameColumnName and nameColumnIndex can be specified")
            }
            mNameIdx = config.nameColumnIndex
            if (!(mNameIdx in 0..<headerLine.length)) {
                throw new IllegalArgumentException("nameColumnIndex: $mNameIdx exceedes header index range <0,${mColumnsCount - 1}>")
            }
        } else {
            if (config.nameColumnName == null) {
                config.nameColumnName = 'name'
            }
            mNameIdx = header.indexOf(config.nameColumnName)
            if (mNameIdx == -1) {
                throw new IllegalArgumentException("Either existing nameColumnName or nameColumnIndex is required")
            }
        }
        if (header.indexOf(config.defaultColumnName) == -1) {
            throw new IllegalStateException("Default locale column not present")
        }

        def reservedColumns = [config.commentColumnName, config.translatableColumnName, config.formattedColumnName]
        if (config.tagEscapingStrategyColumnName != null) {
            reservedColumns.add(config.tagEscapingStrategyColumnName)
        }
        reservedColumns.addAll(config.ignorableColumns)

        header.eachWithIndex { columnName, i ->
            if (!(columnName in reservedColumns) && i != mNameIdx) {
                mBuilders[i] = new XMLBuilder(columnName, config, resDir, outputFileName)
            }
        }
    }
}