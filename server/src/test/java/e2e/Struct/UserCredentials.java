package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;
import lombok.Getter;
import lombok.Setter;

@LHStructDef("struct-user-credentials")
@Getter
@Setter
public class UserCredentials {
    public String username;
    public String password;

    public UserCredentials() {}

    public UserCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @LHStructField(masked = true)
    public String getPassword() {
        return password;
    }
}
