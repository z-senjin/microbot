package net.runelite.client.plugins.microbot.mntn.MntnSlayer.data.monsters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;

public class SlayerMonsterLoader {
    public static List<SlayerMonster> loadMonsters() {
        System.out.println("LOADING SLAYER MONSTERS FROM JSON FILE.");
        try (Reader reader = new FileReader("./slayer_monsters.json")) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<SlayerMonster>>(){}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}

