package com.ordermanagementsystem.dev;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// "test" profili: gerçek MySQL yerine H2 (src/test/resources/application-test.yml)
@SpringBootTest
@ActiveProfiles("test")
class OrdermanagementsystemApplicationTests {

	@Test
	void contextLoads() {
	}

}
