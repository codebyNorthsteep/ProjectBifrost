package org.example.projectbifrost;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
        "openrouter.api.key=test-key",
        "openrouter.model=test-model",
        "openrouter.base-url=test-url"
})
class ProjectBifrostApplicationTests {

    @Test
    void contextLoads() {
    }

}
