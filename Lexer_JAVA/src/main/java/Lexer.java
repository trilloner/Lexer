
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Lexer {
    File file;
    StringBuilder buffer = new StringBuilder();
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
                char character = (char) characterCode;
                analyzeChar(character);

                characterCode = bufferedInputStream.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tokens;
    }

    private void analyzeChar(char character) {


        if (isCharacter()) {

            processCharacter(character);

        } else if (isComment()) {

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

    private void processCharacter(char character) {
        if (character == '\'') {
            if (buffer.length() == 1) {
                tokens.add(createToken(TokenType.CHARACTER, new String(buffer)));
            } else {
                tokens.add(createToken(TokenType.ERROR, new String(buffer)));
            }
            initBuffer();
            stateMachine.setCurrentState(States.START);
        } else {
            buffer.append(character);
        }
    }

    private boolean isCharacter() {
        return stateMachine.getCurrentState() == States.CHARACTER;
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
                buffer.append(character);
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
                || stateMachine.getPreviousState() == States.MULTILINE_COMMENT
                || stateMachine.getCurrentState() == States.END_COMMENT;
    }


    private void processOperator(char operator) {
        States currentState = stateMachine.getCurrentState();
        States previousState = stateMachine.getPreviousState();

        if (currentState == States.OPERATOR && previousState == States.OPERATOR) {
            tokens.add(createToken(TokenType.ERROR, String.valueOf(operator)));
            return;
        }

        if (operator == '-') {
            if (currentState != States.NUMBER) {
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
        stateMachine.setCurrentState(States.LETTER);
    }

    private void processDigit(char number) {
        States currentState = stateMachine.getCurrentState();
        if (currentState == States.LETTER) {
            stateMachine.setCurrentState(States.LETTER);
        } else {
            stateMachine.setCurrentState(States.NUMBER);
        }
        buffer.append(number);
    }

    private void processWhitespace() {
        String word = new String(buffer);
        States currentState = stateMachine.getCurrentState();
        if (Utils.isKeyword(word)) {
            tokens.add(createToken(TokenType.KEYWORD, word));
        } else if (currentState == States.NUMBER) {
            tokens.add(createToken(TokenType.NUMBER, word));
        } else if (!word.equals("")) {
            tokens.add(createToken(TokenType.IDENTIFIER, word));
        }
        initBuffer();
    }


    private void processSeparator(char separator) {
        String word = new String(buffer);
        States currentState = stateMachine.getCurrentState();

        if (currentState == States.DOT || occurrencesCount('.') > 1) {
            stateMachine.setCurrentState(States.START);
            tokens.add(createToken(TokenType.ERROR, new String(buffer)));
            initBuffer();
            return;
        } else if (separator == '"' && currentState != States.STRING) {
            stateMachine.setCurrentState(States.STRING);
            return;
        } else if (separator == '\'') {
            stateMachine.setCurrentState(States.CHARACTER);
            return;
        } else if (separator == '.' && currentState == States.NUMBER) {
            buffer.append(separator);
            stateMachine.setCurrentState(States.DOT);
            return;
        } else if (Utils.isKeyword(word)) {
            tokens.add(createToken(TokenType.KEYWORD, word));
        } else if (currentState == States.NUMBER) {
            tokens.add(createToken(TokenType.NUMBER, word));
        } else if (!word.equals("")) {
            tokens.add(createToken(TokenType.IDENTIFIER, word));
        }
        tokens.add(createToken(TokenType.SEPARATOR, String.valueOf(separator)));
        stateMachine.setCurrentState(States.SEPARATOR);
        initBuffer();
    }

    private long occurrencesCount(char character) {
        return buffer.toString()
                .chars()
                .filter(ch -> ch == character)
                .count();
    }

    public void initBuffer() {
        buffer = new StringBuilder();
    }

    private Token createToken(TokenType type, String value) {
        return new Token(type, value);
    }

    public void printSortedTokens() {
        tokens.stream()
                .sorted(new Comparator<Token>() {
            @Override
            public int compare(Token o1, Token o2) {
                if (o1.getType() == o2.getType() && o1.getValue().equals(o2.getValue())) {
                    return 0;
                } else if (o1.getType().ordinal() >= o2.getType().ordinal()) {
                    return 1;
                }
                return -1;

            }
        })
                .forEach(System.out::println);
    }
}

