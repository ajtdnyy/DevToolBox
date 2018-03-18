package com.lcw.eum;

/**
 * <p>
 * 文件名称：Charset.java </p>
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
 * @since 2017-7-12 20:17:57
 */
public enum Charset {
    UTF8("UTF-8", "UTF-8"),
    GBK("GBK", "GBK"),
    GB2312("GB2312", "GB2312"),
    ISO8859_1("ISO-8859-1", "ISO-8859-1");
    private final String code;
    private final String text;

    private Charset(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return code;
    }
}
