package syms;

import java.util.*;

import java_cup.runtime.ComplexSymbolFactory.Location;
import tree.ConstExp;
import tree.Operator;
import tree.StatementNode;

/**
 * A Scope represents a static scope for a procedure, main program or
 * the predefined scope.
 * It provides operations to add and look up identifiers.
 * Searching for an identifier in a scope starts at the current scope,
 * but then if it is not found, the search proceeds to the next outer
 * (parent) scope, and so on.
 */
public class Scope {
    /**
     * Offset of start of local variables from frame pointer
     */
    final static int LOCALS_BASE = 3;
    /**
     * Parent Scope
     */
    private final Scope parent;
    /**
     * Static level of this scope
     */
    private final int level;
    /**
     * Symbol table entry for the procedure (main program) that owns this scope
     */
    private final SymEntry.ProcedureEntry ownerEntry;
    /**
     * Symbol table entries. A SortedMap is used to avoid issues with
     * hashing functions working differently on different implementations.
     * This only affects minor things like the order of allocating variables
     * and dumping symbol tables in trace backs. A Map/HashMap would still give a
     * valid implementation.
     */
    private final SortedMap<String, SymEntry> entries;
    /**
     * space allocated for local variables within this scope
     */
    private int variableSpace;

    /**
     * This constructs a single scope within a symbol table
     * that is linked to the parent scope, which may be null to
     * indicate that there is no parent.
     *
     * @param parent     scope
     * @param level      of nesting of scope
     * @param ownerEntry the corresponding owner's symbol table entry
     */
    public Scope(Scope parent, int level, SymEntry.ProcedureEntry ownerEntry) {
        this.parent = parent;
        this.level = level;
        this.ownerEntry = ownerEntry;
        /* Initially empty */
        this.entries = new TreeMap<>();
        variableSpace = 0;
    }

    /**
     * Enter a new scope with this as the parent
     */
    public Scope newScope(SymEntry.ProcedureEntry procEntry) {
        Scope newScope = new Scope(this, level + 1, procEntry);
        procEntry.setLocalScope(newScope);
        return newScope;
    }

    public Scope getParent() {
        return parent;
    }

    public int getLevel() {
        return level;
    }

    public SymEntry.ProcedureEntry getOwnerEntry() {
        return ownerEntry;
    }

    /**
     * @return the set of entries in this scope
     */
    public Collection<SymEntry> getEntries() {
        return entries.values();
    }

    /**
     * Lookup id starting in the current scope and
     * thence in the parent scope and so on.
     *
     * @param id identifier to search for.
     * @return symbol table entry for the id, or null if not found.
     */
    public SymEntry lookup(String id) {
        /* Lookup the entry in the current scope */
        SymEntry entry = entries.get(id);
        if (entry == null && parent != null) {
            /* If the entry is not in the current scope
             * look it up in the parent scope, if there is one.
             */
            return parent.lookup(id);
        }
        return entry;
    }

    /**
     * Add an entry to the scope unless an entry for the same name exists.
     *
     * @param entry to be added
     * @return the entry added or null is it already exited in this scope.
     */
    public SymEntry addEntry(SymEntry entry) {
        if (entries.containsKey(entry.getIdent())) {
            return null;
        } else {
            entry.setScope(this);
            entries.put(entry.getIdent(), entry);
            return entry;
        }
    }

    /**
     * Resolve references to type identifiers and allocate space
     * for variables and check for circularly defined types and constants.
     */
    public void resolveScope() {
        // List of types declared in scope
        List<Type> types = new LinkedList<>();
        for (SymEntry entry : entries.values()) {
            //System.out.println("Symbol table resolving " + entry.ident);
            entry.resolve();
            if (entry instanceof SymEntry.TypeEntry typeEntry) {
                types.add(typeEntry.getType());
            }
            //System.out.println("Resolved entry " + entry);
        }
        //System.out.println(currentScope);
    }

    /**
     * @return the amount of space allocated to local variables
     * within the current scope.
     */
    public int getVariableSpace() {
        return variableSpace;
    }

    /**
     * Allocate space for a local variable.
     *
     * @param size is the amount of space required for the variable.
     * @return address (offset) of allocated space
     */
    public int allocVariableSpace(int size) {
        int offset = variableSpace;
        variableSpace += size;
        return LOCALS_BASE + offset;
    }

    /**
     * Add a CONSTANT entry to the current scope - known value
     *
     * @return a reference to the new entry unless an entry with the same name
     * already exists in the current scope, in which case return null.
     */
    public SymEntry.ConstantEntry addConstant(String name, Location loc, Type type, int val) {
        SymEntry.ConstantEntry entry = new SymEntry.ConstantEntry(name, loc, type, val);
        return (SymEntry.ConstantEntry) addEntry(entry);
    }

    /**
     * Add a CONSTANT entry to the current scope - tree to be evaluated later
     *
     * @return a reference to the new entry unless an entry with the same name
     * already exists in the current scope, in which case return null.
     */
    public SymEntry.ConstantEntry addConstant(String name, Location loc,
                                              ConstExp val) {
        SymEntry.ConstantEntry entry =
                new SymEntry.ConstantEntry(name, loc, Type.ERROR_TYPE, val);
        return (SymEntry.ConstantEntry) addEntry(entry);
    }

    /**
     * Lookup a CONSTANT entry in all scopes starting from the current scope.
     *
     * @param name of the CONSTANT to be looked up
     * @return entry for name if one is found, and it is a CONSTANT entry
     * otherwise return null; note that a non-CONSTANT entry may mask
     * a CONSTANT entry of the same name in an outer scope.
     */
    public SymEntry.ConstantEntry lookupConstant(String name) {
        SymEntry entry = lookup(name);
        if (entry instanceof SymEntry.ConstantEntry constEntry) {
            return constEntry;
        }
        return null;
    }

    /**
     * Add a TYPE entry to the current scope
     *
     * @return a reference to the new entry unless an entry with the same name
     * already exists in the current scope, in which case return null.
     */
    public SymEntry.TypeEntry addType(String name, Location loc, Type type) {
        SymEntry.TypeEntry entry = new SymEntry.TypeEntry(name, loc, type);
        type.setName(name);
        return (SymEntry.TypeEntry) addEntry(entry);
    }

    /**
     * Lookup a TYPE entry in all scopes starting from the current scope.
     *
     * @param name of the TYPE to be looked up
     * @return entry for name if one is found, and it is a TYPE entry
     * otherwise return null; note that a non-TYPE entry may mask
     * a TYPE entry of the same name in an outer scope.
     */
    public SymEntry.TypeEntry lookupType(String name) {
        SymEntry entry = lookup(name);
        if (entry instanceof SymEntry.TypeEntry typeEntry) {
            return typeEntry;
        }
        return null;
    }

    /**
     * Add a VARIABLE entry to the current scope.
     * The variable entry will have the supplied name, location (loc), and
     * a type which is a reference to the type supplied as a parameter
     * (i.e. a ReferenceType with a base type given by parameter "type").
     *
     * @return a reference to the new entry unless an entry with the same name
     * already exists in the current scope, in which case return null.
     */
    public SymEntry.VarEntry addVariable(String name, Location loc, Type type) {
        SymEntry.VarEntry entry = new SymEntry.VarEntry(name, loc, type);
        return (SymEntry.VarEntry) addEntry(entry);
    }

    /**
     * Lookup a VARIABLE entry in all scopes starting from the current scope.
     *
     * @param name of the VARIABLE to be looked up
     * @return entry for name if one is found, and it is a VARIABLE entry
     * otherwise return null; note that a non-VARIABLE entry may mask
     * a VARIABLE entry of the same name in an outer scope.
     */
    public SymEntry.VarEntry lookupVariable(String name) {
        SymEntry entry = lookup(name);
        if (entry instanceof SymEntry.VarEntry varEntry) {
            return varEntry;
        }
        return null;
    }

    /**
     * Add a PROCEDURE entry to the current scope
     *
     * @return a reference to the new entry unless an entry with the same name
     * already exists in the current scope, in which case return null.
     */
    public SymEntry.ProcedureEntry addProcedure(String name, Location loc) {
        SymEntry.ProcedureEntry entry =
                new SymEntry.ProcedureEntry(name, loc);
        return (SymEntry.ProcedureEntry) addEntry(entry);
    }

    /**
     * Lookup a PROCEDURE entry in all scopes starting from the current scope.
     *
     * @param name of the PROCEDURE to be looked up
     * @return entry for name if one is found, and it is a PROCEDURE entry
     * otherwise return null; note that a non-PROCEDURE entry may mask
     * a PROCEDURE entry of the same name in an outer scope.
     */
    public SymEntry.ProcedureEntry lookupProcedure(String name) {
        SymEntry entry = lookup(name);
        if (entry instanceof SymEntry.ProcedureEntry procEntry) {
            return procEntry;
        }
        return null;
    }

    /**
     * Add an OPERATOR entry to the current scope
     *
     * @return a reference to the new entry for the operator.
     * If the operator already exists in the current scope,
     * its entry is extended with the new type.
     * If it is defined at an outer scope, the outer entry is
     * duplicated at the current level and extended with the new type.
     */
    public SymEntry.OperatorEntry addOperator(Operator op, Location loc,
                              Type.FunctionType type) {
        Type.OperatorType opType = new Type.OperatorType(type, op);
        SymEntry.OperatorEntry entry = lookupOperator(op.getName());
        if (entry == null) {
            /* Create a new entry for the operator */
            entry = new SymEntry.OperatorEntry(op.getName(), loc, opType);
            return (SymEntry.OperatorEntry) addEntry(entry);
        } else if (entry.getLevel() == getLevel()) {
            /* Already defined at this level - extend intersection type */
            entry.extendType(opType);
            return entry;
        } else {
            /* Defined at an outer level create new entry with old
             * intersection type and extend with new type.
             */
            entry = new SymEntry.OperatorEntry(op.getName(), loc, entry.getType());
            entry.extendType(opType);
            return (SymEntry.OperatorEntry) addEntry(entry);
        }
    }

    /**
     * Lookup an OPERATOR entry in all scopes starting from the current scope.
     *
     * @param name of the OPERATOR to be looked up
     * @return entry for name if one is found, and it is an OPERATOR entry
     * otherwise return null; note that a non-OPERATOR entry may mask
     * a OPERATOR entry of the same name in an outer scope.
     */
    public SymEntry.OperatorEntry lookupOperator(String name) {
        SymEntry entry = lookup(name);
        if (entry instanceof SymEntry.OperatorEntry opEntry) {
            return opEntry;
        }
        return null;
    }

    /* Dump contents of this scope */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Level " + level + " " + ownerEntry.getIdent());
        for (SymEntry entry : entries.values()) {
            s.append(StatementNode.newLine(level)).append(entry);
        }
        return s.toString();
    }
}
