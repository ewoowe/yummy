package com.github.ewoowe;

import java.util.function.Function;

public class Test {

    public static class IpSetter implements Function<String, String> {

        private String ip;

        private IpSetter(String ip) {
            this.ip = ip;
        }

        @Override
        public String apply(String s) {
            String[] split = s.split("/");
            if (split.length != 2) {
                throw new IllegalArgumentException("ip incorrect " + s);
            }
            return ip + "/" + split[1];
        }
    }

    public static void main(String[] args) {
        Yummy.set("test.yaml",
                new String[]{"a.b[1].e", "a.b[0-2].d", "a.b[0].c", "a.c.d", "a.b[1].c"},
                new Object[]{"new1", "new2", "new3", "new4", new IpSetter("192.168.39.200")},
                false);
    }
}
