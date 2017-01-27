package com.konkerlabs.platform.registry.test.business.model;

import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import com.konkerlabs.platform.registry.business.model.User;

public class UserTest {

	@Test
	public void shouldGetFirstName() {

		User user = User.builder().build();
		assertThat(user.getFirstName(), org.hamcrest.Matchers.nullValue());

		user.setName("Konker");
		assertThat(user.getFirstName(), org.hamcrest.Matchers.equalTo("Konker"));

		user.setName("Konker Labs");
		assertThat(user.getFirstName(), org.hamcrest.Matchers.equalTo("Konker"));

	}

}