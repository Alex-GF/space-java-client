package io.github.isagroup.space.springboot.client;

import java.util.Objects;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.MethodParameter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpRequestValues.Builder;

import io.github.isagroup.space.springboot.client.contracts.ContractsService;
import io.github.isagroup.space.springboot.client.services.PricingAvailabilityStatus;
import io.github.isagroup.space.springboot.client.services.ServicesService;
import io.github.isagroup.space.springboot.client.users.UsersService;

import org.springframework.web.service.invoker.HttpServiceArgumentResolver;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@AutoConfiguration
@EnableConfigurationProperties(SpaceClientProperties.class)
public class SpaceClientAutoConfiguration {

    private static final String SPACE_API_KEY_HEADER_NAME = "x-api-key";

    private final HttpServiceProxyFactory factory;

    public SpaceClientAutoConfiguration(SpaceClientProperties spaceClientProperties) {
        Objects.requireNonNull(spaceClientProperties.getApiKey(),
                "apiKey is null, provide a Space API key");
        RestClient restClient = RestClient.builder().baseUrl(spaceClientProperties.getUrl())
                .defaultHeader(SPACE_API_KEY_HEADER_NAME, spaceClientProperties.getApiKey()).build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        this.factory = HttpServiceProxyFactory.builderFor(adapter)
                .customArgumentResolver(new SearchParamsQueryArgumentResolver()).build();

    }

    @Bean
    public UsersService usersService() {
        return factory.createClient(UsersService.class);
    }

    @Bean
    public ServicesService servicesService() {
        return factory.createClient(ServicesService.class);
    }

    @Bean
    public ContractsService contractsService() {
        return factory.createClient(ContractsService.class);
    }

    private static class SearchParamsQueryArgumentResolver implements HttpServiceArgumentResolver {

        @Override
        public boolean resolve(Object argument, MethodParameter parameter, Builder requestValues) {

            if (argument instanceof SearchParams searchParams) {
                requestValues.addRequestParameter("order", searchParams.getSortDirection().toString().toLowerCase());
                requestValues.addRequestParameter("limit", String.valueOf(searchParams.getPagination().getLimit()));
                processPaginationType(searchParams.getPagination(), requestValues);
                return true;
            }

            if (Objects.nonNull(argument) && parameter.getParameterType().equals(PricingAvailabilityStatus.class)) {
                PricingAvailabilityStatus availability = (PricingAvailabilityStatus) argument;
                requestValues.addRequestParameter("availability", availability.toString());
                return true;
            }

            return false;
        }

        private void processPaginationType(Pagination pagination, Builder requestValues) {

            if (pagination instanceof OffsetBasedPagination offsetBased) {
                requestValues.addRequestParameter("offset", String.valueOf(offsetBased.getOffset()));
            }
            if (pagination instanceof PagedBasedPagination pagedBased) {
                requestValues.addRequestParameter("page", String.valueOf(pagedBased.getPage()));
            }
        }

    }
}
