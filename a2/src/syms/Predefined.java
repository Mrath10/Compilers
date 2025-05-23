package syms;

import source.ErrorHandler;
import syms.Type.FunctionType;
import syms.Type.RecordType;
import java.util.*;
import machine.StackMachine;
import syms.Type.ProductType;
import syms.Type.ScalarType;
import tree.Operator;

/**
 * class Predefined - handles the predefined types and symbols.
 */

public class Predefined {
    /**
     * Predefined integer type.
     */
    public static ScalarType INTEGER_TYPE = new ScalarType("int", Type.SIZE_OF_INT,
            Integer.MIN_VALUE, Integer.MAX_VALUE);
    /**
     * Predefined boolean type.
     */
    public static ScalarType BOOLEAN_TYPE = new ScalarType("boolean", Type.SIZE_OF_BOOLEAN,
        Type.FALSE_VALUE, Type.TRUE_VALUE);
    /**
     * Predefined type of nil record
     */
    public static RecordType NIL_TYPE = new RecordType(ErrorHandler.NO_LOCATION, new ArrayList<>()) {
        {
            name = "nil_type";
            resolved = true;
        }
    };

    /**
     * Add the predefined constants, types and operators
     *
     * @param predefined scope to which entries are added
     */
    static void addPredefinedEntries(Scope predefined) {
        // Define types needed for predefined entries
        ProductType NIL_TYPE_PAIR = new ProductType(NIL_TYPE, NIL_TYPE);
        FunctionType NIL_RELATIONAL_TYPE = new FunctionType(NIL_TYPE_PAIR, BOOLEAN_TYPE);
        ProductType PAIR_INTEGER_TYPE = new ProductType(INTEGER_TYPE, INTEGER_TYPE);
        ProductType PAIR_BOOLEAN_TYPE = new ProductType(BOOLEAN_TYPE, BOOLEAN_TYPE);
        FunctionType ARITHMETIC_BINARY = new FunctionType(PAIR_INTEGER_TYPE, INTEGER_TYPE);
        FunctionType INT_RELATIONAL_TYPE = new FunctionType(PAIR_INTEGER_TYPE, BOOLEAN_TYPE);
        FunctionType LOGICAL_BINARY = new FunctionType(PAIR_BOOLEAN_TYPE, BOOLEAN_TYPE);
        FunctionType ARITHMETIC_UNARY = new FunctionType(INTEGER_TYPE, INTEGER_TYPE);
        // Add predefined symbols to predefined scope
        predefined.addType("int", ErrorHandler.NO_LOCATION, INTEGER_TYPE);
        predefined.addType("boolean", ErrorHandler.NO_LOCATION, BOOLEAN_TYPE);
        predefined.addConstant("false", ErrorHandler.NO_LOCATION, BOOLEAN_TYPE,
                Type.FALSE_VALUE);
        predefined.addConstant("true", ErrorHandler.NO_LOCATION, BOOLEAN_TYPE,
                Type.TRUE_VALUE);
        predefined.addOperator(Operator.EQUALS_OP, ErrorHandler.NO_LOCATION, LOGICAL_BINARY);
        predefined.addOperator(Operator.NEQUALS_OP, ErrorHandler.NO_LOCATION, LOGICAL_BINARY);
        predefined.addConstant("nil", ErrorHandler.NO_LOCATION, NIL_TYPE,
                StackMachine.NULL_ADDR);
        predefined.addOperator(Operator.EQUALS_OP, ErrorHandler.NO_LOCATION,
                NIL_RELATIONAL_TYPE);
        predefined.addOperator(Operator.NEQUALS_OP, ErrorHandler.NO_LOCATION,
                NIL_RELATIONAL_TYPE);
        predefined.addOperator(Operator.NEG_OP, ErrorHandler.NO_LOCATION, ARITHMETIC_UNARY);
        predefined.addOperator(Operator.ADD_OP, ErrorHandler.NO_LOCATION, ARITHMETIC_BINARY);
        predefined.addOperator(Operator.SUB_OP, ErrorHandler.NO_LOCATION, ARITHMETIC_BINARY);
        predefined.addOperator(Operator.MUL_OP, ErrorHandler.NO_LOCATION, ARITHMETIC_BINARY);
        predefined.addOperator(Operator.DIV_OP, ErrorHandler.NO_LOCATION, ARITHMETIC_BINARY);
        predefined.addOperator(Operator.EQUALS_OP, ErrorHandler.NO_LOCATION,
                INT_RELATIONAL_TYPE);
        predefined.addOperator(Operator.NEQUALS_OP, ErrorHandler.NO_LOCATION,
                INT_RELATIONAL_TYPE);
        predefined.addOperator(Operator.GREATER_OP, ErrorHandler.NO_LOCATION,
                INT_RELATIONAL_TYPE);
        predefined.addOperator(Operator.LESS_OP, ErrorHandler.NO_LOCATION,
                INT_RELATIONAL_TYPE);
        predefined.addOperator(Operator.GEQUALS_OP, ErrorHandler.NO_LOCATION,
                INT_RELATIONAL_TYPE);
        predefined.addOperator(Operator.LEQUALS_OP, ErrorHandler.NO_LOCATION,
                INT_RELATIONAL_TYPE);
    }
}
