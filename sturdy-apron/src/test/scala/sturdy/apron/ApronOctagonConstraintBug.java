package sturdy.apron;

import apron.*;
import fenv.FEnv;

import java.util.Arrays;

public class ApronOctagonConstraintBug {
    public static void main(String[] args) throws ApronException {
        FEnv.setRoundingMode(FEnv.FE_TONEAREST());
        String[] vars = {"x", "y"};
        Environment env = new Environment(vars, new String[]{});
        Manager manager = new Octagon();
        Abstract1 abs1 = new Abstract1(manager, env);
        abs1.assign(manager, "x", new Texpr1Intern(env, new Texpr1CstNode(new DoubleScalar(3))), null);
        abs1.assign(manager, "y", new Texpr1Intern(env, new Texpr1CstNode(new DoubleScalar(4))), null);
        System.out.println(abs1);

        Tcons1 cons =
            new Tcons1(Tcons1.SUPEQ,
                new Texpr1Intern(env,
                    new Texpr1BinNode(Texpr1BinNode.OP_SUB,
                        new Texpr1BinNode(Texpr1BinNode.OP_ADD,
                            new Texpr1VarNode("x"),
                            new Texpr1VarNode("y")
                        ),
                        new Texpr1CstNode(new DoubleScalar(Integer.MIN_VALUE))
                    )
                )
            );
        System.out.println(cons);
        abs1.meet(manager, cons);
        System.out.println(abs1);
    }
}
