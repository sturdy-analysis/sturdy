package sturdy.elina;

import gmp.*;
import apron.*;
import elina.*;

public class ElinaTest {
    public static void main(String args[]) throws ApronException {
        Manager manager = new elina.OptPoly(false);
        Environment env = new Environment();
        Abstract1 abs1 = new Abstract1(manager, env);
        Interval iv = abs1.getBound(manager, new Texpr1Intern(env, new Texpr1CstNode(new MpqScalar(new Mpq(-100)))));
        System.out.println(iv);
    }
}
