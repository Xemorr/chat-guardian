package me.xemor.chatguardian;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;

public class ClassifierConfig {

    private String type;

    private final TreeSet<String> bannedWords = new TreeSet<>();

    public ClassifierConfig(ConfigurationSection section) {
        type = section.getString("type", "word2vec");
        Map<String, FloatVector> tokenToEmbedding;
        try {
            tokenToEmbedding = loadEmbeddings();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Set<String> keys = section.getConfigurationSection("filter").getKeys(false);
        List<WordSimilarity> filterList = new ArrayList<>();
        for (String key : keys) {
            filterList.add(new WordSimilarity(key, (float) section.getDouble("filter." + key), tokenToEmbedding.get(key)));
        }
        for (WordSimilarity filterElement : filterList) {
            for (Map.Entry<String, FloatVector> entry : tokenToEmbedding.entrySet()) {
                if (filterElement.embedding().cosineDistance(entry.getValue()) > filterElement.similarity()) {
                    bannedWords.add(entry.getKey());
                }
            }
        }
    }

    private Map<String, FloatVector> loadEmbeddings() throws IOException {
        Map<String, FloatVector> tokenToEmbedding = new HashMap<>();
        BufferedReader is = new BufferedReader(new FileReader(new File(JavaPlugin.getPlugin(ChatGuardian.class).getDataFolder(), "embeddings.txt")));
        is.lines().parallel().map((line) -> line.split(", ")).forEach((split) -> {
            String key = split[0];
            float[] embedding = new float[split.length];
            for (int i = 1; i < split.length; i++) {
                embedding[i] = Float.parseFloat(split[i]);
            }
            tokenToEmbedding.put(key, new FloatVector(embedding));
        });
        is.close();
        return tokenToEmbedding;
    }

    public Iterable<String> getBannedWords() {
        return bannedWords;
    }

    public record WordSimilarity(String word, float similarity, FloatVector embedding) {}

}
