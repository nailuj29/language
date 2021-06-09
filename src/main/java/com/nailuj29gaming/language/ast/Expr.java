package com.nailuj29gaming.language.ast;

import java.util.List;

import com.nailuj29gaming.language.Token;

/**
 * An expression generated by the {@link} Parser. Based off CI's Expr class
 */
public abstract class Expr {
	/**
	 * A class capable of traversing an expression tree
	 * @param <R> The type the expressions will return
	 */
	public static interface Visitor<R> {
		/**
		 * Visit an assignment expression and its children
		 * @param expr the expression
		 * @return the result of the expression
		 */
		R visitAssignExpr(Assign expr);
		/**
		 * Visit an assign to index expression and its children
		 * @param expr the expression
		 * @return the result of the expression
		 */
		R visitAssignIndexExpr(AssignIndex expr);
		/**
		 * Visit a binary expression and its children
		 * @param expr the expression
		 * @return the result of the expression
		 */
		R visitBinaryExpr(Binary expr);
		/**
		 * Visit a function call expression and its children
		 * @param expr the expression
		 * @return the result of the expression
		 */
		R visitCallExpr(Call expr);
		/**
		 * Visit a variable access expression
		 * @param expr the expression
		 * @return the result of the expression
		 */
		R visitGetVarExpr(GetVar expr);
		/**
		 * Visit a grouping expression and its children
		 * @param expr the expression
		 * @return the result of the expression
		 */
		R visitGroupingExpr(Grouping expr);
		/**
		 * Visit an import expression
		 * @param expr the expression
		 * @return the result of the expression
		 */
		R visitImportExpr(Import expr);
		/**
		 * Visit an indexing expression
		 * @param expr the expression
		 * @return the result of the expression
		 */
		R visitIndexExpr(Index expr);
		/**
		 * Visit a list expression and its children
		 * @param expr the expression
		 * @return the result of the expression
		 */
		R visitListExpr(EList expr);
		/**
		 * Visit a literal expression
		 * @param expr the expression
		 * @return the result of the expression
		 */
		R visitLiteralExpr(Literal expr);
		/**
		 * Visit a unary expression and its children
		 * @param expr the expression
		 * @return the result of the expression
		 */
		R visitUnaryExpr(Unary expr);
		
	}
	
	/**
	 * Set the value of a variable
	 */
	public static class Assign extends Expr {
		private Token identifier;
		private Expr right;


		/**
		 * @param identifier the identifier being assigned to
		 * @param right the value that is assigned
		 */
		public Assign(Token identifier, Expr right) {
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
		 * @return the value being set
		 */
		public Expr getRight() {
			return right;
		}


		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitAssignExpr(this);
		}
		
	}
	
	/**
	 * Set indexed variable
	 *
	 */
	public static class AssignIndex extends Assign {
		private Expr index;

		/**
		 * @param identifier the identifier of the variable
		 * @param right the value to set it to
		 * @param index the index to set at
		 */
		public AssignIndex(Token identifier, Expr right, Expr index) {
			super(identifier, right);
			this.index = index;
		}

		/**
		 * @return the index
		 */
		public Expr getIndex() {
			return index;
		}

		/**
		 * @param index the index to set
		 */
		public void setIndex(Expr index) {
			this.index = index;
		}
		
		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitAssignIndexExpr(this);
		}
	}
	
	/**
	 * A binary expression
	 */
	public static class Binary extends Expr {

		private Expr left;
		private Token operator;
		private Expr right;
		
		/**
		 * @param left the left hand value
		 * @param operator the operator
		 * @param right the right hand value
		 */
		public Binary(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		/**
		 * @return the left hand value
		 */
		public Expr getLeft() {
			return left;
		}

		/**
		 * @return the operator
		 */
		public Token getOperator() {
			return operator;
		}
		
		/**
		 * @return the right hand value
		 */
		public Expr getRight() {
			return right;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpr(this);
		}
	
	}
	
	/**
	 * A function call expression
	 */
	public static class Call extends Expr {
		private Expr.GetVar callee;
		private List<Expr> args;
		private Token paren;
		
		/**
		 * @param callee the named function being called. IIFEs are not supported
		 * @param args the arguments being called with
		 * @param paren one of the parens of the call, used for debugging
		 */
		public Call(GetVar callee, List<Expr> args, Token paren) {
			this.callee = callee;
			this.args = args;
			this.paren = paren;
		}


		/**
		 * @return the callee
		 */
		public Expr.GetVar getCallee() {
			return callee;
		}


		/**
		 * @return the args
		 */
		public List<Expr> getArgs() {
			return args;
		}

		
		
		/**
		 * @return one of the parens, used for error messages
		 */
		public Token getParen() {
			return paren;
		}


		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitCallExpr(this);
		}
	}
	
	/**
	 * Getting a variable
	 */
	public static class GetVar extends Expr {
		
		private Token identifier;
		
		/**
		 * @param identifier the identifier to set
		 */
		public GetVar(Token identifier) {
			this.identifier = identifier;
		}

		/**
		 * @return the identifier of the variable
		 */
		public Token getIdentifier() {
			return identifier;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitGetVarExpr(this);
		}
		
	}
	
	/**
	 * A grouping expression
	 */
	public static class Grouping extends Expr {
		
		private Expr expression;
		
		/**
		 * @param expression the expression that is enclosed
		 */
		public Grouping(Expr expression) {
			this.expression = expression;
		}

		/**
		 * @return the expression being grouped
		 */
		public Expr getExpression() {
			return expression;
		}
		
		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitGroupingExpr(this);
		}
		
	}
	
	/**
	 * Accessing the value of an import
	 */
	public static class Import extends GetVar {

		private Token module;
		
		/**
		 * @param module the module of the import
		 * @param name the name of the thing being imported
		 */
		public Import(Token module, Token name) {
			super(name);
			this.module = module;
		}

		/**
		 * @return the module the import comes from
		 */
		public Token getModule() {
			return module;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitImportExpr(this);
		}
		
	}
	
	public static class Index extends Expr {
		
		private Expr index;
		private Expr indexee;
		private Token bracket;

		/**
		 * @param index the index
		 * @param indexee the thing being indexed
		 */
		public Index(Expr index, Expr indexee, Token bracket) {
			this.index = index;
			this.indexee = indexee;
			this.bracket = bracket;
		}

		

		/**
		 * @return the bracket
		 */
		public Token getBracket() {
			return bracket;
		}



		/**
		 * @return the index
		 */
		public Expr getIndex() {
			return index;
		}



		/**
		 * @return the indexee
		 */
		public Expr getIndexee() {
			return indexee;
		}



		@Override
		public <R> R accept(Visitor<R> visitor) {
		
			return visitor.visitIndexExpr(this);
		}
		
	}
	
	/**
	 * A list expression
	 */
	public static class EList extends Expr {

		private List<Expr> exprs;
		
		/**
		 * @param exprs the expressions in the list
		 */
		public EList(List<Expr> exprs) {
			this.exprs = exprs;
		}



		/**
		 * @return the exprs
		 */
		public List<Expr> getExprs() {
			return exprs;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
		
			return visitor.visitListExpr(this);
		}
		
	}
	
	/**
	 * A literal expression. String, boolean, or number
	 */
	public static class Literal extends Expr {
		
		private Object value;

		/**
		 * @param value the value to set
		 */
		public Literal(Object value) {
			this.value = value;
		}

		/**
		 * @return the value being held by this expression
		 */
		public Object getValue() {
			return value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteralExpr(this);
		}
		
	}
	
	/**
	 * A unary expression
	 */
	public static class Unary extends Expr {

		private Token operator;
		private Expr value;
		
		/**
		 * @param operator the operator used in the expression
		 * @param value the expression on the right of the operator
		 */
		public Unary(Token operator, Expr value) {
			this.operator = operator;
			this.value = value;
		}

		/**
		 * @return the operator
		 */
		public Token getOperator() {
			return operator;
		}

		/**
		 * @return the value
		 */
		public Expr getValue() {
			return value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpr(this);
		}
	}
	

	
	/**
	 * Utility message to allow a {@link Visitor} to properly visit this
	 * @param <R> the return type of the expression
	 * @param visitor the visitor that will visit this expression
	 * @return the value returned by the visit
	 */
	public abstract <R> R accept(Visitor<R> visitor);
}
