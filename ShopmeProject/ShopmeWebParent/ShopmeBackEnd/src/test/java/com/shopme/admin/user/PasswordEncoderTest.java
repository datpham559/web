package com.shopme.admin.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


public class PasswordEncoderTest {
	@Test
	public void testEncodePassword() {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String rawpassword = "123456";
		String encodedPassword= passwordEncoder.encode(rawpassword);
		System.out.println(encodedPassword);
		boolean matches = passwordEncoder.matches(rawpassword, encodedPassword);
		
		assertThat(matches).isTrue();
	}
}
