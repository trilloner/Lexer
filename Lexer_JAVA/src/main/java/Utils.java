import java.util.Arrays;

public class Utils {
    static boolean isKeyword(String input) {
        String[] keywords = new String[]{"abstract", "assert", "boolean", "break", "byte", "switch",
                "case", "try", "catch", "finally", "char", "class", "continue", "default", "do", "double", "if", "else",
                "enum", "extends", "final", "float", "for", "implements", "import", "instanceOf", "int", "interface", "long",
                "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
                "super", "synchronized", "this", "throw", "throws", "transient", "void", "volatile", "while", "goto", "const"};
        return Arrays.asList(keywords).contains(input);
    }

    static boolean isSeparator(char input) {
        Character[] separators = new Character[]{'{', '}', '[', ']', ';', ':', '"', ',', '/', ' ', '\t', '(', ')', '.'};
        return Arrays.stream(separators)
                .anyMatch(character -> input == character);
    }

    static boolean isOperator(char input) {
        Character[] operators = new Character[]{'-', '+', '=', '<', '>', '?', '!', '%', '&', '*', '^', '|', '/'};
        return Arrays.stream(operators)
                .anyMatch(character -> input == character);

    }
}
