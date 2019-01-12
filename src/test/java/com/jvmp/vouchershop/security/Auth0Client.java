package com.jvmp.vouchershop.security;

import com.jvmp.vouchershop.system.PropertyNames;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@FeignClient(name = "auth0", url = "https://" + PropertyNames.AUTH0_DOMAIN)
public interface Auth0Client {

    @RequestMapping(method = POST, value = "/oauth/token", consumes = APPLICATION_JSON_VALUE, headers = "Content-Type: application/json")
    TokenResponse getToken(TokenRequest tokenRequest);

    @RequestMapping(method = GET, value = "/authorize?audience={audience}&scope={scope}&response_type=code&client_id={clientId}&&state={state}&prompt=none",
            consumes = APPLICATION_JSON_VALUE, headers = "Content-Type: application/json")
    String authorize(@PathVariable("audience") String audience, @PathVariable("scope") String scope, @PathVariable("clientId") String clientId, @PathVariable("state") String state);
}
