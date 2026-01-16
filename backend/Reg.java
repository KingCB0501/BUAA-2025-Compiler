package backend;

import java.util.ArrayList;
import java.util.HashMap;

public class Reg {

    int index;
    String name;

    private Reg(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public static Reg zero = new Reg(0, "zero");
    public static Reg at = new Reg(1, "at");
    public static Reg v0 = new Reg(2, "v0");
    public static Reg ra = new Reg(31, "ra");
    public static Reg sp = new Reg(29, "sp");
    public static Reg a0 = new Reg(4, "a0");
    public static Reg a1 = new Reg(5, "a1");
    public static Reg a2 = new Reg(6, "a2");
    public static Reg a3 = new Reg(7, "a3");
    public static Reg t0 = new Reg(8, "t0");
    public static Reg t1 = new Reg(9, "t1");
    public static Reg t2 = new Reg(10, "t2");
    public static Reg t3 = new Reg(11, "t3");
    public static Reg t4 = new Reg(12, "t4");
    public static Reg t5 = new Reg(13, "t5");
    public static Reg t6 = new Reg(14, "t6");
    public static Reg t7 = new Reg(15, "t7");
    public static Reg t8 = new Reg(24, "t8");
    public static Reg t9 = new Reg(25, "t9");
    public static Reg s0 = new Reg(16, "s0");
    public static Reg s1 = new Reg(17, "s1");
    public static Reg s2 = new Reg(18, "s2");
    public static Reg s3 = new Reg(19, "s3");

    public static Reg s4 = new Reg(20, "s4");
    public static Reg s5 = new Reg(21, "s5");
    public static Reg s6 = new Reg(22, "s6");
    public static Reg s7 = new Reg(23, "s7");

    public static Reg v1 = new Reg(3, "v1");
    public static Reg k0 = new Reg(26, "k0");
    public static Reg k1 = new Reg(27, "k1");
    public static Reg gp = new Reg(28, "gp");
    public static Reg fp = new Reg(30, "fp");


    public static Reg getArgReg(int index) {
        Reg res = null;
        switch (index) {
            case 0 -> res = a0;
            case 1 -> res = a1;
            case 2 -> res = a2;
            case 3 -> res = a3;
        }
        return res;
    }

    public boolean isParamReg() {
        return this.index >= 4 && this.index <= 7;
    }


    @Override
    public String toString() {
        return "$" + name;
    }

}
