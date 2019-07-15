package es.coffeebyt.wtu.security;

import es.coffeebyt.wtu.system.PropertyNames;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@FeignClient(name = "auth0", url = "https://" + PropertyNames.AUTH0_DOMAIN)
public interface Auth0Client {

    @RequestMapping(method = POST, value = "/oauth/token", consumes = APPLICATION_JSON_VALUE, headers = "Content-Type: application/json")
    TokenResponse getToken(TokenRequest tokenRequest);
}
