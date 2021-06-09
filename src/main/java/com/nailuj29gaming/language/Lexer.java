package com.nailuj29gaming.language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lexes a string into a list of tokens
 */
public class Lexer {
	private List<Token> tokens;
	private int current, start, line, column;
	private String source;
	private static Map<String, TokenType> keywords;
	
	static {
		keywords = new HashMap<>();
		keywords.put("fn", TokenType.FN);
		keywords.put("var", TokenType.VAR);
		keywords.put("if", TokenType.IF);
		keywords.put("else", TokenType.ELSE);
		keywords.put("while", TokenType.WHILE);
		keywords.put("for", TokenType.FOR);
		keywords.put("loop", TokenType.LOOP);
		keywords.put("return", TokenType.RETURN);
		keywords.put("nil", TokenType.NIL);
		keywords.put("true", TokenType.TRUE);
		keywords.put("false", TokenType.FALSE);
		keywords.put("break", TokenType.BREAK);
		keywords.put("continue", TokenType.CONTINUE);
		keywords.put("import", TokenType.IMPORT);
		keywords.put("NaN", TokenType.NAN);
		keywords.put("infinity", TokenType.INFINITY);
		keywords.put("in", TokenType.IN);
	}

	/**
	 * Lexes source code into a token stream
	 * @param source the source code to lex
	 * @return the tokens
	 */
	public List<Token> lex(String source) {
		tokens = new ArrayList<>();
		current = start = 0;
		line = column = 1;
		this.source = source;
		while (!isAtEnd()) {
			start = current;
			lexToken();
		}
		start = current;
		tokens.add(makeToken(TokenType.EOF));
		return tokens;
	}

	/**
	 * Lex a single token and add it to the token stream
	 */
	private void lexToken() {
		char c = advance();
		switch (c) {
		case '+':
			tokens.add(makeToken(TokenType.PLUS));
			break;
		case '-':
			tokens.add(makeToken(TokenType.MINUS));
			break;
		case '*':
			tokens.add(makeToken(TokenType.STAR));
			break;
		case '/':
			if (peek() == '/') {
				while (!isAtEnd() && peek() != '\n' && peek() != '\r') {
					advance();
				}
			} else if (peek() == '*') {
				multilineComment();
			} else {
				tokens.add(makeToken(TokenType.SLASH));
			}
			break;
		case '%':
			tokens.add(makeToken(TokenType.PERCENT));
		case '<':
			tokens.add(makeToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS));
			break;
		case '>':
			tokens.add(makeToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER));
			break;
		case '=':
			tokens.add(makeToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUALS));
			break;
		case '!':
			tokens.add(makeToken(match('=') ? TokenType.NOT_EQUAL : TokenType.NOT));
			break;
		case '&':
			tokens.add(makeToken(TokenType.AND));
			break;
		case '|':
			tokens.add(makeToken(TokenType.OR));
			break;
		case '(':
			tokens.add(makeToken(TokenType.PAREN_LEFT));
			break;
		case ')':
			tokens.add(makeToken(TokenType.PAREN_RIGHT));
			break;
		case '{':
			tokens.add(makeToken(TokenType.BRACE_LEFT));
			break;
		case '}':
			tokens.add(makeToken(TokenType.BRACE_RIGHT));
			break;
		case '[':
			tokens.add(makeToken(TokenType.BRACKET_LEFT));
			break;
		case ']':
			tokens.add(makeToken(TokenType.BRACKET_RIGHT));
			break;
		case ',':
			tokens.add(makeToken(TokenType.COMMA));
			break;
		case '.':
			tokens.add(makeToken(TokenType.DOT));
			break;
		case ';':
			tokens.add(makeToken(TokenType.SEMICOLON));
			break;
		case '"':
		case '\'':
			string(c);
			break;
		default:
			if (isWhiteSpace(c)) break;
			if (isDigit(c)) {
				number();
				break;
			}
			if (isAlpha(c)) {
				identifier();
				break;
			}
			Main.error("Invalid Character: " + c, line, column);
			System.exit(1);
		}
	}
	
	/**
	 * Ignore a multline comment, with nesting
	 */
	private void multilineComment() {
		int nesting = 1;
		while (nesting > 0) {
			char c = advance();
			isWhiteSpace(c);
			column++; // Still keep track of the column and line
			if (c == '*') {
				if (match('/')) {
					nesting--;
				}
			}
			
			if (c == '/') {
				if (match('*')) {
					nesting++;
				}
			}

			if (isAtEnd() && nesting != 0) {
				Main.error("Unexpected EOF", line, column);
				System.exit(1);
			}
		}
		
	}
	
	/**
	 * Lex a string
	 * @param c the character used to start the string, ' or "
	 */
	private void string(char c) {
		while (!isAtEnd() && peek() != c) {
			if (advance() == '\n') {
				Main.error("Unterminated string", line, column);
				System.exit(1);
			}
		}
		
		if (isAtEnd()) {
			Main.error("Unterminated string", line, column);
			System.exit(1);
		}
		advance();
		tokens.add(makeToken(TokenType.STRING, source.substring(start + 1, current - 1).replace("\\n", "\n")));
	}
	
	/**
	 * Lex an identifier
	 */
	private void identifier() {
		while (isValidIdentifier(peek())) {
			advance();
		}
		
		TokenType type = keywords.getOrDefault(source.substring(start, current), TokenType.IDENTIFIER);
		tokens.add(makeToken(type));
	}
	/**
	 * Lex a number
	 */
	private void number() {
		while (isDigit(peek())) {
			advance();
		}
		if (match('.')) {
			while (isDigit(peek())) {
				advance();
			}
		}
		
		double value = Double.parseDouble(source.substring(start, current));
		tokens.add(makeToken(TokenType.NUMBER, value));
	}
	
	/**
	 * Checks if a character is whitespace, and handles tabs in counting columns
	 * @param c the character to check
	 * @return whether or not the character is whitespace
	 */
	private boolean isWhiteSpace(char c) {
		if (c == '\n') {
			line++;
			column = 1;
			return true;
		}
		if (c == '\t') {
			column += 3;
			return true;
		}
		return (c == ' ' || c == '\r');
	}
	
	/**
	 * Determines whether or not a character is a digit
	 * @param c the character to check
	 * @return whether or not it is a digit
	 */
	private boolean isDigit(char c) {
		return (c >= '0' && c <= '9');
	}
	
	/**
	 * Determines if a character is in the alphabet
	 * @param c the character to check
	 * @return whether or not the character is in the alphabet
	 */
	private boolean isAlpha(char c) {
		return ((c >= 'a' && c <= 'z') ||
				(c >= 'A' && c <= 'Z'));
	}
	
	/**
	 * Determines if a character is valid in an identifier
	 * A character has to be alphabetic to start an identifier, but any alphanumberic character or an underscore can be in the rest of it
	 * @param c the character to check
	 * @return whether or not the character is valid
	 */
	private boolean isValidIdentifier(char c) {
		return isDigit(c) || isAlpha(c) || c == '_';
	}
	
	/**
	 * creates a token from a type and literal
	 * @param type the token's type
	 * @param literal the token's literal
	 * @return the token
	 */
	private Token makeToken(TokenType type, Object literal) {
		return new Token(type, source.substring(start, current), line, column, literal);
	}

	/**
	 * Creates a token from a type
	 * @param type the token's type
	 * @return the token
	 */
	private Token makeToken(TokenType type) {
		return makeToken(type, null);
	}

	/**
	 * Checks if the lexer has consumed all the source code
	 * @return whether or not the lexer has consumed all of the source code
	 */
	private boolean isAtEnd() {
		return current >= source.length();
	}
	/**
	 * Advances the lexer forward
	 * @return the next character
	 */
	private char advance() {
		if (isAtEnd()) return '\0';
		column++;
		return source.charAt(current++);
	}
	
	/**
	 * @return the current character
	 */
	private char peek() {
		if (isAtEnd()) return '\0';
		return source.charAt(current);
	}

	/**
	 * Conditional advance
	 * @param next the char to advance on
	 * @return whether or not the lexer advanced
	 */
	private boolean match(char next) {
		if (isAtEnd()) return false;
		if (source.charAt(current) == next) {
			advance();
			return true;
		}
		return false;
	}
}
