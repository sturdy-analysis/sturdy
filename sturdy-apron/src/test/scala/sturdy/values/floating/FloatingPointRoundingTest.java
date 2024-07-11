package sturdy.values.floating;
import apron.*;

public class FloatingPointRoundingTest {
    public static void main(String args[]) throws ApronException {
        Manager manager = new Polka(true);
        Abstract1 abs = new Abstract1(manager, new Environment());

        System.out.println("real zero:      " + abs.getBound(manager, add(0, -6.561472834256157E307, Texpr1Node.RTYPE_REAL, Texpr1Node.RDIR_ZERO)));

        System.out.println("double nearest: " + abs.getBound(manager, add(0, -6.561472834256157E307, Texpr1Node.RTYPE_DOUBLE, Texpr1Node.RDIR_NEAREST)));
        System.out.println("double zero:    " + abs.getBound(manager, add(0, -6.561472834256157E307, Texpr1Node.RTYPE_DOUBLE, Texpr1Node.RDIR_ZERO)));
        System.out.println("double up:      " + abs.getBound(manager, add(0, -6.561472834256157E307, Texpr1Node.RTYPE_DOUBLE, Texpr1Node.RDIR_UP)));
        System.out.println("double down:    " + abs.getBound(manager, add(0, -6.561472834256157E307, Texpr1Node.RTYPE_DOUBLE, Texpr1Node.RDIR_DOWN)));
        System.out.println("double rnd:     " + abs.getBound(manager, add(0, -6.561472834256157E307, Texpr1Node.RTYPE_DOUBLE, Texpr1Node.RDIR_RND)));
    }

    public static Texpr1Intern add(double x, double y, int rtype, int rdir) {
        return new Texpr1Intern(new Environment(),
                new Texpr1BinNode(
                        Texpr1BinNode.OP_ADD,
                        rtype,
                        rdir,
                        new Texpr1CstNode(new DoubleScalar(x)),
                        new Texpr1CstNode(new DoubleScalar(y))));
    }
}
