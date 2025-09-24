package sturdy.apron;

import apron.*;
import fenv.FEnv;

import java.util.Arrays;

public class ApronManagerWasExactNonDeterministic {
    public static void main(String[] args) throws ApronException, InterruptedException {
    Environment env = new Environment();
    Manager manager = new Polka(true);
    Abstract1 abs1 = new Abstract1(manager, env);

    Tcons1 cons =
            new Tcons1(env, Tcons1.SUPEQ,
                new Texpr1BinNode(Texpr1BinNode.OP_SUB,
                        new Texpr1CstNode(new DoubleScalar(4)),
                        new Texpr1CstNode(new DoubleScalar(1024))
                        )
            );

    manager.setFlagExactWanted(Manager.FUNID_SAT_TCONS, true);
    System.out.printf("abs1.satisfies(%s) = %b\n", cons, abs1.satisfy(manager, cons));
    System.out.printf("manager.wasExact = %b\n", manager.wasExact());
    }
}
