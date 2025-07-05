package io.github.isagroup.space.springboot.client.contracts;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import io.github.isagroup.space.springboot.client.SearchParams;

@HttpExchange(url = "/contracts", accept = MediaType.APPLICATION_JSON_VALUE)
public interface ContractsService {

    @GetExchange
    List<Object> getContracts(SearchParams searchParams);

    @PostExchange
    Object createContract(@RequestBody CreateContractRequest newContract);

}
