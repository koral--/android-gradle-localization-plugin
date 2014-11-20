package pl.droidsonroids.gradle.localization

import groovy.transform.TupleConstructor

/**
 * Descriptor of string-array item
 */
@TupleConstructor
class StringArrayItem {
    String value
    String comment
}
