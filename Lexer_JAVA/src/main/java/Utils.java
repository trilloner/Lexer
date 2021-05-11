import java.util.Arrays;

public class Utils {
    static boolean isKeyword(String input) {
        String[] keywords = new String[]{"import", "static", "enum", "class", "native", "transient", "public", "boolean", "abstract", "int"};
        return Arrays.asList(keywords).contains(input);
    }

    static boolean isSeparator(char input) {
        Character[] separators = new Character[]{'{', '}', ';', '='};
        return Arrays.stream(separators).anyMatch(character -> input == character);
    }
}
