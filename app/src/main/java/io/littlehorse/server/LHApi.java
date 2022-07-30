package io.littlehorse.server;

import java.util.Arrays;
import java.util.List;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.util.KStreamsStateListener;


public class LHApi {
    private Javalin app;
    private LHConfig config;

    public static List<String> GETables = Arrays.asList(
        WfSpec.class.getSimpleName(), TaskDef.class.getSimpleName()
    );

    public LHApi(LHConfig config) {
        this.config = config;
        KStreamsStateListener listener = new KStreamsStateListener();
        this.app = LHConfig.createAppWithHealth(listener);

        this.app.get("/search/{type}", this::search);
        this.app.get("/{type}/{id}", this::get);

        this.app.post("/WFSpec", this::postWFSpec);
        this.app.post("/TaskDef", this::postTaskDef);
    }

    public void start() {
        app.start(config.getExposedPort());
    }

    public void search(Context ctx) {
        String type = ctx.pathParam("type");
        String key = ctx.pathParam("key");
    }

    public void get(Context ctx) {
        String type = ctx.pathParam("type");
        String id = ctx.pathParam("id");

        if (!GETables.contains(type)) {
            
        }
    }

    public void postTaskDef(Context ctx) {
        // TODO: post the wfspec
    }

    public void postWFSpec(Context ctx) {
        // TODO: post the wfspec
    }
}
