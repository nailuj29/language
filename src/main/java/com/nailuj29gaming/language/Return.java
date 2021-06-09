package com.nailuj29gaming.language;

/**
 * Thrown when the interpreter hits a {@link com.nailuj29gaming.language.ast.Stmt.Return}, and caught in {@link Fn}s
 */
public class Return extends Interpreter.StackUnwindingMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6022506834671311739L;
	private Object value;
	private Token keyword;

	/**
	 * @param value the return value
	 * @param keyword the return keyword, used for error messages
	 */
	public Return(Object value, Token keyword) {
		this.value = value;
		this.keyword = keyword;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return the keyword
	 */
	public Token getKeyword() {
		return keyword;
	}
	
	
}
