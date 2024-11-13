import React, { forwardRef } from 'react';
import { useForm, Controller, FieldValues, SubmitHandler } from 'react-hook-form';
import { VariableType } from 'littlehorse-client/proto';

interface Option {
  value: string;
  label: string;
}

interface WfFormProps {
  variableDefs: any;
  onSubmit: SubmitHandler<FieldValues>;
  errors?: Record<string, string>;
  formRef:any
}

const WfForm = forwardRef<HTMLFormElement, WfFormProps>(({ formRef, variableDefs, onSubmit, errors = {} }) => {
  const { register, handleSubmit, control } = useForm<FieldValues>();

  return (
    <form ref={formRef} onSubmit={handleSubmit(onSubmit)}>
      {variableDefs.map((def: any) => (
        <div key={def.varDef?.name} style={{ marginBottom: '1em' }}>
          {(def.varDef?.type === VariableType.INT || def.varDef?.type === VariableType.DOUBLE || def.varDef?.type === VariableType.STR) && (
            <Controller
              name={def.varDef?.name}
              control={control}
              render={({ field }) => (
                <>
                  <input
                    {...field}
                    type={def.varDef?.type === VariableType.DOUBLE || def.varDef?.type === VariableType.INT ? 'number' : 'text'}
                    placeholder={`Enter ${field.name}`}
                    className="w-full p-2 border"
                  />
                  {errors[field.name] && <span className="text-red-600">{errors[field.name]}</span>}
                </>
              )}

              
            />
          )}
          {(def.varDef?.type === VariableType.JSON_OBJ || def.varDef?.type === VariableType.UNRECOGNIZED || def.varDef?.type === VariableType.BYTES) && (
            <Controller
              name={def.name}
              control={control}
              render={({ field }) => (
                <>
                  <textarea
                    {...field}
                    placeholder={`Enter ${field.name}`}
                    className="p-2 w-full border"
                  />
                  {errors[field.name] && <span className="text-red-600">{errors[field.name]}</span>}
                </>
              )}
            />
          )}
          {def.type === VariableType.BOOL && def.options && (
            <>
              <label>{def.name}</label>
              {def.options.map((option: Option) => (
                <div key={option.value} style={{ marginLeft: '0.5em' }}>
                  <Controller
                    name={def.name}
                    control={control}
                    render={({ field }) => (
                      <input
                        {...field}
                        type="checkbox"
                        value={option.value}
                        checked={field.value?.includes(option.value)}
                        onChange={(e) => {
                          const newValue = e.target.checked
                            ? [...(field.value || []), option.value]
                            : field.value?.filter((val: string) => val !== option.value);
                          field.onChange(newValue); 
                        }}
                      />
                    )}
                  />
                  {option.label}
                </div>
              ))}
              {errors[def.name] && <span className="block text-red-600">{errors[def.name]}</span>}
            </>
          )}
        </div>
      ))}
    </form>
  );
});

export { WfForm };



