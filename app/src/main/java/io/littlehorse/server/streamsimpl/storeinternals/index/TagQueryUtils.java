package io.littlehorse.server.streamsimpl.storeinternals.index;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.LocalTagScanPb;
import io.littlehorse.common.proto.SearchNodeRunPb;
import io.littlehorse.common.proto.SearchVariablePb;
import io.littlehorse.common.proto.SearchWfRunPb;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TagQueryUtils {

    public static LocalTagScanPb translateQuery(MessageOrBuilder req)
        throws LHValidationError {
        Class<?> reqClass = req.getClass();

        if (reqClass.equals(SearchWfRunPb.class)) {
            return translateSearchWfRun((SearchWfRunPb) req);
        } else if (reqClass.equals(SearchNodeRunPb.class)) {
            return translateSearchNodeRun((SearchNodeRunPb) req);
        } else if (SearchVariablePb.class.equals(reqClass)) {
            return translateSearchVariable((SearchVariablePb) req);
        }

        throw new RuntimeException("unimplemented, colt messed up");
    }

    private static LocalTagScanPb translateSearchWfRun(SearchWfRunPb req)
        throws LHValidationError {
        BookmarkPb bookmark = parseBookmark(
            req.hasBookmark() ? req.getBookmark() : null
        );
        int limit = req.hasLimit() ? req.getLimit() : LHConstants.DEFAULT_LIMIT;

        List<Attribute> attrList = Arrays.asList(
            new Attribute("wfSpecName", req.getStatusAndSpec().getWfSpecName()),
            new Attribute("status", req.getStatusAndSpec().getStatus().toString())
        );
        LocalTagScanPb.Builder out = LocalTagScanPb
            .newBuilder()
            .setBookmark(bookmark)
            .setLimit(limit)
            .setObjectType(GETableClassEnumPb.WF_RUN);

        for (Attribute attr : attrList) {
            out.addAttributes(attr.toProto());
        }
        return out.build();
    }

    private static LocalTagScanPb translateSearchNodeRun(SearchNodeRunPb req)
        throws LHValidationError {
        BookmarkPb bookmark = parseBookmark(
            req.hasBookmark() ? req.getBookmark() : null
        );
        int limit = req.hasLimit() ? req.getLimit() : LHConstants.DEFAULT_LIMIT;

        List<Attribute> attrList = new ArrayList<>();

        switch (req.getCriteriaCase()) {
            case WF_RUN_ID:
                attrList.add(new Attribute("wfRunId", req.getWfRunId()));
                break;
            case STATUS_AND_TASKDEF:
                attrList.add(
                    new Attribute(
                        "taskDefName",
                        req.getStatusAndTaskdef().getTaskDefName()
                    )
                );
                attrList.add(
                    new Attribute(
                        "status",
                        req.getStatusAndTaskdef().getStatus().toString()
                    )
                );
                break;
            case CRITERIA_NOT_SET:
                throw new LHValidationError(null, "failed to set criteria");
        }

        LocalTagScanPb.Builder out = LocalTagScanPb
            .newBuilder()
            .setBookmark(bookmark)
            .setLimit(limit)
            .setObjectType(GETableClassEnumPb.WF_RUN);

        for (Attribute attr : attrList) {
            out.addAttributes(attr.toProto());
        }
        return out.build();
    }

    private static LocalTagScanPb translateSearchVariable(SearchVariablePb req)
        throws LHValidationError {
        BookmarkPb bookmark = parseBookmark(
            req.hasBookmark() ? req.getBookmark() : null
        );
        int limit = req.hasLimit() ? req.getLimit() : LHConstants.DEFAULT_LIMIT;

        List<Attribute> attrList = new ArrayList<>();

        switch (req.getCriteriaCase()) {
            case WF_RUN_ID:
                attrList.add(new Attribute("wfRunId", req.getWfRunId()));
                break;
            case STATUS_AND_TASKDEF:
                attrList.add(
                    new Attribute(
                        "taskDefName",
                        req.getStatusAndTaskdef().getTaskDefName()
                    )
                );
                attrList.add(
                    new Attribute(
                        "status",
                        req.getStatusAndTaskdef().getStatus().toString()
                    )
                );
                break;
            case CRITERIA_NOT_SET:
                throw new LHValidationError(null, "failed to set criteria");
        }

        LocalTagScanPb.Builder out = LocalTagScanPb
            .newBuilder()
            .setBookmark(bookmark)
            .setLimit(limit)
            .setObjectType(GETableClassEnumPb.WF_RUN);

        for (Attribute attr : attrList) {
            out.addAttributes(attr.toProto());
        }
        return out.build();
    }

    public static BookmarkPb parseBookmark(ByteString bytes)
        throws LHValidationError {
        if (bytes == null) return null;
        try {
            return BookmarkPb.parseFrom(bytes);
        } catch (InvalidProtocolBufferException exn) {
            throw new LHValidationError(exn, "Invalid bookmark provided: ");
        }
    }
}
