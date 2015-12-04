package com.example.drew.myapplication;

import android.util.Log;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * Created by Paul on 11/21/2015.
 */
enum elem {
    CSS
}

public class HTMLGen {

    public static String generateDirectoryListing(String filename, String rootPath) {
        HTMLGen gen = new HTMLGen();
        String style = gen.getRes("CSS");
        String script = gen.getRes("JS");
        File dir = new File(rootPath, filename);
        if (!dir.isDirectory()) return null;

        File[] files = dir.listFiles();

        String result = "<html><head><title>Directory - " + filename + "</title>\n"
                //+"<link href=\"res/raw/directorystyle.css\" rel=\"stylesheet\" type=\"text/css\">\n"
                + "</head>\n"
                + "<style>" + style + "</style>\n"
                + "<script>" + script + "</script>\n"
                + "<body>\n"
                + "<h1>Directory - " + filename + "</h1>\n";
        //+ "<ul>\n";

        /*

        for (File file : files) {
            try {
                result += "<li><a href=\"/" + URLEncoder.encode(file.getAbsolutePath().replace(rootPath, ""), "UTF-8").replace("%2F", "/") + "\">"
                        + file.getName() + "</a></li>\n";
            } catch (UnsupportedEncodingException e) {
//                DebugBroadcaster.message("Unsupported encoding exception");
                return null;
            }
        }

        result += "</ul>\n<hr />\n";

        /*

        result += "<form action=\"" + filename + "\" method=\"post\" enctype=\"multipart/form-data\">\n" +
                "    <label for=\"file\">Upload a file:</label>\n" +
                "    <input type=\"file\" name=\"file\" id=\"file\" />\n" +
                "    <input type=\"submit\" name=\"submit\" value=\"Upload\" />\n";


               */


        result += HTMLfrags.getUpload(filename);

        result += "</body>\n</html>";

        return result;
    }

    private String getRes(String type) {
        InputStream raw = null;
        String res = null;
        switch (type) {
            case "CSS":
                raw = this.getClass().getClassLoader().getResourceAsStream("res/raw/directorystyle.css");
                break;
            case "JS":
                raw = this.getClass().getClassLoader().getResourceAsStream("res/raw/directory.js");
                break;

        }
        if (raw != null) {
            try {
                res = IOUtils.toString(raw, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    private String getUploadForm(String filename) {

        String result = new String();

        result += "<form action=\"" + filename + "\" method=\"post\" enctype=\"multipart/form-data\">\n" +
                "    <label for=\"file\">Upload a file:</label>\n" +
                "    <input type=\"file\" name=\"file\" id=\"file\" />\n" +
                "    <input type=\"submit\" name=\"submit\" value=\"Upload\" />\n";

        return result;
    }

    private String fileItem() {


        return null;
    }

    private void readFile(File file) {
        String extension = FilenameUtils.getExtension(file.getAbsolutePath());
        switch (extension) {
            case "jpg":
                break;
            case "png":
                break;
            case "gif":
                break;
            case "html":
                break;
            case "txt":
                break;
            case "text":
                break;

        }
        Log.d("PATCH", extension);


    }

    public static String generateDirectoryListing2(String filename, String rootPath) {
        HTMLGen butt = new HTMLGen();
        File dir = new File(rootPath, filename);
        if (!dir.isDirectory()) return null;

        File[] files = dir.listFiles();
        String result = new String();

        for (File file : files) {
            try {
                result += file.getAbsolutePath().replace(rootPath, "") + "/*";
                result += URLEncoder.encode(file.getAbsolutePath().replace(rootPath, ""), "UTF-8").replace("%2F", "/") + "/*";
                result += "!**";
                //result += "<li><a href=\"/" + URLEncoder.encode(file.getAbsolutePath().replace(rootPath, ""), "UTF-8").replace("%2F", "/") + "\">"
                //        + file.getName() + "</a></li>\n";
            } catch (UnsupportedEncodingException e) {
//                DebugBroadcaster.message("Unsupported encoding exception");
                return null;
            }
        }

        return result;
    }
}
