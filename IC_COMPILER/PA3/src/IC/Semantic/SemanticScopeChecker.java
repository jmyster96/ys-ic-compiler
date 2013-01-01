package IC.Semantic;

import IC.AST.*;


public class SemanticScopeChecker implements Visitor {

	private IC.Symbols.GlobalSymbolTable global;

    public SemanticScopeChecker(IC.Symbols.GlobalSymbolTable global) {
        this.global = global;
    }

    /*
     * When visit fails return null 
     * otherwise return true (!= null) 
     */
    @Override
	public Object visit(Program program) {
        // recursive call to class
        for(ICClass clazz: program.getClasses()){
                if (clazz.accept(this) == null) return null;
        }
        return true;
	}

	@Override
	public Object visit(ICClass clazz) {
        for(Method meth: clazz.getMethods()){
                if (meth.accept(this) == null) return null;
        }
        for(Field fld: clazz.getFields()){
            if (fld.accept(this) == null) return null;
        }
        return true;      
    }

	@Override
	public Object visit(Field field) {
		field.getType().accept(this);
		field.getName(); //validate that doesn't exit 
		return true;
	}

	@Override
	public Object visit(VirtualMethod method) {
		for (Formal foraml : method.getFormals()) {
			if(foraml.accept(this) == null) {return null;}
		}
		for (Statement stmnt : method.getStatements()) {
			if(stmnt.accept(this) == null) {return null;}
		}
		if (method.getType().accept(this) == null) {
			return null;
		}
		method.getName(); //FIXME: scope check: validate that exist 
		return true;
	}

	@Override
	public Object visit(StaticMethod method) {
		for (Formal foraml : method.getFormals()) {
			if(foraml.accept(this) == null) {return null;}
		}
		for (Statement stmnt : method.getStatements()) {
			if(stmnt.accept(this) == null) {return null;}
		}
		if (method.getType().accept(this) == null) {
			return null;
		}
		method.getName(); //FIXME: validate that doesn't exist
		return true;
	}

	@Override
	public Object visit(LibraryMethod method) {
		for (Formal foraml : method.getFormals()) {
			if(foraml.accept(this) == null) {return null;}
		}
		for (Statement stmnt : method.getStatements()) {
			if(stmnt.accept(this) == null) {return null;}
		}
		if (method.getType().accept(this) == null) {
			return null;
		}
		method.getName(); //FIXME: validate that exist
		return true;
	}

	@Override
	public Object visit(Formal formal) {
		if(formal.getType().accept(this) == null) {
			return null;
		}
		/* FIXME: 
		 * validate that formal.getName()exists in scope
		 * validate that formal.getType() can be sent to function should be done 
		 * 
		 */
		
		formal.getName();
		return true;
	}

	@Override
	public Object visit(PrimitiveType type) {
		// always defined
		return true;
	}

	@Override
	public Object visit(UserType type) {
		type.getName();// FIXME validate that exists a type		
		return null;
	}

	@Override
	public Object visit(Assignment assignment) {
		if (assignment.getAssignment().accept(this) == null)	{
			return null;
		}
		assignment.getVariable().accept(this);
		return true;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		if (callStatement.getCall().accept(this) == null){
			return true;
		}
		return null;
	}

	@Override
	public Object visit(Return returnStatement) {
		if (returnStatement.getValue().accept(this) == null){
			return true;
		}
		// FIXME: type-check check if return value matches functions definition
		//look at father->father->type (ergo: return type of method) 
		return null;
	}

	@Override
	public Object visit(If ifStatement) {
		if (ifStatement.getCondition().accept(this) == null) return null;
		if (ifStatement.getOperation().accept(this) == null) return null;
		if (ifStatement.getElseOperation().accept(this) == null) return null;
		return true;
	}

	@Override
	public Object visit(While whileStatement) {
		if (whileStatement.getCondition().accept(this) == null) return null;
		if (whileStatement.getOperation().accept(this) == null) return null;
		return true;
	}

	@Override
	public Object visit(Break breakStatement) {
		// FIXME: validate that inside statementsBlock
		return true;
	}

	@Override
	public Object visit(Continue continueStatement) {
		// FIXME: validate that inside statementsBlock
		return true;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		for(Statement stmt : statementsBlock.getStatements()) {
			if (stmt.accept(this) == null) return null;
		}
		return true;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		if (localVariable.getType().accept(this) == null) {
			return null;
		}
		if (localVariable.getInitValue().accept(this) == null){
			return null;
		}
		return true;
	}

	@Override
	public Object visit(VariableLocation location) {
		if (location.getLocation().accept(this) == null) {return null;}
		return true;
	}

	@Override
	public Object visit(ArrayLocation location) {
		if (location.getIndex().accept(this) == null) {return null;}
		if (location.getArray().accept(this) == null) {return null;}
		return true;
	}

	@Override
	public Object visit(StaticCall call) {
		for (Expression arg : call.getArguments()) {
			if (arg.accept(this) == null) {
				return null;
			}
		}
		//FIXME:
		/* validate that name function name is define inside class 
		 * and class is recognized in scope
		 */
		call.getClassName();
		call.getName();
		
		return true;
	}

	@Override
	public Object visit(VirtualCall call) {
		for (Expression arg : call.getArguments()) {
			if (arg.accept(this) == null) {
				return null;
			}
		}
		//FIXME:
		/* validate that name function name is define inside class 
		 * and class is recognized in scope
		 */
		call.getName();
		return true;
	}

	@Override
	public Object visit(This thisExpression) {
		// shuoidn't be called
		return true;
	}

	@Override
	public Object visit(NewClass newClass) {
		newClass.getName();//FIXME: check that the name is none inside scope
		//FIXME: should there be a passing arguments to constructor check?
		return true;
	}

	@Override
	public Object visit(NewArray newArray) {
		if(newArray.getSize().accept(this) == null) {return null;}
		if(newArray.getType().accept(this) == null) {return null;}
		return true;
	}

	@Override
	public Object visit(Length length) {
		// FIXME: shuoldn't be visited?
		if(length.getArray().accept(this) == null){
			return true;
		}
		return true;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		if(binaryOp.getFirstOperand().accept(this) == null) {return null;}
		if(binaryOp.getSecondOperand().accept(this) == null) {return null;}		
		//FIXME: should we validate that getDeclaringClass is ok ?
		binaryOp.getOperator().getDeclaringClass();
		//FIXME: need to validate that these are a part can be up casted to the specific operation types
		binaryOp.getFirstOperand();
		binaryOp.getSecondOperand();
		return true;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		if (binaryOp.getFirstOperand().accept(this) == null) {return null;}
		if (binaryOp.getSecondOperand().accept(this) == null) {return null;}
		//FIXME: should we validate that getDeclaringClass is ok ?
		binaryOp.getOperator().getDeclaringClass();
		//FIXME: need to validate that these are a part can be up casted to the specific operation types
		binaryOp.getFirstOperand();
		binaryOp.getSecondOperand();
		return true;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		if(unaryOp.getOperand().accept(this) == null ) {return null;}
		//FIXME: should we validate that getDeclaringClass is ok ?
		unaryOp.getOperator().getDeclaringClass();
		//FIXME: need to validate that these are a part can be up casted to the specific operation types
		unaryOp.getOperand();
		return true;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		if(unaryOp.getOperand().accept(this) == null ) {return null;}
		//FIXME: should we validate that getDeclaringClass is ok ?
		unaryOp.getOperator().getDeclaringClass();
		//FIXME: need to validate that these are a part can be up casted to the specific operation types
		unaryOp.getOperand();
		return true;
	}

	@Override
	public Object visit(Literal literal) {
		// TODO Auto-generated method stub
		literal.getType();
		literal.getValue();
		return true;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		// TODO Auto-generated method stub
		if (expressionBlock.getExpression().accept(this) == null) {return null;}
		return true;
	}

    
    
}
