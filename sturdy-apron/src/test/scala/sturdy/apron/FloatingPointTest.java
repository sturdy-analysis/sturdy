package sturdy.apron;

import apron.*;

public class FloatingPointTest {
    public static void main(String args[]) throws ApronException {
        test(new Polka(true), 0.0, -6.561472834256157E307d);
        test(new Polka(true), 1E308d, 1E308d);
        test(new Polka(true), -1E308d, -1E308d);
        test(new Polka(true), -Double.MAX_VALUE, -Double.MAX_VALUE);
    }

    public static void test(Manager manager, double x, double y) throws ApronException {
        Abstract1 abs = new Abstract1(manager, new Environment());
        double result = x + y;
        System.out.printf("Test (%s): %e + %e = %e\n", manager.getClass().getSimpleName(), x, y, result);
        contains("double rnd", manager, abs.getBound(manager, add(new DoubleScalar(x), new DoubleScalar(y), Texpr1Node.RTYPE_DOUBLE, Texpr1Node.RDIR_RND)), result);
        System.out.println();
    }

    public static void contains(String message, Manager manager, Interval iv, double result) {
        if(new Interval(new DoubleScalar(result), new DoubleScalar(result)).isLeq(iv)) {
            System.out.printf("Test Succeeded (%s): %s contains %e\n", message, iv, result);
        } else {
            System.out.printf("Test Failed (%s): %s does not contain %e\n", message, iv, result);
        }
    }
    public static Texpr1Intern add(Coeff x, Coeff y, int rtype, int rdir) {
        return new Texpr1Intern(new Environment(),
                new Texpr1BinNode(
                        Texpr1BinNode.OP_ADD,
                        rtype,
                        rdir,
                        new Texpr1CstNode(x),
                        new Texpr1CstNode(y)));
    }
}
