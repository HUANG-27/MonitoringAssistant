package com.example.huang.client.tool;

public class JSONTool {
    public static String filterJSONString(String JSONString) {
        String res = JSONString.replaceAll("\\\\n|\\\\r|\\\\t|\\\\", "");
        return res.substring(1, res.length() - 1);
    }
}
