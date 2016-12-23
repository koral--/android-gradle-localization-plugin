package pl.droidsonroids.gradle.localization;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVStrategy;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * Created by Alexey Puchko on 12/22/16.
 */

public class CSVInnerParser extends CSVParser implements Parser {

    public CSVInnerParser(Reader input) {
        super(input);
    }

    public CSVInnerParser(Reader input, CSVStrategy strategy) {
        super(input, strategy);
    }

    @Override
    public Map<String, String[][]> getResult() throws IOException {
        Map<String, String[][]> result = new HashedMap<>(1);
        result.put(null, getAllValues());
        return result;
    }
}
