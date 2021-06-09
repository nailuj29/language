package com.nailuj29gaming.language.ast;

import java.util.List;

import com.nailuj29gaming.language.Token;

/**
 * A statement in the AST
 */
public abstract class Stmt {

	/**
	 * A class capable of processing a statement
	 * @param <R> the type of the result of processing the statement
	 */
	public static interface Visitor<R> {
		/**
		 * Visit a block statement and its children
		 * @param stmt the statement
		 * @return the result of the statement
		 */
		R visitBlockStmt(Block stmt);
		/**
		 * Visit a break statement
		 * @param stmt the statement
		 * @return the result of the statement
		 */
		R visitBreakStmt(Break stmt);
		/**
		 * Visit a continue statement
		 * @param stmt the statement
		 * @return the result of the statement
		 */
		R visitContinueStmt(Continue stmt);
		/**
		 * Visit an expression statement and its children
		 * @param stmt the statement
		 * @return the result of the statement
		 */
		R visitExpressionStmt(Expression stmt);
		/**
		 * Visit an if statement and its children
		 * @param stmt the statement
		 * @return the result of the statement
		 */
		R visitIfStmt(If stmt);
		/**
		 * Visit an import statement
		 * @param stmt the statement
		 * @return the result of the statement
		 */
		R visitImportStmt(Import stmt);
		/**
		 * Visit a return statement and its children
		 * @param stmt the statement
		 * @return the result of the statement
		 */
		R visitReturnStmt(Return stmt);
		/**
		 * Visit a variable declaration and its children
		 * @param stmt the statement
		 * @return the result of the statement
		 */
		R visitVarStmt(Var stmt);
		/**
		 * Visit a while statement and its children
		 * @param stmt the statement
		 * @return the result of the statement
		 */
		R visitWhileStmt(While stmt);
	}
	
	/**
	 * A block of statements
	 * Creates a new scope
	 */
	public static class Block extends Stmt {
		
		private List<Stmt> stmts;
		
		/**
		 * @param stmts the statements contained in the block
		 */
		public Block(List<Stmt> stmts) {
			this.stmts = stmts;
		}

		/**
		 * @return the stmts
		 */
		public List<Stmt> getStmts() {
			return stmts;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockStmt(this);
		}
	}
	
	/**
	 * A break statement, used to exit a loop early
	 */
	public static class Break extends Stmt {
		
		private Token keyword;
		
		

		/**
		 * @param keyword the break keyword
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



		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBreakStmt(this);
		}
		
	}
	
	/**
	 * A continue statement, used to complete an iteration of a loop earl
	 */
	public static class Continue extends Stmt {
		
		private Token keyword;
		
		

		/**
		 * @param keyword the continue keyword
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



		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitContinueStmt(this);
		}
		
	}
	
	/**
	 * A statement containing a single expression
	 * Mostly useful when the expression has side effects
	 */
	public static class Expression extends Stmt {

		private Expr expression;
		
		/**
		 * @param expression the expression contained in the statement
		 */
		public Expression(Expr expression) {
			this.expression = expression;
		}

		/**
		 * @return the expression
		 */
		public Expr getExpression() {
			return expression;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitExpressionStmt(this);
		}
		
	}
	
	/**
	 * An if statement and its optional else branch
	 */
	public static class If extends Stmt {
		private Expr condition;
		private Stmt.Block ifBranch;
		private Stmt.Block elseBranch;
		private Token keyword;
		
		/**
		 * @param condition the condition of the if statement
		 * @param ifBranch the block to execute if the condition is true
		 * @param elseBranch the block to execute if the statement is false
		 * @param keyword the if keyword
		 */
		public If(Expr condition, Block ifBranch, Block elseBranch, Token keyword) {
			this.condition = condition;
			this.ifBranch = ifBranch;
			this.elseBranch = elseBranch;
			this.keyword = keyword;
		}

		/**
		 * @return the condition
		 */
		public Expr getCondition() {
			return condition;
		}

		/**
		 * @return the ifBranch
		 */
		public Stmt.Block getIfBranch() {
			return ifBranch;
		}

		/**
		 * @return the elseBranch
		 */
		public Stmt.Block getElseBranch() {
			return elseBranch;
		}

		/**
		 * @return the keyword
		 */
		public Token getKeyword() {
			return keyword;
		}

		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitIfStmt(this);
		}
	}
	
	/**
	 * An import statement
	 */
	public static class Import extends Stmt {
		
		private Token importName;

		/**
		 * @param importName the name of the module to import
		 */
		public Import(Token importName) {
			this.importName = importName;
		}

		

		/**
		 * @return the importName
		 */
		public Token getImportName() {
			return importName;
		}



		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitImportStmt(this);
		}
		
	}
	
	/**
	 * A return statement
	 */
	public static class Return extends Stmt {
		private Token keyword;
		private Expr expr;
		
		
		/**
		 * @param keyword the return keyword
		 * @param expr the value to return
		 */
		public Return(Token keyword, Expr expr) {
			this.keyword = keyword;
			this.expr = expr;
		}

		

		/**
		 * @return the keyword
		 */
		public Token getKeyword() {
			return keyword;
		}



		/**
		 * @return the expr
		 */
		public Expr getExpr() {
			return expr;
		}



		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitReturnStmt(this);
		}

	}
	
	/**
	 * A variable declaration
	 */
	public static class Var extends Stmt {
		private Token identifier;
		private Expr right;


		/**
		 * @param identifier the name of the variable to declare
		 * @param right the value of the variable
		 */
		public Var(Token identifier, Expr right) {
			this.identifier = identifier;
			this.right = right;
		}



		/**
		 * @return the identifier
		 */
		public Token getIdentifier() {
			return identifier;
		}



		/**
		 * @return the right
		 */
		public Expr getRight() {
			return right;
		}



		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitVarStmt(this);
		}
		
	}
	
	/**
	 * A while loop. All loops are syntactic sugar for this loop
	 */
	public static class While extends Stmt {

		private Expr condition;
		private Stmt.Block body;
		private Token keyword;
		
		/**
		 * @param condition the condition to terminate on for the loop
		 * @param body the body of the loop
		 * @param keyword the while keyword
		 */
		public While(Expr condition, Block body, Token keyword) {
			this.condition = condition;
			this.body = body;
			this.keyword = keyword;
		}

		/**
		 * @return the condition
		 */
		public Expr getCondition() {
			return condition;
		}

		/**
		 * @return the body
		 */
		public Stmt.Block getBody() {
			return body;
		}

		/**
		 * @return the keyword
		 */
		public Token getKeyword() {
			return keyword;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStmt(this);
		}
		
	}
	
	/**
	 * Allow a {@link Visitor} to process this Stmt
	 * @param <R> the type this {@link Visitor} must return
	 * @param visitor the visitor to proccess this Stmt
	 * @return the value returned
	 */
	public abstract <R> R accept(Visitor<R> visitor);
}
