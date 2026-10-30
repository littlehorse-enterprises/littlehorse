package io.littlehorse.sdk.worker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares mutually exclusive alternatives in a Struct.
 * On a class or record, all included properties are alternatives and {@code group}
 * must be omitted. The type becomes an inline Struct, including when used as a
 * task parameter or return type. Type-level use cannot be combined with
 * {@link LHStructDef}; named StructDefs can contain oneof groups instead.
 * On a field, accessor, or record component, {@code group} is required and names
 * the nested Struct into which the SDK projects the participating properties.
 *
 * <p>
 * Members must use reference types without defaults or nullable annotations.
 * Null means that member is not selected. Every group emits a nested Struct,
 * including an empty Struct when no selection is required and none is provided.
 *
 * <p>Every member of a group must specify the same {@code required} setting.
 * Group membership uses the property's serialized name, including {@link LHStructField#name()}.
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface LHStructOneOf {
    /** Serialized container name; required on properties and omitted on types. */
    String group() default "";

    /** Whether exactly one member must be selected; false allows zero or one. */
    boolean required() default false;
}
