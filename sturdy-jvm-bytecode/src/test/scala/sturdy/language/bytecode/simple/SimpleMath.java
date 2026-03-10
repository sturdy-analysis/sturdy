package sturdy.language.bytecode.simple;

public class SimpleMath {
    int x = 0;
    int y = 0;

    public SimpleMath(){

    }
    public SimpleMath(int x,int y){
        this.x = x;
        this.y = y;
    }

    public int add(){
        return x+y;
    }

    public int mul(){
        return x*y;
    }

    public int inheritTest(){
        return 10;
    }

    public int return10(){
        int tempX = 5;
        int tempX1 = 5;
        int tempX2 = 5;
        int tempX3 = 5;
        int tempX4 = 5;
        tempX4 = 10;
        tempX4 = 20;

        double tX = 5.5;
        double tY = 4.5;
        tY = 10.3;
        String testString = "TSET";
        String testString2 = "TEST";
        testString2 = "NOTEST";
        System.out.println("LUL");

        this.x = 6;
        this.x = 7;

        int tempY = 10;
        int tempResult = tempY + tempX;
        return tempResult;
    }

    public static int staticInt1 = 5;
    public static int staticInt2 = 10;
    public static int sub2(int x, int y){
        return (x-y);
    }
    public static float subFloatFromInt(int x, float y){
        return (x-y);
    }

    public static int branching(boolean flag){
        int x = 0;
        if(flag) {
            x = sub2(100, 5);
        }
        else{
            x = sub2(5, 100);
        }
        return x;
    }
    public static int branchingTrue(){
        boolean flag = true;
        int x = 0;
        if(flag) {
            x = sub2(100, 5);
        }
        else{
            x = sub2(5, 100);
        }
        return x;
    }
    public static int branchingFalse(){
        boolean flag = false;
        int x = 0;
        if(flag) {
            x = sub2(100, 5);
        }
        else{
            x = sub2(5, 100);
        }
        return x;
    }
    
    public static int returnTest(boolean flag){
        int x = 12;

        if(flag){
            return x;
        }
        else{
            x = 3;
        }

        x = 5;
        return x;
    }

    public static int objectTest(){
        SimpleMath testMath = new SimpleMath(7, 12);
        SimpleMath testMath2 = testMath;
        testMath.x = 10;
        int val = testMath.x + testMath2.x;
        return val;
    }

    public static int inheritanceTest(){
        SimpleMath testMath = new SimpleMath(5, 10);
        ComplicatedMath testMath2 = new ComplicatedMath(10, 20, 1);
        return testMath.inheritTest() + testMath2.inheritTest();
    }

    public static int inheritanceTest2(){
        ComplicatedMath testMath = new ComplicatedMath(3, 5,1);
        int x = testMath.add();
        return x;
    }

    public static int inheritanceTest3(){
        AbsurdMath testMath = new AbsurdMath(3,5,12);
        int x = testMath.inheritTest();
        return x;
    }

    public static boolean objectCompTestTrue(){
        SimpleMath testMath = new SimpleMath(7, 12);
        SimpleMath testMath2 = testMath;
        boolean equal = testMath == testMath2;
        return equal;
    }
    public static boolean objectCompTestFalse(){
        SimpleMath testMath = new SimpleMath(7, 12);
        SimpleMath testMath2 = new SimpleMath(7, 12);
        boolean equal = testMath == testMath2;
        return equal;
    }

    public static int switchTest(int flag){
        int res = 0;
        switch (flag){
            case 0:
                res = 1;
                break;
            case 1:
                res = 2;
                break;
            case 2:
                res = 3;
                break;
            case 4:
                res = 5;
                break;
            default:
                res = 0;
        }
        return res;
    }
    public static int switchTest4(){
        int flag = 4;
        int res = 0;
        switch (flag){
            case 0:
                res = 1;
                break;
            case 1:
                res = 2;
                break;
            case 2:
                res = 3;
                break;
            case 4:
                res = 5;
                break;
            default:
                res = 0;
        }
        return res;
    }
    public static int switchTest10(){
        int flag = 10;
        int res = 0;
        switch (flag){
            case 0:
                res = 1;
                break;
            case 1:
                res = 2;
                break;
            case 2:
                res = 3;
                break;
            case 4:
                res = 5;
                break;
            default:
                res = 0;
        }
        return res;
    }

    public static int nonCompactSwitchTest(int flag){
        int res = 0;
        switch (flag){
            case 0:
                res = 10;
                break;
            case 10:
                res = 20;
                break;
            case 200:
                res = 300;
                break;
            case 4000:
                res = 5000;
                break;
            default:
                res = 0;
        }
        return res;
    }
    public static int nonCompactSwitchTest200(){
        int flag = 200;
        int res = 0;
        switch (flag){
            case 0:
                res = 10;
                break;
            case 10:
                res = 20;
                break;
            case 200:
                res = 300;
                break;
            case 4000:
                res = 5000;
                break;
            default:
                res = 0;
        }
        return res;
    }
    public static int nonCompactSwitchTest4000(){
        int flag = 4000;
        int res = 0;
        switch (flag){
            case 0:
                res = 10;
                break;
            case 10:
                res = 20;
                break;
            case 200:
                res = 300;
                break;
            case 4000:
                res = 5000;
                break;
            default:
                res = 0;
        }
        return res;
    }

    public static int staticVarTest(){
        staticInt1 += 5;
        staticInt2 += 10;
        return staticInt1 + staticInt2;
    }

    public static int arrayTest(){
        int[] testArray = {5, 4, 3, 2, 1, 0};
        int[] testArray2 = testArray;
        testArray[0] = 10;
        return testArray[0] + testArray2[0];
    }

    public static int arrayTest2(){
        int[] testArray = {5,4,3,2,1,0};
        int[] testArray2 = {100, 200, 303};
        return testArray[0] + testArray2[2];
    }

    public static int[] arrayTest3(){
        int[] testArray = {5,4,3,2,1,100};
        return testArray;
    }

    public static boolean arrayCompTest(){
        int[] testArray = {5, 4, 3, 2, 1, 0};
        int[] testArray2 = testArray;
        return testArray == testArray2;
    }

    public static int arrayLengthTest(){
        int[] testArray = {5, 4, 3, 2, 1, 0};
        return testArray.length;
    }

    public static int objectArrayTest(){
        SimpleMath[] testArray = {new SimpleMath(1,2), new SimpleMath(3, 5), new SimpleMath(10, 20)};
        testArray[0].x = 5;
        return testArray[0].x + testArray[1].x + testArray[2].x;
    }
    public static SimpleMath[] objectArrayTypeTest(){
        SimpleMath[] testArray = {new SimpleMath(1,2), new SimpleMath(3, 5), new SimpleMath(10, 20)};
        testArray[0].x = 5;
        return testArray;
    }

    public static int multiDArrayTest(){
        int[][] testArray = new int[4][6];
        testArray[0][0] = 2;
        testArray[1][2] = 3;
        testArray[3][3] = 5;
        return testArray[0][0] + testArray[1][2] + testArray[3][3] + testArray[2][2];
    }

    public static int d3ArrayTest(){
        int[][][] testArray = new int[2][3][4];
        testArray[0][0][0] = 2;
        testArray[1][1][1] = 2;
        testArray[0][2][3] = 2;
        return testArray[0][0][0] + testArray[1][1][1] + testArray[0][2][3] + testArray[0][1][0];
    }

    public static int d4ArrayTest(){
        int[][][][] testArray = new int[2][3][4][5];
        testArray[0][0][0][0] = 3;
        testArray[1][1][1][1] = 3;
        testArray[0][2][3][4] = 3;
        return testArray[0][0][0][0] + testArray[1][1][1][1] + testArray[0][2][3][4] + testArray[0][1][0][0];
    }

    public static int[][] arrayTypeTest(){
        int[][][][] testArray = new int[2][3][4][5];
        testArray[0][0][0][0] = 3;
        testArray[1][1][1][1] = 3;
        testArray[0][2][0][0] = 3;
        return testArray[0][2];
    }

    public static int exceptionTest(){
        int[] testArray = {0, 1, 2, 3};
        int x = 0;
        try{
            x = testArray[10];
        } catch (Exception e){
            x = 20;
        }
        return x;
    }

    /*public static int nullPointerTest(){
        SimpleMath math = null;
        try{
            return nullPointerHelper(math);
        } catch (Exception e){
            return 3;
        }


    }
    public static int nullPointerHelper(SimpleMath math){
        return math.x;
    }*/

    public static int nullTest(SimpleMath math){
        if (math == null){
            return 21;
        }
        else{
            return 32;
        }
    }

    public static int nullTestNull() {
        SimpleMath math = null;
        if (math == null){
            return 21;
        }
        else{
            return 32;
        }
    }
    public static int nullTestObj(){
        SimpleMath math = new SimpleMath();
        if (math == null){
            return 21;
        }
        else{
            return 32;
        }
    }

    public static int throwTest0(){
        int i = 0;
        try{
            return cantBeZero(i);
        }
        catch (TestExc exception) {
            return 100;
        }
    }
    public static int throwTest1(){
        int i = 1;
        try{
            return cantBeZero(i);
        }
        catch (TestExc exception) {
            return 100;
        }
    }
    public static int cantBeZero(int i) throws TestExc {
        if (i == 0){
            throw new TestExc("Value can't be zero");
        }
        return i;
    }
    public static int typeTest(){
        SimpleMath math = new ComplicatedMath(4);
        if (math instanceof SimpleMath){
            return 5;
        }
        else{
            return 0;
        }
    }

    public static int typeTest2(SimpleMath math){
        if(math instanceof SimpleMath){
            return 1;
        }
        else{
            return 0;
        }
    }
    public static int typeTest2Null(){
        SimpleMath math = null;
        if(math instanceof SimpleMath){
            return 1;
        }
        else{
            return 0;
        }
    }

    public static int stringTest(){
        String test = "Test";
        test += "22";
        if(test.equals("Test22")){
            return 1;
        }
        else{
            return 0;
        }
    }

    public static int stringTest2(){
        String test = "Test";
        String test2 = "test";
        test += test2;
        if(test.equals("Testtest")){
            return 1;
        }
        else{
            return 0;
        }
    }
    
    public static int stringBuilderTest(){
        StringBuilder test = new StringBuilder("Test");
        test.append(5);
        if(test.toString().equals("Test5")){
            return 1;
        }
        else{
            return 0;
        }
    }

    public static int stringBuilderTest2(){
        StringBuilder test = new StringBuilder("Test");
        test.append(3);
        if(test.toString().equals("Test5")){
            return 1;
        }
        else{
            return 0;
        }
    }

    public static int constantTest(){
        int x = 5;
        x = 10;
        return x;
    }

    public static int constantTest2(boolean flag){
        int x = 0;
        if(flag){
            x = 5;
        }
        else{
            x = 500;
        }
        return x;
    }

    public static int constantLoopTest(int flag){
        int x = 0;
        while(flag < 100){
            x++;
            flag += x;
        }
        return x;
    }

    public static int infiniteLoopTest(boolean flag){
        int x = 0;
        while(flag){
            x++;
        }
        return x;
    }

    public static int fibonacciTest(int n){
        if(n == 0){
            return 0;
        }
        if(n == 1){
            return 1;
        }
        return(fibonacciTest(n-1) + fibonacciTest(n-2));
    }

    public static void main(String[] args) {
        int test = 10;
        test = 20;
        SimpleMath math = new SimpleMath(3, 5);
        int add = math.add();
        int mul = math.mul();
        int num10 = math.return10();
        System.out.println(add + "\n" + mul + "\n" + num10);

        SimpleMath testMath = new ComplicatedMath(19, 5, 1);
        ComplicatedMath secondTestMath = new ComplicatedMath(5, 5, 1);
        AbsurdMath thirdTestMath = new AbsurdMath(5,5,5);


        int test1 = testMath.return10();
        int test2 = secondTestMath.return10();
        int test3 = math.return10();
        System.out.println(test1);
        System.out.println(test2);
        System.out.println(test3);
        testMath.getClass().getClassLoader();


        int x = 5;
        float y = 10.0f;
        int res1 = sub2(x, x);
        float res2 = subFloatFromInt(x, y);
        System.out.println(res1 + " " + res2);
        boolean testbool = false;
        System.out.println("TESTS STARTING HERE");
        System.out.println("--- BranchTest ---");
        System.out.println(branching(testbool));
        System.out.println(branching(true));
        System.out.println("--- ReturnTest ---");
        System.out.println(returnTest(true));
        System.out.println("--- ObjectTest ---");
        System.out.println(objectTest());
        System.out.println("--- InheritanceTest ---");
        System.out.println(inheritanceTest());
        System.out.println(inheritanceTest2());
        System.out.println(inheritanceTest3());
        System.out.println("--- objectCompTest ---");
        System.out.println(objectCompTestTrue());
        System.out.println(objectCompTestFalse());
        System.out.println("--- switchTest ---");
        System.out.println(switchTest(2));
        System.out.println(switchTest(200));
        System.out.println("--- nonCompactSwitchTest ---");
        System.out.println(nonCompactSwitchTest(200));
        System.out.println(nonCompactSwitchTest(201));
        System.out.println("--- staticVarTest ---");
        System.out.println(staticVarTest());
        System.out.println(staticVarTest());
        System.out.println("--- arrayTest ---");
        System.out.println(arrayTest());
        System.out.println(arrayTest2());
        for(int val : arrayTest3()){
            System.out.print(val);
            System.out.print("\t");
        }
        System.out.println();
        System.out.println(arrayCompTest());
        System.out.println(arrayLengthTest());
        System.out.println("--- objectArrayTest ---");
        System.out.println(objectArrayTest());
        System.out.println("--- multiDArrayTest ---");
        System.out.println(multiDArrayTest());
        System.out.println("--- d3ArrayTest ---");
        System.out.println(d3ArrayTest());
        System.out.println("--- d4ArrayTest ---");
        System.out.println(d4ArrayTest());
        System.out.println("--- interfaceTest ---");
        System.out.println(ComplicatedMath.interfaceTest());
        System.out.println(ComplicatedMath.defaultInterfaceTest());
        System.out.println("--- lambdaTest ---");
        System.out.println(ComplicatedMath.lambdaTest());
        System.out.println("--- exceptionTest ---");
        System.out.println(exceptionTest());
        //System.out.println(nullPointerTest());
        System.out.println(throwTest0());
        System.out.println("--- nullTest ---");
        SimpleMath nullMath = new SimpleMath(5,4);
        System.out.println(nullTest(null));
        System.out.println(nullTest(nullMath));
        System.out.println("--- typeTest ---");
        System.out.println(typeTest());
        System.out.println(ComplicatedMath.typeTestInterface());
        System.out.println(ComplicatedMath.typeTestArray());
        System.out.println(typeTest2(new SimpleMath(5,4)));
        System.out.println(typeTest2(null));
        System.out.println("--- stringTest ---");
        System.out.println(stringTest());
        System.out.println(stringTest2());
        //System.out.println(stringBuilderTest());
        System.out.println("--- fibonacciTest ---");
        System.out.println(fibonacciTest(8));


    }
}