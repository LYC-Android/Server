package util;

/**
 * 计算各种东西
 */
public class Caculate {
    float xDpi;
    int mScreenWidth;
    int mScreenHeight;
    private int Horization_line;
    private int Vertical_line;
    private int NumPerMM;
    private int N0;
    private double factor;
    private double Temp;
    private float Temp1;
    private static final int G = 200;

    public Caculate(float xDpi, int mScreenWidth, int mScreenHeight) {
        this.xDpi = xDpi;
        this.mScreenWidth = mScreenWidth;
        this.mScreenHeight = mScreenHeight;
        NumPerMM = (int) (xDpi / 25.4);
        Vertical_line = mScreenWidth / NumPerMM;
        Horization_line = mScreenHeight / NumPerMM;
        int Vmax = (Horization_line / 20) + 1;
        factor = Vmax / 5.0;
        N0 = 10 * Vmax * NumPerMM;
        Temp = N0 / 32768.0;
        Temp1 = 32768000 / Vmax;

    }


    public int getVertical_line() {

        return Vertical_line;
    }

    public int getNumPerMM() {
        return NumPerMM;
    }

    public int getHorization_line() {

        return Horization_line;
    }

    public int getN0() {

        return N0;
    }

    public float getN1() {
        return (float) (int) ((factor * Temp * Temp1 / G) + 0.5);
    }
}
