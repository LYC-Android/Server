package util;

import android.content.Context;
import android.util.Log;
import android.view.WindowManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mrcheng.myapplication.R;

/**
 * Created by mr.cheng on 2016/11/22.
 */
public class ReadFile extends Thread {
    private final int N0;
    private final float N1;
    private int M;
    private int addNum;
    private ArrayList<Double> Result = new ArrayList<>();
    private static final String TAG = "ReadFile";
    private int[] xinlvdatas;
    private ArrayList<Integer> resultX = new ArrayList<>();
    private BufferedReader reader;
    private String mPath;
    private boolean isFinish;
    private String mLock;
    private ArrayList<Double> resultY = new ArrayList<>();

    @Override
    public void run() {
        super.run();
        openFile(mPath);
    }

    public ReadFile(Context context, String path, String lock) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        int mScreenWidth = wm.getDefaultDisplay().getWidth();
        int mScreenHeight = wm.getDefaultDisplay().getHeight();
        float xdpi = context.getResources().getDisplayMetrics().xdpi;
        Caculate caculate = new Caculate(xdpi, mScreenWidth, mScreenHeight);
        N0 = caculate.getN0();
        N1 = caculate.getN1();
        M = caculate.getM();
        addNum = caculate.getAddNum();
        mPath = path;
        mLock = lock;
    }


    private void openFile(String path) {
        try {
            List<Double> TempDoubles = new ArrayList<>();
            List<Double> mDoubls = new ArrayList<>();
            List<Double> mDatas = new ArrayList<>();

            reader = new BufferedReader(new FileReader(path));
            String result;
            int mycount = 4000;
            while ((result = reader.readLine()) != null) {
                TempDoubles.add(Double.valueOf(result));
                if (TempDoubles.size() >= mycount) {
                    synchronized (mLock) {
                        if (!ValueObject.value.equals("")) {
                            mLock.wait();
                        }
                        ArrayList<Double> XH = new ArrayList<>();
                        Result.clear();
                        resultX.clear();
                        resultY.clear();
                        for (int i = 0; i < mycount; i++) {
                            mDoubls.add(TempDoubles.get(i));
                            if (mDoubls.size() >= mycount / 8) {//Modified 每8秒的数据量为500*8=4000，要画出150*8=1200点

                                for (int h = 0; h < mDoubls.size(); h++) {
                                    mDatas.add(mDoubls.get(h));
                                    if (h > 0 && h % addNum == 0) {//Modified 每5点插值一次使得8秒数据量达到4800点
                                        mDatas.add((mDoubls.get(h - 1) + mDoubls.get(h + 1)) / 2);
                                    }
                                }
                                mDatas.add(mDoubls.get((mycount / 8) - 1));
                                for (int l = 0; l < mDatas.size(); l++) {
                                    if (l % M == 0) {//Modified 然后4倍抽取使得8秒画图点数为1200点
                                        XH.add(mDatas.get(l));
                                        Result.add((double) (N0 - (float) (mDatas.get(l) * N1)));
                                    }
                                }
                                mDoubls.clear();
                                mDatas.clear();
                            }
                        }
                        TempDoubles.clear();
                        xinlvdatas = CaculateXinLv(XH);
                        for (int i = 0; i < resultX.size(); i++) {
                            resultY.add(Result.get(resultX.get(i)));
                        }
                        ValueObject.value = "11";
                        mLock.notify();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            isFinish = true;
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 计算心率的方法，返回一个长度为8的数组
     *
     * @param drawList
     */
    private int[] CaculateXinLv(ArrayList<Double> drawList) {
        int size = 1200;
        double[] mFloats = new double[size];
        double[] mDaoshu = new double[size];
        for (int i = 0; i < size; i++) {
            mFloats[i] = drawList.get(i);
        }

        mDaoshu[0] = mDaoshu[1] = mDaoshu[size - 1] = mDaoshu[size - 2] = 0;
        for (int i = 2; i < mFloats.length - 2; i++) {
            mDaoshu[i] = mFloats[i + 1] - mFloats[i - 1] + 2 * (mFloats[i + 2] - mFloats[i - 2]);
        }
        //最大导数
        double max_daoshu = 0;
        for (int i = 0; i < mDaoshu.length; i++) {
            if (max_daoshu < mDaoshu[i]) {
                max_daoshu = mDaoshu[i];
            }
        }
        //存储点数的R波数组
        ArrayList<Integer> dianshuX = new ArrayList<>();
        double threshold = (max_daoshu * 0.19);
        for (int i = 0; i < mFloats.length - 1; i++) {
            if (mFloats[i] > threshold && ((mDaoshu[i] * mDaoshu[i + 1]) < 0 || ((mDaoshu[i] * mDaoshu[i - 1]) < 0))) {
                dianshuX.add(i);
            }
        }
        //计算斜线部分，局部变换法
        double[] dianshuY = new double[dianshuX.size()];
        for (int i = 0; i < dianshuY.length; i++) {
            dianshuY[i] = drawList.get(dianshuX.get(i));
        }


        CaculateQRSWave(drawList, dianshuX, dianshuY);


        int[] result1 = new int[dianshuX.size() - 1];
        for (int i = 0; i < dianshuX.size() - 1; i++) {
            result1[i] = dianshuX.get(i + 1) - dianshuX.get(i);
        }

        int fs = 150;
        int[] result2 = new int[result1.length];
        for (int i = 0; i < result1.length; i++) {
            result2[i] = (60 * fs) / result1[i];
        }
        //输出结果result2【】
        return result2;
    }

    /**
     * 计算QRS波的方法
     *
     * @param drawList
     * @param dianshuX
     * @param dianshuY
     */
    private void CaculateQRSWave(ArrayList<Double> drawList, ArrayList<Integer> dianshuX, double[] dianshuY) {
        if (dianshuX.get(0) < 13) {
            //第一个点小于40的时候，忽略第一个点
            resultX.add(dianshuX.get(0));
            double firstBack15Y = drawList.get(dianshuX.get(0) + 14);
            double firstTan = (dianshuY[0] - firstBack15Y) / 15;
            double[] firstBackTempDouble = new double[13];
            for (int j = 0; j < 13; j++) {
                firstBackTempDouble[j] = Math.abs((firstTan * (13 - j)) + firstBack15Y - drawList.get(dianshuX.get(0) + j + 1));
            }
            int firtMaxIndex = 0;
            double firstMax = firstBackTempDouble[0];
            for (int h = 0; h < firstBackTempDouble.length - 1; h++) {
                if (firstMax < firstBackTempDouble[h + 1]) {
                    firstMax = firstBackTempDouble[h + 1];
                    firtMaxIndex = h + 1;
                }
            }
            int FirstbackTempXresult = dianshuX.get(0) + firtMaxIndex;

            int FirstsecondIndex = 0;
            double FirstsecondMin = Math.abs(drawList.get(FirstbackTempXresult + 1) - drawList.get(FirstbackTempXresult + 2));
            for (int j = 0; j < 13 - firtMaxIndex; j++) {
                if (FirstsecondMin > (Math.abs(drawList.get(FirstbackTempXresult + j + 2) - drawList.get(FirstbackTempXresult + j + 3)))) {
                    FirstsecondIndex  = j + 1;
                    FirstsecondMin= Math.abs(drawList.get(FirstbackTempXresult + j + 2) - drawList.get(FirstbackTempXresult + j + 3));
                }
            }
            resultX.add(FirstbackTempXresult+ FirstsecondIndex  + 1);
            //*************************
            double[] foward13Y = new double[dianshuX.size() - 1];
            for (int i = 1; i < dianshuX.size(); i++) {
                foward13Y[i - 1] = drawList.get(dianshuX.get(i) - 12);
            }
            for (int i = 1; i < foward13Y.length+1; i++) {
                double tan = (dianshuY[i] - foward13Y[i]) / 12;
                double[] tempResult = new double[11];
                for (int j = 0; j < 11; j++) {
                    tempResult[j] = Math.abs((tan * (j + 1)) + foward13Y[i] - drawList.get(dianshuX.get(i) - 11 + j));
                }

                int MaxIndex = 0;
                double max = tempResult[0];
                for (int h = 0; h < tempResult.length - 1; h++) {
                    if (max < tempResult[h + 1]) {
                        max = tempResult[h + 1];
                        MaxIndex = h + 1;
                    }
                }
                int tempXresult = dianshuX.get(i) - 11 + MaxIndex;

                int secondIndex = 0;
                double secondMin = Math.abs(drawList.get(tempXresult - 1) - drawList.get(tempXresult - 2));
                for (int j = 0; j < MaxIndex - 1; j++) {
                    if (secondMin > (Math.abs(drawList.get(tempXresult - j - 2) - drawList.get(tempXresult - j - 3)))) {
                        secondIndex = j + 1;
                        secondMin = Math.abs(drawList.get(tempXresult - j - 2) - drawList.get(tempXresult - j - 3));
                    }
                }
                resultX.add(tempXresult - secondIndex - 1);
                //加上顶点
                resultX.add(dianshuX.get(i));
                //算完第一个点之后应该算的是后面的15个点。
                if (dianshuX.get(i) <= 1185) {
                    double back15Y = drawList.get(dianshuX.get(i) + 14);
                    tan = (dianshuY[i] - back15Y) / 15;
                    double[] backTempDouble = new double[13];
                    for (int j = 0; j < 13; j++) {
                        backTempDouble[j] = Math.abs((tan * (13 - j)) + back15Y - drawList.get(dianshuX.get(i) + j + 1));
                    }

                    MaxIndex = 0;
                    max = backTempDouble[0];
                    for (int h = 0; h < backTempDouble.length - 1; h++) {
                        if (max < backTempDouble[h + 1]) {
                            max = backTempDouble[h + 1];
                            MaxIndex = h + 1;
                        }
                    }
                    int backTempXresult = dianshuX.get(i) + MaxIndex;

                    secondIndex = 0;
                    secondMin = Math.abs(drawList.get(backTempXresult + 1) - drawList.get(backTempXresult + 2));
                    for (int j = 0; j < 13 - MaxIndex; j++) {
                        if (secondMin > (Math.abs(drawList.get(backTempXresult + j + 2) - drawList.get(backTempXresult + j + 3)))) {
                            secondIndex = j + 1;
                            secondMin = Math.abs(drawList.get(backTempXresult + j + 2) - drawList.get(backTempXresult + j + 3));
                        }
                    }
                    resultX.add(backTempXresult + secondIndex + 1);
                }
            }
        } else {
            //***********************************************
            double[] foward13Y = new double[dianshuX.size()];
            for (int i = 0; i < dianshuX.size(); i++) {
                foward13Y[i] = drawList.get(dianshuX.get(i) - 12);
            }
            for (int i = 0; i < foward13Y.length; i++) {
                double tan = (dianshuY[i] - foward13Y[i]) / 12;
                double[] tempResult = new double[11];
                for (int j = 0; j < 11; j++) {
                    tempResult[j] = Math.abs((tan * (j + 1)) + foward13Y[i] - drawList.get(dianshuX.get(i) - 11 + j));
                }

                int MaxIndex = 0;
                double max = tempResult[0];
                for (int h = 0; h < tempResult.length - 1; h++) {
                    if (max < tempResult[h + 1]) {
                        max = tempResult[h + 1];
                        MaxIndex = h + 1;
                    }
                }
                int tempXresult = dianshuX.get(i) - 11 + MaxIndex;

                int secondIndex = 0;
                double secondMin = Math.abs(drawList.get(tempXresult - 1) - drawList.get(tempXresult - 2));
                for (int j = 0; j < MaxIndex - 1; j++) {
                    if (secondMin > (Math.abs(drawList.get(tempXresult - j - 2) - drawList.get(tempXresult - j - 3)))) {
                        secondIndex = j + 1;
                        secondMin = Math.abs(drawList.get(tempXresult - j - 2) - drawList.get(tempXresult - j - 3));
                    }
                }
                resultX.add(tempXresult - secondIndex - 1);
                //加上顶点
                resultX.add(dianshuX.get(i));
                //算完第一个点之后应该算的是后面的15个点。
                if (dianshuX.get(i) <= 1185) {
                    double back15Y = drawList.get(dianshuX.get(i) + 14);
                    tan = (dianshuY[i] - back15Y) / 15;
                    double[] backTempDouble = new double[13];
                    for (int j = 0; j < 13; j++) {
                        backTempDouble[j] = Math.abs((tan * (13 - j)) + back15Y - drawList.get(dianshuX.get(i) + j + 1));
                    }

                    MaxIndex = 0;
                    max = backTempDouble[0];
                    for (int h = 0; h < backTempDouble.length - 1; h++) {
                        if (max < backTempDouble[h + 1]) {
                            max = backTempDouble[h + 1];
                            MaxIndex = h + 1;
                        }
                    }
                    int backTempXresult = dianshuX.get(i) + MaxIndex;

                    secondIndex = 0;
                    secondMin = Math.abs(drawList.get(backTempXresult + 1) - drawList.get(backTempXresult + 2));
                    for (int j = 0; j < 13 - MaxIndex; j++) {
                        if (secondMin > (Math.abs(drawList.get(backTempXresult + j + 2) - drawList.get(backTempXresult + j + 3)))) {
                            secondIndex = j + 1;
                            secondMin = Math.abs(drawList.get(backTempXresult + j + 2) - drawList.get(backTempXresult + j + 3));
                        }
                    }
                    resultX.add(backTempXresult + secondIndex + 1);
                }
            }
        }
    }

    public int[] getXinlvdatas() {
        return xinlvdatas;
    }

    public ArrayList<Integer> getResultX() {
        return resultX;
    }

    public ArrayList<Double> getResult() {
        return Result;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public ArrayList<Double> getResultY() {
        return resultY;
    }
}
