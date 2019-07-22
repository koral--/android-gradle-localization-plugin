package pl.droidsonroids.gradle.localization

import org.junit.Test

import static org.assertj.core.api.AssertionsForClassTypes.assertThat
import static pl.droidsonroids.gradle.localization.Utils.containsHTML

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
}