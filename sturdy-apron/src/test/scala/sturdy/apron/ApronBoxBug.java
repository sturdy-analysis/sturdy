package sturdy.apron;

import apron.*;
import fenv.FEnv;

import java.util.Arrays;

public class ApronBoxBug {
    public static void main(String[] args) throws ApronException {
        FEnv.setRoundingMode(FEnv.FE_TONEAREST());
        Manager manager = new Box();
        Environment env = new Environment(new String[]{"x", "y"}, new String[]{});
        Abstract1 abs1 = new Abstract1(manager, env);
        abs1.assign(manager, "x", new Texpr1Intern(env, new Texpr1CstNode(new Interval(Integer.MIN_VALUE,Integer.MAX_VALUE))), null);
        abs1.assign(manager, "y", new Texpr1Intern(env, new Texpr1CstNode(new Interval(-1853936221,1863658670))), null);
        System.out.println(abs1);

        Interval ivX = abs1.getBound(manager, "x");
        Interval ivY = abs1.getBound(manager, "y");
        
        if(ivX.sup().cmp(ivY.inf()) <= 0) {
            System.out.printf("%s <= %s = %b", ivX.sup(), ivY.inf(), ivX.sup().cmp(ivY.inf()) <= 0);
        }
    }
}
