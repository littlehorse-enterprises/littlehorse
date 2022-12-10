package io.littlehorse.server.streamsbackend.storeinternals.index;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.PaginatedTagQueryPb;
import io.littlehorse.common.proto.SearchWfRunPb;
import java.util.Arrays;
import java.util.List;

public class TagQueryUtils {

    public static PaginatedTagQueryPb translateSearchWfRun(SearchWfRunPb req)
        throws LHValidationError {
        BookmarkPb bookmark = parseBookmark(
            req.hasBookmark() ? req.getBookmark() : null
        );
        int limit = req.hasLimit() ? req.getLimit() : LHConstants.DEFAULT_LIMIT;

        List<Attribute> attrList = Arrays.asList(
            new Attribute("wfSpecName", req.getStatusAndSpec().getWfSpecName()),
            new Attribute("status", req.getStatusAndSpec().getStatus().toString())
        );
        PaginatedTagQueryPb.Builder out = PaginatedTagQueryPb
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
