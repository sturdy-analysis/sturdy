package sturdy.apron;

import apron.*;

public class OctagonTest {

    public static void floatingpointRounding() throws ApronException {
        double x = -1E308d;
        double y = -1E308d;
        assert(x + y == Double.NEGATIVE_INFINITY);
        new apron.Octagon();
        assert(x + y == -Double.MAX_VALUE);
    }

    public static void main(String args[]) throws ApronException {
        floatingpointRounding();
    }
}
