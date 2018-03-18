package com.lcw.eum;

/**
 *
 * @author lancw
 */
public enum ContentType {
    /**
     * text_html:text_plain HTML格式:纯文本格式
     */
    CONTENT_TYPE_TEXT_PLAIN("text/plain", "纯文本格式"),
    /**
     * text_html:text_xml HTML格式:XML格式
     */
    CONTENT_TYPE_TEXT_XML("text/xml", "XML格式"),
    /**
     * text_html:application_xhtml+xml HTML格式:XHTML格式
     */
    CONTENT_TYPE_APPLICATION_XHTML_XML("application/xhtml+xml", "XHTML格式"),
    /**
     * text_html:application_xml HTML格式:XML数据格式
     */
    CONTENT_TYPE_APPLICATION_XML("application/xml", "XML数据格式"),
    /**
     * text_html:application_atom+xml HTML格式:AtomXML聚合格式
     */
    CONTENT_TYPE_APPLICATION_ATOM_XML("application/atom+xml", "AtomXML聚合格式"),
    /**
     * text_html:application_json HTML格式:JSON数据格式
     */
    CONTENT_TYPE_APPLICATION_JSON("application/json", "JSON数据格式"),
    /**
     * text_html:application_x-www-form-urlencoded
     * HTML格式:<formencType=””>中默认的encType，form表单数据被编码为key_value格式（表单默认的提交数据的格式）
     */
    CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED("application/x-www-form-urlencoded", "<formencType=””>中默认的encType，form表单数据被编码为key_value格式（表单默认的提交数据的格式）"),
    /**
     * text_html:multipart_form-data HTML格式:需要在表单中进行文件上传
     */
    CONTENT_TYPE_MULTIPART_FORM_DATA("multipart/form-data", "需要在表单中进行文件上传");

    private final String code;
    private final String text;

    private ContentType(String code, String text) {
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
