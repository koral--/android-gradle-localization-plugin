package pl.droidsonroids.gradle.localization

import groovy.transform.TupleConstructor

/**
 * Created by koral on 19.11.14.
 */
@TupleConstructor
class QuantityEntry
{
    Quantity quantity
    String value
    String comment
}
