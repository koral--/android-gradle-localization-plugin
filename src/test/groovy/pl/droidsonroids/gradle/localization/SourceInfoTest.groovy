package pl.droidsonroids.gradle.localization

import org.junit.Before
import org.junit.Test

class SourceInfoTest {
    private static final String[] VALID_HEADER = ['name', 'default']
    private ConfigExtension config

    @Before
    void setUp() {
        config = new ConfigExtension()
    }

    @Test(expected = IllegalArgumentException.class)
    void testEmptyHeader() {
        new SourceInfo([] as String[], config, null)
    }

    @Test(expected = IllegalArgumentException.class)
    void testBothNameColumnIndexAndName() {
        config.nameColumnIndex = 1
        config.nameColumnName = 'name'
        new SourceInfo(VALID_HEADER, config, null)
    }

    @Test(expected = IllegalArgumentException.class)
    void testInvalidNameColumnIndex() {
        config.nameColumnIndex = 4
        new SourceInfo(VALID_HEADER, config, null)
    }

    @Test(expected = IllegalArgumentException.class)
    void testNonExistentNameColumnName() {
        config.nameColumnName = 'dummy'
        new SourceInfo(VALID_HEADER, config, null)
    }

    @Test(expected = IllegalStateException.class)
    void testNonExistentDefaultColumnName() {
        new SourceInfo(['name', 'dummy'] as String[], config, null)
    }
}