
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

        if (isComment()) {

            processComment(character);

        } else if (isString()) {

            processString(character);

        } else if (isLetter(character)) {

            processLetter(character);

        } else if (isDigit(character)) {

            processDigit(character);


        } else if (isWhitespace(character)) {

            processWhitespace();

        } else if (isSeparator(character)) {

            processSeparator(character);


        } else if (isOperator(character)) {

            processOperator(character);

        }

    }

    private void processComment(char character) {
        States currentState = stateMachine.getCurrentState();
        States prevState = stateMachine.getPreviousState();

        if (currentState == States.MULTILINE_COMMENT || prevState == States.MULTILINE_COMMENT) {
            if (character == '*') {
                stateMachine.setCurrentState(States.END_COMMENT);
            } else if (character == '/') {
                if (currentState == States.END_COMMENT) {
                    tokens.add(createToken(TokenType.COMMENT, new String(buffer)));
                    stateMachine.setCurrentState(States.START);
                    initBuffer();
                }

            }
            buffer.append(character);
        } else {
            if (character == '\n') {
                tokens.add(createToken(TokenType.COMMENT, new String(buffer)));
                stateMachine.setCurrentState(States.START);
                stateMachine.setPreviousState(States.START);
                initBuffer();
                return;
            }
            buffer.append(character);
        }

    }

    private boolean isComment() {
        return stateMachine.getCurrentState() == States.ONELINE_COMMENT
                || stateMachine.getPreviousState() == States.ONELINE_COMMENT
                || stateMachine.getCurrentState() == States.MULTILINE_COMMENT
                || stateMachine.getPreviousState() == States.MULTILINE_COMMENT;
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
        } else if (operator == '/') {
            if (stateMachine.getCurrentState() == States.BACKSLASH) {
                stateMachine.setCurrentState(States.ONELINE_COMMENT);
            } else {
                stateMachine.setCurrentState(States.BACKSLASH);
            }
        } else if (operator == '*') {
            if (stateMachine.getCurrentState() == States.BACKSLASH) {
                stateMachine.setCurrentState(States.MULTILINE_COMMENT);
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

    private boolean isString() {
        return stateMachine.getCurrentState() == States.STRING;
    }

    private void processString(char input) {
        if (input == '"') {
            stateMachine.setCurrentState(States.START);
            tokens.add(createToken(TokenType.STRING, new String(buffer)));
            initBuffer();
        } else {
            buffer.append(input);
        }
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
        if (separator == '"' && state != States.STRING) {
            stateMachine.setCurrentState(States.STRING);
            return;
        } else if (Utils.isKeyword(word)) {
            tokens.add(createToken(TokenType.KEYWORD, word));
        } else if (state == States.NUMBER) {
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

