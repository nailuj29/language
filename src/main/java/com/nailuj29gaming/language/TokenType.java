package com.nailuj29gaming.language;

/**
 * The type of token
 */
public enum TokenType {
	// Literals
	STRING, NUMBER, TRUE, FALSE, NIL,
	// Operators
	PLUS, MINUS, STAR, SLASH, PERCENT, // Arithmetic
	AND, OR, NOT, // Logical
	LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, EQUAL_EQUAL, NOT_EQUAL, // Comparison
	// Keywords
	FN, VAR, IF, ELSE, WHILE, FOR, RETURN, BREAK, CONTINUE, LOOP, IMPORT, NAN, INFINITY, IN,
	// Misc
	IDENTIFIER, PAREN_LEFT, PAREN_RIGHT, BRACE_LEFT, BRACE_RIGHT, BRACKET_LEFT, BRACKET_RIGHT, COMMA, SEMICOLON, EQUALS, DOT, EOF
}
