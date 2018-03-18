package com.lcw.eum;

/**
 *
 * @author lancw
 */
public enum CodeFormat {
    FORMAT_JSON("format_json", "JSON美化"),
    FORMAT_XML("format_xml", "XML美化"),
    FORMAT_HTML("format_html", "HTML美化"),
    FORMAT_SQL("format_json", "SQL美化"),
    JSON_XML_EXCHANGE("json_xml_xml_exchange", "json xml互转");

    private final String code;
    private final String text;

    private CodeFormat(String code, String text) {
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
        return text;
    }
}
