public class Main {
    public static void main(String[] args) {
        Lexer testedInstance = new Lexer("src/main/resources/input.txt");
        testedInstance.tokenize().forEach(System.out::println);

    }
}
