package com.tdl.redis.client;

import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class OfferService {

    @Resource
    private RedisConnectionService redisConnectionService;


    public Offer save(Offer offer) {

        redisConnectionService.execute(connection -> {
            connection.set(
                    new StringBuilder("test:").append(offer.getId()).toString()
                            .getBytes(), SerializationUtils.serialize(offer), Expiration.milliseconds(5000), RedisStringCommands.SetOption.UPSERT);
            return null;
        });

        return offer;
    }

    public Offer findById(String id) {
        return (Offer) SerializationUtils.deserialize(redisConnectionService.execute(connection -> {
           return connection.get(
                    new StringBuilder("test:").append(id).toString()
                            .getBytes());

        }, false));
    }

    public List<Offer> findAll() {
        return (List<Offer>) SerializationUtils.deserialize( redisConnectionService.execute(connection -> {
            return connection.get(
                    new StringBuilder("test:*").toString()
                            .getBytes());
        }, false));
    }

    public void saveAll(List<Offer> asList) {

        asList.stream().forEach(this::save);
    }
}
