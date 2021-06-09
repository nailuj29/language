package com.nailuj29gaming.language;

import java.util.List;

/**
 * A callable class. 
 * Used in {@link Fn}s and created as anonmyous inner classes for native functions
 */
public interface IFn {
	/**
	 * @return The number of arguments the function takes
	 */
	public int getArity();
	/**
	 * Calls a function
	 * @param interpreter the {@link Interpreter} to run it inside of. ignored for native functions
	 * @param args The arguments passed to it
	 * @param paren The opening parenthesis, used for error reporting 
	 * @return the value returned by the function
	 */
	public Object call(Interpreter interpreter, List<Object> args, Token paren);
	
	/**
	 * Calls a function with currying
	 * @param interpreter same as {@link call}
	 * @param args same as {@link call}
	 * @param paren same as {@link call}
	 * @return a {@link CurriedFn} if there were too few arguments, or the result of calling the function. should 
	 */
	public default Object callCurried(Interpreter interpreter, List<Object> args, Token paren) {
		if (args.size() == getArity())  {
			return call(interpreter, args, paren);
		}
		return new CurriedFn(this, args);
	}
}