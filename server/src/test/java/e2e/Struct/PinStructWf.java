package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@LHStructDef("pin-struct")
@Getter
@Setter
public class PinStructWf {
    public Integer x;
    public String y;

    public PinStructWf() {}

    public PinStructWf(Integer x, String y) {
        this.x = x;
        this.y = y;
    }
}
