package com.androidfaceauthentication.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.androidfaceauthentication.R;
import com.androidfaceauthentication.network.APIInterface;
import com.androidfaceauthentication.network.RetrofitClient;
import com.androidfaceauthentication.utils.PreferenceUtils;
import com.androidfaceauthentication.view.pojo.KelasDTOResponse;
import com.androidfaceauthentication.view.pojo.ProfilResponse;
import com.androidfaceauthentication.view.pojo.ProfileUserRequest;
import com.androidfaceauthentication.view.rvKelas.KelasRVAdapter;
import com.androidfaceauthentication.view.rvKelas.SelectListener;
import com.firebase.ui.auth.AuthUI;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements SelectListener {

    private static final int PERMISSION_REQUESTS = 1;

    private String TAG = "MainActivity";
    public static final String KEY_TEST_MODE = "test_mode";
    public static final String REAL_FACE_TEST = "real_face_test";
    public static final String SPOOF_FACE_TEST = "spoof_face_test";

    private Button btnAddFace;
    private Button btnRealTest;
    private Button btnSpoofTest;
    private Button btnKeluar;
    RecyclerView rvKelas;
    List<KelasDTOResponse> listkelas;
    KelasRVAdapter kelasRVAdapter;
    TextView tvNamaUser;
    TextView tvSampingCard;
    APIInterface apiInterface;
    String nama, nim, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Intent receiveintent = getIntent();
        email = receiveintent.getStringExtra("email");
        btnRealTest = findViewById(R.id.btn_real_test);
        btnSpoofTest = findViewById(R.id.btn_spoof_test);
        btnKeluar = findViewById(R.id.btn_keluar);
        btnAddFace = findViewById(R.id.btn_add_face);
        tvNamaUser = findViewById(R.id.namaUser);
        tvSampingCard = findViewById(R.id.tvSampingCard);
        rvKelas = findViewById(R.id.rvJadwal);
        tvSampingCard.setVisibility(View.GONE);

        float[] faceEmbedings = PreferenceUtils.getFaceEmbeddings(this);
        //check if user embedding already exist
        if (faceEmbedings != null) {
            if (faceEmbedings.length > 0) {
                btnAddFace.setVisibility(View.GONE);
            } else {
                btnAddFace.setVisibility(View.VISIBLE);
            }
        } else {
            btnAddFace.setVisibility(View.VISIBLE);
        }

        btnRealTest.setOnClickListener(view -> {
            Intent intent = new Intent(this, CameraXLivePreviewActivity.class);
            intent.putExtra(KEY_TEST_MODE, REAL_FACE_TEST);
            startActivity(intent);
        });

        btnSpoofTest.setOnClickListener(view -> {
            Intent intent = new Intent(this, FaceVerificationActivity.class);
            //intent.putExtra(KEY_TEST_MODE, SPOOF_FACE_TEST);
            startActivity(intent);
        });

        btnKeluar.setOnClickListener(view -> {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(task -> {
                        SharedPreferences.Editor myEdit = PreferenceManager.getDefaultSharedPreferences(this).edit();
                        myEdit.clear();
                        myEdit.apply();
                        Intent intent = new Intent(this, SplashScreen.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal keluar Applikasi :" + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        btnAddFace.setOnClickListener(view -> {
            Intent intent = new Intent(this, TakeFaceImageActivity.class);
            startActivity(intent);
        });
        ProfileUserRequest request = new ProfileUserRequest("yose8161@gmail.com");
        Call<ProfilResponse> call1 = apiInterface.getUserProfile(request);
        call1.enqueue(new Callback<ProfilResponse>() {
            @Override
            public void onResponse(Call<ProfilResponse> call, Response<ProfilResponse> response) {
                nama = response.body().getNama();
                nim = response.body().getNim();
                tvNamaUser.setText(nama);
            }

            @Override
            public void onFailure(Call<ProfilResponse> call, Throwable t) {

            }
        });

        Call<List<KelasDTOResponse>> call = apiInterface.daftarKelas();
        call.enqueue(new Callback<List<KelasDTOResponse>>() {
            @Override
            public void onResponse(Call<List<KelasDTOResponse>> call, Response<List<KelasDTOResponse>> response) {
                System.out.println(response.code());
                listkelas = response.body();
                rvBuilder(listkelas);

            }

            @Override
            public void onFailure(Call<List<KelasDTOResponse>> call, Throwable t) {
                Log.v(TAG, "ini on failure " + t.getMessage());

            }
        });

        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }

    }

    public void rvBuilder(List<KelasDTOResponse> listkelas) {
        rvKelas.setHasFixedSize(true);
        rvKelas.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));
        kelasRVAdapter = new KelasRVAdapter(this, listkelas, this);
        rvKelas.setAdapter(kelasRVAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
//        float[] faceEmbedings = PreferenceUtils.getFaceEmbeddings(this);
//        //check if user embedding already exist
//        if (faceEmbedings != null) {
//            if (faceEmbedings.length > 0) {
//                btnAddFace.setVisibility(View.GONE);
//            } else {
//                btnAddFace.setVisibility(View.VISIBLE);
//            }
//        } else {
//            btnAddFace.setVisibility(View.VISIBLE);
//        }
    }

    @Override
    public void onItemClicked(KelasDTOResponse kelasDTOResponse) {
        Intent intent = new Intent(this, KelasActivity.class);
        intent.putExtra("idkelas", kelasDTOResponse.getKodeKelas());
        intent.putExtra("namakelas", kelasDTOResponse.getMatakuliah());
        intent.putExtra("nim", nim);
        intent.putExtra("email", email);
        startActivity(intent);

    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (!allPermissionsGranted()) {
            this.finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(" main activity ", "Permission granted: " + permission);
            return true;
        }
        Log.i( "main activiry", "Permission NOT granted: " + permission);
        return false;
    }

}