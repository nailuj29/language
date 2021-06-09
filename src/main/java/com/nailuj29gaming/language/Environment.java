package com.nailuj29gaming.language;

import java.util.HashMap;
import java.util.Map;

/**
 * A collection of named variables
 */
public class Environment {
	
	private Map<String, Object> values;
	
	/**
	 * The enclosing scope. Calls to get or set will try this scope if the variable is undefined.
	 */
	private Environment enclosing;
	/**
	 * Initializes the Environment with an enclosing scope
	 * @param enclosing the enclosing scope
	 */
	public Environment(Environment enclosing) {
		this();
		this.enclosing = enclosing;
	}
	/**
	 * Initializes a global scope
	 */
	public Environment() {
		values = new HashMap<>();
	}
	
	/**
	 * Gets a variable
	 * @param name the name to get
	 * @param location the location of the variable
	 * @return the variable
	 * @throws com.nailuj29gaming.language.Interpreter.InterpretError if the variable is undefined
	 */
	public Object get(String name, Token location) {
		if (values.containsKey(name)) {
			return values.get(name);
		}
		
		if (enclosing != null) {
			return enclosing.get(name, location);
		}
		
		throw new Interpreter.InterpretError("Undefined variable '" + name +"'", location);
	}
	
	/**
	 * Declares and defined a variable at the same time
	 * @param name the name of the variable
	 * @param value the definition of the variable
	 * @see declare
	 * @see set
	 */
	public void define(String name, Object value) {
		declare(name);
		set(name, value);
	}
	
	/**
	 * Set a a variable that is guaranteed to exist, and will never throw an error
	 * @param name the name of the variable
	 * @param value the value to set
	 */
	public void set(String name, Object value) {
		set(name, value, null);
	}
	
	/**
	 * Set a variable, including proper error handling
	 * @param name The name to set
	 * @param value the new value of the of the variable
	 * @param location the location for error handling
	 */
	public void set(String name, Object value, Token location) {
		if (values.containsKey(name)) {
			values.put(name, value);
			return;
		}
		
		if (enclosing != null) {
			enclosing.set(name, value, location);
			return;
		}
		
		throw new Interpreter.InterpretError("Undefined variable '" + name +"'", location);
	}
	
	/**
	 * Declares a variable, setting it to null
	 * @param name the name of the variable
	 */
	public void declare(String name) {
		if (Main.DEBUG) {
			System.out.printf("Defining %s\n", name);
		}
		values.put(name, null);
	}
}
