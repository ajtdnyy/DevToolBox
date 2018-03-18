package com.lcw.util;

import java.io.StringWriter;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jsoup.Jsoup;

/**
 * <p>
 * 文件名称：Formatter.java </p>
 * <p>
 * 文件描述：</p>
 * <p>
 * 版权所有： 版权所有(C)2017-2099 </p>
 * <p>
 * 内容摘要： </p>
 * <p>
 * 其他说明： </p>
 *
 * @version 1.0
 * @author lancw
 * @since 2017-6-27 10:14:41
 */
public class Formatter {

    public static String xml2json(String xmlString) {
        XMLSerializer xmlSerializer = new XMLSerializer();
        JSON json = xmlSerializer.read(xmlString);
        return json.toString(1);
    }

    public static String json2xml(String jsonString) throws Exception {
        XMLSerializer xmlSerializer = new XMLSerializer();
        return formatXML(xmlSerializer.write(JSONSerializer.toJSON(jsonString)));
    }

    public static String formatSQL(String sql) {
        return SqlFormatter.format(sql);
    }

    public static String formatXML(String xml) throws Exception {
        StringWriter out = null;
        try {
            Document d = DocumentHelper.parseText(xml);
            OutputFormat formate = OutputFormat.createPrettyPrint();
            out = new StringWriter();
            XMLWriter writer = new XMLWriter(out, formate);
            writer.write(d);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return out == null ? null : out.toString();
    }

    public static String formatHtml(String str, String charset) throws Exception {
        org.jsoup.nodes.Document html = Jsoup.parse(str);
        return html.toString();
    }

    /**
     * 格式化
     *
     * @param jsonStr
     * @return
     * @author lizhgb
     * @Date 2015-10-14 下午1:17:35
     * @Modified 2017-04-28 下午8:55:35
     */
    public static String formatJson(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr)) {
            return "";
        }
        if (jsonStr.startsWith("[")) {
            return JSONArray.fromObject(jsonStr).toString(2, 2);
        } else if (jsonStr.startsWith("{")) {
            return JSONObject.fromObject(jsonStr).toString(2, 2);
        }
        return jsonStr;
    }

    public static String formatDubboParam(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        char last = '\0';
        char current = '\0';
        int indent = 0;
        boolean isInQuotationMarks = false;
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = jsonStr.charAt(i);
            switch (current) {
                case '"':
                    if (last != '\\') {
                        isInQuotationMarks = !isInQuotationMarks;
                    }
                    sb.append(current);
                    break;
                case '{':
                case '[':
                    sb.append(current);
                    if (!isInQuotationMarks) {
                        sb.append('\n');
                        indent++;
                        addIndentBlank(sb, indent);
                    }
                    break;
                case '}':
                case ']':
                    if (!isInQuotationMarks) {
                        sb.append('\n');
                        indent--;
                        addIndentBlank(sb, indent);
                    }
                    sb.append(current);
                    break;
                case ',':
                    sb.append(current);
                    if (last != '\\' && !isInQuotationMarks) {
                        sb.append('\n');
                        addIndentBlank(sb, indent);
                    }
                    break;
                default:
                    sb.append(current);
            }
        }

        return sb.toString();
    }

    /**
     * 添加space
     *
     * @param sb
     * @param indent
     * @author lizhgb
     * @Date 2015-10-14 上午10:38:04
     */
    private static void addIndentBlank(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append('\t');
        }
    }

}
