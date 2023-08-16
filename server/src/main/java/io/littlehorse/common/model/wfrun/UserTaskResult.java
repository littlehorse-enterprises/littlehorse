package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.UserTaskFieldResult;
import io.littlehorse.sdk.common.proto.UserTaskResultPb;
import java.util.ArrayList;
import java.util.List;

public class UserTaskResult extends LHSerializable<UserTaskResultPb> {

    public List<UserTaskFieldResult> fields;

    public UserTaskResult() {
        fields = new ArrayList<>();
    }

    public Class<UserTaskResultPb> getProtoBaseClass() {
        return UserTaskResultPb.class;
    }

    public UserTaskResultPb.Builder toProto() {
        UserTaskResultPb.Builder out = UserTaskResultPb.newBuilder();

        for (UserTaskFieldResult utfr : fields) {
            out.addFields(utfr);
        }
        return out;
    }

    public void initFrom(Message proto) {
        UserTaskResultPb p = (UserTaskResultPb) proto;
        for (UserTaskFieldResult utfr : p.getFieldsList()) {
            fields.add(utfr);
        }
    }
}
