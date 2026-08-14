package e2e.Struct;

import lombok.Getter;
import lombok.Setter;

/** Intentionally unannotated — becomes an {@code INLINE_STRUCT_DEF} field inside {@link Product}. */
@Getter
@Setter
public class Dimensions {
    public int width;
    public int height;

    public Dimensions() {}

    public Dimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
