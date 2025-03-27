using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Helper;

namespace LittleHorse.Sdk.Workflow.Spec;

public class WfRunVariable
{
    public string Name;
    private WorkflowThread _parent;
    private readonly object _typeOrDefaultVal;
    private WfRunVariableAccessLevel _accessLevel;
    public VariableType Type;
    private VariableValue? _defaultValue;
    private bool _required;
    private bool _searchable;
    private bool _masked;
    private readonly List<JsonIndex> _jsonIndexes;
    public string? JsonPath { get; private set; }
    
    public WfRunVariable(string name, object typeOrDefaultVal, WorkflowThread parent)
    {
        Name = name;
        _parent = parent;
        _jsonIndexes = new List<JsonIndex>();

        _typeOrDefaultVal = typeOrDefaultVal ?? throw new InvalidOperationException(
            "The 'typeOrDefaultVal' argument must be either a VariableType " +
            "or a default value, but a null value was provided.");

        // As per GH Issue #582, the default is now PRIVATE_VAR.
        _accessLevel = WfRunVariableAccessLevel.PrivateVar;
        InitializeType();
    }
    
    private void InitializeType() 
    {
        if (_typeOrDefaultVal is VariableType) 
        {
            Type = (VariableType) _typeOrDefaultVal;
        } 
        else 
        {
            SetDefaultValue(_typeOrDefaultVal);
            Type = LHMappingHelper.ValueCaseToVariableType(_defaultValue!.ValueCase);
        }
    }
    
    private void SetDefaultValue(object defaultVal) 
    {
        try 
        {
            _defaultValue = LHMappingHelper.ObjectToVariableValue(defaultVal);
        } 
        catch (LHSerdeException e) 
        {
            throw new ArgumentException("Was unable to convert provided default value to LH Variable Type", e);
        }
    }
    
    public ThreadVarDef Compile() 
    {
        VariableDef varDef = new VariableDef
        {
            Type = Type,
            Name = Name,
            MaskedValue = _masked
        };

        if (_defaultValue != null) 
        {
            varDef.DefaultValue = _defaultValue;
        }

        var threadVarDef = new ThreadVarDef
        {
            VarDef = varDef,
            Required = _required,
            Searchable = _searchable,
            AccessLevel = _accessLevel
        };
        threadVarDef.JsonIndexes.Add(_jsonIndexes);
        
        return threadVarDef;
    }
    
    public WfRunVariable Searchable() 
    {
        _searchable = true;
        return this;
    }
    
    public WfRunVariable Masked()
    {
        _masked = true;
        return this;
    }
    
    public WfRunVariable Required() 
    {
        _required = true;
        return this;
    }
    
    public WfRunVariable WithDefault(object defaultVal) 
    {
        SetDefaultValue(defaultVal);

        if (!LHMappingHelper.ValueCaseToVariableType(_defaultValue!.ValueCase).Equals(Type)) 
        {
            throw new ArgumentException($"Default value type does not match LH variable type {Type}");
        }

        return this;
    }
    
    public WfRunVariable WithJsonPath(string path) 
    {
        if (JsonPath != null)
        {
            throw new LHMisconfigurationException("Cannot use jsonpath() twice on same var!");
        }
        if (Type != VariableType.JsonObj && Type != VariableType.JsonArr) 
        {
            throw new LHMisconfigurationException($"JsonPath not allowed in a {Type.ToString()} variable");
        }
        var outVariable = new WfRunVariable(Name, _typeOrDefaultVal, _parent)
        {
            JsonPath = path
        };
        
        return outVariable;
    }
    
    /// <summary>
    /// Mutates the value of this WfRunVariable and sets it to the value provided on the RHS.
    ///
    /// If the LHS of this WfRunVariable is set, then the sub-element of this WfRunVariable
    /// provided by the Json Path is mutated.
    /// </summary>
    /// <param name="rhs">
    /// It is the value to set this WfRunVariable to.
    /// </param>
    public void Assign(object rhs)
    {
        _parent.Mutate(this, VariableMutationType.Assign, rhs);
    }
    
    /// <summary>
    /// Returns an expression whose value is the `other` added to this expression.
    /// </summary>
    /// <param name="other">
    /// It is the value to be added to this expression.
    /// </param>
    /// <returns> An expression whose value is the `other` added to this expression.
    /// </returns>
    public LHExpression Add(object other)
    {
        return new LHExpression(this, VariableMutationType.Add, other);
    }
    
    /// <summary>
    /// Returns an expression whose value is the `other` subtracted from this expression.
    /// </summary>
    /// <param name="other">
    /// It is the value to be subtracted from this expression.
    /// </param>
    /// <returns> An expression whose value is the `other` subtracted from this expression.
    /// </returns>
    public LHExpression Subtract(object other) 
    {
        return new LHExpression(this, VariableMutationType.Subtract, other);
    }
    
    /// <summary>
    /// Returns an expression whose value is the `other` multiplied by this expression.
    /// </summary>
    /// <param name="other">
    /// It is the value to be multiplied by this expression.
    /// </param>
    /// <returns> An expression whose value is the `other` multiplied by this expression.
    /// </returns>
    public LHExpression Multiply(object other) 
    {
        return new LHExpression(this, VariableMutationType.Multiply, other);
    }
    
    /// <summary>
    /// Returns an expression whose value is the `other` divided by the `other`.
    /// </summary>
    /// <param name="other">
    /// It is the value to divide this expression by.
    /// </param>
    /// <returns> An expression whose value is this expression divided by the `other`.
    /// </returns>
    public LHExpression Divide(object other) 
    {
        return new LHExpression(this, VariableMutationType.Divide, other);
    }
    
    /// <summary>
    /// Returns an expression whose value is this expression extended by the `other`.
    /// </summary>
    /// <param name="other">
    /// It is the value to extend this expression by.
    /// </param>
    /// <returns> An expression whose value is this expression extended by the `other`. </returns>
    public LHExpression Extend(object other) 
    {
        return new LHExpression(this, VariableMutationType.Extend, other);
    }
    
    /// <summary>
    /// Returns an expression whose value is this expression with all occurrences of
    /// `other` removed.
    /// </summary>
    /// <param name="other">
    /// It is the value to remove from this expression.
    /// </param>
    /// <returns> An expression whose value is this expression with all occurrences of
    /// `other` removed.
    /// </returns>
    public LHExpression RemoveIfPresent(object other) 
    {
        return new LHExpression(this, VariableMutationType.RemoveIfPresent, other);
    }
    
    /// <summary>
    /// Returns an expression whose value is this expression with the index specified
    /// by `index` removed.
    ///
    /// Valid only for JSON_ARR expressions.
    /// </summary>
    /// <param name="index">
    /// It is the index at which to insert the other `index`.
    /// </param>
    /// <returns> An expression whose value is this expression with the `other` inserted
    /// at the specified `index`.
    /// </returns>
    public LHExpression RemoveIndex(int index) 
    {
        return new LHExpression(this, VariableMutationType.RemoveIndex, index);
    }
    
    /// <summary>
    /// Returns an expression whose value is this expression with the index specified
    /// by `index` removed.
    ///
    /// Valid only for JSON_ARR expressions.
    /// </summary>
    /// <param name="index">
    /// It is the index at which to remove the value.
    /// </param>
    /// <returns> An expression whose value is this expression with the value at the
    /// specified `index` removed.
    /// </returns>
    public LHExpression RemoveIndex(LHExpression index) 
    {
        return new LHExpression(this, VariableMutationType.RemoveIndex, index);
    }
    
    /// <summary>
    /// Returns an expression whose value is this expression with the key specified
    /// by `key` removed.
    ///
    /// Valid only for JSON_OBJ expressions.
    /// </summary>
    /// <param name="key">
    /// It is the key to remove from this expression.
    /// </param>
    /// <returns> An expression whose value is this expression with the key specified
    /// by `key` removed.
    /// </returns>
    public LHExpression RemoveKey(object key) 
    {
        return new LHExpression(this, VariableMutationType.RemoveKey, key);
    }
}