package sturdy.apron;

import apron.*;
import java.util.stream.*;

public class ApronDoubleFreeBug {
    public static void main(String[] args) {
        IntStream.range(0,1000).parallel().forEach(i -> {
            try {
                Manager manager = i % 2 == 0 ? new Polka(true) : new Octagon();
                Environment env = new Environment(new String[]{"x"}, new String[]{});
                Abstract1 abstract1 = new Abstract1(manager, env);
                abstract1.assign(manager, "x", new Texpr1Intern(env, new Texpr1CstNode(new DoubleScalar(1))), null);
            } catch(ApronException exc){
                exc.printStackTrace();
            }
        });
    }
}
