package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.testUtil.FlywayResetExtension;

@ExtendWith(FlywayResetExtension.class)
@SpringBootTest
class EcommerceRestApplicationTests {
    // TODO:
    // src/main/resources/db/migration に追加したら、
    // 　　自動で src/test/resources/db/migration_test にもコピーしたい。

	@Test
	void contextLoads() {
	}

}
