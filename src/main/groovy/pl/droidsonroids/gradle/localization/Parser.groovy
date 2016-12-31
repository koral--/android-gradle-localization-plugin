package pl.droidsonroids.gradle.localization
/**
 * Created by Alexey Puchko on 12/22/16.
 */

public interface Parser {
    Map<String, String[][]> getResult() throws IOException
}
