package util;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by mr.cheng on 2016/10/29.
 */
public class TCPService extends Service {
    public static Socket remoteSocket;
    private ServerSocket serverSocket;
    private boolean flag = true;
    private boolean flag2=true;
    private static ObjectInputStream objectInputStream;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (flag2) {
                    try {
                        flag=true;
                        serverSocket = new ServerSocket(Constant.TCP_PORT);
                        remoteSocket = serverSocket.accept();
                        Object object;
                        objectInputStream = new ObjectInputStream(new BufferedInputStream(remoteSocket.getInputStream()));
                        while ((object=objectInputStream.readObject())!=null){

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            serverSocket.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public void setFlag2(boolean flag2) {
        this.flag2 = flag2;
    }

    public static Socket getRemoteSocket() {
        return remoteSocket;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (remoteSocket!=null){
                remoteSocket.close();
            }
            if (serverSocket!=null){
                serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }
}