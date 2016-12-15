package mrcheng.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.listener.ValueEventListener;
import util.Caculate;
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
    @InjectView(R.id.realName)
    TextView mRealName;
    @InjectView(R.id.age)
    TextView mAge;
    private Thread drawThread;
    BmobRealTimeData rtd = new BmobRealTimeData();
    private int index;
    private static final String TAG = "MPAndroidActivity";
    private int N0;
    private float N1;
    private XAxis xl;
    private List<Float> drawList = new ArrayList<>();
    private List<Float> TempList = new ArrayList<>();
    private int[] xinlv;
    private boolean Stop;
    private ILineDataSet set;
    private int DataComeCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpandroid);
        ButterKnife.inject(this);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowManager wm = (WindowManager)
                getSystemService(Context.WINDOW_SERVICE);
        int mScreenWidth = wm.getDefaultDisplay().getWidth();
        int mScreenHeight = wm.getDefaultDisplay().getHeight();
        float xdpi = getResources().getDisplayMetrics().xdpi;
        Caculate caculate = new Caculate(xdpi, mScreenWidth, mScreenHeight);
        N0 = caculate.getN0();
        N1 = caculate.getN1();
        connect();
        initView();
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
        xl.setLabelCount(9, true);
        xl.setTextSize(16);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xl.setAvoidFirstLastClipping(true);

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
        rtd.start(MPAndroidActivity.this, new ValueEventListener() {
            @Override
            public void onConnectCompleted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MPAndroidActivity.this, "连接成功,请耐心等待数据", Toast.LENGTH_SHORT).show();
                    }
                });
                if (rtd.isConnected()) {
                    rtd.subTableUpdate("Trasmit");
                }
            }

            @Override
            public void onDataChange(JSONObject jsonObject) {
                try {
                    JSONObject object = jsonObject.getJSONObject("data");
                    if (!object.getBoolean("isOnline")) {
                        rtd.unsubTableUpdate("Trasmit");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MPAndroidActivity.this, "该病人已经取消连接", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Stop = true;
                        return;
                    }
                    if (object.getString("batteryInfo") != null) {
                        mBattery.setText("病人剩余电量 : " + object.getString("batteryInfo") + "%");
                    } else {
                        mBattery.setText("病人剩余电量 : --");
                    }
                    JSONArray array = object.getJSONArray("datas");
                    for (int i = 0; i < array.length(); i++) {
                        TempList.add((float) array.getDouble(i));
                    }
                    synchronized (drawList) {
                        Log.d(TAG, drawList.size() + "");
                        if (drawList.size() != 0) {
                            drawList.wait();
                        }
                        drawList.addAll(TempList);
                        TempList.clear();
                        DataComeCounter = DataComeCounter + drawList.size();
                        drawList.notify();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

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
        drawThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (!Stop) {
                    try {
                        synchronized (drawList) {
                            if (drawList.size() == 0) {
                                drawList.wait();
                            }
                            CaculateXinLv();
                            HandlerDatas();
                            index = 0;
                            for (int i = 0; i < drawList.size(); i++) {
                                index = i;
                                runOnUiThread(runnable);
                                try {
                                    Thread.sleep(8000 / 1200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            while (!((set.getEntryCount()) == DataComeCounter)) {
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
        xl.setValueFormatter(new MyXFormatter(0));
        drawThread.start();
    }

    private void addEntry() {
        LineData data = mChart.getData();
        if (data != null) {
            set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            data.addEntry(new Entry(set.getEntryCount(), (drawList.get(index) + 0f)), 0);
            data.notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(1200);
            mChart.setVisibleXRangeMinimum(1200);
            mChart.moveViewToX(data.getEntryCount());
            if (index % 150 == 0 && index > 0) {
                mXinlv.setText("心率:" + xinlv[(index / 150) - 1] + "/min");
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
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
        builder.setTitle("提示");
        builder.setMessage("确定要退出吗?");
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        setResult(RESULT_OK);
                        Stop = true;
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


    private void HandlerDatas() {

        List<Float> TempDoubles = new ArrayList<>();
        List<Float> mDoubls = new ArrayList<>();
        List<Float> Result = new ArrayList<>();

        for (int i = 0; i < drawList.size(); i++) {
            TempDoubles.add(drawList.get(i));
            if (TempDoubles.size() >= drawList.size() / 8) {//Modified 每8秒的数据量为500*8=4000，要画出150*8=1200点
                for (int h = 0; h < TempDoubles.size(); h++) {
                    mDoubls.add(TempDoubles.get(h));
                    if (h > 0 && h % 5 == 0) {//Modified 每5点插值一次使得8秒数据量达到4800点
                        mDoubls.add((TempDoubles.get(h - 1) + TempDoubles.get(h + 1)) / 2);
                    }
                }
                mDoubls.add(TempDoubles.get((drawList.size() / 8) - 1));
                for (int l = 0; l < mDoubls.size(); l++) {
                    if (l % 4 == 0) {//Modified 然后4倍抽取使得8秒画图点数为1200点
                        Result.add((N0 - (float) (mDoubls.get(l) * N1)));
                    }
                }
                mDoubls.clear();
                TempDoubles.clear();
            }
        }
        drawList.clear();
        drawList.addAll(Result);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        }
    }

    @OnClick(R.id.commit)
    public void onClick() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MPAndroidActivity.this);
        dialog.setTitle("提示");
        dialog.setMessage("结束此次心电传输，并填写诊断报告吗?");
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(RESULT_OK);
                Stop = true;
                finish();
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }
}
