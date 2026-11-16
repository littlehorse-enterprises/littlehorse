package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;
import lombok.Getter;
import lombok.Setter;

@LHStructDef("mutation-validation-profile")
@Getter
@Setter
public class MutationProfile {
    public String name;
    public String secret;

    public MutationProfile() {}

    public MutationProfile(String name, String secret) {
        this.name = name;
        this.secret = secret;
    }

    @LHStructField(masked = true, isNullable = true)
    public String getSecret() {
        return secret;
    }
}
