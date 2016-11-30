package thread;

import java.util.List;

/**
 * Created by mr.cheng on 2016/8/24.
 */
public class MyDrawThread extends Thread {
    private MyDrawMethod mMethod;
    private List<Float> recbuf;
    private boolean stop = true;
    private boolean haveDatas = true;

    public MyDrawThread(MyDrawMethod mMethod, List<Float> recbuf) {
        this.mMethod = mMethod;
        this.recbuf = recbuf;
    }

    @Override
    public void run() {
        super.run();
        while (stop) {
            if (!haveDatas) {
                haveDatas = true;
                synchronized (recbuf) {
                    mMethod.GoToDraw(recbuf);
                    recbuf.clear();
                }
            }
        }

    }


    public void setHaveDatas(boolean haveDatas) {
        this.haveDatas = haveDatas;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
