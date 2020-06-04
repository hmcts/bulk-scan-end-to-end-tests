package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import com.google.common.collect.ImmutableList;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.model.Classification;

import java.util.List;
import java.util.Map;

import static java.util.AbstractMap.SimpleEntry;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

class AllServicesTest {

    // value - list of services
    private static final Map<Classification, List<Object>> servicesPerClassification = ConfigFactory
        .load()
        .getObject("tests.all-services")
        .entrySet()
        .stream()
        .map(entry -> new SimpleEntry<>(
            Classification.valueOf(entry.getKey()),
            ((ConfigList) entry.getValue()).unwrapped()
        ))
        .collect(toMap(SimpleEntry::getKey, SimpleEntry::getValue));

    @Test
    void should_verify_all_services_are_in_the_test_config() {
        // given
        var expectedServices = ImmutableList.of("cmc", "divorce", "finrem", "probate", "publiclaw", "sscs");

        // when
        var actualServices = servicesPerClassification
            .values()
            .stream()
            .flatMap(List::stream)
            .collect(toList());

        // then
        assertThat(actualServices).containsExactlyInAnyOrderElementsOf(expectedServices);
    }

    @ParameterizedTest
    @MethodSource("serviceWithClassification")
    void should_create_case_for_service(String service, Classification classification) {
        // to be implemented
        System.out.println("SERVICE: " + service + "; CLASSIFICATION: " + classification);
    }

    private static Object[][] serviceWithClassification() {
        return servicesPerClassification
            .entrySet()
            .stream()
            .flatMap(entry -> entry
                .getValue()
                .stream()
                .map(service -> new Object[] {service, entry.getKey()})
            )
            .collect(toList())
            .toArray(new Object[][]{});
    }
}
