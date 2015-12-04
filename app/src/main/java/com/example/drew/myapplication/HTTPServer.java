package com.example.drew.myapplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

enum Verb {
    GET, POST, ERROR, PATCH
}

class HTTPRequest {
    public Map<String, String> headers;
    public Verb verb;
    public String resourceName;
    public String bodyString;
    public Boolean isJSON = false;

    HTTPRequest() {
        headers = new HashMap<>();
    }

    public static HTTPRequest RequestFromString(String requestString) {

        if (!requestString.contains("\n")) return null; // not a complete request yet

        HTTPRequest request = new HTTPRequest();

        String[] firstSplit = requestString.split("\r\n\r\n");

        String header = firstSplit[0];
        String body = requestString.replace(header + "\r\n\r\n", "");

        String[] headerStrings = header.split("\r\n");

        String requestLine = headerStrings[0];

        String[] requestLineComponents = requestLine.split(" ");
        String verbString = requestLineComponents[0];
        String resource = requestLineComponents[1];



        // store the data
        for (int i = 1; i < headerStrings.length; i++) {
            String line = headerStrings[i];
            String[] split = line.split(":");
            if (split.length > 0) request.headers.put(split[0].trim(), line.replaceFirst(split[0]+":", "").trim());
        }

        switch (verbString) {
            case "GET": request.verb = Verb.GET; break;
            case "POST": request.verb = Verb.POST; break;
            case "PATCH": request.verb = Verb.PATCH; break;
            default: request.verb = Verb.ERROR; break;
        }

        try {
            request.resourceName = URLDecoder.decode(resource, "UTF-8");
            //
            if(HTMLfrags.isjsonRequest(request.resourceName)){
                request.resourceName = HTMLfrags.stripjsonRequest(request.resourceName);
                request.isJSON = true;
            }
            //
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        request.bodyString = body;

        return request;
    }

    public boolean isMultipart() {
        return headers.get("Content-Type").contains("multipart");
    }

    public String getMultipartBoundary() {

        if (!isMultipart()) return null;

        String boundary = null;

        String contentType = headers.get("Content-Type");

        if (contentType == null) return null;

        String[] pieces = contentType.split(" ");

        for (String piece : pieces) {
            if (piece.contains("boundary")) {
                boundary = piece.substring(9);
                break;
            }
        }

        return boundary;
    }

    private String getFileUploadPart() {
        String boundary = getMultipartBoundary();
        if (boundary == null) return null;

        String[] parts = bodyString.split("--" + boundary);

        for (String part : parts) {
            String[] segments = part.split("\n\n");
            String head = segments[0];
            if (head.contains("name=\"file\"")) return part;
        }

        return null;
    }

    public String getFileName() {
        String filePart = getFileUploadPart();
        if (filePart == null) return null;

        String header = filePart.split("\r\n\r\n")[0];

        String[] parts = header.split("\r\n");

        String name = null;

        for (String part : parts) {
            int idx = part.indexOf("filename=");

            if (idx != -1) {
                String[] comps = part.substring(idx+9).split("/");
                name = comps[comps.length-1].replace("\"", "");
            }
        }

        return name;
    }

    public byte[] getFileUploadData() {
        String filePart = getFileUploadPart();
        if (filePart == null) return null;

        String header = filePart.split("\r\n\r\n")[0];
        String body = filePart.substring(header.length() + 4);

        return rawBytesFromString(body);
    }

    private byte[] rawBytesFromString(String s) {

        char[] chars = s.toCharArray();

        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            bytes[i] = (byte) chars[i];
            if (bytes[i] < 0) bytes[i] += 256;
        }

        try {
            return s.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

class HTTPWorker implements Runnable {

    private Socket socket;
    private Thread thread;
    private String rootPath;

    private boolean shouldQuit = false;
    synchronized void setShouldQuit(boolean b) {
        shouldQuit = b;
    }
    synchronized boolean getShouldQuit() {
        return shouldQuit;
    }

    HTTPWorker(Socket s, String rootPath) {
        this.rootPath = rootPath;
        socket = s;
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        setShouldQuit(true);

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private byte[] retrieveFile(String filename) {
        FileSystem fs = new FileSystem(rootPath);
        return fs.getFile(filename);
    }

    private String generateDirectoryListing(String filename) {
        File dir = new File(rootPath, filename);
        if (!dir.isDirectory()) return null;

        File[] files = dir.listFiles();

        String result = "<html><head><title>Directory - " + filename + "</title></head>\n"
                + "<body>\n"
                + "<h1>Directory - " + filename + "</h1>\n"
                + "<ul>\n";

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

        result += "<form action=\"" + filename + "\" method=\"post\" enctype=\"multipart/form-data\">\n" +
                "    <label for=\"file\">Upload a file:</label>\n" +
                "    <input type=\"file\" name=\"file\" id=\"file\" />\n" +
                "    <input type=\"submit\" name=\"submit\" value=\"Upload\" />\n";

        result += "</body>\n</html>";

        return result;
    }

    private void sendDataForRequest(HTTPRequest request) {
        try {
            OutputStream ostream = socket.getOutputStream();
            String directoryString = null;
            byte[] fileBytes = null;

            //String directoryString = generateDirectoryListing(request.resourceName);
            String test = request.resourceName;
            if(request.isJSON){
                directoryString = HTMLGen.generateDirectoryListing2(request.resourceName, rootPath);
            } else {

                directoryString = HTMLGen.generateDirectoryListing(request.resourceName, rootPath);
            }

            if (directoryString == null) {
                fileBytes = retrieveFile(request.resourceName);
            } else {
                fileBytes = directoryString.getBytes();
            }

            if (fileBytes == null) {
                // 404 not found
                DebugBroadcaster.message("404 for file: " + request.resourceName);

                String out = "HTTP/1.0 404 Not Found\r\n\r\n"
                        + "<html><body><h1>404 File Not Found</h1></body></html>";
                ostream.write(out.getBytes());
                ostream.flush();
            } else {
                // 200 ok
                DebugBroadcaster.message("200 OK for file: " + request.resourceName);

                String out = "HTTP/1.0 200 OK\r\n\r\n";
                ostream.write(out.getBytes());
                ostream.write(fileBytes);
                ostream.flush();
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //
    private void sendDataForGENRequest(HTTPRequest request) {
        try {
            OutputStream ostream = socket.getOutputStream();
            byte[] fileBytes;




            //String directoryString = generateDirectoryListing(request.resourceName);

            String directoryString = HTMLGen.generateDirectoryListing2(request.resourceName, rootPath);


            if (directoryString == null) {
                fileBytes = retrieveFile(request.resourceName);
            } else {
                fileBytes = directoryString.getBytes();
            }

            if (fileBytes == null) {
                // 404 not found
                DebugBroadcaster.message("404 for file: " + request.resourceName);

                String out = "HTTP/1.0 404 Not Found\r\n\r\n"
                        + "<html><body><h1>404 File Not Found</h1></body></html>";
                ostream.write(out.getBytes());
                ostream.flush();
            } else {
                // 200 ok
                DebugBroadcaster.message("200 OK for file: " + request.resourceName);

                String out = "HTTP/1.0 200 OK\r\n\r\n";
                ostream.write(out.getBytes());
                ostream.write(fileBytes);
                ostream.flush();
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //

    private void saveFileFromRequest(HTTPRequest request) {
        OutputStream ostream = null;
        try {
            ostream = socket.getOutputStream();
            String boundary = request.getMultipartBoundary();

            if (boundary == null) {
                DebugBroadcaster.message("Recieved malformed post request");
                // 500 internal server error
                DebugBroadcaster.message("500 Internal Server Error for socket: " + socket.toString());

                String out = "HTTP/1.0 500 Internal Server Error\r\n\r\n"
                        + "<html><h1>500 Internal Server Error</h1></html>";
                ostream.write(out.getBytes());
                ostream.flush();
                socket.close();
                return;
            }

            String filename = request.getFileName();
            byte[] fileData = request.getFileUploadData();

            DebugBroadcaster.message("Recieved file: " + filename);

            File f = new File(rootPath + request.resourceName, filename);
            if (!f.exists()) {
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(fileData, 0, fileData.length);
                fos.close();
            } else {
                DebugBroadcaster.message("Couldn't receive file \"" + filename + "\". "
                        + "A file with that name exists already.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        String requestString = "";

        InputStream istream = null;
        OutputStream ostream = null;

        try {
            istream = socket.getInputStream();
            ostream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert istream != null;
        assert ostream != null;

        while (socket.isConnected()) {

            try {

                byte[] buffer = new byte[256];

                socket.setSoTimeout(1000);

                // This will block the thread until data is recieved or the timeout expires
                int readCount = istream.read(buffer);

                while (readCount != -1) {
                    String chunk = new String(buffer, 0, readCount, "ISO-8859-1");

                    requestString += chunk;

                    if (istream.available() == 0) {
                        DebugBroadcaster.message("Waiting for input.");
                    }

                    // This will block the thread until data is recieved or the timeout expires
                    readCount = istream.read(buffer);
                }

            } catch (SocketTimeoutException te) {
                try {
                    HTTPRequest request = HTTPRequest.RequestFromString(requestString);

                    if (getShouldQuit()) {
                        DebugBroadcaster.message("Quitting socket.");
                        socket.close();
                        return;
                    }

                    if (request == null) continue;


                    if (request.verb == Verb.GET) {

                        sendDataForRequest(request);

                    } else if (request.verb == Verb.POST) {

                        saveFileFromRequest(request);
                        sendDataForRequest(request);

                    } else if (request.verb == Verb.PATCH){

                        sendDataForGENRequest(request);


                    }
                    else if (request.verb == Verb.ERROR) {
                        // 500 internal server error
                        DebugBroadcaster.message("500 Internal Server Error for socket: " + socket.toString());

                        String out = "HTTP/1.0 500 Internal Server Error\n\n<html><h1>500 Internal Server Error</h1></html>";
                        ostream.write(out.getBytes());
                        ostream.flush();
                        socket.close();
                        return;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

public class HTTPServer {

    static public HTTPServer sharedServer;

    static public String rootPath;

    private ServerSocket serverSock;
    private Thread listenThread;
    private ArrayList<HTTPWorker> workers;

    public HTTPServer(int port) {
        workers = new ArrayList<>();

        try {
            serverSock = new ServerSocket(port);
        } catch (IOException e) {
            DebugBroadcaster.message(String.format("Error creating socket: %s", e.getMessage()));
            return;
        }

        DebugBroadcaster.message("Listening...");

        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!serverSock.isClosed()) {
                    try {
                        Socket newSock = serverSock.accept();

                        DebugBroadcaster.message("Accepted connection: " + newSock.toString());

                        HTTPWorker newWorker = new HTTPWorker(newSock, rootPath);

                        workers.add(newWorker);

                    } catch (SocketException e) {
                        // should only be when close() is called on serverSock
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        listenThread.start();
    }

    public void close() {

        try {
            serverSock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            listenThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (HTTPWorker worker: workers) {
            worker.stop();
        }

        listenThread = null;
        serverSock = null;
    }

    static public void startServer(int port, String root) {
        rootPath = root;
        if (sharedServer == null) {
            DebugBroadcaster.message("Starting server with port " + port + ".");
            sharedServer = new HTTPServer(port);
        }
    }

    static public void stopServer() {
        if (sharedServer != null) {
            DebugBroadcaster.message("Closing server.");
            sharedServer.close();
            sharedServer = null;
        }
    }
}
