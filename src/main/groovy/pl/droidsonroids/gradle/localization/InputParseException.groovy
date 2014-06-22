package pl.droidsonroids.gradle.localization;

/**
 * @author koral--
 * Thrown when the input string being parsed is not in the correct form.
 */
public class InputParseException extends IOException
{
    InputParseException(String message)
    {
        super(message)
    }
}
