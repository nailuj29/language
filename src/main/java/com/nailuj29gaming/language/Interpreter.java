package com.nailuj29gaming.language;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.nailuj29gaming.language.ast.AstPrinter;
import com.nailuj29gaming.language.ast.Expr;
import com.nailuj29gaming.language.ast.Stmt;

/**
 * Interprets an AST
 * 
 * @see Expr
 * @see Stmt
 * @see Parser
 */
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

	/**
	 * The global variables defined by default
	 */
	public static Environment globals = new Environment();
	
	/**
	 * All modules that have been imported
	 */
	public Map<String, Environment> imports = new HashMap<>();

	/**
	 * The built-in modules you can import. If a module isn't found, fall back to one of these
	 */
	public static Map<String, Environment> builtInImports = new HashMap<>();
	
	/**
	 * The variables and functions currently defined
	 */
	private Environment environment = new Environment(globals);
	
	/**
	 * The scanner used in <code>input</code>
	 */
	private static Scanner scan = new Scanner(System.in);
	

	private static String stringify(Object value) {
		if (value instanceof Double) {
			double d = (Double) value;
			if (d == (long) d) {
				value = (long) d;
			}
		}
		
		if (value instanceof List) {
			List l = (List)value;
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			for (Object item : l) {
				sb.append(stringify(item));
				sb.append(", ");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.deleteCharAt(sb.length() - 1);
			sb.append("]");
			value = sb;
		}
		
		if (value == null) {
			value = "nil";
		}
		
		return value.toString();
	}
	
	static {
		// Builtin functions
		globals.define("print", new IFn() {

			@Override
			public int getArity() {
				return 1;
			}

			@Override
			public Object call(Interpreter interpreter, List<Object> args, Token paren) {
				Object value = args.get(0);
				
				System.out.println(stringify(value));
				return null;
			}
			
			@Override
			public String toString() {
				return "<natve fn print>";
			}
		});
		
		globals.define("printRaw", new IFn() {

			@Override
			public int getArity() {
				return 1;
			}

			@Override
			public Object call(Interpreter interpreter, List<Object> args, Token paren) {
				Object value = args.get(0);
				System.out.print(stringify(value));
				return null;
			}
			
			@Override
			public String toString() {
				return "<natve fn printRaw>";
			}
		});
		
		globals.define("input", new IFn() {
			
			@Override
			public int getArity() {
				return 0;
			}
			
			@Override
			public Object call(Interpreter interpreter, List<Object> args, Token paren) {
				String line = scan.next();
				
				return line;
			}
			
			@Override
			public String toString() {
				return "<natve fn input>";
			}
		});
		
		globals.define("len", new IFn() {
			
			@Override
			public int getArity() {
				return 1;
			}
			
			@Override
			public Object call(Interpreter interpreter, List<Object> args, Token paren) {
				if (args.get(0) instanceof List<?>) {
					return (double)(((List<?>)args.get(0)).size());
				}
				
				if (args.get(0) instanceof String) {
					return (double)((String)args.get(0)).length();
				}
				throw new InterpretError("Expect a list", paren);
			}
			
			@Override
			public String toString() {
				return "<natve fn len>";
			}
		});
		
		globals.define("VERSION", "0.0.1");
		
		Environment os = new Environment();
		os.define("name", System.getProperty("os.name"));
		builtInImports.put("os", os);
		
		Environment io = new Environment();
		io.define("write", new IFn() {
			
			@Override
			public int getArity() {
				return 2;
			}
			
			@Override
			public Object call(Interpreter interpreter, List<Object> args, Token paren) {
				String filename = "";
				if (args.get(0) instanceof String && args.get(0) != null) {
					filename = (String) args.get(0);
				} else {
					throw new InterpretError("Filename must be a string", paren);
				}
				
				if (args.get(1) != null) {
					String contents = args.get(1).toString();
					try {
						Files.write(Paths.get(filename), contents.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
					} catch (IOException e) {
						throw new InterpretError(e.getMessage(), paren);
					}
				} else {
					throw new InterpretError("Cannot write nil to a file", paren);
				}
				return null;
			}
			
			@Override
			public String toString() {
				return "<natve fn io.write>";
			}
		});
		
		io.define("append", new IFn() {
			
			@Override
			public int getArity() {
				return 2;
			}
			
			@Override
			public Object call(Interpreter interpreter, List<Object> args, Token paren) {
				String filename = "";
				if (args.get(0) instanceof String && args.get(0) != null) {
					filename = (String) args.get(0);
				} else {
					throw new InterpretError("Filename must be a string", paren);
				}
				
				if (args.get(1) != null) {
					String contents = args.get(1).toString();
					try {
						Files.write(Paths.get(filename), contents.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
					} catch (IOException e) {
						throw new InterpretError(e.getMessage(), paren);
					}
				} else {
					throw new InterpretError("Cannot write nil to a file", paren);
				}
				return null;
			}
			
			@Override
			public String toString() {
				return "<natve fn io.append>";
			}
		});
		
		io.define("read", new IFn() {
			
			@Override
			public int getArity() {
				return 1;
			}
			
			@Override
			public Object call(Interpreter interpreter, List<Object> args, Token paren) {
				String filename = "";
				if (args.get(0) instanceof String && args.get(0) != null) {
					filename = (String) args.get(0);
					try {
						return new String(Files.readAllBytes(Paths.get(filename)));
					} catch (IOException e) {
						throw new InterpretError(e.getMessage(), paren);
					}
				} else {
					throw new InterpretError("Filename must be a string", paren);
				}
			}
			
			@Override
			public String toString() {
				return "<natve fn io.read>";
			}
		});
		
		builtInImports.put("io", io);
		
		Environment math = new Environment();
		math.define("pi", Math.PI);
		math.define("e", Math.E);
		math.define("sqrt", new IFn() {
			
			@Override
			public int getArity() {
				return 1;
			}
			
			@Override
			public Object call(Interpreter interpreter, List<Object> args, Token paren) {
			
				if (args.get(0) instanceof Double) {
					double value = (Double)args.get(0);
					return Math.sqrt(value);
				}
				
				throw new InterpretError("Expect a number", paren);
			}
			
			@Override
			public String toString() {
				return "<natve fn math.sqrt>";
			}
		});
		
		math.define("pow", new IFn() {
			
			@Override
			public int getArity() {
			
				return 2;
			}
			
			@Override
			public Object call(Interpreter interpreter, List<Object> args, Token paren) {
	
				if (args.get(0) instanceof Double && args.get(1) instanceof Double) {
					double base = (Double)args.get(0);
					double exp = (Double)args.get(1);
					return Math.pow(base, exp);
				}
				
				throw new InterpretError("Expect two numbers", paren);
			}
			
			@Override
			public String toString() {
				return "<natve fn math.pow>";
			}
		});
	
		math.define("exp", new IFn() {
			
			@Override
			public int getArity() {
				return 1;
			}
			
			@Override
			public Object call(Interpreter interpreter, List<Object> args, Token paren) {
				if (args.get(0) instanceof Double) {
					double value = (Double)args.get(0);
					return Math.exp(value);
				}
				
				throw new InterpretError("Expect a number", paren);
			}
			
			@Override
			public String toString() {
				return "<natve fn math.exp>";
			}
		});
		
		math.define("sin", new IFn() {
			
			@Override
			public int getArity() {
				return 1;
			}
			
			@Override
			public Object call(Interpreter interpreter, List<Object> args, Token paren) {
				if (args.get(0) instanceof Double) {
					double value = (Double)args.get(0);
					return Math.sin(value);
				}
				
				throw new InterpretError("Expect a number", paren);
			}
			
			@Override
			public String toString() {
				return "<natve fn math.sin>";
			}
		});
		
		math.define("cos", new IFn() {
			
			@Override
			public int getArity() {
				return 1;
			}
			
			@Override
			public Object call(Interpreter interpreter, List<Object> args, Token paren) {
				if (args.get(0) instanceof Double) {
					double value = (Double)args.get(0);
					return Math.cos(value);
				}
				
				throw new InterpretError("Expect a number", paren);
			}
			
			@Override
			public String toString() {
				return "<natve fn math.cos>";
			}
		});
		
		math.define("tan", new IFn() {
			
			@Override
			public int getArity() {
				return 1;
			}
			
			@Override
			public Object call(Interpreter interpreter, List<Object> args, Token paren) {
				if (args.get(0) instanceof Double) {
					double value = (Double)args.get(0);
					return Math.tan(value);
				}
				
				throw new InterpretError("Expect a number", paren);
			}
			
			@Override
			public String toString() {
				return "<natve fn math.tan>";
			}
		});
		
		math.define("log", new IFn() {
			
			@Override
			public int getArity() {
				return 1;
			}
			
			@Override
			public Object call(Interpreter interpreter, List<Object> args, Token paren) {
				if (args.get(0) instanceof Double) {
					double value = (Double)args.get(0);
					return Math.log(value);
				}
				
				throw new InterpretError("Expect a number", paren);
			}
			
			@Override
			public String toString() {
				return "<natve fn math.log>";
			}
		});
		
		
		
	}

	/**
	 * An error occurring during runtime
	 */
	public static class InterpretError extends RuntimeException {
		/**
		 * Eclipse generated
		 */
		private static final long serialVersionUID = -8910418909617108623L;

		private String message;
		private Token token;

		public InterpretError(String message, Token token) {
			super(message);
			this.message = message;
			this.token = token;
		}

		/**
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}

		/**
		 * @return the location
		 */
		public Token getToken() {
			return token;
		}

	}

	/**
	 * An exception meant to send a message to certain syntactic structures by unwinding the stack until it finds one of them
	 *
	 */
	static class StackUnwindingMessage extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6816328639177916215L;

	}

	/**
	 * Signals a <code>break</code> statement
	 */
	private class Break extends StackUnwindingMessage {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4281311888639783169L;
		Token keyword;

		/**
		 * @param keyword
		 */
		public Break(Token keyword) {
			this.keyword = keyword;
		}

		/**
		 * @return the keyword
		 */
		public Token getKeyword() {
			return keyword;
		}

	}

	/**
	 * Signals a <code>continue</code> statement
	 */
	private class Continue extends StackUnwindingMessage {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8406027968416425753L;
		Token keyword;

		/**
		 * @param keyword
		 */
		public Continue(Token keyword) {
			this.keyword = keyword;
		}

		/**
		 * @return the keyword
		 */
		public Token getKeyword() {
			return keyword;
		}

	}

	/**
	 * Evaluate an expression
	 * @param expr the expression to evaluate
	 * @return the value returned by the expression
	 */
	public Object evaluate(Expr expr) {
		return expr.accept(this);
	}

	/**
	 * Runs a list of statements
	 * @param stmts the statements to run
	 */
	public void interpret(List<Stmt> stmts) {
		try {
			for (Stmt stmt : stmts) {
				execute(stmt);
			}
		} catch (Break e) {
			throw new InterpretError("Cant break outside a loop", e.getKeyword());
		} catch (Continue e) {
			throw new InterpretError("Cant break outside a loop", e.getKeyword());
		}

	}
	
	/**
	 * Runs a list of statements, returning the environment to be used in an import
	 * @param stmts The statements to run
	 * @return the environment after running these statements
	 */
	public Environment interpretForImport(List<Stmt> stmts) {
		interpret(stmts);
		return environment;
	}

	/**
	 * Runs a single statement
	 * @param stmt the statement to run
	 */
	public void execute(Stmt stmt) {
		stmt.accept(this);
	}

	/**
	 * Runs a single statement in an environment
	 * @param stmt the statement to run
	 * @param scope the environment to run it in
	 */
	public void execute(Stmt stmt, Environment scope) {
		Environment previous = this.environment;
		this.environment = scope;
		try {
			execute(stmt);
		} finally {
			this.environment = previous;
		}
	}

	@Override
	public Void visitBlockStmt(Stmt.Block stmt) {
		Environment previous = environment;
		environment = new Environment(previous);
		try {
			for (Stmt statement : stmt.getStmts()) {
				statement.accept(this);
			}
		} finally {
			environment = previous;
		}
		return null;
	}

	@Override
	public Void visitBreakStmt(Stmt.Break stmt) {
		throw new Break(stmt.getKeyword());
	}

	@Override
	public Void visitContinueStmt(Stmt.Continue stmt) {
		throw new Continue(stmt.getKeyword());
	}

	@Override
	public Void visitExpressionStmt(Stmt.Expression stmt) {
		evaluate(stmt.getExpression());
		return null;
	}

	public Void visitIfStmt(Stmt.If stmt) {
		if (isTrue(stmt.getCondition())) {
			execute(stmt.getIfBranch());
		} else {
			execute(stmt.getElseBranch());
		}

		return null;
	}
	
	public Void visitImportStmt(Stmt.Import stmt) {
		String filename = String.format("%s.scr", stmt.getImportName().getLexeme());
		if (Files.exists(Paths.get(filename))) {
			String source;
			try {
				source = new String(Files.readAllBytes(Paths.get(filename)));

				Lexer lexer = new Lexer();
				Parser parser = new Parser();
				List<Token> tokens = lexer.lex(source);
				
				
				if (Main.DEBUG) {
					for (Token tkn : tokens) {
						System.out.println(tkn);
					}
					System.out.println("Lexed " + tokens.size() + " tokens.");
				}
				List<Stmt> statements = new ArrayList<Stmt>();
				try {
					statements = parser.parse(tokens);
				} catch (Parser.ParseError e) {
					Main.error(e.getMessage(), e.getToken().getLine(), e.getToken().getColumn());
					System.exit(1);
				}
				if (Main.DEBUG) {
					AstPrinter printer = new AstPrinter();
					System.out.println(printer.print(statements));
					System.out.println("Done parsing");
				}
				Interpreter interpreter = new Interpreter();
				try {
					imports.put(stmt.getImportName().getLexeme(), interpreter.interpretForImport(statements));
				} catch (InterpretError e) {
					Main.error(e.getMessage(), e.getToken().getLine(), e.getToken().getColumn());
					System.exit(1);
				}
			} catch (IOException e) {
				// Should never happen
			}
	
		} else if (builtInImports.containsKey(stmt.getImportName().getLexeme())) {
			imports.put(stmt.getImportName().getLexeme(), builtInImports.get(stmt.getImportName().getLexeme()));
		} else {
			throw new InterpretError("Could not find import", stmt.getImportName());
		}
		return null;
	}

	@Override
	public Void visitReturnStmt(Stmt.Return stmt) {
		if (stmt.getExpr() == null) {
			throw new Return(null, stmt.getKeyword());
		}
		throw new Return(evaluate(stmt.getExpr()), stmt.getKeyword());
	}

	@Override
	public Void visitVarStmt(Stmt.Var stmt) {
		environment.declare(stmt.getIdentifier().getLexeme());
		environment.set(stmt.getIdentifier().getLexeme(), evaluate(stmt.getRight()), stmt.getIdentifier());

		return null;
	}

	@Override
	public Void visitWhileStmt(Stmt.While stmt) {
		while (isTrue(stmt.getCondition())) {
			try {
				execute(stmt.getBody());
			} catch (Break e) {
				break;
			} catch (Continue e) {
				continue;
			}
		}
		return null;
	}

	@Override
	public Object visitAssignExpr(Expr.Assign expr) {
		environment.set(expr.getIdentifier().getLexeme(), evaluate(expr.getRight()), expr.getIdentifier());
		return null;
	}
	
	@Override
	public Object visitAssignIndexExpr(Expr.AssignIndex expr) {
		Object value = environment.get(expr.getIdentifier().getLexeme(), expr.getIdentifier());
		if (value instanceof List) {
			List list = (List)value;
			
			Object indexValue = evaluate(expr.getIndex());
			if (indexValue instanceof Double) {
				int index = (int)(double)(Double)indexValue;
				try {
					list.set(index, evaluate(expr.getRight()));
				} catch (IndexOutOfBoundsException e) {
					throw new InterpretError(String.format("Index out of bounds: %s", e.getMessage()), expr.getIdentifier());
				}
				environment.set(expr.getIdentifier().getLexeme(), list, expr.getIdentifier());
			} else {
				throw new InterpretError("Cannot index using a value that isn't a number", expr.getIdentifier());
			}
		} else {
			throw new InterpretError("Cannot index non-iterable", expr.getIdentifier());
		}
		return value;
	}
	
	@Override
	public Object visitBinaryExpr(Expr.Binary expr) {
		Object left = evaluate(expr.getLeft());
		Object right = evaluate(expr.getRight());
		switch (expr.getOperator().getType()) {
		case PLUS:
			if (left instanceof Double && right instanceof Double) {
				return (Double) left + (Double) right;
			}

			if (left instanceof String || right instanceof String) {
				return stringify(left) + stringify(right);
			}
			
			if (left instanceof List<?> && right instanceof List<?>) {
				List<?> leftList = (List<?>)left;
				List<?> rightList = (List<?>)right;
				
				List<Object> res = new ArrayList<>();
				for (int i = 0; i < leftList.size(); i++) {
					res.add(leftList.get(i));
				}
				
				for (int i = 0; i < rightList.size(); i++) {
					res.add(rightList.get(i));
				}
				
				return res;
			}
			throw new InterpretError("Invalid types for '+'", expr.getOperator());
		case MINUS:
			if (left instanceof Double && right instanceof Double) {
				return (Double) left - (Double) right;
			}
			throw new InterpretError("Invalid types for '-'", expr.getOperator());
		case STAR:
			if (left instanceof Double && right instanceof Double) {
				return (Double) left * (Double) right;
			}
			throw new InterpretError("Invalid types for '*'", expr.getOperator());
		case SLASH:
			if (left instanceof Double && right instanceof Double) {
				return (Double) left / (Double) right;
			}
			throw new InterpretError("Invalid types for '/'", expr.getOperator());
		case PERCENT:
			if (left instanceof Double && right instanceof Double) {
				return (Double) left % (Double) right;
			}
			throw new InterpretError("Invalid types for '%'", expr.getOperator());
		case EQUAL_EQUAL:
			if (left == null) {
				return right == null;
			}
			return (left.equals(right));

		case NOT_EQUAL:
			if (left == null) {
				return right != null;
			}
			return !(left.equals(right));

		case GREATER:
			if (left instanceof Double && right instanceof Double) {
				return (Double) left > (Double) right;
			}
			throw new InterpretError("Invalid types for '>'", expr.getOperator());

		case GREATER_EQUAL:
			if (left instanceof Double && right instanceof Double) {
				return (Double) left >= (Double) right;
			}
			throw new InterpretError("Invalid types for '>='", expr.getOperator());

		case LESS:
			if (left instanceof Double && right instanceof Double) {
				return (Double) left < (Double) right;
			}
			throw new InterpretError("Invalid types for '<'", expr.getOperator());

		case LESS_EQUAL:
			if (left instanceof Double && right instanceof Double) {
				return (Double) left <= (Double) right;
			}
			throw new InterpretError("Invalid types for '<='", expr.getOperator());

		case OR:
			if (left instanceof Boolean && right instanceof Boolean) {
				return (Boolean) left || (Boolean) right;
			}
			throw new InterpretError("Invalid types for '|'", expr.getOperator());

		case AND:
			if (left instanceof Boolean && right instanceof Boolean) {
				return (Boolean) left && (Boolean) right;
			}
			throw new InterpretError("Invalid types for '&'", expr.getOperator());
		default:
			// unreachable
			System.out.println("default?");
			return null;
		}
	}

	public Object visitCallExpr(Expr.Call expr) {
		Object function = evaluate(expr.getCallee());
		if (function instanceof IFn) {
			IFn fn = (IFn) function;
			List<Object> args = new ArrayList<>();
			for (Expr arg : expr.getArgs()) {
				args.add(evaluate(arg));
			}

			if (args.size() > fn.getArity()) {
				throw new InterpretError("Incorrect argument count", expr.getParen()); // Currying is planned
			}

			return fn.callCurried(this, args, expr.getParen());
		}
		throw new InterpretError("Cannot call non-function", expr.getParen());
	}

	@Override
	public Object visitGetVarExpr(Expr.GetVar expr) {
		return environment.get(expr.getIdentifier().getLexeme(), expr.getIdentifier());
	}

	@Override
	public Object visitGroupingExpr(Expr.Grouping expr) {
	
		return evaluate(expr.getExpression());
	}
	
	@Override
	public Object visitImportExpr(Expr.Import expr) {
		if (imports.containsKey(expr.getModule().getLexeme())) {
			return imports.get(expr.getModule().getLexeme()).get(expr.getIdentifier().getLexeme(), expr.getIdentifier());
		}
		throw new InterpretError("Undefined or un-imported module", expr.getModule());
	}
	
	@Override
	public Object visitIndexExpr(Expr.Index expr) {
		Object index = evaluate(expr.getIndex());
		Object indexee = evaluate(expr.getIndexee());
		
		if (indexee instanceof List<?>) {
			List<Object> list = (List<Object>) indexee;
			if (index instanceof Double) {
				double d = (Double)index;
				try {
					return list.get((int)d);
				} catch (IndexOutOfBoundsException e) {
					throw new InterpretError(String.format("Index out of bounds: %s", e.getMessage()), expr.getBracket());
				}
			} else {
				throw new InterpretError("Cannot index with a non-number", expr.getBracket());
			}
		} else {
			throw new InterpretError("Cannot index a non-iterable", expr.getBracket());
		}
	}
	
	@Override
	public Object visitListExpr(Expr.EList expr) {
		List<Object> items = new ArrayList<>();
		
		for (Expr item : expr.getExprs()) {
			items.add(evaluate(item));
		}
		
		return items;
	}

	@Override
	public Object visitLiteralExpr(Expr.Literal expr) {
		return expr.getValue();
	}

	@Override
	public Object visitUnaryExpr(Expr.Unary expr) {
		Object target = evaluate(expr.getValue());
		switch (expr.getOperator().getType()) {
		case MINUS:
			if (target instanceof Double) {
				return -(Double) target;
			}
			throw new InterpretError("Invalid type for '-'", expr.getOperator());
		case NOT:
			if (target instanceof Boolean) {
				return !(Boolean) target;
			}
			throw new InterpretError("Invalid type for '!'", expr.getOperator());
		default:
			return null;
		}
	}

	private boolean isTrue(Expr value) {
		Object result = evaluate(value);
		if (result == null) {
			return false;
		}

		if (result instanceof Boolean) {
			return (Boolean) result;
		}

		return true;
	}
}
