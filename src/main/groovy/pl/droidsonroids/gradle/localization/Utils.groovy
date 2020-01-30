package pl.droidsonroids.gradle.localization

import java.util.regex.Pattern

class Utils {
    static final int BUFFER_SIZE = 128 * 1024
    private static
    final Pattern TAG_PATTERN = Pattern.compile('<([A-Z][A-Z0-9]*)\\b[^>]*((/?)|(>.*?</\\1>))', Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
    private static final Pattern CDATA_PATTERN = Pattern.compile('<!\\[CDATA\\[(.*?)]]>', Pattern.DOTALL)

    static BufferedReader wrapReader(Reader reader) {
        new BufferedReader(reader, BUFFER_SIZE)
    }

    static BufferedInputStream wrapInputStream(InputStream inputStream) {
        new BufferedInputStream(inputStream, BUFFER_SIZE)
    }

    static boolean containsHTML(String text) {
        return TAG_PATTERN.matcher(text).find() || CDATA_PATTERN.matcher(text).find()
    }

    static void validateColumnEmptiness(String[][] allCells, boolean shouldIgnoreEmptyHeaderCell) {
        def headerLine = allCells[0]
        if (headerLine == null || headerLine.length < 2) {
            throw new IllegalArgumentException("Invalid header: $headerLine")
        }

        if (!shouldIgnoreEmptyHeaderCell) {
            def emptyHeaderIndices = headerLine.findIndexValues { it.empty }
            allCells.eachWithIndex { String[] row, int rowIndex ->
                emptyHeaderIndices.forEach { i ->
                    if (i < row.length && !row[i.intValue()].empty) {
                        throw new IllegalArgumentException("Not ignored column #$i contains empty header but non-empty cell at row #$rowIndex")
                    }
                }
            }
        }
    }
}