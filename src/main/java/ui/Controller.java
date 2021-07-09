package ui;

import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import utils.*;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class Controller implements Initializable {
    @FXML
    TextArea result;
    @FXML
    ChoiceBox com;
    @FXML
    ChoiceBox bradrate;
    @FXML
    Button open;
    @FXML
    TextField sendStr;
    @FXML
    Button clear;
    @FXML
    Button send;
    @FXML
    CheckBox hex;
    @FXML
    CheckBox print;
    String mac = "";
    String server = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            server = (String) PropertiesUtils.getInstance().getProperty("server");
            updateTv("server:" + server);
            mac = NetUtils.getLocalMac();
            updateTv("MAC:" + mac);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<String> ports = SerialPortManager.findPorts();
        com.setItems(FXCollections.observableArrayList(ports));
        bradrate.setItems(FXCollections.observableArrayList(9600, 19200, 38400, 57600, 115200));
        open.setOnAction(event -> {
            String comStr = com.getValue().toString();
            String bradStr = bradrate.getValue().toString();
            if (!comStr.isEmpty()) {
                if (!bradStr.isEmpty()) {
                    if ("打开串口".equals(open.getText()) && serialPort == null) {
                        open(comStr, Integer.parseInt(bradStr));
                    } else {
                        close();
                    }
                } else {
                    updateTv("请选择波特率");
                }
            } else {
                updateTv("请选择串口号");
            }
        });
        send.setOnAction(event -> {
            String sendSt = sendStr.getText();
            if (!sendSt.isEmpty()) send(sendSt);
        });
        clear.setOnAction(event -> result.setText(""));
    }

    SerialPort serialPort;

    public void open(String portName, int baudrate) {
        try {
            serialPort = SerialPortManager.openPort(portName, baudrate);
            SerialPortManager.addListener(serialPort, () -> {
                byte[] data = SerialPortManager.readFromPort(serialPort);
                try {
                    if (print.isSelected()) {
                        if (hex.isSelected()) {
                            updateTv("接收十六进制:" + ByteUtils.byteArrayToHexString(data));
                        } else {
                            updateTv("接收字符串:" + new String(data));
                        }
                    }
                    if (data.length > 5) {
                        byte[] temp = new byte[5];
                        System.arraycopy(data, 5, temp, 0, temp.length);
                        byte[] hum = new byte[5];
                        System.arraycopy(data, 15, hum, 0, hum.length);
                        upload(new String(temp), new String(hum));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            open.setText("关闭串口");
        } catch (PortInUseException e) {
            updateTv("串口已被占用！");
        } catch (Exception e) {
            updateTv(e.getMessage());
        }
    }

    public void send(String message) {
        if (serialPort != null) {
            if (hex.isSelected()) {
                SerialPortManager.sendToPort(serialPort, ByteUtils.hexStr2Byte(message));
                updateTv("发送十六进制:" + ByteUtils.byteArrayToHexString(ByteUtils.hexStr2Byte(message)));
            } else {
                SerialPortManager.sendToPort(serialPort, message.getBytes());
                updateTv("发送字符串:" + message);
            }
        } else {
            updateTv("串口未打开");
        }
    }

    long currentTime = 0L;

    public void upload(String temp, String hub) {
        //Temp:29.29,Hum:48.68   Temp:130.00,Hum:100.00   需要排除掉异常数据
        double tem = 131.0;
        try {
            tem = Double.parseDouble(temp);
            if (System.currentTimeMillis() - currentTime >= 1000 && tem < 131) {//一秒以内的数据屏蔽掉
                System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + " 温度:" + temp + "℃ 湿度:" + hub + "%");
                currentTime = System.currentTimeMillis();
                Map<String, String> param = new HashMap<>();
                param.put("mac", mac);
                param.put("temp", temp);
                param.put("hum", hub);
                HttpRequest.sendPost("http://" + server + "/insert", param, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (serialPort != null) {
            SerialPortManager.closePort(serialPort);
            open.setText("打开串口");
            serialPort = null;
        } else {
            updateTv("串口未打开");
        }
    }

    public void updateTv(String message) {
        Platform.runLater(() -> result.appendText(message + "\n"));
    }
}
