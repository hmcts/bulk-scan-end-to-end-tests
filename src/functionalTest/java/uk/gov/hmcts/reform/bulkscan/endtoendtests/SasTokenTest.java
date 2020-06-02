package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.SasTokenRetriever;

import static org.assertj.core.api.Assertions.assertThat;

public class SasTokenTest {

    @Test
    public void testSasToken() {
        var token = SasTokenRetriever.getTokenFor("bulkscan");

        assertThat(token).isNotEmpty();
    }
}
