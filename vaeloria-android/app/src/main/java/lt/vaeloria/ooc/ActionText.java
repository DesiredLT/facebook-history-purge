package lt.vaeloria.ooc;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** Lithuanian aliases are used only to resolve an intent; game rules validate its target. */
final class ActionText {
    static String normalized(String value) {
        return Normalizer.normalize(value == null ? "" : value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }

    static boolean contains(String value, String... tokens) {
        String text = normalized(value);
        for (String token : tokens) if (text.contains(normalized(token))) return true;
        return false;
    }

    static boolean travel(String value) {
        return normalized(value).matches("^(?:(?:noriu|ketinu|bandau) )?(?:keliaut\\w*|vykti|eiti|vaziuot\\w*|grizt\\w*)(?: .*|$)");
    }

    static boolean social(String value) {
        return normalized(value).matches("^(?:(?:noriu|ketinu|bandau) )?(?:kalb\\w*|pasikalb\\w*|klaust\\w*|paklaus\\w*|susisiekt\\w*|paprasy\\w*|prasyt\\w*|deret\\w*)(?: .*|$)");
    }

    static boolean rest(String value) {
        return normalized(value).matches("^(?:(?:noriu|ketinu|bandau|trumpai) )?(?:poils\\w*|pails\\w*|ilset\\w*|mieg\\w*|laukti|palaukt\\w*|stovyklau\\w*|atsigau\\w*|atsikvep\\w*)(?: .*|$)");
    }

    static boolean mentions(String text, String name) {
        String query = normalized(text);
        String[] words = normalized(name).split(" +");
        if (words.length == 0 || words[0].isEmpty()) return false;
        for (String word : words) {
            String stem = stem(word);
            if (!Pattern.compile("(?<![a-z0-9])" + Pattern.quote(stem) + "[a-z]*(?![a-z0-9])").matcher(query).find()) return false;
        }
        return true;
    }

    static boolean mentionsPerson(String text, String name) {
        if (mentions(text, name)) return true;
        for (String word : normalized(name).split(" +")) {
            if (word.matches("kapitonas|regente|archyvaras|sarge")) continue;
            if (word.length() >= 3) return mentions(text, word);
        }
        return false;
    }

    private static String stem(String word) {
        if (word.length() < 4) return word;
        String result = word.replaceFirst("(?:uju|omis|uose|yje|iui|ius|iu|io|ai|as|is|ys|us|os|es|a|o|u|e|i)$", "");
        return result.length() >= 3 ? result : word;
    }

    private ActionText() {}
}
