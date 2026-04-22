package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;

@LHStructDef("pin-struct")
public class PinStructV1 {
    public Integer x;
    public String y = "default";

    public PinStructV1() {}

    public PinStructV1(Integer x, String y) {
        this.x = x;
        this.y = y;
    }

    public Integer getX() {
        return this.x;
    }

    public String getY() {
        return this.y;
    }

    public void setX(final Integer x) {
        this.x = x;
    }

    public void setY(final String y) {
        this.y = y;
    }
}
