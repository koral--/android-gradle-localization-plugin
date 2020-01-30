package pl.droidsonroids.gradle.localization

import org.junit.Test

import static org.assertj.core.api.AssertionsForClassTypes.assertThat
import static pl.droidsonroids.gradle.localization.Utils.containsHTML
import static pl.droidsonroids.gradle.localization.Utils.validateColumnEmptiness

class UtilsTest {

    @Test
    void plainTextContainsNoHTML() {
        assertThat(containsHTML('')).isFalse()
        assertThat(containsHTML('test')).isFalse()
        assertThat(containsHTML('test\ntest2')).isFalse()
        assertThat(containsHTML('<')).isFalse()
        assertThat(containsHTML('>')).isFalse()
        assertThat(containsHTML('<>')).isFalse()
    }

    @Test
    void entitiesContainsNoHTML() {
        assertThat(containsHTML('&test;')).isFalse()
        assertThat(containsHTML('&lt;')).isFalse()
    }

    @Test
    void textWithTagsContainsHTML() {
        assertThat(containsHTML('<b>')).isTrue()
        assertThat(containsHTML('<b/>')).isTrue()
        assertThat(containsHTML('<b></b>')).isTrue()
        assertThat(containsHTML('<b>outer1<b>inner</b>outer2</b>')).isTrue()
    }

    @Test
    void CDATAContainsHTML() {
        assertThat(containsHTML('<![CDATA[test<a href="http://test.test"><b>test</b></a>test]]>')).isTrue()
        assertThat(containsHTML('<![CDATA[test]]>')).isTrue()
        assertThat(containsHTML('<![CDATA[]]>')).isTrue()
    }

	@Test(expected = IllegalArgumentException.class)
    void throwsOnNullHeader() {
		validateColumnEmptiness(new String[1][], false)
    }

	@Test(expected = IllegalArgumentException.class)
    void throwsOnTooShortHeader() {
		String[][] cells = new String[1][1]
		cells[0][0] = "test"
		validateColumnEmptiness(cells, false)
    }

	@Test(expected = IllegalArgumentException.class)
    void throwsOnInconsistentColumn() {
		String[][] cells = new String[2][2]
		cells[0][0] = "test"
		cells[0][1] = ""
		cells[1][1] = "test"
		cells[0][0] = "test"
		validateColumnEmptiness(cells, false)
    }
}