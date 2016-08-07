package pl.droidsonroids.gradle.localization

import org.junit.Test

import static org.assertj.core.api.AssertionsForClassTypes.assertThat
import static pl.droidsonroids.gradle.localization.Utils.containsNoTags

public class UtilsTest {

	@Test
	public void testContainsNoTags() {
		assertThat(containsNoTags('')).isTrue()
		assertThat(containsNoTags('test')).isTrue()
		assertThat(containsNoTags('<')).isTrue()
		assertThat(containsNoTags('<b>')).isFalse()
		assertThat(containsNoTags('<b></b>')).isFalse()
	}
}