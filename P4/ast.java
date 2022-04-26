import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a minim program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Kids
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignExpNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignExpNode       ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of kids, or
// internal nodes with a fixed number of kids:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of kids:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, ReadStmtNode,   WriteStmtNode   
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
//   ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode { 
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
        for (int k=0; k<indent; k++) p.print(" ");
    }
}

// **********************************************************************
//   ProgramNode,  DeclListNode, FormalsListNode, FnBodyNode,
//   StmtListNode, ExpListNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }
    
    public void nameAnalysis() {
    	// Create root table for whole program
    	SymTable table = new SymTable();
    	myDeclList.nameAnalysis(table);
    	
    	// System.out.println(" ----- Final table ----- ");
    	// table.print();
    	
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    // one kid
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }
    
    /**
     * Name analysis for decl list node
     * 
     * @param SymTable table
     */
    public void nameAnalysis(SymTable table) {
    	Iterator it = myDecls.iterator();	
    	
    	// Iterate over decl list and call nameAnalysis() on each node
    	try {
            while (it.hasNext()) {
            	DeclNode node = (DeclNode) it.next();
            	node.nameAnalysis(table);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }
    
    public void nameAnalysis(SymTable table, SymTable structTable) {
    	Iterator it = myDecls.iterator();	
    	
    	// Iterate over decl list and call nameAnalysis() on each node
    	try {
            while (it.hasNext()) {
            	VarDeclNode node = (VarDeclNode) it.next();
            	node.structFieldsNameAnalysis(table, structTable);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    // list of kids (DeclNodes)
    private List<DeclNode> myDecls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }
    
    /**
     * Name analysis for formals list
     * 
     * @param SymTable table
     */
    public void nameAnalysis(SymTable table) {
    	Iterator it = myFormals.iterator();

    	try {
            while (it.hasNext()) {
            	FormalDeclNode node = (FormalDeclNode) it.next();
            	node.nameAnalysis(table);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }
    
    /**
     * Get list of types for formal declerations
     * 
     * @return LinkedList<String> list of formal decleration types
     */
    public LinkedList<String> getTypes() {
    	LinkedList<String> fnParameterTypes = new LinkedList<String>();
    	
    	Iterator<FormalDeclNode> it = myFormals.iterator();
    	
        if (it.hasNext()) {
            while (it.hasNext()) {  
            	FormalDeclNode node = (FormalDeclNode) it.next();
            	String type = node.getType();
            	
            	fnParameterTypes.add(type);
            }
        } 
    	
    	return fnParameterTypes;
    }

    // list of kids (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }
    
    /**
     * Name analysis for function body
     * 
     * @param SymTable table
     */
    public void nameAnalysis(SymTable table){
        this.myDeclList.nameAnalysis(table);
        this.myStmtList.nameAnalysis(table);
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    // two kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }
    
    /**
     * Name analysis for statement list
     * 
     * @param SymTable table
     */
    public void nameAnalysis(SymTable table) {
    	Iterator it = myStmts.iterator();

    	try {
            while (it.hasNext()) {
            	StmtNode node = (StmtNode) it.next();	
                node.nameAnalysis(table);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }


    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    // list of kids (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }
    
    /**
     * Name analysis for exp list
     * 
     * @param SymTable table
     */
    public void nameAnalysis(SymTable table) {
    	Iterator it = myExps.iterator();

    	try {
            while (it.hasNext()) {
            	ExpNode node = (ExpNode) it.next();	
                node.nameAnalysis(table);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // list of kids (ExpNodes)
    private List<ExpNode> myExps;
}

// **********************************************************************
// ******  DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
	abstract public void nameAnalysis(SymTable table);
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }
    
    /**
     * Name analysis for decl node
     * Calls correct name analysis function depending on decl node type
     * 
     * @param SymTable table
     */
    public void nameAnalysis(SymTable table) {	
    	if (this.myType instanceof StructNode) {
    		structInstanceNameAnalysis(table);
    	} else {
    		varNameAnalysis(table);
    	}	
    }
    
    public void structFieldsNameAnalysis(SymTable table, SymTable structTable) {	
    	if (this.myType instanceof StructNode) {
    		structInstanceNameAnalysis(table, structTable);
    	} else {
    		varNameAnalysis(structTable);
    	}	
    }
    
    /**
     * Name analysis for declaring a variable of struct type (struct instance)
     * 
     * RELEVANT GRAMMAR: STRUCT id id SEMICOLON
     * 
     * @param SymTable table
     * @return SymTable table
     */
    public void structInstanceNameAnalysis(SymTable table) {         	
    	structInstanceNameAnalysis(table, null);
    }
    
    public void structInstanceNameAnalysis(SymTable table, SymTable structTable) {
    	// Get id of decl node
    	String structInstanceType = this.myType.toString();
    	String id = this.myId.getMyStrVal();
    	    	
    	// Create new struct instance symbol
    	StructInstanceSym newStructSymbol = new StructInstanceSym(id, structInstanceType);
    
    	// Check to see if struct actually exists
    	try {
    		Sym structSymbol = table.lookupGlobal(structInstanceType);
    		
    		if (structSymbol == null) {
    			int lineNum = this.myId.getMyLineNum();
        		int charNum = this.myId.getMyCharNum();
        		
        		ErrMsg.fatal(lineNum, charNum, "Name of struct type invalid");
    		}
    		
    	} catch (EmptySymTableException e) {
    		System.out.println("VarDeclNode: structInstanceNameAnalysis(): " + e);
    	}
    	
    	// Attempt to add decl to sym table
    	try {
    		if (structTable != null) {
    			structTable.addDecl(id, newStructSymbol);
    		} else {
    			table.addDecl(id, newStructSymbol);
    		}
    	} catch (DuplicateSymException e) {
    		int lineNum = this.myId.getMyLineNum();
    		int charNum = this.myId.getMyCharNum();
    		
    		ErrMsg.fatal(lineNum, charNum, "Identifier multiply-declared");
    		
    	} catch (EmptySymTableException e) {
    		System.out.println("VarDeclNode: structInstanceNameAnalysis(): " + e);
    	}
    }
      
    /**
     * Name analysis for declaring a primitive variable (i.e. not struct)
     * 
     * RELEVANT GRAMMAR: type id SEMICOLON
     * 
     * @param SymTable table
     * @return SymTable table
     */
    public void varNameAnalysis(SymTable table) { 
    	
    	// Check if variable is declared void
    	if (this.myType instanceof VoidNode) {
    		int lineNum = this.myId.getMyLineNum();
    		int charNum = this.myId.getMyCharNum();
    		
    		ErrMsg.fatal(lineNum, charNum, "Non-function declared void");
    		
    		return;
    	}
    	
    	// Get type and id of decl node
    	String type = this.myType.getType();
    	String id = this.myId.getMyStrVal();
    	
    	// Create new regular symbol and pass type/name
    	Sym symbol = new Sym(type, id);
    	    	
    	// Attempt to add decl to sym table
    	try {
    		table.addDecl(id, symbol);
    	} catch (DuplicateSymException e) {
    		int lineNum = this.myId.getMyLineNum();
    		int charNum = this.myId.getMyCharNum();
    		
    		ErrMsg.fatal(lineNum, charNum, "Identifier multiply-declared");
    		
    	} catch (EmptySymTableException e) {
    		System.out.println("VarDeclNode: varNameAnalysis(): " + e);
    	}	
    	
    }
    
    public TypeNode getMyType() {
    	return this.myType;
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.println(";");
    }

    // three kids
    private TypeNode myType;
    private IdNode myId;
    private int mySize;  // use value NOT_STRUCT if this is not a struct type
    
    public static int NOT_STRUCT = -1;
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type,
                      IdNode id,
                      FormalsListNode formalList,
                      FnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }
    
    /**
     * Name analysis for function decleration
     *  
     * @param SymTable table
     * @return SymTable table
     */
    public void nameAnalysis(SymTable table) {
    	// Get type and name/id
    	String returnType = this.myType.getType();
    	String id = this.myId.getMyStrVal();
    	    	
    	// Get parameter types of function
    	LinkedList<String> parameterTypes = this.myFormalsList.getTypes();
    	    	
    	// Create new function symbol
    	FnSym symbol = new FnSym(id, returnType, parameterTypes);
    	
    	// Add function decleration to sym table
    	try {
    		table.addDecl(id, symbol);
    	} catch (DuplicateSymException e) {
    		int lineNum = this.myId.getMyLineNum();
    		int charNum = this.myId.getMyCharNum();
    		
    		ErrMsg.fatal(lineNum, charNum, "Identifier multiply-declared");
    		
    	} catch (EmptySymTableException e) {
    		System.out.println("FnDeclNode nameAnalysis(): " + e);
    		
    	} catch (Exception e) {
    		System.out.println("FnDeclNode nameAnalysis(): Unknown error occurred: " + e);
    		
    	}
    	    	        	
    	// Add new scope for function params and body
    	table.addScope();
    	    	
    	// Process formals and body
    	this.myFormalsList.nameAnalysis(table);
    	this.myBody.nameAnalysis(table);
    	    	
    	// Remove new scope
    	try {
    		table.removeScope();
    	} catch (EmptySymTableException e) {
    		System.out.println("FnDeclNode nameAnalysis(): " + e);
    	}
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent+4);
        p.println("}\n");
    }

    // 4 kids
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FnBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }
    
    /**
     * Name analysis for formal (fn parameter)
     *  
     * @param SymTable table
     * @return SymTable table
     */
    public void nameAnalysis(SymTable table) {
    	// Get type and name/id
    	String type = this.myType.getType();
    	String id = this.myId.getMyStrVal();
    	
    	// Create new regular symbol
    	Sym symbol = new Sym(type, id);

    	// Attempt to add param decleration to table
    	try {
    		table.addDecl(id, symbol);
    	} catch (DuplicateSymException e) {
    		int lineNum = this.myId.getMyLineNum();
    		int charNum = this.myId.getMyCharNum();
    		
    		ErrMsg.fatal(lineNum, charNum, "Identifier multiply-declared");
    		
    	} catch (EmptySymTableException e) {
    		System.out.println("FormalDeclNode nameAnalysis(): " + e);
    		
    	} catch (Exception e) {
    		System.out.println("FormalDeclNode nameAnalysis(): Unknown error occurred: " + e);
    		
    	}
    }

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
    }
    
    public String getType() {
    	return this.myType.getType();
    }

    // two kids
    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }
    
    /**
     * Name analysis for struct definition
     *  
     * @param SymTable table
     * @return SymTable table
     */
    public void nameAnalysis(SymTable table) {
    	
    	// Create new sym table for variables defined inside struct
    	SymTable structFieldsSymTable = new SymTable();
    	    	
    	// Get id/name of struct
    	String id = this.myId.getMyStrVal();
    			
    	// Create new struct definition symbol and pass it sym table for fields
    	StructDefSym symbol = new StructDefSym(id, structFieldsSymTable);   
    	
    	// Attempt to add struct decl to symbol table
    	try {
    		table.addDecl(id, symbol);
    	} catch (DuplicateSymException e) {
    		int lineNum = this.myId.getMyLineNum();
    		int charNum = this.myId.getMyCharNum();
    		
    		ErrMsg.fatal(lineNum, charNum, "Identifier multiply-declared");
    		
    		// Return early if we fail so we do not process the structs fields
    		return;
    		
    	} catch (EmptySymTableException e) {
    		System.out.println("StructDeclNode nameAnalysis(): " + e);
    		
    	} catch (Exception e) {
    		System.out.println("StructDeclNode nameAnalysis(): Unknown error occurred: " + e);
    		
    	}
    	    	
    	// Process struct fields
    	this.myDeclList.nameAnalysis(table, structFieldsSymTable);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("struct ");
		myId.unparse(p, 0);
		p.println("{");
        myDeclList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("};\n");

    }

    // two kids
    private IdNode myId;
	private DeclListNode myDeclList;
}

// **********************************************************************
// ******  TypeNode and its Subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
	abstract public String getType();
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }
    
    public String getType() {
    	return "int";
    }
}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }
    
    public String getType() {
    	return "bool";
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
    
    public String getType() {
    	return "void";
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
		myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
		myId.unparse(p, 0);
    }
    
    public String getType() {
    	return "struct";
    }
    
    public IdNode getMyId(){
        return this.myId;
    }
    
    public String toString() {
    	return this.myId.getMyStrVal();
    }
	
	// one kid
    private IdNode myId;
}

// **********************************************************************
// ******  StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
	abstract public void nameAnalysis(SymTable table);
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignExpNode assign) {
        myAssign = assign;
    }
    
    public void nameAnalysis(SymTable table) {
    	this.myAssign.nameAnalysis(table);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    // one kid
    private AssignExpNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    public void nameAnalysis(SymTable table) {
    	this.myExp.nameAnalysis(table);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++;");
    }

    // one kid
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    public void nameAnalysis(SymTable table) {
    	this.myExp.nameAnalysis(table);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--;");
    }

    // one kid
    private ExpNode myExp;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }
    
    public void nameAnalysis(SymTable table) {
    	this.myExp.nameAnalysis(table);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("input >> ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // one kid (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    public void nameAnalysis(SymTable table) {
    	this.myExp.nameAnalysis(table);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("disp << ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // one kid
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }
    
    public void nameAnalysis(SymTable table) {
    	this.myExp.nameAnalysis(table);
    	
    	table.addScope();
    	
    	this.myDeclList.nameAnalysis(table);
    	this.myStmtList.nameAnalysis(table);
    	
    	try {
    		table.removeScope();
    	} catch (EmptySymTableException e) {
    		System.out.println("IfStmtNode nameAnalysis(): " + e);
    	} catch (Exception e) {
    		System.out.println("IfStmtNode nameAnalysis(): Unknown error occurred: " + e);
    	}
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }

    // three kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
                          StmtListNode slist1, DeclListNode dlist2,
                          StmtListNode slist2) {
        myExp = exp;
        
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }
    
    public void nameAnalysis(SymTable table) {
    	this.myExp.nameAnalysis(table);
    	
    	table.addScope();
    	
    	this.myThenDeclList.nameAnalysis(table);
    	this.myThenStmtList.nameAnalysis(table);
    	
    	try {
    		table.removeScope();
    	} catch (EmptySymTableException e) {
    		System.out.println("IfElseStmtNode nameAnalysis() if: " + e);
    	} catch (Exception e) {
    		System.out.println("IfElseStmtNode nameAnalysis() if: Unknown error occurred: " + e);
    	}
    	
    	table.addScope();
    	
    	this.myElseDeclList.nameAnalysis(table);
    	this.myElseStmtList.nameAnalysis(table);
    	
    	try {
    		table.removeScope();
    	} catch (EmptySymTableException e) {
    		System.out.println("IfElseStmtNode nameAnalysis() else: " + e);
    	} catch (Exception e) {
    		System.out.println("IfElseStmtNode nameAnalysis() else: Unknown error occurred: " + e);
    	}
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent+4);
        myThenStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
        doIndent(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent+4);
        myElseStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");        
    }

    // 5 kids
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }
    
    public void nameAnalysis(SymTable table) {
    	this.myExp.nameAnalysis(table);
    	
    	table.addScope();
    	
    	this.myDeclList.nameAnalysis(table);
    	this.myStmtList.nameAnalysis(table);
    	
    	try {
    		table.removeScope();
    	} catch (EmptySymTableException e) {
    		System.out.println("WhileStmtNode nameAnalysis(): " + e);
    	} catch (Exception e) {
    		System.out.println("WhileStmtNode nameAnalysis(): Unknown error occurred: " + e);
    	}
    }
	
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }

    // three kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }
    
    public void nameAnalysis(SymTable table) {
    	myCall.nameAnalysis(table);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }

    // one kid
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    public void nameAnalysis(SymTable table) {
    	if (this.myExp != null) {
    		myExp.nameAnalysis(table);
    	}
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(";");
    }

    // one kid
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// ******  ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
	abstract public void nameAnalysis(SymTable table);
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }
    
    public void nameAnalysis(SymTable table) {
    	return;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }
    
    public void nameAnalysis(SymTable table) {
    	return;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }
    
    public void nameAnalysis(SymTable table) {
    	return;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }

    private int myLineNum;
    private int myCharNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }
    
    public void nameAnalysis(SymTable table) {
    	return;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }
    
    public void nameAnalysis(SymTable table) {
    	try {
    		Sym symbol = table.lookupGlobal(this.myStrVal);
    		    		
    		// If sym is undeclared
        	if (symbol == null) {
        		ErrMsg.fatal(this.myLineNum, this.myCharNum, "Identifier undeclared");    		
        	}
        	
        	this.mySym = symbol;
        	
    	} catch (EmptySymTableException e) {
    		System.out.println("IdNode: nameAnalysis(): " + e);
    	}  
    }
    
    /**
     * Getter for myLineNum
     * 
     * @return int
     */
    public int getMyLineNum() {
    	return this.myLineNum;
    }
    
    /**
     * Getter for myCharNum
     * 
     * @return int
     */
    public int getMyCharNum() {
    	return this.myCharNum;
    }
    
    /**
     * Getter for myStrVal
     * 
     * @return int
     */
    public String getMyStrVal() {
    	return this.myStrVal;
    }    
    
    public Sym getMySym() {
    	return this.mySym;
    }
    
    public void setMySym(Sym sym) {
    	this.mySym = sym;
    }

    /**
     * Edited unparse method to include ID in parenthesis after name
     */
    public void unparse(PrintWriter p, int indent) {      	
    	if (this.mySym == null) {
    		p.print(this.myStrVal);
    	} else {
    		 p.print(this.myStrVal + "(" + this.mySym.toString() + ")");
    	}
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private Sym mySym;
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;	
        myId = id;
    }
    
    public void nameAnalysis(SymTable table) {
    	String structType = null;
    	
    	if (this.myLoc instanceof DotAccessExpNode) {    		
    		this.myLoc.nameAnalysis(table);
    		this.myId.nameAnalysis(table);
    		return;
    	}
    	    	
    	IdNode lhs = (IdNode) this.myLoc;
    	IdNode rhs = this.myId;
    	
    	// Check lhs is trying to access struct type and is declared
    	try {
    		Sym lhsSymbol = table.lookupLocal(lhs.getMyStrVal());
        	
    		if (lhsSymbol == null) {
    			int lineNum = lhs.getMyLineNum();
        		int charNum = rhs.getMyCharNum();
        		
    			ErrMsg.fatal(lineNum, charNum, "Identifier undeclared");    	
    		}
    		
    		if (lhsSymbol instanceof StructInstanceSym) {
    			StructInstanceSym instance = (StructInstanceSym) lhsSymbol;
    			structType = instance.getStructType();
    			lhs.setMySym(lhsSymbol);
    			
    		} else {
    			int lineNum = lhs.getMyLineNum();
        		int charNum = rhs.getMyCharNum();
        		
    			ErrMsg.fatal(lineNum, charNum, "Dot-access of non-struct type");    
    			
    			return;
    		}
    		
    	} catch (EmptySymTableException e) {
    		System.out.println("DotAccessExpNode: nameAnalysis() LHS: " + e);
    	}
    	
    	// Check rhs is valid field of structt
    	try {
    		// Get struct def and struct fields
    		Sym symbol = table.lookupGlobal(structType);
    		
    		// Make sure symbol is actually struct definition
    		if (symbol instanceof StructDefSym) {
    			StructDefSym structDef = (StructDefSym) table.lookupGlobal(structType);        		
        		SymTable fieldsTable = structDef.getFieldsSymTable();
        		
        		// Get id on rhs that is being accessed
        		String rhsId = rhs.getMyStrVal();
        		
        		// See if rhs id exists on struct type
        		Sym accessSymbol = fieldsTable.lookupLocal(rhsId);
        		        		
        		if (accessSymbol == null) {
        			int lineNum = lhs.getMyLineNum();
            		int charNum = rhs.getMyCharNum();
            		
        			ErrMsg.fatal(lineNum, charNum, "Struct field name invalid");    
        			
        			return;
        		}
        		
        		
        		rhs.setMySym(accessSymbol);
    		} else {
    			return;
    		}
    		
    		
    	} catch (EmptySymTableException e) {
    		System.out.println("DotAccessExpNode: nameAnalysis() RHS: " + e);
    	}

 
    	
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myLoc.unparse(p, 0);
		p.print(").");
		myId.unparse(p, 0);
    }
    
    

    // two kids
    private ExpNode myLoc;	
    private IdNode myId;
}

class AssignExpNode extends ExpNode {
    public AssignExpNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }
    
    public void nameAnalysis(SymTable table) {
    	this.myLhs.nameAnalysis(table);
    	this.myExp.nameAnalysis(table);
    }

    // ** unparse **
    public void unparse(PrintWriter p, int indent) {
		if (indent != -1)  p.print("(");
	    myLhs.unparse(p, 0);
		p.print(" = ");
		myExp.unparse(p, 0);
		if (indent != -1)  p.print(")");
    }

    // two kids
    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }
    
    public void nameAnalysis(SymTable table) {
    	this.myId.nameAnalysis(table);
    	this.myExpList.nameAnalysis(table);
    }

    public void unparse(PrintWriter p, int indent) {
	    myId.unparse(p, 0);
		p.print("(");
		if (myExpList != null) {
			myExpList.unparse(p, 0);
		}
		p.print(")");
    }

    // two kids
    private IdNode myId;
    private ExpListNode myExpList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }
    
    public void nameAnalysis(SymTable table) {
    	this.myExp.nameAnalysis(table);
    }

    // one kid
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }
    
    public void nameAnalysis(SymTable table) {
    	this.myExp1.nameAnalysis(table);
    	this.myExp2.nameAnalysis(table);
    }

    // two kids
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}




//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//DO NOT NEED NAME ANALYSIS BELOW
//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


// **********************************************************************
// ******  Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(-");
		myExp.unparse(p, 0);
		p.print(")");
    }
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(!");
		myExp.unparse(p, 0);
		p.print(")");
    }
}


// **********************************************************************
// ******  Subclasses of BinaryExpNode
// **********************************************************************

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" + ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" - ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" * ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" / ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" && ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" || ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" == ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class NotEqualsNode extends BinaryExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" != ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" < ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" > ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" <= ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" >= ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}
