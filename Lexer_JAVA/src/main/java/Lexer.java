
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Lexer {
    File file;
    StringBuilder stringBuilder = new StringBuilder();
    TokenType state = TokenType.START;

    public Lexer(String filename) {
        file = new File(filename);
    }

    public int startAnalyze() {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
            int characterCode = bufferedInputStream.read();
            while (characterCode != -1) {
                char character = (char) characterCode;
                if (Character.isLetter(character)) {
                    stringBuilder.append(character);
                    state = TokenType.KEYWORD;
                }
                if (Character.isDigit(character)) {
                    stringBuilder.append(character);
                    state = TokenType.NUMBER;
                }
                if (Character.isWhitespace(character)) {
                    String word = new String(stringBuilder);
                    if (Utils.isKeyword(word)) {
                        Token token = new Token(state, word);
                        System.out.println(token);
                    } else {
                        Token token = new Token(TokenType.IDENTIFIER, word);
                        System.out.println(token);
                    }
                    stringBuilder = new StringBuilder();
                }
                if (Utils.isSeparator(character)) {
                    Token token = new Token(TokenType.SEPARATOR, String.valueOf(character));
                    System.out.println(token);
                }

                characterCode = bufferedInputStream.read();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }

}

