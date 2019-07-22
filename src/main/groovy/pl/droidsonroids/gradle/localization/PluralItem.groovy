package pl.droidsonroids.gradle.localization

import groovy.transform.TupleConstructor

/**
 * Descriptor of quantity item
 */
@TupleConstructor
class PluralItem implements Comparable<PluralItem> {
    Quantity quantity
    String value
    String comment

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        PluralItem that = (PluralItem) o

        if (quantity != that.quantity) return false

        return true
    }

    int hashCode() {
        return quantity.hashCode()
    }

    @Override
    String toString() {
        return "PluralItem{value='$value', quantity=$quantity}"
    }

    @Override
    int compareTo(PluralItem pluralItem) {
        return quantity <=> pluralItem.quantity
    }
}
