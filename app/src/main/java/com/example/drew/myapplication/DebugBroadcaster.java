package com.example.drew.myapplication;

import java.util.ArrayList;

interface DebugListener {
    void debugMessage(String message);
}

public class DebugBroadcaster {

    private static ArrayList<DebugListener> listeners = new ArrayList<>();

    public static void addListener(DebugListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(DebugListener listener) {
        listeners.remove(listener);
    }

    public static void message(String message) {
        for (DebugListener l :
                listeners) {
            l.debugMessage(message);
        }
    }
}
