package com.androidfaceauthentication.network;

import com.androidfaceauthentication.view.pojo.JadwalAndroidRequest;
import com.androidfaceauthentication.view.pojo.JadwalDTOResponse;
import com.androidfaceauthentication.view.pojo.KelasDTOResponse;
import com.androidfaceauthentication.view.pojo.LokasiRuangRequest;
import com.androidfaceauthentication.view.pojo.LokasiRuangResponse;
import com.androidfaceauthentication.view.pojo.PresensiMahasiswaRequest;
import com.androidfaceauthentication.view.pojo.ProfilResponse;
import com.androidfaceauthentication.view.pojo.ProfileUserRequest;
import com.androidfaceauthentication.view.pojo.QRCodeScanResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface APIInterface {

    @POST("verifyqrcode")
    Call<QRCodeScanResult> sendScannerresult(
            @Body QRCodeScanResult qrCodeScanResult);

    @POST("presensimhs")
    Call<Boolean> sendPresensimhsRequest(
            @Body PresensiMahasiswaRequest request);

    @GET("daftarkelasandroid")
    Call<List<KelasDTOResponse>> daftarKelas();

    @POST("statuspresensimhs")
    Call<Boolean> cekStatusPresensi(
            @Body PresensiMahasiswaRequest request);

    @POST("daftarjadwalandroid")
    Call<List<JadwalDTOResponse>> getDaftarJadwal(
            @Body JadwalAndroidRequest request);

    @POST("lokasiruang")
    Call<LokasiRuangResponse> getLokasiRuang(
            @Body LokasiRuangRequest request);

    @POST("profiluser")
    Call<ProfilResponse> getUserProfile(
            @Body ProfileUserRequest request);


}
