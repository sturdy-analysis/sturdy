package sturdy.apron;

import apron.*;
import fenv.FEnv;

import java.util.Arrays;

public class ApronOctagonFloatingPointBug {
    public static void main(String[] args) throws ApronException {
        FEnv.setRoundingMode(FEnv.FE_TONEAREST());
        String[] vars = {"a", "b", "c", "d"};
        Environment env = new Environment(new String[]{}, vars);
        Interval top = new Interval(); top.setTop();
        Manager manager = new Octagon();
        Abstract1 abs1 = Abstract1.deserialize(manager, env, new byte[]{34, 2, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -64, -52, -44, -108, -64, 0, 0, 0, -64, -52, -44, -108, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -102, -103, -103, -7, -66, 102, 102, 102, -58, -52, -44, -108, -64, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, 0, 0, -51, -52, -52, -52, -52, -44, -108, -64, 102, 102, 102, -58, -52, -44, -108, 64, 0, 0, 0, -102, -103, -103, -7, 62, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, -51, -52, -52, -52, -52, -44, -108, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, -16, 127, 0, 0, 0, 0, 0, 0, 0, 0});
        System.out.println(abs1); // {  1a -666.5999755859375 >= 0;  -1a +666.5999755859375 >= 0;  -1a +1c -2.4414062522737368E-5 >= 0;  1a +1c -1333.1999755859374 >= 0;  1c -666.6 >= 0;  -1a -1c +1333.1999755859374 >= 0;  1a -1c +2.4414062522737368E-5 >= 0;  -1c +666.6 >= 0 }
        System.out.println(Arrays.toString(abs1.toBox(manager))); // [[666.5999755859375,666.5999755859375], [-Infinity,Infinity], [666.6,666.6], [-Infinity,Infinity]]
        abs1.assign(manager, "a", new Texpr1Intern(env, new Texpr1CstNode(new Interval(666.5999755859375d,666.5999755859375d))), null);
//        Abstract1 abs2 = abs1.assignCopy(manager, new String[]{"b", "d"}, new Texpr1Intern[]{ new Texpr1Intern(env, new Texpr1CstNode(new Interval(1,1))), new Texpr1Intern(env, new Texpr1CstNode(new Interval(2,2)))}, null);
        Abstract1 abs2 = abs1.assignCopy(manager, "b", new Texpr1Intern(env, new Texpr1CstNode(new Interval(1,1))), null);
        Abstract1 abs3 = abs2.assignCopy(manager, "d", new Texpr1Intern(env, new Texpr1CstNode(new Interval(2,2))), null);

        System.out.println(abs3); // <empty>
    }
}
