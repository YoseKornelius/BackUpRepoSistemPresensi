package com.androidfaceauthentication.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.androidfaceauthentication.network.APIInterface;
import com.androidfaceauthentication.network.RetrofitClient;
import com.androidfaceauthentication.view.pojo.QRCodeScanResult;
import com.google.zxing.Result;

import org.json.JSONObject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private String TAG = "ScannerActicity";
    private ZXingScannerView scannerView;
    String idJadwal;

    JSONObject json = new JSONObject();
    APIInterface apiInterface;
    String respon, nim, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView = new ZXingScannerView(this);
        scannerView.setAspectTolerance(0.5f);
        setContentView(scannerView);
        Intent receiveintent = getIntent();
        idJadwal = receiveintent.getStringExtra("idjadwal");
        nim = receiveintent.getStringExtra("nim");
        email = receiveintent.getStringExtra("email");
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();

    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        String result = rawResult.getText();
        Log.v(TAG + "raw result", result);
        if (result != null) {
            QRCodeScanResult qrCodeScanResult = new QRCodeScanResult(result, idJadwal);
            Call<QRCodeScanResult> call = apiInterface.sendScannerresult(qrCodeScanResult);
            call.enqueue(new Callback<QRCodeScanResult>() {
                @Override
                public void onResponse(Call<QRCodeScanResult> call, Response<QRCodeScanResult> response) {

                    QRCodeScanResult qrCodeResponse = response.body();
                    respon = qrCodeResponse.getQrCodeData();
                    Log.v(TAG, "ini nge log hasil response body : " + respon);

                    if (respon.equals("verify qrcode berhasil")) {
                        Intent intent = new Intent(getApplicationContext(), FaceVerificationActivity.class);
                        intent.putExtra("idjadwal", idJadwal);
                        intent.putExtra("nim", nim);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    }else{
                        Toast.makeText(getApplicationContext(), "INVALID QRCODE, TRY AGAIN", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<QRCodeScanResult> call, Throwable t) {
                    Log.v(TAG, "ini on failure " + t.getMessage());
                }
            });
        }

        scannerView.resumeCameraPreview(this);
    }
}