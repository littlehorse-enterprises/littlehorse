package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;

@LHStructDef(name = "unknown-struct-def")
public class UnknownStructDef {
    // This is a dummy StructDef used in shouldRejectPutWfSpecRequestUsingUnknownStructDef
}
