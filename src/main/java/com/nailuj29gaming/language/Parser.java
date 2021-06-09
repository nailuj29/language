package com.nailuj29gaming.language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nailuj29gaming.language.ast.Expr;
import com.nailuj29gaming.language.ast.Stmt;

/**
 * This class turns the tokens into an AST.
 * Recursive descent parser
 * @see Token
 * @see Lexer
 */
public class Parser {
	/**
	 * An error that occurs during parsing.
	 */
	public static class ParseError extends RuntimeException {

		private static final long serialVersionUID = -5855126947276871289L;
		
		private Token token;
		private String message;
		
		/**
		 * @param token the token the error occurred at
		 * @param message the error message
		 */
		public ParseError(Token token, String message) {
			super(message);
			this.token = token;
			this.message = message;
		}
		
		/**
		 * @return the token
		 */
		public Token getToken() {
			return token;
		}
		/**
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}
		
	}

	private List<Token> tokens;
	private int current;

	/**
	 * Parses a token list into a list of statements
	 * @param tokens the list of tokens
	 * @return a list of statements
	 */
	public List<Stmt> parse(List<Token> tokens) {
		this.tokens = tokens;
		
		List<Stmt> stmts = new ArrayList<Stmt>();
		while (!isAtEnd()) {
			stmts.add(statement());
		}
		return stmts;
	}
	
	/**
	 * Parse a single statement
	 * @return the statement parsed
	 */
	private Stmt statement() {
		if (match(TokenType.BRACE_LEFT)) {
			return blockStatement();
		}
		
		if (match(TokenType.BREAK)) {
			Stmt statement = new Stmt.Break(previous());
			consume(TokenType.SEMICOLON, "Expect ';' after expression");
			return statement;
		}
		
		if (match(TokenType.CONTINUE)) {
			Stmt statement = new Stmt.Continue(previous());
			consume(TokenType.SEMICOLON, "Expect ';' after expression");
			return statement;
		}
		
		if (match(TokenType.FOR)) {
			return forStatement();
		}
		
		if (match(TokenType.FN) && check(TokenType.IDENTIFIER)) {
			return functionDeclaration();
		}
		
		if (match(TokenType.IF)) {
			return ifStatement();
		}
		
		if (match(TokenType.IMPORT)) {
			return importStatement();
		}
		
		if (match(TokenType.LOOP)) {
			return infiniteLoop();
		}
		
		if (match(TokenType.RETURN)) {
			return returnStatement();
		}
		
		if (match(TokenType.WHILE)) {
			return whileStatement();
		}
		
		if (match(TokenType.VAR)) {
			return varStatement();
		}
		
		return expressionStatement();
	}
	
	/**
	 * Parse a single import statement
	 * @return the statement parsed
	 */
	private Stmt importStatement() {
		Token identifier = consume(TokenType.IDENTIFIER, "Expect an identifier after 'import'");
		consume(TokenType.SEMICOLON, "Expect ';' after statement");
		
		return new Stmt.Import(identifier);
	}
	
	/**
	 * Parse a single return statement
	 * @return the statement parsed
	 */
	private Stmt returnStatement() {
		Token keyword = previous();
		if (match(TokenType.SEMICOLON)) {
			return new Stmt.Return(keyword, null);
		}
		Expr expr = expression();
		consume(TokenType.SEMICOLON, "Expect ';' after return");
		return new Stmt.Return(keyword, expr);
	}
	
	/**
	 * Parse a function declaration
	 * @return a statement defining the function
	 */
	private Stmt functionDeclaration() {
		Token name = consume(TokenType.IDENTIFIER, "This should never happen, please report a bug");
		consume(TokenType.PAREN_LEFT, "Expect '(' after function name");
		List<String> params = new ArrayList<>();
		if (!match(TokenType.PAREN_RIGHT)) {
			while(previous().getType() != TokenType.PAREN_RIGHT) {
				params.add(consume(TokenType.IDENTIFIER, "Expect identifier for parameter").getLexeme());
				
				if (!match(TokenType.COMMA)) {
					if (!(match(TokenType.PAREN_RIGHT))) {
						throw new ParseError(peek(), "Expect ')' or ',' after parameter name");
					}
				}
			}
		}
		consume(TokenType.BRACE_LEFT, "Expect '{' after function header");
		
		List<Stmt> body = new ArrayList<>();
		while (!check(TokenType.BRACE_RIGHT) && !isAtEnd()) {
			body.add(statement());
		}
		
		consume(TokenType.BRACE_RIGHT, "Unclosed block");

		return new Stmt.Var(name, new Expr.Literal(new Fn(params, new Stmt.Block(body), name)));
	}
	
	/**
	 * Parse an infinite loop
	 * @return a while statement with the condition of true
	 */
	private Stmt infiniteLoop() {
		Token keyword = previous();
		Expr condition = new Expr.Literal(true);
		consume(TokenType.BRACE_LEFT, "Expect '{' after 'loop'");
		List<Stmt> body = new ArrayList<>();
		while (!check(TokenType.BRACE_RIGHT) && !isAtEnd()) {
			body.add(statement());
		}
		
		consume(TokenType.BRACE_RIGHT, "Unclosed block");

		
		return new Stmt.While(condition, new Stmt.Block(body), keyword);
	}
	
	/**
	 * Desugar a for loop into a while loop
	 * ex:
	 * for var i = 0; i < 10; i = i + 1 {
	 * 	print(i);
	 * }
	 * 
	 * desugars to:
	 * {
	 * 	var i = 0;
	 * 	while i < 10 {
	 * 		print(i);
	 * 		i = i + 1;
	 * 	}
	 * }
	 * @return the block that is parsed
	 */
	private Stmt forStatement() {
		Token keyword = previous();
		if (peek().getType() == TokenType.VAR && peek(1).getType() == TokenType.IDENTIFIER && peek(2).getType() == TokenType.IN) {
			return forEach();
		}
		Stmt initializer = statement();
		Expr condition = expression();
		consume(TokenType.SEMICOLON, "Expect ';' after for loop condition");
		Expr increment = expression();
		
		consume(TokenType.BRACE_LEFT, "Expect '{' to begin for loop");
		List<Stmt> body = new ArrayList<>();
		while (!check(TokenType.BRACE_RIGHT) && !isAtEnd()) {
			body.add(statement());
		}
		body.add(new Stmt.Expression(increment));
		consume(TokenType.BRACE_RIGHT, "Unclosed block");
		List<Stmt> block = Arrays.asList(initializer, new Stmt.While(condition, new Stmt.Block(body), keyword));
		return new Stmt.Block(block);
	}
	
	/**
	 * Desugars a for-each loop
	 * Ex:
	 * var list = [1, 2, 3, 4];
	 * for var item in list {
	 * 	print(item);
	 * }
	 * Desugars to:
	 * var list = [1, 2, 3, 4];
	 * {
	 * 	var __iter__ = 0;
	 * 	var __iterable__ = list;
	 * 	while __iter__ < len(__iterable__) {
	 * 		var item = __iterable__[__iter__];
	 * 		print(item);
	 * 		__iter__ = __iter__ + 1;
	 * 	}
	 * }
	 * @return the desugared loop
	 */
	private Stmt forEach() {
		System.out.println("ForEach");
		Token keyword = previous();
		consume(TokenType.VAR, "Expect 'var'");
		Token identifier = consume(TokenType.IDENTIFIER, "Expect an identifier");
		Token in = consume(TokenType.IN, "Expect 'in'");
		Expr iterable = expression();
		Stmt initializer = new Stmt.Var(new Token(TokenType.IDENTIFIER, "__iter__", -1, -1), new Expr.Literal(0.0));
		Stmt initializeIterable = new Stmt.Var(new Token(TokenType.IDENTIFIER, "__iterable__", -1, -1), iterable);
		Expr condition = new Expr.Binary(
				new Expr.GetVar(new Token(TokenType.IDENTIFIER, "__iter__", -1, -1)), 
				new Token(TokenType.LESS, "<", -1, -1),
				new Expr.Call(
						new Expr.GetVar(new Token(TokenType.IDENTIFIER, "len", -1, -1)), 
						Arrays.asList(new Expr.GetVar(new Token(TokenType.IDENTIFIER, "__iterable__", -1, -1))),
						in));
		
		Stmt increment = new Stmt.Expression(
				new Expr.Assign(
						new Token(TokenType.IDENTIFIER, "__iter__", -1, -1), 
						new Expr.Binary(
								new Expr.GetVar(new Token(TokenType.IDENTIFIER, "__iter__", -1, -1)),
								new Token(TokenType.PLUS, "+", -1, -1), 
								new Expr.Literal(1.0))));
		
		consume(TokenType.BRACE_LEFT, "Expect '{' to begin for loop");
		List<Stmt> body = new ArrayList<>();
		body.add(new Stmt.Var(identifier, new Expr.Index(
				new Expr.GetVar(
						new Token(TokenType.IDENTIFIER, "__iter__", -1, -1)), 
				new Expr.GetVar(new Token(TokenType.IDENTIFIER, "__iterable__", -1, -1)), 
				in)));
		
		while (!check(TokenType.BRACE_RIGHT) && !isAtEnd()) {
			body.add(statement());
		}
		body.add(increment);
		consume(TokenType.BRACE_RIGHT, "Expect '}' to close block");
		List<Stmt> block = Arrays.asList(initializer, initializeIterable, new Stmt.While(condition, new Stmt.Block(body), keyword));
		return new Stmt.Block(block);
	}
	
	/**
	 * Parse a while statement
	 * @return the statement parsed
	 */
	private Stmt whileStatement() {
		Token keyword = previous();
		Expr condition = expression();
		consume(TokenType.BRACE_LEFT, "Expect '{' after while condition");
		List<Stmt> body = new ArrayList<>();
		while (!check(TokenType.BRACE_RIGHT) && !isAtEnd()) {
			body.add(statement());
		}
		
		consume(TokenType.BRACE_RIGHT, "Unclosed block");

		
		return new Stmt.While(condition, new Stmt.Block(body), keyword);
	}
	
	/**
	 * Parse a var declaration
	 * @return the declaration
	 */
	private Stmt varStatement() {
		Token identifier = consume(TokenType.IDENTIFIER, "Expect identifier after 'var'");
		Expr right = null;
		if (match(TokenType.EQUALS)) {
			right = expression();
		}
		consume(TokenType.SEMICOLON, "Expect ';' after statement.");
		
		return new Stmt.Var(identifier, right);
	}
	
	/**
	 * Parse an if statement
	 * @return the statement parsed
	 */
	private Stmt ifStatement() {
		Token keyword = previous();
		Expr condition = expression();
		consume(TokenType.BRACE_LEFT, "Expect '{' after if condition");
		List<Stmt> ifBranch = new ArrayList<>();
		while (!check(TokenType.BRACE_RIGHT) && !isAtEnd()) {
			ifBranch.add(statement());
		}
		
		consume(TokenType.BRACE_RIGHT, "Unclosed block");
		
		List<Stmt> elseBranch = new ArrayList<>();
		if (match(TokenType.ELSE)) {
			consume(TokenType.BRACE_LEFT, "Expect '{' after 'else'");
			while (!check(TokenType.BRACE_RIGHT) && !isAtEnd()) {
				elseBranch.add(statement());
			}
			
			consume(TokenType.BRACE_RIGHT, "Unclosed block");
		}
		
		return new Stmt.If(condition, new Stmt.Block(ifBranch), new Stmt.Block(elseBranch), keyword);
	}
	
	/**
	 * Parse a block statement
	 * @return the block parsed
	 */
	private Stmt blockStatement() {
		List<Stmt> stmts = new ArrayList<>();
		while (!check(TokenType.BRACE_RIGHT) && !isAtEnd()) {
			stmts.add(statement());
		}
		
		consume(TokenType.BRACE_RIGHT, "Unclosed block");
		
		return new Stmt.Block(stmts);
	}
	
	/**
	 * Parse an expression statement
	 * @return the statement parsed
	 */
	private Stmt expressionStatement() {
		Expr expression = expression();
		
		consume(TokenType.SEMICOLON, "Expect ';' after statement.");
		
		return new Stmt.Expression(expression);
	}

	/**
	 * Parse an expression
	 * @return the expression parsed
	 */
	public Expr expression() {
		return or();
	}
	
	/**
	 * Parse an expression containing an or operator
	 * @return the expression parsed
	 */
	private Expr or() {
		Expr expr = and();
		
		while (match(TokenType.OR)) {
			Token operator = previous();
			Expr right = and();
			expr = new Expr.Binary(expr, operator, right);
		}
		
		return expr;
	}
	
	/**
	 * Parse an expression containing an and operator
	 * @return the expression parsed
	 */
	private Expr and() {
		Expr expr = equality();
		
		while (match(TokenType.AND)) {
			Token operator = previous();
			Expr right = equality();
			expr = new Expr.Binary(expr, operator, right);
		}
		
		return expr;
	}
	
	/**
	 * Parse an expression with an equality operator
	 * @return the expression parsed
	 */
	private Expr equality() {
		Expr expr = comparison();
		
		while (match(TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL)) {
			Token operator = previous();
			Expr right = comparison();
			expr = new Expr.Binary(expr, operator, right);
		}
		
		return expr;
	}

	/**
	 * Parse an expression with a comparison operator
	 * @return the expression parsed
	 */
	private Expr comparison() {
		Expr expr = addition();
		
		while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
			Token operator = previous();
			Expr right = addition();
			expr = new Expr.Binary(expr, operator, right);
		}
		
		return expr;
	}

	/**
	 * Parse an expression with an addition or subtraction operator
	 * @return the expression parsed
	 */
	private Expr addition() {
		Expr expr = multiplication();
		
		while (match(TokenType.PLUS, TokenType.MINUS)) {
			Token operator = previous();
			Expr right = multiplication();
			expr = new Expr.Binary(expr, operator, right);
		}
		
		return expr;
	}

	/**
	 * Parse an expression with a multiplication or division operator
	 * @return the expression parsed
	 */
	private Expr multiplication() {
		Expr expr = unary();
		
		while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
			Token operator = previous();
			Expr right = unary();
			expr = new Expr.Binary(expr, operator, right);
		}
		
		return expr;
	}
	
	/**
	 * Parse an expression with a unary operator
	 * @return the expression parsed
	 */
	private Expr unary() {
		
		if (match(TokenType.NOT, TokenType.MINUS)) {
			Token operator = previous();
			Expr right = unary();
			return new Expr.Unary(operator, right);
		}
		
		return indexing();
	}
	
	/**
	 * Parse an indexing expression
	 * @return the expression parsed
	 */
	private Expr indexing() {
		Expr expr = primary();
		Token finalOfIndexee = previous();
		if (match(TokenType.BRACKET_LEFT)) {
			Token bracket = previous();
			expr = new Expr.Index(expression(), expr, bracket);
			consume(TokenType.BRACKET_RIGHT, "Expect ']' after index");
			if (match(TokenType.EQUALS) && finalOfIndexee.getType() == TokenType.IDENTIFIER) {
				expr = new Expr.AssignIndex(finalOfIndexee, expression(), ((Expr.Index)expr).getIndex());
			}
		}
		return expr;
	}
	
	/**
	 * Parse a literal or grouping expression
	 * @return the expression parsed
	 */
	private Expr primary() {
		if (match(TokenType.NUMBER, TokenType.STRING)) {
			return new Expr.Literal(previous().getLiteral());
		}
		
		if (match(TokenType.TRUE)) {
			return new Expr.Literal(true);
		}
		
		if (match(TokenType.FALSE)) {
			return new Expr.Literal(false);
		}
		
		if (match(TokenType.NIL)) {
			return new Expr.Literal(null);
		}
		
		if (match(TokenType.NAN)) {
			return new Expr.Literal(Double.NaN);
		}
		
		if (match(TokenType.INFINITY)) {
			return new Expr.Literal(Double.POSITIVE_INFINITY);
		}
		
		if (match(TokenType.IDENTIFIER)) {
			return variable();
		}
		
		if (match(TokenType.PAREN_LEFT)) {
			Expr expr = expression();
			consume(TokenType.PAREN_RIGHT, "Expect closing ')'");
			return new Expr.Grouping(expr);
		}
		
		if (match(TokenType.BRACKET_LEFT)) {
			List<Expr> exprs = new ArrayList<>();
			while (!check(TokenType.BRACKET_RIGHT)) {
				exprs.add(expression());
				if (!match(TokenType.COMMA)) {
					if (!match(TokenType.BRACKET_RIGHT)) {
						throw new ParseError(peek(), "Expect ']' or ',' after expression");
					}
					break;
				}
			}
			
			return new Expr.EList(exprs);
		}
	
		
		throw error(peek(), "Expect Expression");
	}
	
	/**
	 * Parse a expression containing a variable
	 * @return the expression parsed
	 */
	private Expr variable() {
		Token identifier = previous();
		Expr expr = new Expr.GetVar(identifier);
		if (match(TokenType.EQUALS)) {
			return new Expr.Assign(identifier, expression());
		}
		if (match(TokenType.DOT)) {
			expr = new Expr.Import(identifier, consume(TokenType.IDENTIFIER, "Expect an identifier"));
		}
		if (match(TokenType.PAREN_LEFT)) {
			Token paren = previous();
			List<Expr> args = new ArrayList<Expr>();
			if (!match(TokenType.PAREN_RIGHT)) {
				while(previous().getType() != TokenType.PAREN_RIGHT) {
					args.add(expression());
					
					if (!match(TokenType.COMMA)) {
						if (!(match(TokenType.PAREN_RIGHT))) {
							throw new ParseError(peek(), "Expect ')' or ',' after argument name");
						}
					}
				}
			}
			expr = new Expr.Call((Expr.GetVar)expr, args, paren);
		}
		return expr;
	}

	// Methods adapted from Crafting Interpreters
	/**
	 * Conditionally moves forward in parsing if the current token's type is matched
	 * @param types the list of types to match
	 * @return whether or not the parser moved forward
	 */
	private boolean match(TokenType... types) {
		for (TokenType type : types) {
			if (check(type)) {
				advance();
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the current token is a certain type, and fails with an error if it doesn't match
	 * @param type the type to match
	 * @param message the message to fail with
	 * @return the token matched
	 */
	private Token consume(TokenType type, String message) {
		if (check(type))
			return advance();

		throw error(peek(), message);
	}
	
	/**
	 * Moves the parser forward
	 * @return the token that was previously under the parser
	 */
	private Token advance() {
		if (!isAtEnd())
			current++;
		return previous();
	}
	
	/**
	 * Determines whether the current tokens
	 * @param type the type to check
	 * @return whether or not it was matched
	 */
	private boolean check(TokenType type) {
		if (isAtEnd())
			return false;
		return peek().getType() == type;
	}

	/**
	 * Checks if the current token is an EOF
	 * @return whether or not the current token is an EOF
	 */
	private boolean isAtEnd() {
		return peek().getType() == TokenType.EOF;
	}

	/**
	 * The current token
	 * @return the current token
	 */
	private Token peek() {
		return tokens.get(current);
	}
	
	private Token peek(int distance) {
		if (distance == 0) {
			return peek();
		}
		try {
			return tokens.get(current + distance);
		} catch (IndexOutOfBoundsException e) {
			throw error(peek(), "Unexpected EOF");
		}
	}
	
	/**
	 * The previous token
	 * @return the previous token
	 */
	private Token previous() {
		return tokens.get(current - 1);
	}

	/**
	 * Generates an error
	 * @param token the token that caused the error
	 * @param message the error message
	 * @return the error generated
	 */
	private ParseError error(Token token, String message) {
		return new ParseError(token, message);
	}
}
