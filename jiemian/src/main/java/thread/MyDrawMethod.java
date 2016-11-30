package thread;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import util.Caculate;

/**
 * Created by mr.cheng on 2016/8/7.
 */
public class MyDrawMethod {
    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private Paint mPaint;
    private int mScreenWidth, mScreenHeight;
    private int start;
    private int oldX;
    private float oldY;
    private int N0;
    private int counter;
    private float N1;
    private TextView one, two, three, four, five, six, seven, eight, xinlv;
    List<Float> mDraws = new ArrayList<>();
    List<Float> TempDoubles = new ArrayList<>();
    List<Float> mDoubls = new ArrayList<>();
    private Handler mHandler;
    private SimpleDateFormat format;
    private Date date;
    private int[] xinlvdatas=new int[8];

    public MyDrawMethod(Context context, SurfaceView sfv, TextView eight, TextView five,
                        TextView four, TextView two, TextView three,
                        TextView six, TextView one, TextView seven,
                        TextView xinlv, Handler handler) {
        this.eight = eight;
        this.five = five;
        this.four = four;
        this.two = two;
        this.three = three;
        this.six = six;
        this.one = one;
        this.seven = seven;
        this.xinlv = xinlv;
        this.mHandler = handler;
        mHolder = sfv.getHolder();
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);// 画笔为绿色
        mPaint.setStrokeWidth((float) 2.5);// 设置画笔粗细
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);//设置上抗锯齿，自己加的不知道有没有必要咯
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        mScreenWidth = wm.getDefaultDisplay().getWidth();//获得屏幕的宽
        mScreenHeight = wm.getDefaultDisplay().getHeight();//获得屏幕的高

        float xdpi = context.getResources().getDisplayMetrics().xdpi;
        Caculate caculate = new Caculate(xdpi, mScreenWidth, mScreenHeight);
        N0 = caculate.getN0();
        N1 = caculate.getN1();
    }

    public void GoToDraw(List<Float> recbuf) {
        xinlvdatas=CaculateXinLv((ArrayList<Float>) recbuf);
        for (int k = 0; k < recbuf.size(); k++) {
            TempDoubles.add(recbuf.get(k));
            if (TempDoubles.size() >= 1024) {
                for (int h = 0; h < TempDoubles.size(); h++) {
                    mDoubls.add(TempDoubles.get(h));
                    if (h > 0 && h % 38 == 0) {
                        mDoubls.add((TempDoubles.get(h - 1) + TempDoubles.get(h + 1)) / 2);
                    }
                }
                for (int l = 0; l < mDoubls.size(); l++) {
                    if (l % 7 == 0) {
                        mDraws.add(mDoubls.get(l));
                    }
                }

                Float[] tt = new Float[30];
                for (int c = 0; c < 5; c++) {
                    for (int d = 0; d < 30; d++) {
                        tt[d] = mDraws.get(d + 30 * c);
                    }
                    MyDraw(tt);
                    start = tt.length + start;
                    if (start >= mScreenWidth) {
                        start = 0;
                    }
                }
                mDraws.clear();
                mDoubls.clear();
                TempDoubles.clear();
            }
        }

    }

    private void MyDraw(Float[] buf) {
        try {
            counter++;
            if (start == 0) oldX = 0;//如果X返回了0点，那么旧的X自然也要是0点了
            //获得canvas对象，并且规定只能在一个矩形(上面参数分别对应上下左右的坐标)上画图
            mCanvas = mHolder.lockCanvas(new Rect(start, 0, start + buf.length, mScreenHeight));
            mCanvas.drawColor(Color.WHITE);//设置如果上面已经有图了，那么就用黑色覆盖它再画图
            for (int i = 0; i < buf.length; i++) {
                int x = i + start;//X坐标，
                float y = (float) (N0 - (buf[i] * N1));
                mCanvas.drawLine(oldX, oldY, x, y, mPaint);
                oldX = x;
                oldY = y;

            }//都画完了
            mHolder.unlockCanvasAndPost(mCanvas);
            try {
                Thread.sleep(200);//线程睡眠1S，
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (counter > 0 && counter % 5 == 0) {
                updateUI(counter / 5);
                if (counter == 40) counter = 0;
            }
        } catch (Exception e) {
        }
    }

    private void updateUI(final int i) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                switch (i) {
                    case 1:
                        one.setText(getCurrentTime());
                        break;
                    case 2:
                        two.setText(getCurrentTime());
                        break;
                    case 3:
                        three.setText(getCurrentTime());
                        break;
                    case 4:
                        four.setText(getCurrentTime());
                        break;
                    case 5:
                        five.setText(getCurrentTime());
                        break;
                    case 6:
                        six.setText(getCurrentTime());
                        break;
                    case 7:
                        seven.setText(getCurrentTime());
                        break;
                    case 8:
                        eight.setText(getCurrentTime());
                        break;
                }
                xinlv.setText(xinlvdatas[i-1]+"/min");
            }
        });
    }

    private String getCurrentTime() {
        format = new SimpleDateFormat("HH:mm:ss");
        date = new Date(System.currentTimeMillis());
        return format.format(date);
    }

    private int[] CaculateXinLv(ArrayList<Float> datas) {
        float[] mFloats = new float[datas.size()];
        float[] mDaoshu = new float[datas.size()];
        for (int i = 0; i < datas.size(); i++) {
            mFloats[i] = datas.get(i);
        }

        mDaoshu[0] = mDaoshu[1] = mDaoshu[datas.size()-1] = mDaoshu[datas.size()-2] = 0;
        for (int i = 2; i < mFloats.length - 2; i++) {
            mDaoshu[i] = mFloats[i + 1] - mFloats[i - 1] + 2 * (mFloats[i + 2] - mFloats[i - 2]);
        }
        //最大导数
        float max_daoshu = 0;
        for (int i = 0; i < mDaoshu.length; i++) {
            if (max_daoshu < mDaoshu[i]) {
                max_daoshu = mDaoshu[i];
            }
        }
        //存储点数的R波数组
        ArrayList<Integer> dianshu = new ArrayList<>();
        float threshold = (float) (max_daoshu * 0.375);
        for (int i = 0; i < mFloats.length - 1; i++) {
            if (mFloats[i] > threshold && (mDaoshu[i] * mDaoshu[i + 1]) < 0) {
                dianshu.add(i);
            }
        }

        int[] result1 = new int[dianshu.size() - 1];
        for (int i = 0; i < dianshu.size() - 1; i++) {
            result1[i] = dianshu.get(i + 1) - dianshu.get(i);
        }

        int fs = 1024;
        int[] result2 = new int[result1.length];
        for (int i = 0; i < result1.length; i++) {
            result2[i] = (60 * fs) / result1[i];
        }

        //输出结果result2【】
        return result2;
    }
}
