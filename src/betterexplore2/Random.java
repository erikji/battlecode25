package betterexplore2;

public class Random {
    public static int state;
    public static int rand() throws Exception {
        //xorshift32
        state ^= state << 13;
        state ^= state >> 17;
        state ^= state << 15;
        return state<0?~state:state;
    }
}
