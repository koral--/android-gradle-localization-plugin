package pl.droidsonroids.gradle.localization

import org.apache.commons.collections4.map.HashedMap
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVStrategy

/**
 * Created by Alexey Puchko on 12/22/16.
 */

class CSVInnerParser extends CSVParser implements Parser {

    CSVInnerParser(Reader input) {
        super(input)
    }

    CSVInnerParser(Reader input, CSVStrategy strategy) {
        super(input, strategy)
    }

    @Override
    Map<String, String[][]> getResult() throws IOException {
        Map<String, String[][]> result = new HashedMap<>(1)
        result.put(null, getAllValues())
        return result
    }
}
