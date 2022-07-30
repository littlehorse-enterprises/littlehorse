package io.littlehorse.server.model.internal;

import java.util.List;

public class RangeResponse {
    public String nextToken;
    public List<String> ids;
}

class SearchParam {
    public String labelKey;
    public String from;
    public String to;
}

class SearchParams {
    public String type;
    public List<SearchParam> params;
}
