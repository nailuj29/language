package com.nailuj29gaming.language;

import java.util.List;

/**
 * A function that has been curried
 */
public class CurriedFn implements IFn {

	private IFn parent;
	private List<Object> params;
	
	/**
	 * @param parent the original function, before currying
	 * @param params the params passed to the function before currying
	 */
	public CurriedFn(IFn parent, List<Object> params) {
		this.parent = parent;
		this.params = params;
	}

	@Override
	public int getArity() {
		return parent.getArity() - params.size();
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> args, Token paren) {
		int temp = params.size();
		params.addAll(args);
	
		Object result = parent.call(interpreter, params, paren);
		
		params = params.subList(0, temp);
		return result;
	}

}
