using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Helper;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// A WfRunVariable is a handle on a Variable in a WfSpec.
/// </summary>
public class WfRunVariable
{
    /// <value>
    /// The name of this WfRunVariable.
    /// </value>
    public string Name { get; private set; }

    /// <value>
    /// Gets the Type of this WfRunVariable.
    /// </value>
    public VariableType Type { get; private set; }
    
    /// <value>
    /// Gets the JsonPath of this WfRunVariable that is JSON_OBJ or JSON_ARR types.
    /// </value>
    public string? JsonPath { get; private set; }
    
    private WorkflowThread _parent;
    private readonly object _typeOrDefaultVal;
    private WfRunVariableAccessLevel _accessLevel;
    private VariableValue? _defaultValue;
    private bool _required;
    private bool _searchable;
    private bool _masked;

    internal bool IsMasked
    {
        get => _masked;
    }

    private readonly List<JsonIndex> _jsonIndexes;
    
    /// <summary>
    /// Initializes a new instance of the <see cref="WfRunVariable"/> class.
    /// </summary>
    /// <param name="name">The name of the wfRunVariable.</param>
    /// <param name="typeOrDefaultVal">The  variable type or a default value (literal value).</param>
    /// <param name="parent">The workflow thread where the user task output belongs to.</param>
    /// <exception cref="InvalidOperationException">Throws an exception when typeOrDefault param is null.</exception>
    public WfRunVariable(string name, object typeOrDefaultVal, WorkflowThread parent)
    {
        Name = name;
        _parent = parent ?? throw new ArgumentNullException(nameof(parent));
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
    
    /// <summary>
    /// Compile this into Protobuf objects.
    /// </summary>
    /// <returns>
    /// The ThreadVarDef
    /// </returns>
    public ThreadVarDef Compile() 
    {
        VariableDef varDef = new VariableDef
        {
            TypeDef = new TypeDefinition
            {
                PrimitiveType = Type,
                Masked = _masked
            },
            Name = Name
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
    
    /// <summary>
    /// Marks the Variable as "Searchable", which creates an Index on the Variable
    /// in the LH Data Store.
    /// </summary>
    /// <returns>
    /// Same WfRunVariable instance
    /// </returns>
    public WfRunVariable Searchable() 
    {
        _searchable = true;
        return this;
    }
    
    /// <summary>
    /// Marks the JSON_OBJ or JSON_ARR Variable as "Searchable", and creates an
    /// index on the specified field.
    /// </summary>
    /// <param name="fieldPath">
    /// It is the JSON Path to the field that we are indexing.
    /// </param>
    /// <param name="fieldType">
    /// It is the type of the field we are indexing.
    /// </param>
    /// <returns>
    /// Same WfRunVariable instance
    /// </returns>
    public WfRunVariable SearchableOn(String fieldPath, VariableType fieldType)
    {
        if (!fieldPath.StartsWith("$.")) {
            throw new LHMisconfigurationException($"Invalid JsonPath: {fieldPath}");
        }
        if (!Type.Equals(VariableType.JsonObj) && !Type.Equals(VariableType.JsonArr)) {
            throw new LHMisconfigurationException($"Non-Json {Name} variable contains jsonIndex.");
        }
        _jsonIndexes.Add(new JsonIndex
        {
            FieldPath = fieldPath,
            FieldType = fieldType
        });
        
        return this;
    }
    
    /// <summary>
    /// Sets the access level of a WfRunVariable.
    /// </summary>
    /// <param name="accessLevel">
    /// It is the access level to set.
    /// </param>
    /// <returns>
    /// This WfRunVariable.
    /// </returns>
    public WfRunVariable WithAccessLevel(WfRunVariableAccessLevel accessLevel)
    {
        _accessLevel = accessLevel;
        return this;
    }
    
    /// <summary>
    /// Marks the Variable as a `PUBLIC_VAR`, which does three things:
    /// 1. Considers this variable in determining whether a new version of this WfSpec
    ///    should be a major version or minor revision.
    /// 2. Freezes the type of this variable so that you cannot create future WfSpec
    ///    versions with a variable of the same name and different type.
    /// 3. Allows defining child WfSpec's that use this variable.
    /// 
    /// This is an advanced feature that you should use in any of the following cases:
    /// - You are treating a WfSpec as a data model and a WfRun as an instance of data.
    /// - You need child workflows to access this variable.
    /// </summary>
    /// <returns>
    /// This WfRunVariable.
    /// </returns>
    public WfRunVariable AsPublic()
    {
        return WithAccessLevel(WfRunVariableAccessLevel.PublicVar);
    }
    
    /// <summary>
    /// Marks the Variable as a `INHERITED_VAR`, which means that it comes from the
    /// parent `WfRun`. This means that:
    /// - There must be a parent WfSpec reference.
    /// - The parent must have a PUBLIC_VAR variable of the same name and type.
    /// </summary>
    /// <returns>
    /// This WfRunVariable.
    /// </returns>
    public WfRunVariable AsInherited()
    {
        return WithAccessLevel(WfRunVariableAccessLevel.InheritedVar);
    }
    
    /// <summary>
    /// Marks a WfRunVariable to show masked values
    /// </summary>
    /// <returns>
    /// Same WfRunVariable instance
    /// </returns>
    public WfRunVariable Masked()
    {
        _masked = true;
        return this;
    }

    /// <summary>
    /// Marks the variable as "Required", meaning that the ThreadSpec cannot be
    /// started without this variable being provided as input. For Entrypoint
    /// ThreadSpec's, this also triggers the WfSpec Required Variable Compatibility
    /// Rules.
    /// </summary>
    /// <returns>
    /// A WfRunVariable.
    /// </returns>
    public WfRunVariable Required()
    {
        _required = true;
        return this;
    }
    
    /// <summary>
    /// Sets the default value for this WfRunVariable.
    /// </summary>
    /// <param name="defaultVal">
    /// It is the default value for this variable.
    /// </param>
    /// <returns>
    /// This WfRunVariable.
    /// </returns>
    public WfRunVariable WithDefault(object defaultVal) 
    {
        SetDefaultValue(defaultVal);

        if (!LHMappingHelper.ValueCaseToVariableType(_defaultValue!.ValueCase).Equals(Type)) 
        {
            throw new ArgumentException($"Default value type does not match LH variable type {Type}");
        }

        return this;
    }
    
    /// <summary>
    /// Valid only for output of the JSON_OBJ or JSON_ARR types. Returns a new WfRunVariable handle
    /// which points to Json element referred to by the json path.
    ///
    /// Can only be called once. You can't call node.WithJsonPath().WithJsonPath().
    /// </summary>
    /// <param name="path">
    /// It is the json path to evaluate.
    /// </param>
    /// <returns>
    /// A WfRunVariable.
    /// </returns>
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
        WorkflowThread activeThread = _parent;
        WorkflowThread lastThread = _parent.Parent.Threads.Peek();

        if (lastThread.IsActive)
        {
            activeThread = lastThread;
        }

        activeThread.Mutate(this, VariableMutationType.Assign, rhs);
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
    
    /// <summary>
    /// Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
    /// evaluates to true if this WfRunVariable is LESS_THAN the provided rhs.
    ///
    /// Equivalent to WorkflowThread#condition(this, Comparator.LessThan, rhs);
    /// </summary>
    /// <param name="rhs">
    /// It is the RHS to compare this WfRunVariable to.
    /// </param>
    /// <returns>
    /// true if this WfRunVariable is LESS_THAN the provided rhs.
    /// </returns>
    public WorkflowCondition IsLessThan(object rhs)
    {
        return _parent.Condition(this, Comparator.LessThan, rhs);
    }
    
    /// <summary>
    /// Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
    /// evaluates to true if this WfRunVariable is LESS_THAN_EQU the provided rhs.
    ///
    /// Equivalent to WorkflowThread#condition(this, Comparator.LessThanEq, rhs);
    /// </summary>
    /// <param name="rhs">
    /// It is the RHS to compare this WfRunVariable to.
    /// </param>
    /// <returns>
    /// true if this WfRunVariable is LESS_THAN_EQ the provided rhs.
    /// </returns>
    public WorkflowCondition IsLessThanEq(object rhs)
    {
        return _parent.Condition(this, Comparator.LessThanEq, rhs);
    }
    
    /// <summary>
    /// Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
    /// evaluates to true if this WfRunVariable is GREATER_THAN_EQ the provided rhs.
    ///
    /// Equivalent to WorkflowThread#condition(this, Comparator.GreaterThanEq, rhs);
    /// </summary>
    /// <param name="rhs">
    /// It is the RHS to compare this WfRunVariable to.
    /// </param>
    /// <returns>
    /// true if this WfRunVariable is GREATER_THAN_EQ the provided rhs.
    /// </returns>
    public WorkflowCondition IsGreaterThanEq(object rhs)
    {
        return _parent.Condition(this, Comparator.GreaterThanEq, rhs);
    }
    
    /// <summary>
    /// Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
    /// evaluates to true if this WfRunVariable is GREATER_THAN the provided rhs.
    ///
    /// Equivalent to WorkflowThread#condition(this, Comparator.GreaterThan, rhs);
    /// </summary>
    /// <param name="rhs">
    /// It is the RHS to compare this WfRunVariable to.
    /// </param>
    /// <returns>
    /// true if this WfRunVariable is GREATER_THAN the provided rhs.
    /// </returns>
    public WorkflowCondition IsGreaterThan(object rhs)
    {
        return _parent.Condition(this, Comparator.GreaterThan, rhs);
    }
    
    /// <summary>
    /// Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
    /// evaluates to true if this WfRunVariable is EQUALS the provided rhs.
    ///
    /// Equivalent to WorkflowThread#condition(this, Comparator.Equals, rhs);
    /// </summary>
    /// <param name="rhs">
    /// It is the RHS to compare this WfRunVariable to.
    /// </param>
    /// <returns>
    /// true if this WfRunVariable is EQUALS the provided rhs.
    /// </returns>
    public WorkflowCondition IsEqualTo(object rhs)
    {
        return _parent.Condition(this, Comparator.Equals, rhs);
    }
    
    /// <summary>
    /// Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
    /// evaluates to true if this WfRunVariable is NOT_EQUALS the provided rhs.
    ///
    /// Equivalent to WorkflowThread#condition(this, Comparator.NotEquals, rhs);
    /// </summary>
    /// <param name="rhs">
    /// It is the RHS to compare this WfRunVariable to.
    /// </param>
    /// <returns>
    /// true if this WfRunVariable is NOT_EQUALS the provided rhs.
    /// </returns>
    public WorkflowCondition IsNotEqualTo(object rhs)
    {
        return _parent.Condition(this, Comparator.NotEquals, rhs);
    }
    
    /// <summary>
    /// Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
    /// evaluates to true if the RHS is contained inside this variable. For JSON_OBJ, returns
    /// true if the RHS is a key. For JSON_ARR, returns true if the RHS is equal to one of the
    /// elements in the array.
    ///
    /// Equivalent to WorkflowThread#condition(rhs, Comparator.In, this);
    /// </summary>
    /// <param name="rhs">
    /// It is the RHS to compare this WfRunVariable to.
    /// </param>
    /// <returns>
    /// true if the provided rhs is INSIDE this WfRunVariable.
    /// </returns>
    public WorkflowCondition DoesContain(object rhs)
    {
        return _parent.Condition(rhs, Comparator.In, this);
    }
    
    /// <summary>
    /// Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
    /// evaluates to true if the RHS is not contained inside this variable. For JSON_OBJ, returns
    /// true if the RHS is a key. For JSON_ARR, returns true if the RHS is not equal to one of the
    /// elements in the array.
    ///
    /// Equivalent to WorkflowThread#condition(rhs, Comparator.NotIn, this);
    /// </summary>
    /// <param name="rhs">
    /// It is the RHS to compare this WfRunVariable to.
    /// </param>
    /// <returns>
    /// true if the provided rhs is NOT INSIDE this WfRunVariable.
    /// </returns>
    public WorkflowCondition DoesNotContain(object rhs)
    {
        return _parent.Condition(rhs, Comparator.NotIn, this);
    }
    
    /// <summary>
    /// Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
    /// evaluates to true if the value of this WfRunVariable is contained in the provided RHS.
    /// For an RHS of type JSON_OBJ, returns true if the RHS contains a key that is equal to the
    /// value of this WfRunVariable. For an RHS of type JSON_ARR, returns true if the RHS contains
    /// an element that is equal to the value of this WfRunVariable.
    ///
    /// Equivalent to WorkflowThread#condition(this, Comparator.In, rhs);
    /// </summary>
    /// <param name="rhs">
    /// It is the RHS to compare this WfRunVariable to.
    /// </param>
    /// <returns>
    /// true if this WfRunVariable is INSIDE the provided rhs.
    /// </returns>
    public WorkflowCondition IsIn(object rhs)
    {
        return _parent.Condition(this, Comparator.In, rhs);
    }
    
    /// <summary>
    /// Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
    /// evaluates to true if the value of this WfRunVariable is not contained in the provided RHS.
    /// For an RHS of type JSON_OBJ, returns true if the RHS does not contain a key that is equal
    /// to the value of this WfRunVariable. For an RHS of type JSON_ARR, returns true if the RHS does
    /// not contain an element that is equal to the value of this WfRunVariable.
    ///
    /// Equivalent to WorkflowThread#condition(this, Comparator.NotIn, rhs);
    /// </summary>
    /// <param name="rhs">
    /// It is the RHS to compare this WfRunVariable to.
    /// </param>
    /// <returns>
    /// true if this WfRunVariable is NOT INSIDE the provided rhs.
    /// </returns>
    public WorkflowCondition IsNotIn(object rhs)
    {
        return _parent.Condition(this, Comparator.NotIn, rhs);
    }
}