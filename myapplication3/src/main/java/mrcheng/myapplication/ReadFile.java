package mrcheng.myapplication;

import android.content.Context;
import android.view.WindowManager;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mr.cheng on 2016/11/22.
 */
public class ReadFile {
    private final int N0;
    private final float N1;
    private FileInputStream fis;
    private BufferedInputStream bis;
    private int lenchunkdescriptor = 4;
    private int lenwaveflag = 4;
    private int lenfmtubchunk = 4;
    private int lendatasubchunk = 4;
    private ArrayList<Double> Result = new ArrayList<>();

    public ReadFile(Context context, String path) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        int mScreenWidth = wm.getDefaultDisplay().getWidth();
        int mScreenHeight = wm.getDefaultDisplay().getHeight();
        float xdpi = context.getResources().getDisplayMetrics().xdpi;
        Caculate caculate = new Caculate(xdpi, mScreenWidth, mScreenHeight);
        N0 = caculate.getN0();
        N1 = caculate.getN1();
        this.read(path, context);
    }

    static {
        System.loadLibrary("myNativeLib");
    }

    public void read(String filename, Context context) {
        MyThread myThread = new MyThread(context);
        try {
            fis = new FileInputStream(filename);
            bis = new BufferedInputStream(fis);

            String chunkdescriptor = readString(lenchunkdescriptor);
            if (!chunkdescriptor.endsWith("RIFF"))  //0~3检查前四个字节是否为RIFF
                throw new IllegalArgumentException("RIFF miss, " + filename + " is not a wave file.");

            long chunksize = readLong();  //4~7代表大小
            String waveflag = readString(lenwaveflag);
            if (!waveflag.endsWith("WAVE"))  //8~11检查是不是WAVE这四个字节
                throw new IllegalArgumentException("WAVE miss, " + filename + " is not a wave file.");

            String fmtubchunk = readString(lenfmtubchunk);
            if (!fmtubchunk.endsWith("fmt "))  //12~15 fmt"" 检查是不是这个
                throw new IllegalArgumentException("fmt miss, " + filename + " is not a wave file.");

            long subchunk1size = readLong();  //16~19代表PCM数据
            int audioformat = readInt();  //20~21fmt的头
            int numchannels = readInt();  //22~23单声道还是双声道
            long samplerate = readLong();  //24~27采样率
            long byterate = readLong();  //28~31每秒播放字节数
            int blockalign = readInt();  //32~33采样一次占字节数
            int bitspersample = readInt();  //34~35量化数 8或者16

            String datasubchunk = readString(lendatasubchunk);
            if (!datasubchunk.endsWith("data"))  //36~39肯定是data
                throw new IllegalArgumentException("data miss, " + filename + " is not a wave file.");
            long subchunk2size = readLong();  //40~43采样数据字节数

            int len = (int) (subchunk2size / (bitspersample / 8) / numchannels);

            int length;
            byte[] buf = new byte[len * 2];
            double[] doubles = new double[65536];
            for (int i = 0; i < len; ++i) {
                for (int n = 0; n < numchannels; ++n) {
                    while ((length = bis.read(buf, 0, buf.length)) != -1) {
                        short[] shorts = byteArray2ShortArray(buf, buf.length / 2);
                        myThread.getStringFromNative(shorts, doubles);
                        //这里是写文件的方法
                        //**********************************************************************//
                        //结束
                        List<Double> TempDoubles = new ArrayList<>();
                        List<Double> mDoubls = new ArrayList<>();
                        for (int k = 0; k < doubles.length / 8; k++) {
                            TempDoubles.add(doubles[k]);
                            if (TempDoubles.size() > 1024) {
                                for (int h = 0; h < TempDoubles.size(); h++) {
                                    mDoubls.add(TempDoubles.get(h));
                                    if (h > 0 && h % 38 == 0) {
                                        mDoubls.add((TempDoubles.get(h - 1) + TempDoubles.get(h + 1)) / 2);
                                    }
                                }
                                for (int l = 0; l < mDoubls.size(); l++) {
                                    if (l % 7 == 0) {
                                        Result.add((double) (N0 - (float) (mDoubls.get(l) * N1)));
                                    }
                                }

                                mDoubls.clear();
                                TempDoubles.clear();
                            }
                        }

                    }


                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null)
                    bis.close();
                if (fis != null)
                    fis.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }


    private String readString(int len) {
        byte[] buf = new byte[len];
        try {
            if (bis.read(buf) != len)
                throw new IOException("no more data!!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(buf);
    }

    private long readLong() {
        long res = 0;
        try {
            long[] l = new long[4];
            for (int i = 0; i < 4; ++i) {
                l[i] = bis.read();
                if (l[i] == -1) {
                    throw new IOException("no more data!!!");
                }
            }
            res = l[0] | (l[1] << 8) | (l[2] << 16) | (l[3] << 24);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    private int readInt() {
        byte[] buf = new byte[2];
        int res = 0;
        try {
            if (bis.read(buf) != 2)
                throw new IOException("no more data!!!");
            res = (buf[0] & 0x000000FF) | (((int) buf[1]) << 8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public short[] byteArray2ShortArray(byte[] data, int items) {
        short[] retVal = new short[items];
        for (int i = 0; i < retVal.length; i++)
            retVal[i] = (short) ((data[i * 2] & 0xff) | (data[i * 2 + 1] & 0xff) << 8);

        return retVal;
    }

    public ArrayList<Double> getResult() {
        return Result;
    }
}
