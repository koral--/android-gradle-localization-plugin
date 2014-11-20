package pl.droidsonroids.gradle.localization

import groovy.transform.TupleConstructor

/**
 * Wrapper for XML node which can have translatable attribute
 */
@TupleConstructor
class TranslatableNode {
    String name
    boolean translatable

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        TranslatableNode that = (TranslatableNode) o

        if (name != that.name) return false

        return true
    }

    int hashCode() {
        return name.hashCode()
    }
}
