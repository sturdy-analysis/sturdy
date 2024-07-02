public class AbsurdMath extends ComplicatedMath {

    int x = 0;
    int y = 0;
    public AbsurdMath(int x, int y, int z){
        super(x,y,z);
    }

    @Override
    public int add() {
        return this.x + this.y + this.z;
    }
}
