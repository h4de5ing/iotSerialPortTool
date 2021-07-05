package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

public class PropertiesUtils {
    private Properties properties;
    private static PropertiesUtils propertiesUtils = new PropertiesUtils();

    private PropertiesUtils() {
        String filename = "config.properties";
        properties = new Properties();
        InputStream in = null;
        try {
            File localFile = new File(System.getProperty("user.dir") + File.separator + filename);
            if (localFile.exists()) {
                in = new FileInputStream(localFile);
            } else {
                in = PropertiesUtils.class.getClassLoader().getResourceAsStream(filename);
            }
            properties.load(in);
            Enumeration<?> names = properties.propertyNames();
            while (names.hasMoreElements()) {
                String key = (String) names.nextElement();
                String value = properties.getProperty(key);
                System.out.println(key + "=" + value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PropertiesUtils getInstance() {
        if (propertiesUtils == null) {
            propertiesUtils = new PropertiesUtils();
        }
        return propertiesUtils;
    }

    public Object getProperty(String name) {
        return properties.getProperty(name);
    }
}
