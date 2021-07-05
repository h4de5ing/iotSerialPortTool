import gnu.io.SerialPort;
import utils.HttpRequest;
import utils.NetUtils;
import utils.SerialPortManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//代码参考 https://github.com/alidili/SerialPortDemo
//java -classpath SPDemo.jar Main COM4 9600 127.0.0.1:3001
public class Main {
    static String mac = "";
    static String server = "127.0.0.1:3001";

    public static void main(String[] args) throws Exception {
        try {
            mac = NetUtils.getLocalMac();
            System.out.println("MAC:" + mac);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SerialPort serialPort = SerialPortManager.openPort(args[0], Integer.parseInt(args[1]));
        if (!args[2].isEmpty()) {
            server = args[2];
        }
        assert serialPort != null;
        SerialPortManager.addListener(serialPort, () -> {
            byte[] data = SerialPortManager.readFromPort(serialPort);
            try {
                //System.out.println(new String(data));
                byte[] temp = new byte[5];
                System.arraycopy(data, 5, temp, 0, temp.length);
                byte[] hum = new byte[5];
                System.arraycopy(data, 15, hum, 0, hum.length);
                upload(new String(temp), new String(hum));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    static long currentTime = 0L;

    public static void upload(String temp, String hub) {
        if (System.currentTimeMillis() - currentTime >= 1000) {//一秒以内的数据屏蔽掉
            try {
                System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + " 温度:" + temp + " 湿度:" + hub);
                currentTime = System.currentTimeMillis();
                Map<String, String> param = new HashMap<>();
                param.put("mac", mac);
                param.put("temp", temp);
                param.put("hum", hub);
                HttpRequest.sendPost("http://" + server + "/insert", param, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
