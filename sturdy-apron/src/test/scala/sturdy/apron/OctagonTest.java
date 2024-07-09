package sturdy.apron;

import apron.*;

public class OctagonTest {
    public static void main(String args[]) throws ApronException {
        Manager manager = new Octagon();
        Environment env = new Environment(new String[]{}, new String[]{"x"});
        Abstract1 abstract1 = new Abstract1(manager, env);

        Interval top = new Interval();
        top.setTop();

        abstract1.assign(manager, "x", new Texpr1Intern(env, new Texpr1CstNode(top)), null);
        System.out.println(abstract1);
        System.out.println(abstract1.getBound(manager, new Texpr1Intern(env, new Texpr1VarNode("x"))));

    }
}
