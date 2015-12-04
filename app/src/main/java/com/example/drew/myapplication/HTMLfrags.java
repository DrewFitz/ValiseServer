package com.example.drew.myapplication;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

/**
 * Created by Paul on 11/21/2015.
 */
public class HTMLfrags {

    public static String getTitle(String title){
        return "<title>" + title + "</title>";
    }
    public static String getStyle(String style){
        return "<style>\n" + style + "</style>\n";
    }
    public static String getScript(String script){
        return "<script>\n" + script + "</script>\n";
    }
    public static String getUpload(String filename){

        String result = new String();
        result+= "<div id=\"dirprev\" class=\"container\"></div>\n";

        result += "<form id=\"upload\" method=\"post\" enctype=\"multipart/form-data\">\n" +
                "    <label for=\"file\">Upload a file:</label>\n" +
                "    <input type=\"file\" name=\"file\" id=\"file\" />\n" +
                "    <input type=\"submit\" name=\"submit\" value=\"Upload\" />\n";



        result += "<div id=\"refreshbutton\">\n<button type=\"button\">Refresh List</button>\n</div>\n";


        //result += "<div id=\"fileselect\">SELECT TEST</div>\n";
        //result += "<div id=\"submitbutton\">\n<button type=\"submit\">SUBMIT TEST</button>\n</div>\n";
        //result += "<div id=\"filedrag\">DROP TEST</div>\n";
        //result += "<div id=\"messages\"><p>Status Messages</p>\n</div>\n";

        return result;




    }
    public static boolean isjsonRequest(String resource){
        return resource.endsWith(".json");
    }
    public static String stripjsonRequest(String resource){
        String resc = resource.replace(".json", "");



        return resc;
    }
    public static String returnjsonObject(File file, String rootPath){
        String result = null;
        String abspath = null;
        String url = null;
        String name = null;
        try {

            abspath = file.getAbsolutePath().replace(rootPath, "");
            url = URLEncoder.encode(file.getAbsolutePath().replace(rootPath, ""), "UTF-8").replace("%2F", "/");
            name = file.getName();
            result = "{\"filepath\":\"" + abspath + "\",";
            result += "\"url\":\"" + url + "\",";
            result += "\"name\":\""+ name + "\"}";
        } catch (UnsupportedEncodingException e) {
//                DebugBroadcaster.message("Unsupported encoding exception");
            return null;
        }

        return result;

    }


}
