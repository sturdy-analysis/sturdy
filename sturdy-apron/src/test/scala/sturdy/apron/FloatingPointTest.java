package sturdy.apron;

import apron.*;
import java.math.*;

public class FloatingPointTest {
    public static void main(String args[]) throws ApronException {
//        testAdd(new Polka(true), 0.0, -6.561472834256157E307d);
//        testAdd(new Polka(true), 1E308d, 1E308d);
//        testAdd(new Polka(true), -1E308d, -1E308d);
//        testAdd(new Polka(true), -Double.MAX_VALUE, -Double.MAX_VALUE);
        testCast(new Polka(true), 9223372036854776000d);
    }

    public static void testAdd(Manager manager, double x, double y) throws ApronException {
        Abstract1 abs = new Abstract1(manager, new Environment());
        double result = x + y;
        System.out.printf("Test (%s): %e + %e = %e\n", manager.getClass().getSimpleName(), x, y, result);
        containsDouble("double rnd", manager, abs.getBound(manager, add(new DoubleScalar(x), new DoubleScalar(y), Texpr1Node.RTYPE_DOUBLE, Texpr1Node.RDIR_RND)), result);
        System.out.println();
    }

    public static void testCast(Manager manager, double x) throws ApronException {
        Abstract1 abs = new Abstract1(manager, new Environment());
        long result = (long) x;
        System.out.printf("Test (%s): (long)%f = %d\n", manager.getClass().getSimpleName(), x, result);
        containsBigInt("int nearest", manager, abs.getBound(manager, cast(new DoubleScalar(x), Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_NEAREST)), BigInteger.valueOf(result));
        containsBigInt("int zero", manager, abs.getBound(manager, cast(new DoubleScalar(x), Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_ZERO)), BigInteger.valueOf(result));
        containsBigInt("int up", manager, abs.getBound(manager, cast(new DoubleScalar(x), Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_UP)), BigInteger.valueOf(result));
        containsBigInt("int down", manager, abs.getBound(manager, cast(new DoubleScalar(x), Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_DOWN)), BigInteger.valueOf(result));
        containsBigInt("int rnd", manager, abs.getBound(manager, cast(new DoubleScalar(x), Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_RND)), BigInteger.valueOf(result));
        System.out.println();
    }

    public static void containsDouble(String message, Manager manager, Interval iv, double result) {
        if(new Interval(new DoubleScalar(result), new DoubleScalar(result)).isLeq(iv)) {
            System.out.printf("Test Succeeded (%s): %s contains %e\n", message, iv, result);
        } else {
            System.out.printf("Test Failed (%s): %s does not contain %e\n", message, iv, result);
        }
    }

    public static void containsBigInt(String message, Manager manager, Interval iv, BigInteger result) {
        if(new Interval(new MpqScalar(result), new MpqScalar(result)).isLeq(iv)) {
            System.out.printf("Test Succeeded (%s): %s contains %d\n", message, iv, result);
        } else {
            System.out.printf("Test Failed (%s): %s does not contain %d\n", message, iv, result);
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

    public static Texpr1Intern cast(Coeff x, int rtype, int rdir) {
        return new Texpr1Intern(new Environment(),
                new Texpr1UnNode(
                        Texpr1UnNode.OP_CAST,
                        rtype,
                        rdir,
                        new Texpr1CstNode(x)));
    }
}
