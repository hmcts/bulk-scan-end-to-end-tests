package uk.gov.hmcts.reform.bulkscan.endtoendtests.utils;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Container;

import java.util.Map;

public final class ContainerJurisdictionPoBoxMapper {

    private static final Map<Container, JurisdictionAndPoBox> CONTAINER_MAPPINGS = ImmutableMap
        .<Container, JurisdictionAndPoBox>builder()
        .put(Container.BULKSCAN, new JurisdictionAndPoBox(Container.BULKSCAN.name(), "BULKSCANPO"))
        .put(Container.CMC, new JurisdictionAndPoBox(Container.CMC.name(), "12747"))
        .put(Container.DIVORCE, new JurisdictionAndPoBox(Container.DIVORCE.name(), "12706"))
        .put(Container.FINREM, new JurisdictionAndPoBox(Container.DIVORCE.name(), "12746"))
        .put(Container.PROBATE, new JurisdictionAndPoBox(Container.PROBATE.name(), "12625"))
        .put(Container.PUBLICLAW, new JurisdictionAndPoBox(Container.PUBLICLAW.name(), "12879"))
        .put(Container.SSCS, new JurisdictionAndPoBox(Container.SSCS.name(), "12626"))
        .build();

    private ContainerJurisdictionPoBoxMapper() {
        // utility class construct
    }

    // name is as is in case we need to include more mapped data
    public static JurisdictionAndPoBox getMappedContainerData(Container container) {
        return CONTAINER_MAPPINGS.get(container);
    }

    public static class JurisdictionAndPoBox {

        public final String jurisdiction;

        public final String poBox;

        private JurisdictionAndPoBox(
            String jurisdiction,
            String poBox
        ) {
            this.jurisdiction = jurisdiction;
            this.poBox = poBox;
        }
    }
}
