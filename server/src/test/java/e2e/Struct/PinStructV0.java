package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;

@LHStructDef("pin-struct")
public class PinStructV0 {
    public Integer x;

    public PinStructV0() {}

    public PinStructV0(Integer x) {
        this.x = x;
    }

    public Integer getX() {
        return this.x;
    }

    public void setX(final Integer x) {
        this.x = x;
    }
}
