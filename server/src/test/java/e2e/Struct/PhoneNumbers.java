package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@LHStructDef("person-phone-numbers")
@Getter
@Setter
public class PhoneNumbers {
    public String home;
    public String work;

    public PhoneNumbers() {}

    public PhoneNumbers(String home, String work) {
        this.home = home;
        this.work = work;
    }
}
