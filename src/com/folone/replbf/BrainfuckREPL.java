package com.folone.replbf;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.folone.Evaluator;

public class BrainfuckREPL extends Service {

    private final Evaluator.Stub evaluator = new Evaluator.Stub() {

        public String evaluate(String script) throws RemoteException {
            try {
                return Brainfuck.evaluate(script);
            } catch (IOException e) {
                return "Some error";
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return evaluator;
    }

}
