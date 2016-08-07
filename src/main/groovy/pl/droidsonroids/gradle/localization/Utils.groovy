package pl.droidsonroids.gradle.localization

import org.jsoup.Jsoup

class Utils {
    static final int BUFFER_SIZE = 128 * 1024

    static BufferedReader wrapReader(Reader reader) {
        new BufferedReader(reader, BUFFER_SIZE)
    }

    static BufferedInputStream wrapInputStream(InputStream inputStream) {
        new BufferedInputStream(inputStream, BUFFER_SIZE)
    }

    static boolean containsNoTags(String value) {
        Jsoup.parse(value).body().children().empty
    }
}