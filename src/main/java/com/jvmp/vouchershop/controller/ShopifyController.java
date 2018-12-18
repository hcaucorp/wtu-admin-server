package com.jvmp.vouchershop.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController()
public class ShopifyController {

    /**
     * Triggered when something has been sold on Shopify and needs fullfilling
     */
    @GetMapping("/shopify/webhook/fulfill")
    public ResponseEntity<?> fullFillmentHook(@RequestBody String body) {

        log.info("Received: {}", body);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
