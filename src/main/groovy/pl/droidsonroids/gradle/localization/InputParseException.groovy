package pl.droidsonroids.gradle.localization;

/**
 * Thrown when the input string being parsed is not in the correct form.
 * @author koral--
 */
public class InputParseException extends IOException {
    InputParseException(String message) {
        super(message)
    }
}
