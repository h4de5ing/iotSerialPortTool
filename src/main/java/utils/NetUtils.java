package utils;

import java.net.InetAddress;
import java.net.NetworkInterface;

public class NetUtils {
    public static String getLocalMac() throws Exception {
        InetAddress ia = InetAddress.getLocalHost();
        System.out.println(ia.getHostName());
        System.out.println(ia.getHostAddress());
        byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            if (i != 0) sb.append(":");
            int temp = mac[i] & 0xff;
            String str = Integer.toHexString(temp);
            if (str.length() == 1) {
                sb.append("0").append(str);
            } else {
                sb.append(str);
            }
        }
        return sb.toString().toUpperCase();
    }
}
