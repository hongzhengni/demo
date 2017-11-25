package com.nee.game.service;

import com.nee.game.common.PokeData;
import com.nee.game.data.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class CardService {

    private final static String prex = "table-mahjong-card-";

    @Autowired
    private RedisService redisService;


    public void initCard(int tableId) {

        Byte [] cards = new Byte[136];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < PokeData._Mahjong.length; j++) {
                cards[i * j] = PokeData._Mahjong[j];
            }
        }

        List<Byte> pokes = Arrays.asList(cards);

        Collections.shuffle(pokes);
        pokes.forEach(poke -> {
            redisService.lpush(prex + tableId, poke);

        });
    }

    public Byte dealCard(int tableId) {

        return Byte.valueOf(redisService.lpop(prex + tableId));
    }

    // init card
    public void dealCards(User user, int base) {

        List<Byte> pokes = new ArrayList<>();
        for (int i = 0; i < base + user.getHog(); i++) {
            pokes.add(Byte.valueOf(redisService.lpop(prex + user.getTableId())));
        }

        user.setPokes(pokes);
    }
}
