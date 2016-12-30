package pl.droidsonroids.gradle.localization;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Alexey Puchko on 12/22/16.
 */

public interface Parser {
    Map<String, String[][]> getResult() throws IOException;
}
