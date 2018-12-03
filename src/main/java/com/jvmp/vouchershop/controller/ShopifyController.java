package com.jvmp.vouchershop.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController("/shopify")
public class ShopifyController {
    
    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }


    @PostMapping("/vouchers")
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
    
}
