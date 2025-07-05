package io.github.isagroup.space.springboot.client.services;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import io.github.isagroup.space.springboot.client.SearchParams;

@HttpExchange(url = "/services", accept = MediaType.APPLICATION_JSON_VALUE)
public interface ServicesService {

        @GetExchange
        List<Service> getServices(@RequestParam(required = false) SearchParams searchParams,
                        @RequestParam(required = false) String name);

        @PostExchange
        Service createService(@RequestPart Resource pricing);

        @DeleteExchange
        void deleteAllServices();

        @GetExchange("/{serviceName}")
        Service getServiceByName(@PathVariable String serviceName);

        @PutExchange("/{serviceName}")
        Service updateServiceByName(@PathVariable String serviceName, @RequestBody UpdateServiceRequest serviceRequest);

        @DeleteExchange("/{serviceName}")
        void deleteServiceByName(@PathVariable String serviceName);

        // TODO: Devuelve un pricing
        @GetExchange("/{serviceName}/pricings")
        List<Object> getPricingsDefinedInService(@PathVariable String serviceName,
                        @RequestParam(required = false) PricingAvailabilityStatus pricingStatus);

        // TODO: Devuelve un pricing
        @PostExchange("/{serviceName}/pricings")
        Object addPricingToService(@PathVariable String serviceName, @RequestPart Resource pricing);

        // TODO: Devuelve un pricing
        @GetExchange("/{serviceName}/pricings/{pricingVersion}")
        Object getServicePricingByVersion(@PathVariable String serviceName, @PathVariable String pricingVersion);

        @PutExchange("/{serviceName}/pricings/{pricingVersion}")
        Service updatePricingAvailabilityByVersion(@PathVariable String serviceName,
                        @PathVariable String pricingVersion, @RequestParam PricingAvailabilityStatus availability,
                        @RequestBody FallBackSubscription fallbackSubscription);

        @DeleteExchange("/{serviceName}/pricings/{pricingVersion}")
        void deletePricingByVersionAndService(@PathVariable String serviceName, @PathVariable String pricingVersion);
}
