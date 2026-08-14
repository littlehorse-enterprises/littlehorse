package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

/**
 * Named StructDef whose {@code dimensions} field is an unannotated POJO. With PR 6,
 * {@link Dimensions} is automatically emitted as an {@code INLINE_STRUCT_DEF} field
 * rather than falling through to the forbidden {@code JSON_OBJ} type.
 */
@LHStructDef("inline-test-product")
@Getter
@Setter
public class Product {
    public String name;
    public Dimensions dimensions;

    public Product() {}

    public Product(String name, Dimensions dimensions) {
        this.name = name;
        this.dimensions = dimensions;
    }
}
