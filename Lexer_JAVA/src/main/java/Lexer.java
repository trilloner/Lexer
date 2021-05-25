import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Lexer {
    File file;
    StringBuilder buffer = new StringBuilder();
    ArrayList<Token> tokens;
    StateMachine stateMachine;
    boolean isCharacter = false;

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

    private void analyzeChar(char input) {


        if (isCharacter()) {

            processCharacter(input);

        } else if (isComment()) {

            processComment(input);

        } else if (isString()) {

            processString(input);

        } else if (isLetter(input)) {

            processLetter(input);

        } else if (isDigit(input)) {

            processDigit(input);


        } else if (isWhitespace(input)) {

            processWhitespace();

        } else if (isSeparator(input)) {

            processSeparator(input);


        } else if (isOperator(input)) {

            processOperator(input);

        }

    }

    private void processCharacter(char character) {
        States currentState = stateMachine.getCurrentState();
        if (character == '\'') {
            if (buffer.length() == 1 || isUnicode() || currentState == States.SPECIFIC_CHAR) {
                if (new String(buffer).equals("\\")) {
                    tokens.add(createToken(TokenType.ERROR, new String(buffer)));
                }
                tokens.add(createToken(TokenType.CHARACTER, new String(buffer)));
            } else if (buffer.length() > 1) {
                if (isCharacterNumber()) {
                    tokens.add(createToken(TokenType.CHARACTER, new String(buffer)));
                } else {
                    tokens.add(createToken(TokenType.ERROR, new String(buffer)));
                }
            }
            initBuffer();
            stateMachine.setCurrentState(States.START);
            isCharacter = false;
        } else if (character == 'u' && currentState == States.BACKSLASH) {
            stateMachine.setCurrentState(States.UNICODE_CHAR);
            buffer.append(character);
        } else if (currentState == States.BACKSLASH && isSpecificSymbol(character)) {
            stateMachine.setCurrentState(States.SPECIFIC_CHAR);
            buffer.append(character);
        } else if (character == '\\') {
            stateMachine.setCurrentState(States.BACKSLASH);
            buffer.append(character);
        } else {
            buffer.append(character);
        }
    }

    private boolean isCharacterNumber() {
        return new String(buffer).matches("\\\\[1-3][0-6][0-7]");
    }

    private boolean isSpecificSymbol(char input) {
        Character[] symbols = {'n', 't', 'r', 'f', 'b', '\\'};
        return Arrays.asList(symbols).contains(input);
    }

    private boolean isUnicode() {
        return new String(buffer).matches("\\\\[u|U][1-9]{4}");
    }

    private boolean isCharacter() {
        return isCharacter;
    }

    private void processComment(char character) {
        States currentState = stateMachine.getCurrentState();
        States prevState = stateMachine.getPreviousState();

        if (currentState == States.MULTILINE_COMMENT || prevState == States.MULTILINE_COMMENT) {
            if (character == '*') {
                stateMachine.setCurrentState(States.END_COMMENT);
            } else if (character == '/') {
                if (currentState == States.END_COMMENT) {
                    buffer.append(character);
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
                buffer.append(operator);
            }
        } else if (operator == '*') {
            if (stateMachine.getCurrentState() == States.BACKSLASH) {
                stateMachine.setCurrentState(States.MULTILINE_COMMENT);
                buffer.append(operator);
            }
        } else if (currentState == States.OPERATOR) {
            stateMachine.setCurrentState(States.SECOND_OPERATOR);
            buffer.append(operator);
            tokens.add(createToken(TokenType.OPERATOR, new String(buffer)));
            initBuffer();
        } else {
            stateMachine.setCurrentState(States.OPERATOR);
            buffer.append(operator);
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
        States currentState = stateMachine.getCurrentState();
        if (currentState == States.ZERO && letter == 'X' || letter == 'x') {
            stateMachine.setCurrentState(States.NUMBER);
        } else if (currentState == States.NUMBER && letter == 'E' || letter == 'e') {
            stateMachine.setCurrentState(States.EXPONENTIAL);
        } else {
            stateMachine.setCurrentState(States.LETTER);
        }
        buffer.append(letter);
    }

    private void processDigit(char number) {
        States currentState = stateMachine.getCurrentState();
        if (currentState == States.LETTER) {
            stateMachine.setCurrentState(States.LETTER);
        } else if (number == '0') {
            stateMachine.setCurrentState(States.ZERO);
        } else if (currentState == States.EXPONENTIAL) {
            stateMachine.setCurrentState(States.EXPONENTIAL);
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
        } else if (currentState == States.OPERATOR || currentState == States.BACKSLASH) {
            tokens.add(createToken(TokenType.OPERATOR, word));
        } else if (!word.equals("")) {
            tokens.add(createToken(TokenType.IDENTIFIER, word));
        }
        initBuffer();
    }


    private void processSeparator(char separator) {
        String word = new String(buffer);
        States currentState = stateMachine.getCurrentState();

        if (occurrencesCount('.') > 1) {
            stateMachine.setCurrentState(States.START);
            tokens.add(createToken(TokenType.ERROR, new String(buffer)));
            initBuffer();
            return;
        } else if (separator == '"' && currentState != States.STRING) {
            stateMachine.setCurrentState(States.STRING);
            return;
        } else if (separator == '\'') {
            stateMachine.setCurrentState(States.CHARACTER);
            isCharacter = true;
            return;
        } else if (separator == '.' && currentState == States.NUMBER) {
            buffer.append(separator);
            stateMachine.setCurrentState(States.DOT);
            return;
        } else if (Utils.isKeyword(word)) {
            tokens.add(createToken(TokenType.KEYWORD, word));
        } else if (currentState == States.NUMBER || currentState == States.DOT) {
            tokens.add(createToken(TokenType.NUMBER, word));
        } else if (currentState == States.EXPONENTIAL) {
            if (isCorrectExponential()) {
                tokens.add(createToken(TokenType.EXPONENTIAL, new String(buffer)));
            } else {
                tokens.add(createToken(TokenType.ERROR, new String(buffer)));
            }
        } else if (!word.equals("")) {
            tokens.add(createToken(TokenType.IDENTIFIER, word));
        }
        tokens.add(createToken(TokenType.SEPARATOR, String.valueOf(separator)));
        stateMachine.setCurrentState(States.SEPARATOR);
        initBuffer();
    }

    private boolean isCorrectExponential() {
        return new String(buffer).matches("\\d{1,}[e|E]\\d{1,255}");
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
                        if (o1.getType().ordinal() == o2.getType().ordinal()) {
                            return 0;
                        } else if (o1.getType().ordinal() > o2.getType().ordinal()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                })
                .forEach(System.out::println);
    }
}

