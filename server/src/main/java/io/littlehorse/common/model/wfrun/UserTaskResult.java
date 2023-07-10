package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.UserTaskFieldResultPb;
import io.littlehorse.sdk.common.proto.UserTaskResultPb;
import java.util.ArrayList;
import java.util.List;

public class UserTaskResult extends LHSerializable<UserTaskResultPb> {

    public List<UserTaskFieldResultPb> fields;

    public UserTaskResult() {
        fields = new ArrayList<>();
    }

    public Class<UserTaskResultPb> getProtoBaseClass() {
        return UserTaskResultPb.class;
    }

    public UserTaskResultPb.Builder toProto() {
        UserTaskResultPb.Builder out = UserTaskResultPb.newBuilder();

        for (UserTaskFieldResultPb utfr : fields) {
            out.addFields(utfr);
        }
        return out;
    }

    public void initFrom(Message proto) {
        UserTaskResultPb p = (UserTaskResultPb) proto;
        for (UserTaskFieldResultPb utfr : p.getFieldsList()) {
            fields.add(utfr);
        }
    }
}
