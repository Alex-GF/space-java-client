package io.github.pgmarc.space.serializers;

import io.github.pgmarc.space.features.UsageLimitConsumption;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UsageLimitConsumptionSerializerTest {

    private final ConsumptionSerializer serializer = new ConsumptionSerializer();

    @Test
    void givenConsumptionShouldSerialize() {

        String service = "Petclinic";
        int petsRegistered = 1;
        long fooConsumption = (long) Integer.MAX_VALUE + 1;
        float barConsumption = 0.1f;
        double consumedSeconds = (double) Float.MAX_VALUE + 1;

        UsageLimitConsumption usageLimitConsumption = UsageLimitConsumption.builder(service)
            .addInt( "maxPets", petsRegistered)
            .addLong( "fooLimit", fooConsumption)
            .addDouble("maxSeconds", consumedSeconds)
            .addFloat("barLimit", barConsumption)
            .build();

        assertThatNoException().isThrownBy(() -> serializer.toJson(usageLimitConsumption));

        String intUsageLimit = "petclinic-maxPets";
        String longUsageLimit = "petclinic-fooLimit";
        String floatUsageLimit = "petclinic-barLimit";
        String doubleUsageLimit =  "petclinic-maxSeconds";
        JSONObject consumptionPayload = serializer.toJson(usageLimitConsumption);

        assertThat(consumptionPayload.keySet()).contains(intUsageLimit, longUsageLimit, floatUsageLimit, doubleUsageLimit);
        assertThat(consumptionPayload.getInt(intUsageLimit)).isEqualTo(petsRegistered);
        assertThat(consumptionPayload.getLong(longUsageLimit)).isEqualTo(fooConsumption);
        assertThat(consumptionPayload.getFloat(floatUsageLimit)).isEqualTo(barConsumption);
        assertThat(consumptionPayload.getDouble(doubleUsageLimit)).isEqualTo(consumedSeconds);
    }

}
