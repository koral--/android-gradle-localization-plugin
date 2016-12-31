package pl.droidsonroids.gradle.localization

import java.util.regex.Pattern

class Utils {
    static final int BUFFER_SIZE = 128 * 1024
    private static
    final Pattern TAG_PATTERN = Pattern.compile('<([A-Z][A-Z0-9]*)\\b[^>]*((/?)|(>.*?</\\1>))', Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
    private static final Pattern CDATA_PATTERN = Pattern.compile('<!\\[CDATA\\[(.*?)\\]\\]>', Pattern.DOTALL)

    static BufferedReader wrapReader(Reader reader) {
        new BufferedReader(reader, BUFFER_SIZE)
    }

    static BufferedInputStream wrapInputStream(InputStream inputStream) {
        new BufferedInputStream(inputStream, BUFFER_SIZE)
    }

    static boolean containsHTML(String text) {
        return TAG_PATTERN.matcher(text).find() || CDATA_PATTERN.matcher(text).find()
    }
}