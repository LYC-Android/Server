package mrcheng.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import bean.Trasmit;
import butterknife.ButterKnife;
import butterknife.InjectView;
import util.Constant;
import util.MyXFormatter;

/**
 * Created by mr.cheng on 2016/11/30.
 */
public class MPAndroidActivity extends Activity {
    @InjectView(R.id.chart1)
    LineChart mChart;
    @InjectView(R.id.xinlv)
    TextView mXinlv;
    @InjectView(R.id.battery)
    TextView mBattery;
    private Thread drawThread;
    private int index;
    private XAxis xl;
    private Socket remoteSocket;
    private ObjectInputStream objectInputStream;
    private List<Float> drawList = new ArrayList<>();
    private List<Float> TempList = new ArrayList<>();
    private int[] xinlv;
    private boolean Stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_mpandroid);
        ButterKnife.inject(this);
        initView();
        new Thread(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }).start();
    }

    /**
     * 设置MPAndroid的一些属性
     */
    private void initView() {
        // enable description text
        mChart.getDescription().setEnabled(false);


        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);//#########这里background

        // if disabled, scaling can be done on x- and y-axis separately
        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);


        LineData data = new LineData();
//        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);
        mChart.setExtraRightOffset(50);
        //还有轴没有设置
        Legend l = mChart.getLegend();
        l.setEnabled(false);
        xl = mChart.getXAxis();
        xl.setDrawGridLines(false);
        xl.setGranularity(1f);
        xl.setLabelCount(8, true);
        xl.setTextSize(16);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xl.setAvoidFirstLastClipping(true);
        xl.setValueFormatter(new MyXFormatter(0));
        YAxis leftAxis = mChart.getAxisLeft();
        //反转轴值
        leftAxis.setInverted(true);
        leftAxis.setEnabled(false);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
        feedMultiple();

    }

    /**
     * 使用Socket进行连接
     */
    private void connect() {
        try {
            String ip = getIntent().getStringExtra("ip");
            remoteSocket = new Socket(ip, Constant.TCP_PORT);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MPAndroidActivity.this, "连接成功,请耐心等待数据", Toast.LENGTH_SHORT).show();
                }
            });
            objectInputStream = new ObjectInputStream(new BufferedInputStream(remoteSocket.getInputStream()));
            Object object;
            while ((object = objectInputStream.readObject()) != null) {
                final Trasmit trasmit = (Trasmit) object;
                if (!trasmit.getIsOnline()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MPAndroidActivity.this, "该病人已取消连接", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBattery.setText("病人剩余电量 : " + trasmit.getBatteryInfo() + "%");
                    }
                });
                TempList.addAll(trasmit.getDatas());
                synchronized (drawList) {
                    if (drawList.size()!=0){
                        drawList.wait();
                    }
                    drawList.addAll(TempList);
                    TempList.clear();
                    drawList.notify();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (remoteSocket != null) {
                    remoteSocket.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 获得信息
     */
    private void feedMultiple() {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                addEntry();
            }
        };
        drawThread =new Thread(new Runnable() {
                @Override
                public void run() {

                    while (!Stop) {
                        try {
                            synchronized (drawList) {
                                if (drawList.size() == 0) {
                                    drawList.wait();
                                }
                                CaculateXinLv();
                                index = 0;
                                for (int i = 0; i < drawList.size(); i++) {
                                    runOnUiThread(runnable);
                                    try {
                                        Thread.sleep(8000 / 1200);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                                drawList.clear();
                                drawList.notify();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
        });
        drawThread.start();
    }

    private void addEntry() {
        LineData data = mChart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            data.addEntry(new Entry(set.getEntryCount(),(drawList.get(index) + 0f)), 0);
            data.notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(1200);
            mChart.setVisibleXRangeMinimum(1200);
            mChart.moveViewToX(data.getEntryCount());
            index++;
            if (index % 150 == 0) {
                mXinlv.setText(xinlv[(index / 150) - 1] + "/min");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (drawThread != null) {
            drawThread.interrupt();
        }
    }

    /**
     * 设置数据集
     *
     * @return
     */
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, null);
        set.setDrawCircleHole(false);
        set.setDrawHighlightIndicators(false);
        set.setLineWidth(3f);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setColor(Color.BLACK);
        return set;
    }

    /**
     * 计算心率的方法，返回一个长度为8的数组
     */
    private void CaculateXinLv() {
        int size = 4000;
        float[] mFloats = new float[size];
        float[] mDaoshu = new float[size];
        for (int i = 0; i < size; i++) {
            mFloats[i] = drawList.get(i);
        }

        mDaoshu[0] = mDaoshu[1] = mDaoshu[size - 1] = mDaoshu[size - 2] = 0;
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

        int fs = 500;
        int[] result2 = new int[result1.length];
        for (int i = 0; i < result1.length; i++) {
            result2[i] = (60 * fs) / result1[i];
        }
        //输出结果result2【】
        xinlv = result2;
    }

    /**
     * 捕捉back
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            ExitDialog(MPAndroidActivity.this).show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 提示退出系统
     *
     * @param context
     * @return
     */
    private Dialog ExitDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("系统信息");
        builder.setMessage("确定要退出吗?");
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        setResult(RESULT_OK);
                        Stop=true;
                        finish();
                    }
                });
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        return builder.create();
    }
}
