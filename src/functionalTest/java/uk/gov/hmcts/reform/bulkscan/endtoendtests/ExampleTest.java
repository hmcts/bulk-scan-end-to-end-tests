package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExampleTest {
    private static final String TEST_NAME = ConfigFactory.load().getString("test-name");

    @Test
    public void testConfig() {
        assertEquals(TEST_NAME, "e2etests", "Config loaded");
    }

}
