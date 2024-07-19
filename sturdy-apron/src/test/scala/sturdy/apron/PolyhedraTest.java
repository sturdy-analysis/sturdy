package sturdy.apron;

import apron.ApronException;
import apron.Polka;

public class PolyhedraTest {

    public static void floatingpointRounding() throws ApronException {
        int x = 16777217;
        assert(new Integer(x).floatValue() == 16777216f);
        new Polka(true);
        assert(new Integer(x).floatValue() == 16777218f);
    }

    public static void main(String args[]) throws ApronException {
        floatingpointRounding();
    }
}
