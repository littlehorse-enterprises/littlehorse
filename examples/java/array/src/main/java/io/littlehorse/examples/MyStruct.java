package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@LHStructDef(name="my-struct")
@Getter
@Setter
public class MyStruct {
  String[] list;

  public MyStruct(String[] list) {
    this.list = list;
  }
}
