package com.github.ewoowe;

public class Test {
    public static void main(String[] args) {
        Yummy.set("P:\\WorkSpace\\yummy\\src\\test\\resources\\test.yaml",
                new String[]{"a.b[1].c", "a.b[0][1].d", "a.b[0].c", "a.c.d"},
                new Object[]{"new1", "new2", "new3", "new4"},
                false);
    }
}
