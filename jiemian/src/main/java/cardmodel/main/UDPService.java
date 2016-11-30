package cardmodel.main;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

import bean.MyUser;
import cn.bmob.v3.BmobUser;
import mrcheng.myapplication.MainActivity;
import util.Constant;

/**
 * Created by mr.cheng on 2016/10/27.
 */
public class UDPService extends Thread {
    private Context mContext;
    private static final int BUF_SIZE = 30;
    private String mCardNumber;
    private byte[] localIpBytes;
    private byte[] regBuffer = new byte[BUF_SIZE];
    private MulticastSocket multicastSocket;
    private String mObjectId;
private Handler mHandler;

    public UDPService(String cardNumber, Context context, String objectId, Handler handler) {
        this.mCardNumber = cardNumber;
        this.mContext = context;
       this.mObjectId=objectId;
        this.mHandler=handler;
    }

    @Override
    public void run() {
        try {
            super.run();
            byte[] sendBuffer = mCardNumber.getBytes();
            for (int i = 0; i < sendBuffer.length; i++) {
                regBuffer[i] = sendBuffer[i];
            }
            getIp();
            multicastSocket = new MulticastSocket(Constant.UDP_PORT);
            multicastSocket.joinGroup(InetAddress.getByName(Constant.MULTICAST_IP));
            this.findSicker();
            while (!multicastSocket.isClosed() && null != multicastSocket) {
                byte[] recvBuffer = new byte[BUF_SIZE];
                for (int i = 0; i < BUF_SIZE; i++) {
                    recvBuffer[i] = 0;
                }
                DatagramPacket rdp = new DatagramPacket(recvBuffer, recvBuffer.length);
                multicastSocket.receive(rdp);
                parsePackage(recvBuffer);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void parsePackage(byte[] recvBuffer) throws UnknownHostException {
        byte[] bytes = new byte[4];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = recvBuffer[i];
        }
        String result = new String(bytes);
        if (result.equals(Constant.isMe)) {
            this.closeUdpSocekt();
            byte[] ipBytes = new byte[4];// 获得请求方的ip地址
            System.arraycopy(recvBuffer, 20, ipBytes, 0, 4);
            InetAddress targetIp = InetAddress.getByAddress(ipBytes);
            String IP = targetIp.getHostAddress().toString();
           Intent intent=new Intent(mContext,MainActivity.class);
            intent.putExtra("ip",IP);
            intent.putExtra("objectId",mObjectId);
            mContext.startActivity(intent);
        }else if (result.equals(Constant.Refuse)){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "对方拒绝了", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void getIp() throws IOException {
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress()) {
                    if (inetAddress.isReachable(1000)) {
                        localIpBytes = inetAddress.getAddress();
                        MyUser myUser = BmobUser.getCurrentUser(mContext, MyUser.class);
                        byte[] bytes=myUser.getObjectId().getBytes();
                        System.arraycopy(bytes,0,regBuffer,9,10);
                        System.arraycopy(localIpBytes, 0, regBuffer, 20, 4);
                    }
                }
            }
        }
    }

    private void closeUdpSocekt() {
        if (multicastSocket.isClosed() && null != multicastSocket) {
            try {
                multicastSocket.leaveGroup(InetAddress.getByName(Constant.MULTICAST_IP));
                multicastSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void findSicker() {
        try {
            if (null != multicastSocket && !multicastSocket.isClosed()) {
                DatagramPacket dp = new DatagramPacket(regBuffer, BUF_SIZE, InetAddress.getByName(Constant.MULTICAST_IP), Constant.UDP_PORT);
                multicastSocket.send(dp);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "已发送请求", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        } catch (IOException e) {
            e.printStackTrace();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "发送请求失败", Toast.LENGTH_SHORT).show();

                }
            });

        }
    }
}
