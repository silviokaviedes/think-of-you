package de.kaviedes.thinkofyou3;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.data.mongodb.uri=mongodb://localhost:27017/test")
class ThinkOfYou3ApplicationTests {

    @Test
    void contextLoads() {
    }

}
