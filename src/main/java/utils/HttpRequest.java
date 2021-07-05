package utils;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HttpRequest {

    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url    发送请求的URL
     * @param params 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, Map<String, String> params, Map<String, String> header) throws Exception {
        String result = "";
        BufferedReader in = null;
        String param = "";
        if (params != null && params.size() > 0) {
            param = param + "?";
            for (String key : params.keySet()) {
                param = param + key + "=" + params.get(key) + "&";
            }
            param = param.substring(0, param.length() - 1);
        }
        URL realUrl = new URL(url + param);
        URLConnection connection = realUrl.openConnection();
        if (url.startsWith("https")) {
            SSLContext ctx = MyX509TrustManagerUtils();
            ((HttpsURLConnection) connection).setSSLSocketFactory(ctx.getSocketFactory());
            ((HttpsURLConnection) connection).setHostnameVerifier((arg0, arg1) -> true);
        }
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Keep-Alive");
        connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        if (header != null && header.size() > 0) {
            for (String key : header.keySet()) {
                connection.setRequestProperty(key, header.get(key));
            }
        }
        Map<String, List<String>> map = connection.getHeaderFields();
        System.out.println(url);
        for (String key : map.keySet()) {
            System.out.println(key + "--->" + map.get(key));
        }
        connection.connect();
        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }
        if (in != null) {
            in.close();
        }
        return result;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url   发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, Map<String, String> param, Map<String, String> header) throws Exception {
        String result = "";
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder paramStr = new StringBuilder();
        if (param != null && param.size() > 0) {
            for (String key : param.keySet()) {
                paramStr.append(key).append("=").append(param.get(key)).append("&");
            }
        }
        URL realUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        if (header != null && header.size() > 0) {
            for (String key : header.keySet()) {
                conn.setRequestProperty(key, header.get(key));
            }
        }
        conn.setDoOutput(true);
        conn.setDoInput(true);
        out = new PrintWriter(conn.getOutputStream());
        out.print(paramStr.toString());
        out.flush();
        System.out.println(url);
        System.out.println(paramStr);
        Map<String, List<String>> map = conn.getHeaderFields();
        for (String key : map.keySet()) {
            System.out.println(key + "--->" + map.get(key));
        }
        in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }
        if (out != null) {
            out.close();
        }
        if (in != null) {
            in.close();
        }
        return result;
    }

    public static String postJson(String url, String json, Map<String, String> header) throws Exception {
        String result = "";
        URL realUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        conn.setRequestProperty("Content-Length", String.valueOf(json.getBytes().length));
        conn.setRequestProperty("Content-type", "application/json");
        if (header != null && header.size() > 0) {
            for (String key : header.keySet()) {
                conn.setRequestProperty(key, header.get(key));
            }
        }
        conn.setDoOutput(true);
        conn.setDoInput(true);
        OutputStream out = conn.getOutputStream();
        out.write(json.getBytes());
        out.flush();
        out.close();
        Map<String, List<String>> map = conn.getHeaderFields();
        System.out.println("网址:" + url);
        System.out.println("json:" + json);
        for (String key : map.keySet()) {
            System.out.println(key + "--->" + map.get(key));
        }
        if (conn.getResponseCode() == 200) {
            StringBuffer strBuf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            reader.close();
            result = strBuf.toString();
        }
        System.out.println("请求结果:" + result);
        return result;
    }

    public static String uploadFile(String urlStr, Map<String, File> fileMap, Map<String, String> params, Map<String, String> header) throws Exception {
        String result = "";
        HttpURLConnection conn = null;
        String BOUNDARY = "---------------------------123821742118716"; //boundary就是request头和上传文件内容的分隔符
        for (String key : params.keySet()) {
            System.out.println("上传参数：" + key + "->" + params.get(key));
        }
        for (String key : header.keySet()) {
            System.out.println("上传头：" + key + "->" + header.get(key));
        }
        URL url = new URL(urlStr);
        conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(30000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        if (header != null && header.size() > 0) {
            for (String key : header.keySet()) {
                conn.setRequestProperty(key, header.get(key));
            }
        }
        OutputStream out = new DataOutputStream(conn.getOutputStream());
        if (params != null) {
            StringBuffer strBuf = new StringBuffer();
            Iterator<Map.Entry<String, String>> iter = params.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                String inputName = entry.getKey();
                String inputValue = entry.getValue();
                strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
                strBuf.append(inputValue);
            }
            out.write(strBuf.toString().getBytes());
        }
        if (fileMap != null) {
            Iterator<Map.Entry<String, File>> iter = fileMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, File> entry = iter.next();
                String inputName = (String) entry.getKey();
                File file = (File) entry.getValue();
                String filename = file.getName();
                StringBuffer strBuf = new StringBuffer();
                strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + filename + "\"\r\n");
                strBuf.append("Content-Type:multipart/form-data\r\n\r\n");
                out.write(strBuf.toString().getBytes());
                DataInputStream in = new DataInputStream(new FileInputStream(file));
                int bytes = 0;
                byte[] bufferOut = new byte[1024];
                while ((bytes = in.read(bufferOut)) != -1) {
                    out.write(bufferOut, 0, bytes);
                }
                in.close();
            }
        }

        byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
        out.write(endData);
        out.flush();
        out.close();
        System.out.println("网址:" + url);
        Map<String, List<String>> map = conn.getHeaderFields();
        for (String key : map.keySet()) {
            System.out.println("head:" + key + "--->" + map.get(key));
        }
        StringBuffer strBuf = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null) {
            strBuf.append(line).append("\n");
        }
        result = strBuf.toString();
        reader.close();
        reader = null;
        if (conn != null) {
            conn.disconnect();
            conn = null;
        }
        return result;
    }

    /*
     * HTTPS忽略证书验证,防止高版本jdk因证书算法不符合约束条件,使用继承X509ExtendedTrustManager的方式
     */
    class MyX509TrustManager extends X509ExtendedTrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1, Socket arg2) throws CertificateException {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1, Socket arg2) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
                throws CertificateException {
        }
    }

    public static SSLContext MyX509TrustManagerUtils() {

        TrustManager[] tm = {new HttpRequest().new MyX509TrustManager()};
        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tm, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ctx;
    }
}
