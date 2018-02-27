package types;

import java.util.HashMap;
import java.util.List;

import ast.*;

public class TypeChecker implements CommandVisitor {
    
    private HashMap<Command, Type> typeMap;
    private StringBuffer errorBuffer;

    /* Useful error strings:
     *
     * "Function " + func.name() + " has a void argument in position " + pos + "."
     * "Function " + func.name() + " has an error in argument in position " + pos + ": " + error.getMessage()
     *
     *
     * "Not all paths in function " + currentFunctionName + " have a return."
     *

     *
     * "Function " + currentFunctionName + " returns " + currentReturnType + " not " + retType + "."
     *

     */

    public TypeChecker()
    {
        typeMap = new HashMap<Command, Type>();
        errorBuffer = new StringBuffer();
    }

    private void reportError(int lineNum, int charPos, String message)
    {
        errorBuffer.append("TypeError(" + lineNum + "," + charPos + ")");
        errorBuffer.append("[" + message + "]" + "\n");
    }

    private void put(Command node, Type type)
    {
        if (type instanceof ErrorType) {
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType)type).getMessage());
        }
        typeMap.put(node, type);
    }
    
    public Type getType(Command node)
    {
        return typeMap.get(node);
    }
    
    public boolean check(Command ast)
    {
        ast.accept(this);
        return !hasError();
    }
    
    public boolean hasError()
    {
        return errorBuffer.length() != 0;
    }
    
    public String errorReport()
    {
        return errorBuffer.toString();
    }

    @Override
    public void visit(ExpressionList node) {
    	TypeList list = new TypeList();
    	node.forEach(x ->
    	{
    		x.accept(this);
    		list.append(getType((Command)x));
    	});
    	put(node, list);
    }

    @Override
    public void visit(DeclarationList node) {
    	TypeList list = new TypeList();
    	node.forEach(x -> 
    	{
    		x.accept(this);
    		list.append(getType((Command)x));
    	});
    	put(node, list);
    }

    @Override
    public void visit(StatementList node) {
    	node.forEach(x -> 
    	{
    		x.accept(this);
    		if(x instanceof Return)
    		{
    			put(node, getType((Command)x));
    		}
    	});
    	if (getType((Command)node) == null)
    	{
    		put(node, new VoidType());
    	}
    }

    @Override
    public void visit(AddressOf node) {
    	put(node, node.symbol().type());
    }

    @Override
    public void visit(LiteralBool node) {
    	put(node, new BoolType());
    }

    @Override
    public void visit(LiteralFloat node) {
    	put(node, new FloatType());
    }

    @Override
    public void visit(LiteralInt node) {
    	put(node, new IntType());
    }

    @Override
    public void visit(VariableDeclaration node) {
    	if(node.symbol().type() instanceof ErrorType)
    	{
    		put(node, new ErrorType("Variable " + node.symbol().name() + " has invalid type " + node.symbol().type() + "."));
    	}
    	else
    	{
        	put(node, node.symbol().type());

    	}
    }

    @Override
    public void visit(ArrayDeclaration node) {
    	if(node.symbol().type() instanceof ErrorType)
    	{
    		put(node, new ErrorType("Array " + node.symbol().name() + " has invalid base type " + node.symbol().type() + "."));
    	}
    	else
    	{
        	put(node, node.symbol().type());

    	}
    }
    @Override
    public void visit(FunctionDefinition node) {
    	Type retType = ((FuncType)node.function().type()).returnType();
        if (node.symbol().name().equals("main") && !(retType instanceof VoidType))
        {
        	put(node, new ErrorType("Function main has invalid signature."));
        }
        else
        {       
        	put(node, node.function().type());
        	
        	node.arguments().forEach(x -> 
        	{
        		if(x.type() instanceof VoidType)
        		{
        			put(node, new ErrorType("Function " + node.function().name() + " has a void argument in position " + (node.charPosition() - 1) + "."));
        		}
        		if(x.type() instanceof ErrorType)
        		{
        			put(node, new ErrorType("Function " + node.function().name() + " has an error in argument in position " + 
        					(node.charPosition() - 1) + ": " + ((ErrorType)x.type()).getMessage()));
        		}
        	});
        	node.body().accept(this);
        	if(!getType((Command)node.body()).equivalent(retType))
        	{
        		put(node, new ErrorType("Function " + node.function().name() + " returns " + getType((Command)node.body()) + " not " + retType + "."));
        	}
        }
    }

    @Override
    public void visit(Comparison node) {
    	node.leftSide().accept(this);
    	node.rightSide().accept(this);
    	put(node, getType((Command)node.leftSide()).compare(getType((Command)node.rightSide())));
    }
    
    @Override
    public void visit(Addition node) {
    	node.leftSide().accept(this);
    	node.rightSide().accept(this);
    	put(node, getType((Command)node.leftSide()).add(getType((Command)node.rightSide())));
    }
    
    @Override
    public void visit(Subtraction node) {
    	node.leftSide().accept(this);
    	node.rightSide().accept(this);
    	put(node, getType((Command)node.leftSide()).sub(getType((Command)node.rightSide())));
    }
    
    @Override
    public void visit(Multiplication node) {
    	node.leftSide().accept(this);
    	node.rightSide().accept(this);
    	put(node, getType((Command)node.leftSide()).mul(getType((Command)node.rightSide())));
    }
    
    @Override
    public void visit(Division node) {
    	node.leftSide().accept(this);
    	node.rightSide().accept(this);
    	put(node, getType((Command)node.leftSide()).div(getType((Command)node.rightSide())));
    }
    
    @Override
    public void visit(LogicalAnd node) {
    	node.leftSide().accept(this);
    	node.rightSide().accept(this);
    	put(node, getType((Command)node.leftSide()).and(getType((Command)node.rightSide())));
    }

    @Override
    public void visit(LogicalOr node) {
    	node.leftSide().accept(this);
    	node.rightSide().accept(this);
    	put(node, getType((Command)node.leftSide()).or(getType((Command)node.rightSide())));
    }

    @Override
    public void visit(LogicalNot node) {
    	node.expression().accept(this);
    	put(node, getType((Command)node.expression()));
    }
    
    @Override
    public void visit(Dereference node) {
    	node.expression().accept(this);
    	put(node, getType((Command)node.expression()));
    }

    @Override
    public void visit(Index node) {
    	
    }

    @Override
    public void visit(Assignment node) {
    	node.destination().accept(this);
    	node.source().accept(this);
    	put(node, getType((Command)node.destination()).assign(getType((Command)node.source())));
    }

    @Override
    public void visit(Call node) {
    	node.arguments().accept(this);
    	node.arguments().forEach(x ->
    	{
    		x.accept(this);
    	});
    	put(node, ((FuncType)node.function().type()).returnType());
    }

    @Override
    public void visit(IfElseBranch node) {
    	if (getType((Command)node.condition()).equivalent(new BoolType()))
    	{
    		put(node, new VoidType());
    	}
    	else
    	{
    		put(node, new ErrorType("IfElseBranch requires bool condition not " + getType((Command)node.condition()) + "."));
    	}
    }
    
    @Override
    public void visit(WhileLoop node) {
    	if (getType((Command)node.condition()).equivalent(new BoolType()))
    	{
    		put(node, new VoidType());
    	}
    	else
    	{
    		put(node, new ErrorType("WhileLoop requires bool condition not " + getType((Command)node.condition()) + "."));
    	}
    }

    @Override
    public void visit(Return node) {
    	node.argument().accept(this);
    	put(node, getType((Command)node.argument()));
    }
    
    @Override
    public void visit(ReadSymbol node) {
    	put(node, node.symbol().type());
    }

    @Override
    public void visit(ast.Error node) {
        put(node, new ErrorType(node.message()));
    }
}
