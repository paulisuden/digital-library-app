package com.pm.library.controllers.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.pm.library.R;
import com.pm.library.controllers.activities.CaptureScanActivity;

public class ScanFragment extends Fragment {

    private TextView scanResult;

    private final ActivityResultLauncher<ScanOptions> scanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    scanResult.setText(result.getContents());
                } else {
                    scanResult.setText("No se escaneó nada");
                }
            });

    public ScanFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        scanResult = view.findViewById(R.id.scan_result);

        ScanOptions options = new ScanOptions();
        options.setPrompt("Escanea el código QR");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setBarcodeImageEnabled(false);
        options.setCaptureActivity(CaptureScanActivity.class);
        scanLauncher.launch(options);
        return view;

    }
}

/*scanResult = view.findViewById(R.id.scan_result);
        Button btnScan = view.findViewById(R.id.scan_btn);

        btnScan.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Escanea el código QR");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setBarcodeImageEnabled(false);
            scanLauncher.launch(options);
        });*/