package io.littlehorse.server.streams.lhinternalscan.util;

import com.google.protobuf.Message;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagScan;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Setter;

public class TagScanModel extends ScanBoundary<TagScan> {

    private GetableClassEnum scanObjectType;
    private List<Attribute> attributes;

    @Setter
    private Date earliestCreateTime;

    @Setter
    private Date latestCreateTime;

    public TagScanModel() {
        this.attributes = new ArrayList<>();
    }

    public TagScanModel(GetableClassEnum objectType) {
        this();
        this.scanObjectType = objectType;
    }

    public TagScanModel(GetableClassEnum objectType, Date earliestStart, Date latestStart) {
        this(objectType);
        this.earliestCreateTime = earliestStart;
        this.latestCreateTime = latestStart;
    }

    @Override
    public Class<TagScan> getProtoBaseClass() {
        return TagScan.class;
    }

    @Override
    public TagScan.Builder toProto() {
        TagScan.Builder out = TagScan.newBuilder().setScanObjectType(scanObjectType);
        attributes.stream().forEach(attr -> out.addAttributes(attr.toProto()));

        if (earliestCreateTime != null) out.setEarliestCreateTime(LHLibUtil.fromDate(earliestCreateTime));
        if (latestCreateTime != null) out.setLatestCreateTime(LHLibUtil.fromDate(latestCreateTime));
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        TagScan p = (TagScan) proto;
        scanObjectType = p.getScanObjectType();
        p.getAttributesList().stream().forEach(attr -> attributes.add(Attribute.fromProto(attr, ctx)));

        if (p.hasEarliestCreateTime()) earliestCreateTime = LHLibUtil.fromProtoTs(p.getEarliestCreateTime());
        if (p.hasLatestCreateTime()) latestCreateTime = LHLibUtil.fromProtoTs(p.getLatestCreateTime());
    }

    @Override
    public String getStartKey() {
        StringBuilder out = new StringBuilder(Tag.getAttributeString(scanObjectType, attributes));
        out.append("/");
        if (earliestCreateTime != null) {
            out.append(LHUtil.toLhDbFormat(earliestCreateTime));
            out.append("/");
        }
        return out.toString();
    }

    @Override
    public String getEndKey() {
        StringBuilder out = new StringBuilder(Tag.getAttributeString(scanObjectType, attributes));
        out.append("/");
        if (latestCreateTime != null) {
            out.append(LHUtil.toLhDbFormat(latestCreateTime));
            out.append("/");
        }
        out.append("~");
        return out.toString();
    }

    @Override
    public Class<? extends Storeable<?>> getIterType() {
        // Tag scan iterates over...you guessed it...Tags!
        return Tag.class;
    }

    public TagScanModel addAttributes(String key, String val) {
        attributes.add(new Attribute(key, val));
        return this;
    }

    public TagScanModel add(Attribute attr) {
        attributes.add(attr);
        return this;
    }
}
