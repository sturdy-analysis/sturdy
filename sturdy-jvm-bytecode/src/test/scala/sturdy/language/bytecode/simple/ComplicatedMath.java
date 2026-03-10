package sturdy.language.bytecode.simple;

interface MathOps {
    int binOp(int x, int y);
}

interface TestInterface {
    int testMeth(int x);

    default int addOp(int x, int y){
        int z = x + y;
        return z;
    }
}

public class ComplicatedMath extends SimpleMath implements TestInterface{

    int z = 0;

    public ComplicatedMath(int z){
        this.z = z;
    }
    public ComplicatedMath(int x, int y, int z){
        super(x,y);
        this.z = z;
    }

    public int return10(){
        int test = this.x;
        return (this.x + this.y);
    }

    public int testMeth(int x){
        return x+12;
    }

    @Override
    public int inheritTest() {
        return 5;
    }

    public static int lambdaTest(){
        int x = 5;
        int y = 10;
        MathOps multOperator = (v1, v2) -> v1 * v2;
        MathOps addOperator = (v1, v2) -> v1 + v2;
        int z1 = multOperator.binOp(x,y);
        int z2 = addOperator.binOp(x, y);
        return z1+z2;

    }

    public static int interfaceTest(){
        TestInterface testMath = new ComplicatedMath(3,2,12);
        return testMath.testMeth(12);
    }

    public static int typeTestInterface() {
        TestInterface testMath = new ComplicatedMath(3, 2, 12);
        if (testMath instanceof TestInterface) {
            return 5;
        } else {
            return 0;
        }
    }

    public static int typeTestArray(){
        int[][] testArray = new int[5][5];
        if(testArray instanceof int[][]){
            return 5;
        }
        else{
            return 0;
        }
    }

    public static int defaultInterfaceTest(){
        ComplicatedMath math = new ComplicatedMath(56);
        return math.addOp(math.z,10);
    }
}
