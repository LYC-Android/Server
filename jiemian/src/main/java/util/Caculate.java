package util;

/**
 * 计算各种东西
 */

/*Edited by John Fan  12/08/2016*/

public class Caculate {
    float xDpi;
    int mScreenWidth;
    int mScreenHeight;
    private int Horization_line;
    private int Vertical_line;
    private int Vmax;
    private int NumPerMM;
    private int N0;
    //private double factor;
    private static final int fs=500;
    private int M;
    private int addNum;
    private int Pdot;

    public Caculate(float xDpi, int mScreenWidth, int mScreenHeight) {
        this.xDpi = xDpi;
        this.mScreenWidth = mScreenWidth;
        this.mScreenHeight = mScreenHeight;
        NumPerMM=(int) (xDpi/25.4);
        Vertical_line=mScreenWidth/NumPerMM;
        Horization_line=mScreenHeight/NumPerMM;
        Vmax=(int)(Horization_line/20.0+1);
        N0=10*Vmax*NumPerMM;
        Pdot=25*NumPerMM;
        M=1+(fs-fs%Pdot)/Pdot;
        addNum = fs/(Pdot-fs%Pdot);
    }


    public int getM() {
        return M;
    }


    public int getVertical_line() {

        return Vertical_line;
    }

    public int getAddNum() {
        return addNum;
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
    public float getN1(){
        return (float)(10*NumPerMM);//(factor*N0/Vmax)*1);
    }
}
