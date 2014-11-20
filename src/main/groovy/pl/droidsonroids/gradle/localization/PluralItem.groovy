package pl.droidsonroids.gradle.localization

import groovy.transform.TupleConstructor

/**
 * Descriptor of quantity item
 */
@TupleConstructor
class PluralItem {
    Quantity quantity
    String value
    String comment
}
