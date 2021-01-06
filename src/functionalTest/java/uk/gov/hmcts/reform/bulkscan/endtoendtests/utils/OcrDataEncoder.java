package uk.gov.hmcts.reform.bulkscan.endtoendtests.utils;

import com.google.common.io.Resources;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Container;

import java.io.IOException;
import java.util.Base64;

public final class OcrDataEncoder {

    private OcrDataEncoder() {
        // utility class construct
    }

    public static String encodeDefaultOcrData(Container container) throws IOException {
        return encodeOcrData(container + "");
    }

    public static String encodeOcrData(String fileName) throws IOException {
        return Base64
            .getEncoder()
            .encodeToString(
                Resources.toByteArray(
                    Resources.getResource("ocr-data/" + fileName + ".json")
                )
            );
    }
}
