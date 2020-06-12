package com.company;

public class Token {
    public TokenType tokentype;
    public String value;
    public int line;
    public int pos;

    Token(TokenType token, String value, int line, int pos) {
        this.tokentype = token;
        this.value = value;
        this.line = line;
        this.pos = pos;
    }

    @Override
    public String toString() {
        String result = String.format("%-20s %5d  %5d  %s", this.tokentype, this.line, this.pos, this.value);
        if (this.tokentype == TokenType.String || this.tokentype == TokenType.Integer || this.tokentype == TokenType.Identifier){
            result = String.format("%-20s %5d  %5d ", this.tokentype, this.line, this.pos);
        }
        switch (this.tokentype) {
            case Integer:
                result += String.format("%4s", value);
                break;
            case Identifier:
                result += String.format(" %s", value);
                break;
            case String:
                result += String.format(" \"%s\"", value);
                break;
        }
        return result;
    }
}
