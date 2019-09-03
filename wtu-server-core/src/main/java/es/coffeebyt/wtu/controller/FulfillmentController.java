package es.coffeebyt.wtu.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.coffeebyt.wtu.fulfillment.Fulfillment;
import es.coffeebyt.wtu.fulfillment.FulfillmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
@CrossOrigin
@Slf4j
@Deprecated
public class FulfillmentController {

    private final FulfillmentService fulfillmentService;

    @GetMapping("/fulfillments/{order_id}")
    public Fulfillment getFulfillmentForOrder(@PathVariable("order_id") long orderId) {
        return fulfillmentService.findByOrderId(orderId);
    }

}
