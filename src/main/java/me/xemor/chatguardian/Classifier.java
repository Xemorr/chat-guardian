package me.xemor.chatguardian;

import java.util.Arrays;
import java.util.regex.Pattern;

public class Word2VecClassifier {

    private static final char[][] qwertyKeyboard = new char[][] {
                    {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'},
                    {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'},
                    {'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ';'},
                    {'z', 'x', 'c', 'v', 'b', 'n', 'm', ',', '.', '/'}
    };
    private static final Pattern notAlphanumerical = Pattern.compile("[^A-Za-z0-9]");
    private ClassifierConfig config;

    public Word2VecClassifier(ClassifierConfig config) {
        this.config = config;
    }

    public boolean[] classify(String message) {
        String[] split = notAlphanumerical.split(message.toLowerCase());
        boolean[] results = new boolean[split.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = classifyToken(split[i]);
        }
        return results;
    }

    private boolean classifyToken(String token) {
        for (String bannedWord : config.getBannedWords()) {
            if (token.length() > 3) { // stop silly scenario where any single letter is banned
                int distance = calculateDLDistance(token, bannedWord);
                if (distance <= 1) {
                    System.out.println(token + ", " + bannedWord + " : " + distance);
                    return true;
                }
            } else {
                if (bannedWord.equals(token)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int calculateDLDistance(String x, String y) {
        int xSize = x.length();
        int ySize = y.length();
        int[][] dp = new int[xSize + 1][ySize + 1];

        for (int i = 0; i <= xSize; i++) {
            for (int j = 0; j <= ySize; j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + charDistance(x.charAt(i - 1), y.charAt(j - 1)),
                            Math.min(dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1));
                }
            }
        }

        return dp[xSize][ySize];
    }

    public static int charDistance(char a, char b) {
        if (a == b) { return 0; }
        else {
            int[] aIndex = new int[]{-1, -1};
            int[] bIndex = new int[]{-1, -1};
            for (int i = 0; i < qwertyKeyboard.length; i++) {
                for (int j = 0; j < 10; j++) {
                    if (qwertyKeyboard[i][j] == a) {
                        aIndex = new int[]{i, j};
                    }
                    else if (qwertyKeyboard[i][j] == b) {
                        bIndex = new int[]{i, j};
                    }
                }
            }
            if (aIndex[0] == -1 || bIndex[0] == -1) {
                return 1; // be generous to characters not in alphabet
            }
            for (int i = -1; i <= 0; i += 1) {
                for (int j = -1; j <= 1; j++) {
                    if (Arrays.equals(new int[]{aIndex[0] + i, aIndex[1] + j}, bIndex)) {
                        return 1;
                    }
                }
            }
            return 2;
        }
    }



}
