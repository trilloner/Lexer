
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Lexer {
    File file;
    StringBuilder buffer = new StringBuilder();
    TokenType state = TokenType.NO_TYPE;
    ArrayList<Token> tokens;
    StateMachine stateMachine;

    public Lexer(String filename) {
        file = new File(filename);
        tokens = new ArrayList<>();
        stateMachine = new StateMachine();
    }

    public ArrayList<Token> tokenize() {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
            int characterCode = bufferedInputStream.read();
            while (characterCode != -1) {

                analyzeChar(characterCode);

                characterCode = bufferedInputStream.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tokens;
    }

    private void analyzeChar(int input) {
        char character = (char) input;
        if (state == TokenType.STRING) {
            if (character == '"') {
                tokens.add(createToken(TokenType.STRING, new String(buffer)));
                initBuffer();
                state = TokenType.NO_TYPE;
            } else {
                buffer.append(character);
            }
        } else {
            if (character == '"') {

                state = TokenType.STRING;
                initBuffer();

            } else if (isLetter(character)) {

                processLetter(character);
                state = TokenType.KEYWORD;

            } else if (isDigit(character)) {

                processDigit(character);
                state = TokenType.NUMBER;

            } else if (isWhitespace(character)) {

                processWhitespace();

            } else if (isSeparator(character)) {

                processSeparator(character);


            } else if (isOperator(character)) {

                processOperator(character);

            }

        }
    }


    private void processOperator(char operator) {
        States state = stateMachine.getCurrentState();
        if (operator == '-') {
            if (state != States.NUMBER) {
                stateMachine.setCurrentState(States.MINUS);
                buffer.append(operator);
            } else {
                stateMachine.setCurrentState(States.OPERATOR);
                tokens.add(createToken(TokenType.OPERATOR, String.valueOf(operator)));
                initBuffer();
            }
        } else {
            stateMachine.setCurrentState(States.OPERATOR);
            tokens.add(createToken(TokenType.OPERATOR, String.valueOf(operator)));
            initBuffer();
        }

    }

    private boolean isLetter(char input) {
        return Character.isLetter(input) || input == '_' || input == '$';
    }

    private boolean isSeparator(char input) {
        return Utils.isSeparator(input);
    }

    private boolean isOperator(char input) {
        return Utils.isOperator(input);
    }

    private boolean isDigit(char input) {
        return Character.isDigit(input);
    }

    private boolean isWhitespace(char input) {
        return input == ' ';
    }

    private void processLetter(char letter) {
        buffer.append(letter);
    }

    private void processDigit(char number) {
        buffer.append(number);
        stateMachine.setCurrentState(States.NUMBER);
    }

    private void processWhitespace() {
        String word = new String(buffer);
        if (Utils.isKeyword(word)) {
            tokens.add(createToken(state, word));
        } else {
            tokens.add(createToken(TokenType.IDENTIFIER, word));
        }
        initBuffer();
    }


    private void processSeparator(char separator) {
        String word = new String(buffer);
        States state = stateMachine.getCurrentState();
        if (Utils.isKeyword(word)) {
            tokens.add(createToken(TokenType.KEYWORD, word));
        }
        if (state == States.NUMBER) {
            tokens.add(createToken(TokenType.NUMBER, word));
        } else {
            tokens.add(createToken(TokenType.IDENTIFIER, word));
        }
        tokens.add(createToken(TokenType.SEPARATOR, String.valueOf(separator)));
        initBuffer();
    }

    public void initBuffer() {
        buffer = new StringBuilder();
    }

    private Token createToken(TokenType type, String value) {
        return new Token(type, value);
    }

}

