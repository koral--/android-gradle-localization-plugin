package pl.droidsonroids.gradle.localization

import org.junit.Before
import org.junit.Test

class SourceInfoTest {
    private static final String[] VALID_HEADER = ['name', 'default']
    private ConfigExtension config

    @Before
    public void setUp() {
        config = new ConfigExtension()
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullHeader() {
        new SourceInfo(null, config, null)
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyHeader() {
        new SourceInfo([] as String[], config, null)
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooShortHeader() {
        new SourceInfo(['name'] as String[], config, null)
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBothNameColumnIndexAndName() {
        config.nameColumnIndex = 1
        config.nameColumnName = 'name'
        new SourceInfo(VALID_HEADER, config, null)
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidNameColumnIndex() {
        config.nameColumnIndex = 4
        new SourceInfo(VALID_HEADER, config, null)
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistentNameColumnName() {
        config.nameColumnName = 'dummy'
        new SourceInfo(VALID_HEADER, config, null)
    }

    @Test(expected = IllegalStateException.class)
    public void testNonExistentDefaultColumnName() {
        new SourceInfo(['name', 'dummy'] as String[], config, null)
    }
}