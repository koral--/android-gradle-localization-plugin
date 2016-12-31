package pl.droidsonroids.gradle.localization

/**
 * X(H)TML tag brackets (&lt; and &gt;) escaping strategy
 */
public enum TagEscapingStrategy
{
    /**
     * Brackets are always escaped. Eg. "&lt;" in source becomes "&amp;lt;" in output XML
     */
    ALWAYS,
    /**
     * Brackets are never escaped. Eg. "&lt;" in source is passed without change to output XML
     */
            NEVER,
    /**
     * Brackets aren't escaped if text contains tags CDATA section.
     * Eg. {@code <b>bold</b>} will be passed without change,
     * but "if x&lt;4 then…" becomes "if x&amp;lt;4 then…".
     */
            IF_TAGS_ABSENT
}
