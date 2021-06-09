package com.nailuj29gaming.language;

import java.util.List;

import com.nailuj29gaming.language.ast.Stmt;

/**
 * A function defined in {LANGUAGE NAME}
 */
public class Fn implements IFn {
	
	private final List<String> params;
	private final Stmt.Block body;
	private final int arity;
	private final Token name;

	/**
	 * @param params the parameters of the function
	 * @param body the statements in the function
	 * @param name the name of the function, for error handling
	 */
	public Fn(List<String> params, Stmt.Block body, Token name) {
		this.name = name;
		this.params = params;
		this.body = body;
		this.arity = params.size();
	}

	@Override
	public int getArity() {
		return arity;
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> args, Token paren) {
		Environment scope = new Environment(Interpreter.globals);
		for (int i = 0; i < args.size(); i++) {
			scope.declare(params.get(i));
			scope.set(params.get(i), args.get(i), null);
		}
		scope.define(name.getLexeme(), this);
		try {
			interpreter.execute(body, scope);
		} catch (Return r) {
			return r.getValue();
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return String.format("fn %s(%s)", name.getLexeme(), String.join(", ", params));
	}
}
