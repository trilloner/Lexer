public class Main {
    public static void main(String[] args) {
        Lexer testedInstance = new Lexer("src/main/resources/input2.txt");
        testedInstance.tokenize().forEach(System.out::println);
        System.out.println("\n\n\n");
        testedInstance.printSortedTokens();

        int a = '\057';
        int b = 0x112;
        a = 12 / b;
        a /= b;
        double c = 23434345345345344534534456456456456456456456456456456e255;
        System.out.println(c);

    }
}
