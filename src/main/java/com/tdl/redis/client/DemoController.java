package com.tdl.redis.client;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
public class DemoController {

    @Resource
    private OfferService offerService;

    @GetMapping("/offers")
    public List<Offer> getOffers(){
        return StreamSupport.stream(offerService.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @GetMapping("/offer")
    public Offer getOffersbyId(@RequestParam String id){
        return offerService.findById(id);
    }

    @PutMapping("/offer")
    public Offer createOffer(@RequestBody Offer offer)
    {
        return offerService.save(offer);
    }

    @GetMapping("/bulkcreate")
    public String bulkcreate(){

        for (Integer i=10; i<= 20; i++)
        {
            Offer offer = new Offer();
            offer.setId(i.toString());
            offer.setOfferType(OfferType.COMPLEX);
            offer.setName("Offer"+i);
            offer.setGrade(i);
            offerService.save(offer);
        }



        return "Offers are created";
    }

}
