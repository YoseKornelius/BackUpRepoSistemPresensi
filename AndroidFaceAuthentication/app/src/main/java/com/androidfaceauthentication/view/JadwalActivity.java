package com.androidfaceauthentication.view;

import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.androidfaceauthentication.R;
import com.androidfaceauthentication.network.APIInterface;
import com.androidfaceauthentication.network.RetrofitClient;
import com.androidfaceauthentication.view.pojo.PresensiMahasiswaRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JadwalActivity extends AppCompatActivity {

    TextView tvStatusPresensi;
    TextView tvnamaKelas;
    Button btnPresensi;

    Button btnKembali;

    APIInterface apiInterface;
    String nim,email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jadwal);
        tvnamaKelas = findViewById(R.id.namajadwal);
        btnPresensi = findViewById(R.id.btn_presensi);
        btnKembali = findViewById(R.id.btn_kembali);
        tvStatusPresensi = findViewById(R.id.statuskehadiran);
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        btnKembali.setVisibility(View.GONE);

        Intent receiveintent = getIntent();

        String tanggal = receiveintent.getStringExtra("tanggal");
        String idjadwal = receiveintent.getStringExtra("idjadwal");
        nim = receiveintent.getStringExtra("nim");
        email = receiveintent.getStringExtra("email");
        tvnamaKelas.setText(tanggal);
        PresensiMahasiswaRequest request = new PresensiMahasiswaRequest(idjadwal, "123");
        Call<Boolean> call = apiInterface.cekStatusPresensi(request);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {

                }
                Boolean apiResponse = response.body();
                if (apiResponse == true) {
                    tvStatusPresensi.setText("HADIR");
                    btnPresensi.setVisibility(View.GONE);
                    btnKembali.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "kembali otomatis ke halaman home dalam 10 detik", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("email",email);
                    btnKembali.setOnClickListener(view -> {
                        startActivity(intent);
                    });
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(intent);

                            finish();
                        }
                    }, 10000);


                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {

            }
        });
        btnPresensi.setOnClickListener(view -> {
            Intent intent = new Intent(this, CheckLocationActivity.class);
            intent.putExtra("idjadwal", idjadwal);
            intent.putExtra("nim",nim);
            intent.putExtra("email",email);
            startActivity(intent);

        });

    }

//    private final ActivityResultLauncher<Intent> scannerActivityLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            (result) -> {
//                tvStatusPresensi.setText("hadir");
//                Toast.makeText(this, "Absen success", Toast.LENGTH_LONG).show();
//            }
//    );
//
//    private void isSuccess(){
//
//    }
}