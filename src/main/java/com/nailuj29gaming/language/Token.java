package com.nailuj29gaming.language;

/** 
 * Token stores a single token from the lexer. Based off CI Token class
 */
public class Token {
	private TokenType type;
	private String lexeme;
	private int line, column;
	private Object literal;
	
	
	/**
	 * @param type what kind of Token this is
	 * @param lexeme the text the Token covers
	 * @param line the line the token is on
	 * @param column the column the token occurs at
	 */
	public Token(TokenType type, String lexeme, int line, int column) {
		this(type, lexeme, line, column, null);
	}

	
	/**
	 * @param type what kind of Token this is
	 * @param lexeme the text the Token covers
	 * @param line the line the token is on
	 * @param column the column the token occurs at
	 * @param literal the value of the token
	 */
	public Token(TokenType type, String lexeme, int line, int column, Object literal) {
		this.type = type;
		this.lexeme = lexeme;
		this.line = line;
		this.column = column;
		this.literal = literal;
	}

	/**
	 * @return the type
	 */
	public TokenType getType() {
		return type;
	}

	/**
	 * @return the lexeme
	 */
	public String getLexeme() {
		return lexeme;
	}

	/**
	 * @param lexeme the lexeme to set
	 */
	public void setLexeme(String lexeme) {
		this.lexeme = lexeme;
	}

	/**
	 * @return the line
	 */
	public int getLine() {
		return line;
	}
	
	/**
	 * @return the column
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * @return the literal
	 */
	public Object getLiteral() {
		return literal;
	}

	@Override
	public String toString() {
		return "Token [type=" + type + ", lexeme=`" + lexeme + "`, line=" + line + ", literal=" + literal + "]";
	}
	
	
}
