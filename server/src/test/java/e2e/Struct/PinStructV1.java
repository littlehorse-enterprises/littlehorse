package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@LHStructDef("pin-struct")
@Getter
@Setter
public class PinStructV1 {
    public Integer x;
    public String y = "default";

    public PinStructV1() {}

    public PinStructV1(Integer x, String y) {
        this.x = x;
        this.y = y;
    }
}
