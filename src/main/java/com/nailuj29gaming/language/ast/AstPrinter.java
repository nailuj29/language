package com.nailuj29gaming.language.ast;

import java.util.List;

/**
 * This class assists in debugging the AST, ensuring it is generated properly
 */
public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

	/**
	 * Turns an {@link Expr} into a sexp-like string
	 * 
	 * @param expr the expression to print
	 * @return a sexp to be printed
	 */
	public String print(Expr expr) {
		return expr.accept(this);
	}

	/**
	 * Turns a list of {@link Stmt}s into a sexp-like string to be printed
	 * 
	 * @param statements the statements to be printed
	 * @return a list of sexps, seperated by newlines
	 */
	public String print(List<Stmt> statements) {
		StringBuilder builder = new StringBuilder();

		for (Stmt stmt : statements) {
			builder.append(stmt.accept(this));
			builder.append('\n');
		}

		return builder.toString();
	}

	@Override
	public String visitBlockStmt(Stmt.Block stmt) {
		StringBuilder sb = new StringBuilder();

		for (Stmt statement : stmt.getStmts()) {
			sb.append("  ");
			sb.append(statement.accept(this));
			sb.append('\n');
		}
		return sb.toString();
	}
	
	@Override
	public String visitBreakStmt(Stmt.Break stmt) {
		return "(break)";
	}
	
	@Override
	public String visitContinueStmt(Stmt.Continue stmt) {
		return "(contunue)";
	}
	
	@Override
	public String visitExpressionStmt(Stmt.Expression stmt) {
		return print(stmt.getExpression());
	}
	
	@Override
	public String visitIfStmt(Stmt.If stmt) {
		return parenthesize("if " + print(stmt.getCondition()), stmt.getIfBranch(), stmt.getElseBranch());
	}
	
	@Override
	public String visitImportStmt(Stmt.Import stmt) {
		return String.format("(import %s)", stmt.getImportName().getLexeme());
	}
	
	@Override
	public String visitReturnStmt(Stmt.Return stmt) {
		if (stmt.getExpr() == null) {
			return "(return)";
		}
		return parenthesize("return", stmt.getExpr());
	}

	@Override
	public String visitVarStmt(Stmt.Var stmt) {
		return parenthesize("var " + stmt.getIdentifier().getLexeme(), stmt.getRight());
	}
	
	@Override
	public String visitWhileStmt(Stmt.While stmt) {
		return parenthesize("while " + print(stmt.getCondition()), stmt.getBody());
	}

	@Override
	public String visitAssignExpr(Expr.Assign expr) {
		return parenthesize("set " + expr.getIdentifier().getLexeme(), expr.getRight());
	}
	
	@Override
	public String visitAssignIndexExpr(Expr.AssignIndex expr) {
		return parenthesize("set " + expr.getIdentifier().getLexeme(), expr.getIndex(), expr.getRight());
	}
	
	@Override
	public String visitBinaryExpr(Expr.Binary expr) {
		return parenthesize(expr.getOperator().getLexeme(), expr.getLeft(), expr.getRight());
	}
	
	@Override
	public String visitCallExpr(Expr.Call expr) {
		Expr[] args = new Expr[expr.getArgs().size()];
		expr.getArgs().toArray(args);
		return parenthesize(expr.getCallee().getIdentifier().getLexeme(), args);
	}
	
	@Override
	public String visitGetVarExpr(Expr.GetVar expr) {
		return expr.getIdentifier().getLexeme();
	}


	@Override
	public String visitGroupingExpr(Expr.Grouping expr) {
		return parenthesize("group", expr.getExpression());
	}
	
	@Override
	public String visitImportExpr(Expr.Import expr) {
		return expr.getModule().getLexeme() + "/" + expr.getIdentifier().getLexeme();
	}
	
	@Override
	public String visitIndexExpr(Expr.Index expr) {
		return parenthesize("get", expr.getIndexee(), expr.getIndex());
	}
	
	@Override
	public String visitListExpr(Expr.EList expr) {
		return parenthesize("list", expr.getExprs());
	}

	@Override
	public String visitLiteralExpr(Expr.Literal expr) {
		if (expr.getValue() instanceof String) {
			return "\"" + expr.getValue() + "\"";
		}
		return expr.getValue().toString();
	}


	@Override
	public String visitUnaryExpr(Expr.Unary expr) {
		return parenthesize(expr.getOperator().getLexeme(), expr.getValue());
	}

	/**
	 * Copied from CI's parenthesize method, turns a list of {@link Expr}s and a
	 * name into a sexp
	 * 
	 * @param name  The first item of the sexp
	 * @param exprs The body of the sexp
	 * @return a sexp
	 */
	private String parenthesize(String name, Expr... exprs) {
		StringBuilder builder = new StringBuilder();

		builder.append("(").append(name);
		for (Expr expr : exprs) {
			builder.append(" ");
			builder.append(expr.accept(this));
		}
		builder.append(")");

		return builder.toString();
	}
	
	/**
	 * Copied from CI's parenthesize method, turns a list of {@link Expr}s and a
	 * name into a sexp
	 * 
	 * @param name  The first item of the sexp
	 * @param exprs The body of the sexp
	 * @return a sexp
	 */
	private String parenthesize(String name, List<Expr> exprs) {
		StringBuilder builder = new StringBuilder();

		builder.append("(").append(name);
		for (Expr expr : exprs) {
			builder.append(" ");
			builder.append(expr.accept(this));
		}
		builder.append(")");

		return builder.toString();
	}
	
	/**
	 * turns a list of {@link Sxpr}s and a
	 * name into a sexp
	 * 
	 * @param name  The first item of the sexp
	 * @param stmts The body of the sexp
	 * @return a sexp
	 */
	private String parenthesize(String name, Stmt... stmts) {
		StringBuilder builder = new StringBuilder();

		builder.append("(").append(name);
		for (Stmt stmt : stmts) {
			builder.append("\n");
			builder.append(stmt.accept(this));
		}
		builder.append(")");

		return builder.toString();
	}
}
