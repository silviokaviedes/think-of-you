package de.kaviedes.thinkofyou;

import android.os.Bundle;

import com.google.firebase.FirebaseApp;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
    }
}
