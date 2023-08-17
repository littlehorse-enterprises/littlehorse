package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.UserTaskFieldResult;
import io.littlehorse.sdk.common.proto.UserTaskResult;
import java.util.ArrayList;
import java.util.List;

public class UserTaskResultModel extends LHSerializable<UserTaskResult> {

    public List<UserTaskFieldResult> fields;

    public UserTaskResultModel() {
        fields = new ArrayList<>();
    }

    public Class<UserTaskResult> getProtoBaseClass() {
        return UserTaskResult.class;
    }

    public UserTaskResult.Builder toProto() {
        UserTaskResult.Builder out = UserTaskResult.newBuilder();

        for (UserTaskFieldResult utfr : fields) {
            out.addFields(utfr);
        }
        return out;
    }

    public void initFrom(Message proto) {
        UserTaskResult p = (UserTaskResult) proto;
        for (UserTaskFieldResult utfr : p.getFieldsList()) {
            fields.add(utfr);
        }
    }
}
