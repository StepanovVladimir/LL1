package com.company;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Lexer {
    private int line;
    private int pos;
    private int position;
    private char chr;
    private String s;

    private Map<String, TokenType> keywords = new HashMap<>();

    public Lexer(String fileName) throws FileNotFoundException {
        File f = new File(fileName);
        Scanner s = new Scanner(f);
        StringBuilder source = new StringBuilder(" ");
        while (s.hasNext()) {
            source.append(s.nextLine()).append("\n");
        }

        this.line = 1;
        this.pos = 0;
        this.position = 0;
        this.s = source.toString();
        this.chr = this.s.charAt(0);
        this.keywords.put("if", TokenType.KeywordIf);
        this.keywords.put("else", TokenType.KeywordElse);
        this.keywords.put("print", TokenType.KeywordPrint);
        this.keywords.put("read", TokenType.KeywordRead);
        this.keywords.put("integer", TokenType.KeywordInteger);
        this.keywords.put("string", TokenType.KeywordString);
        this.keywords.put("hex", TokenType.KeywordHex);
        this.keywords.put("binary", TokenType.KeywordBinary);
        this.keywords.put("while", TokenType.KeywordWhile);
        this.keywords.put("return", TokenType.KeywordReturn);
    }

    public Token getToken() {
        int line, pos;
        while (Character.isWhitespace(this.chr)) {
            getNextChar();
        }
        line = this.line;
        pos = this.pos;

        switch (this.chr) {
            case '\u0000': return new Token(TokenType.EndOfInput, "", this.line, this.pos);
            case '/': return div_or_comment(line, pos);
            case '<': return follow('=', TokenType.LessEqual, TokenType.Less, line, pos);
            case '>': return follow('=', TokenType.GreaterEqual, TokenType.Greater, line, pos);
            case '=': return follow('=', TokenType.Equal, TokenType.Assign, line, pos);
            case '!': return follow('=', TokenType.NotEqual, TokenType.Not, line, pos);
            case '&': return follow('&', TokenType.And, TokenType.EndOfInput, line, pos);
            case '|': return follow('|', TokenType.Or, TokenType.EndOfInput, line, pos);
            case '"': return string(this.chr, line, pos);
            case '{': getNextChar(); return new Token(TokenType.Begin, "{", line, pos);
            case '}': getNextChar(); return new Token(TokenType.End, "}", line, pos);
            case '(': getNextChar(); return new Token(TokenType.LeftParentheses, "(", line, pos);
            case ')': getNextChar(); return new Token(TokenType.RightParentheses, ")", line, pos);
            case '+': getNextChar(); return new Token(TokenType.Plus, "+", line, pos);
            case '-': getNextChar(); return new Token(TokenType.Minus, "-", line, pos);
            case '*': getNextChar(); return new Token(TokenType.Multiply, "*", line, pos);
            case '%': getNextChar(); return new Token(TokenType.Mod, "%", line, pos);
            case ';': getNextChar(); return new Token(TokenType.Semicolon, ";", line, pos);
            case ',': getNextChar(); return new Token(TokenType.Comma, ",", line, pos);
            default: return identifier_or_integer(line, pos);
        }
    }

    public void printTokens() throws IOException {
        Token t;
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("out.txt"), StandardCharsets.UTF_8))) {
            while ((t = getToken()).tokentype != TokenType.EndOfInput) {
                out.write(String.valueOf(t));
                out.newLine();
            }
            out.write(String.valueOf(t));
            out.newLine();
        }
    }

    private void error(int line, int pos, String msg) {
        if (line > 0 && pos > 0) {
            System.out.printf("%s in line %d, pos %d\n", msg, line, pos);
        } else {
            System.out.println(msg);
        }
        System.exit(1);
    }

    private Token follow(char expect, TokenType ifyes, TokenType ifno, int line, int pos) {
        if (getNextChar() == expect) {
            getNextChar();
            if (ifyes == TokenType.Equal){
                return new Token(ifyes, "=" + expect, line, pos);
            } if (ifyes == TokenType.LessEqual) {
                return new Token(ifyes, "<" + expect, line, pos);
            } if (ifyes == TokenType.GreaterEqual) {
                return new Token(ifyes, ">" + expect, line, pos);
            } if (ifyes == TokenType.NotEqual) {
                return new Token(ifyes, "!" + expect, line, pos);
            }
            return new Token(ifyes, "", line, pos);
        }
        if (ifno == TokenType.EndOfInput) {
            return new Token(TokenType.Error, String.valueOf(expect), line, pos);
        }
        return new Token(ifno, String.valueOf(expect), line, pos);
    }

    private Token string(char start, int line, int pos) {
        StringBuilder result = new StringBuilder();
        while (getNextChar() != start) {
            if (this.chr == '\u0000'){
                return new Token(TokenType.EndOfInput, "", this.line, this.pos);
            } else {
                result.append(this.chr);
            }

        }
        getNextChar();
        return new Token(TokenType.String, result.toString(), line, pos);
    }

    private Token div_or_comment(int line, int pos) {
        if (getNextChar() != '*') {
            return new Token(TokenType.Divide, "/", line, pos);
        }
        getNextChar();

        while (true) {
            if (this.chr == '\u0000'){
                return new Token(TokenType.EndOfInput, "", this.line, this.pos);
            } else {
                if (this.chr == '*') {
                    if (getNextChar() == '/') {
                        getNextChar();
                        return getToken();
                    }
                } else {
                    getNextChar();
                }
            }
        }
    }

    private Token identifier_or_integer(int line, int pos) {
        boolean is_number = true;
        StringBuilder text = new StringBuilder();

        while (Character.isAlphabetic(this.chr) || Character.isDigit(this.chr) || Character.toString(this.chr).matches("[$:?@#'.^+-]")) {
            text.append(this.chr);
            if (!Character.isDigit(this.chr)) {
                is_number = false;
            }
            getNextChar();
        }

        if (text.toString().equals("")) {
            error(line, pos, String.format("error: (%d) '%c'", (int)this.chr, this.chr));
        }

        if (text.toString().matches("^0b[01]{1,32}")){
            return new Token(TokenType.Binary, text.toString(), line, pos);
        }

        if (text.toString().matches("0x([0-9a-fA-F]{1,8})")){
            return new Token(TokenType.Hexadecimal, text.toString(), line, pos);
        }

        if (text.toString().matches("[-+]?[0-9]{0,6}\\.[0-9]{1,15}([eE][-+][0-9]{1,6})")){
            return new Token(TokenType.FloatPointNumber, text.toString(), line, pos);
        }

        if (text.toString().matches("^[-+]?[0-9]{0,15}\\.[0-9]{1,15}$")){
            return new Token(TokenType.FloatPointNumber, text.toString(), line, pos);
        }

        if (Character.isDigit(text.charAt(0))) {
            if (!is_number) {
                return new Token(TokenType.Error, text.toString(), line, pos);
            }
            return new Token(TokenType.Integer, text.toString(), line, pos);
        }

        if (this.keywords.containsKey(text.toString())) {
            return new Token(this.keywords.get(text.toString()), text.toString(), line, pos);
        }

        if (text.toString().matches("[A-Za-z][A-Za-z0-9_]{0,30}")) {
            return new Token(TokenType.Identifier, text.toString(), line, pos);
        } else {
            return new Token(TokenType.Error, text.toString(), line, pos);
        }

    }

    private char getNextChar() {
        this.pos++;
        this.position++;
        if (this.position >= this.s.length()) {
            this.chr = '\u0000';
            return this.chr;
        }
        this.chr = this.s.charAt(this.position);
        if (this.chr == '\n') {
            this.line++;
            this.pos = 0;
        }
        return this.chr;
    }
}
