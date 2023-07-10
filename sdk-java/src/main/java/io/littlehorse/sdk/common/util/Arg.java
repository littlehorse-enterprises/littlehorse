package io.littlehorse.jlib.common.util;

public class Arg {

    public String name;
    public Object value;

    public static Arg of(String name, Object value) {
        Arg out = new Arg();
        out.value = value;
        out.name = name;
        return out;
    }
}
