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

        Byte[] cards = new Byte[136];
        int index = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < PokeData._Mahjong.length && index < 136; j++, index++) {
                cards[index] = PokeData._Mahjong[j];
            }
        }

        List<Byte> pokes = Arrays.asList(cards);

        Collections.shuffle(pokes);
        System.out.println("init table " + tableId + " pokes: ---->" + pokes.toString());
        pokes.forEach(poke -> {
            redisService.lpush(prex + tableId, poke);

        });
    }

    public Byte dealCard(int tableId) {

        return Byte.valueOf(redisService.lpop(prex + tableId));
    }

    // init card
    public void dealCards(User user, int base) {

        System.out.println("init table " + user.getTableId() + " pokes");
        List<Byte> pokes = new ArrayList<>();
        for (int i = 0; i < base; i++) {
            pokes.add(Byte.valueOf(redisService.lpop(prex + user.getTableId())));
        }

        user.setPokes(pokes);
    }

    /*public static void main(String args[]) {
        Byte[] cards = new Byte[136];
        int index = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < PokeData._Mahjong.length && index < 136; j++, index++) {
                cards[index] = PokeData._Mahjong[j];
            }
        }

        List<Byte> pokes = Arrays.asList(cards);

        Collections.shuffle(pokes);
        System.out.println("init table pokes: ---->" + pokes.toString());
    }*/
}
