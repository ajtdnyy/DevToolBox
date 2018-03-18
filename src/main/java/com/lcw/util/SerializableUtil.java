package com.lcw.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author lancw
 */
public class SerializableUtil {

    private static final String DIR = System.getProperty("user.dir");

    static {
        File dir = new File(DIR + "/cache");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static enum FileNameEnum {
        DEPENDENCY_CACHE(DIR + "/cache/dependencyCache"),
        PARAMETER_CACHE(DIR + "/cache/parameterCache");
        private final String name;

        private FileNameEnum(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    public static void saveObject(Object o, FileNameEnum fileName) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(fileName.getName()); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(o);
            oos.flush();
        }
    }

    public static Object readObject(FileNameEnum fileName) throws Exception {
        Object o;
        try (FileInputStream fis = new FileInputStream(fileName.getName()); ObjectInputStream ois = new ObjectInputStream(fis)) {
            o = ois.readObject();
        }
        return o;
    }

    public static void clearCache(FileNameEnum fileName) {
        File file = new File(fileName.getName());
        if (file.exists()) {
            file.delete();
        }
    }
}
