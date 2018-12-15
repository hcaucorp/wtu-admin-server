package com.jvmp.vouchershop.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class ShopifyController {


    @PostMapping("/shopify/vouchers")
    public String abct(@RequestParam("quantity") long quantity) {


//        Shopify.post('/admin/inventory_levels/set.json',
//                {
//                        location_id: 10255499328,
//                inventory_item_id: 16019805634624,
//                available: 2000
//        },
//        function (err: any, data: any, headers: any) {
//            console.log("Vouchers stock updated.");
//        })
//        ;
        return null;
    }

    /**
     * Triggered when something has been sold on Shopify and needs fullfilling
     */
    @GetMapping("/shopify/fullfill")
    public ResponseEntity<?> fullFillmentHook() {
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
