package uk.gov.hmcts.reform.bulkscan.endtoendtests.utils;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Container;

import java.util.Map;

public final class ContainerJurisdictionPoBoxMapper {

    private static final Map<Container, ContainerMapping> CONTAINER_MAPPINGS = ImmutableMap
        .<Container, ContainerMapping>builder()
        .put(Container.BULKSCAN, new ContainerMapping(Container.BULKSCAN.name(), "BULKSCANPO"))
        .put(Container.CMC, new ContainerMapping(Container.CMC.name(), "12747"))
        .put(Container.DIVORCE, new ContainerMapping(Container.DIVORCE.name(), "12706"))
        .put(Container.FINREM, new ContainerMapping(Container.DIVORCE.name(), "12746"))
        .put(Container.PROBATE, new ContainerMapping(Container.PROBATE.name(), "12625"))
        .put(Container.PUBLICLAW, new ContainerMapping(Container.PUBLICLAW.name(), "12879"))
        .put(Container.SSCS, new ContainerMapping(Container.SSCS.name(), "12626"))
        .build();

    private ContainerJurisdictionPoBoxMapper() {
        // utility class construct
    }

    // name is as is in case we need to include more mapped data
    public static ContainerMapping getMappedContainerData(Container container) {
        return CONTAINER_MAPPINGS.get(container);
    }

    public static class ContainerMapping {

        public final String jurisdiction;

        public final String poBox;

        public final String formType;

        private ContainerMapping(
            String jurisdiction,
            String poBox,
            String formType
        ) {
            this.jurisdiction = jurisdiction;
            this.poBox = poBox;
            this.formType = formType;
        }

        private ContainerMapping(String jurisdiction, String poBox) {
            this(jurisdiction, poBox, null);
        }
    }
}
