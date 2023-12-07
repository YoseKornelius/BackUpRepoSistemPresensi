package com.androidfaceauthentication.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.androidfaceauthentication.R;
import com.androidfaceauthentication.network.APIInterface;
import com.androidfaceauthentication.network.RetrofitClient;
import com.androidfaceauthentication.view.pojo.JadwalAndroidRequest;
import com.androidfaceauthentication.view.pojo.JadwalDTOResponse;
import com.androidfaceauthentication.view.rvDetailKelas.JadwalRVAdapter;
import com.androidfaceauthentication.view.rvDetailKelas.JadwalSelectListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KelasActivity extends AppCompatActivity implements JadwalSelectListener {
    private String TAG = "KelasActivity";

    RecyclerView rvJadwal;
    List<JadwalDTOResponse> listJadwal;
    APIInterface apiInterface;
    JadwalRVAdapter jadwalRVAdapter;
    String nim,email;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kelas);
        rvJadwal = findViewById(R.id.rvListJadwal);
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Intent receiveintent = getIntent();

        String kelas = receiveintent.getStringExtra("namakelas");
        String idKelas = receiveintent.getStringExtra("idkelas");
        nim = receiveintent.getStringExtra("nim");
        email = receiveintent.getStringExtra("email");
        TextView tvNamaKelas = findViewById(R.id.namakelas);
        TextView tvRuangKelas = findViewById(R.id.ruang_kelas);
        tvNamaKelas.setText(kelas);
        JadwalAndroidRequest jadwalAndroidRequest = new JadwalAndroidRequest(idKelas);
        Call<List<JadwalDTOResponse>> call = apiInterface.getDaftarJadwal(jadwalAndroidRequest);
        call.enqueue(new Callback<List<JadwalDTOResponse>>() {
            @Override
            public void onResponse(Call<List<JadwalDTOResponse>> call, Response<List<JadwalDTOResponse>> response) {
                Log.v(TAG, "respon code = " + response.code());
                listJadwal =response.body();
                tvRuangKelas.setText(response.body().get(0).getRuang());
                rvBuilderJadwal(listJadwal);



            }

            @Override
            public void onFailure(Call<List<JadwalDTOResponse>> call, Throwable t) {

            }
        });


    }
    public void rvBuilderJadwal(List<JadwalDTOResponse> listJadwal){
        rvJadwal.setHasFixedSize(true);
        rvJadwal.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));
        jadwalRVAdapter = new JadwalRVAdapter(this, this,listJadwal);
        rvJadwal.setAdapter(jadwalRVAdapter);
    }

    @Override
    public void onItemClicked(JadwalDTOResponse jadwalDTOResponse) {
        Intent intent = new Intent(this, JadwalActivity.class);
        intent.putExtra("tanggal", jadwalDTOResponse.getTanggal());
        intent.putExtra("idjadwal", jadwalDTOResponse.getJadwal());
        intent.putExtra("nim", nim);
        intent.putExtra("email",email);
        startActivity(intent);


    }
}