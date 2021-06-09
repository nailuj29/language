package com.nailuj29gaming.language;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import com.nailuj29gaming.language.ast.AstPrinter;
import com.nailuj29gaming.language.ast.Expr;
import com.nailuj29gaming.language.ast.Stmt;

/**
 * The main class
 */
public class Main {
	
	public static final boolean DEBUG = true;

	private static String[] lines;
	
	
	/**
	 * The main method
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Must pass only a single file");
			System.exit(1);
		}
		Path filename = Paths.get(args[0]);
		String source;
		try {
			source = new String(Files.readAllBytes(filename));
		} catch (IOException e) {
			System.err.printf("Cannot find file %s", filename.toString());
			System.exit(1);
			return;
		}
		lines = source.split("\n");
		Lexer lexer = new Lexer();
		Parser parser = new Parser();
		List<Token> tokens = lexer.lex(source);
		if (DEBUG) {
			for (Token tkn : tokens) {
				System.out.println(tkn);
			}
			System.out.println("Lexed " + tokens.size() + " tokens.");
		}
		List<Stmt> statements = new ArrayList<Stmt>();
		try {
			statements = parser.parse(tokens);
		} catch (Parser.ParseError e) {
			error(e.getMessage(), e.getToken().getLine(), e.getToken().getColumn());
			System.exit(1);
		}
		if (DEBUG) {
			AstPrinter printer = new AstPrinter();
			System.out.println(printer.print(statements));
			System.out.println("Done parsing");
		}
		Interpreter interpreter = new Interpreter();
		try {
			interpreter.interpret(statements);
		} catch (Interpreter.InterpretError e) {
			error(e.getMessage(), e.getToken().getLine(), e.getToken().getColumn());
			System.exit(1);
		}
	}

	/**
	 * Generate a human-readable error message and write it to stderr
	 * @param error the error message
	 * @param line the line the error occurs at
	 * @param column the column the error occurs at
	 */
	public static void error(String error, int line, int column) {
		System.out.printf("Message: %s, line: %d, column: %d", error, line, column);
		String lineText = lines[line - 1];
		System.err.println("There was an error running your program\n" +
						   "---------------------------------------");
		if (line != 1) {
			System.err.printf("%3d| %s\n", line - 1, lines[line - 2]);
		}
		System.err.printf("%3d| %s\n", line, lineText);
		String arrow = new String(new char[column + 3]).replace("\0", "~") + "^";
		System.err.println(arrow);
		System.err.printf("Message: %s\n", error);
		
		if (line < lines.length) {
			System.err.printf("%3d| %s\n", line + 1, lines[line]);	
		}
	}
}
