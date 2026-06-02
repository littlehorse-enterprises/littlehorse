package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@LHStructDef("pin-struct")
@Getter
@Setter
public class PinStructV0 {
    public Integer x;

    public PinStructV0() {}

    public PinStructV0(Integer x) {
        this.x = x;
    }
}
