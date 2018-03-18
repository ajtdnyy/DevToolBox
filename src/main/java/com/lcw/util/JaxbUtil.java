package com.lcw.util;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lancw
 */
public class JaxbUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JaxbUtil.class.getName());
    private static final String CHARSET_UTF8 = "UTF-8";
    private static final String DIR = System.getProperty("user.dir") + "/conf";
    public static final String SETTING_FILE = DIR + "/setting.xml";
    public static final String HTTP_CONFIG_FILE = DIR + "/httpConfig.xml";

    /**
     * JavaBean转换成xml 默认编码UTF-8
     *
     * @param obj
     * @return
     */
    public static String convertToXml(Object obj) {
        return convertToXml(obj, CHARSET_UTF8);
    }

    /**
     * 将对象转成xml并存储到xml
     *
     * @param ss
     * @param file
     */
    public static void saveToXML(Object ss, String file) {
        try {
            String content = convertToXml(ss, CHARSET_UTF8);
            FileUtils.mkdir(DIR);
            FileUtils.fileWrite(file, content);
        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * 加载系统设置
     *
     * @param c
     * @param file
     * @return
     */
    public static Object loadXMLToBean(Class c, String file) {
        try {
            String xml = FileUtils.fileRead(file);
            return converyToJavaBean(xml, c);
        } catch (Exception ex) {
            try {
                return c.newInstance();
            } catch (Exception ex1) {
                return null;
            }
        }
    }

    /**
     * JavaBean转换成xml
     *
     * @param obj
     * @param encoding
     * @return
     */
    public static String convertToXml(Object obj, String encoding) {
        String result = null;
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

            StringWriter writer = new StringWriter();
            marshaller.marshal(obj, writer);
            result = writer.toString();
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return result;
    }

    /**
     * xml转换成JavaBean
     *
     * @param <T>
     * @param xml
     * @param c
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T converyToJavaBean(String xml, Class<T> c) {
        T t = null;
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            t = (T) unmarshaller.unmarshal(new StringReader(xml));
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return t;
    }
}
