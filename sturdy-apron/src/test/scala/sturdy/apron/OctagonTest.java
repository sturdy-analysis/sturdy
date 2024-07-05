package sturdy.apron;
public class OctagonTest {
    public static void main(String args[]) {
        double x = -1E308d;
        double y = -1E308d;
        System.out.printf("%e\n", x + y);
        new apron.Octagon();
        System.out.printf("%e, %s\n", x + y, Double.isInfinite(x + y));
    }
}
