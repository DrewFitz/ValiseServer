package com.example.drew.myapplication;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;


/**
 * Created by Paul on 11/9/2015.
 */


public class FileSystem implements Serializable {
    private String path;

    public FileSystem(String npath){
        path = npath;
    }
    public String getPath(){
        return path;
    }

    public boolean receiveQuery(String dir){
        File f = new File(path);
        if(f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File file : files) {
                    String tstring = file.getAbsolutePath();
                    Log.d("Query", tstring);
                    if (tstring.equals(dir)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return path.equals(dir);
        }
    }

    public byte[] getFile(String dir){

        File file = new File(path, dir);

//        if(file.isDirectory()) {
//
//            File f = new File(path);
//            File[] files = f.listFiles();
//
//            if (files != null) {
//                for (File file2 : files) {
//
//                    Log.d("Query", file2.getAbsolutePath());
//                    if (file2.getAbsolutePath().equals(dir)) {
//                        file = file2;
//                        break; //no point in continuing
//                    }
//                }
//            }
//        }

        byte[] data = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);

            int content = fis.read();

            while (content != -1) {
                outputStream.write(content);
                content = fis.read();
            }

            data = outputStream.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                if(fis != null) fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return data;
    }

    public String[] getList(){
        File file = new File(path);
        String[] list = null;

        if(file.isDirectory()){
            File[] files = file.listFiles();
            list = new String[files.length];

            for(int i = 0; i< files.length;i++) {
                list[i] = files[i].getAbsolutePath();
            }
        }

        return list;

    }
}
