package com.qrcodegenerator.controller;

import com.qrcodegenerator.entity.Jadwal;
import com.qrcodegenerator.entity.Kelas;
import com.qrcodegenerator.entity.Mahasiswa;
import com.qrcodegenerator.entity.Ruang;
import com.qrcodegenerator.repository.*;
import com.qrcodegenerator.request.JadwalAndroidRequest;
import com.qrcodegenerator.request.LokasiRuangRequest;
import com.qrcodegenerator.request.PresensiMahasiswaRequest;
import com.qrcodegenerator.request.ProfileUserRequest;
import com.qrcodegenerator.response.JadwalDTOResponse;
import com.qrcodegenerator.response.KelasDTOResponse;
import com.qrcodegenerator.response.LokasiRuangResponse;
import com.qrcodegenerator.response.ProfilResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class KelasAndroidController {
    @Autowired
    KelasSesiRepository kelasSesiRepository;

    @Autowired
    KelasRepository kelasRepository;

    @Autowired
    MahasiswaRepository mahasiswaRepository;

    @Autowired
    JadwalRepository jadwalRepository;

    @Autowired
    PresensiMahasiswaRepository presensiMahasiswaRepository;

    @Autowired
    RuangRepository ruangRepository;

    @GetMapping("/daftarkelasandroid")
    public List<KelasDTOResponse> getDaftarKelasAndroid() {
        List<Kelas> daftarKelas = kelasRepository.findAll();
        List<KelasDTOResponse> daftarKelasDTO = daftarKelas.stream()
                .map(kelas -> new KelasDTOResponse(
                        kelas.getKodeKelas(),
                        kelas.getMatakuliah().getNamaMatakuliah(),
                        kelas.getSemester().getNamaSemester(),
                        kelas.getDosen().getNama(),
                        kelas.getGroup()
                ))
                .collect(Collectors.toList());
        System.out.println("getkelasandroid berjalan");

        return daftarKelasDTO;
    }

    @PostMapping("/daftarjadwalandroid")
    public List<JadwalDTOResponse> getJadwalAndroid (@RequestBody JadwalAndroidRequest request){
        Kelas kelas = kelasRepository.findById(request.kodeKelas).orElseThrow();
        List<Jadwal> jadwalkelas = jadwalRepository.findAllByKelas(kelas);
        List<JadwalDTOResponse> jadwalDTOResponses = jadwalkelas.stream()
                .map(jadwal -> new JadwalDTOResponse(
                        jadwal.getKodeJadwal(),
                        jadwal.getKelas().getMatakuliah().getNamaMatakuliah(),
                        jadwal.getSesi().getSesiStart() +" - " + jadwal.getSesi().getSesiEnd(),
                        jadwal.getRuang().getNama(),
                        jadwal.getTanggal()


                )).collect(Collectors.toList());
        System.out.println("getdaftarjadwalandroid berjalan");


        return jadwalDTOResponses;
    }

    @PostMapping("/profiluser")
    public ProfilResponse getProfileUser (@RequestBody ProfileUserRequest request){
        Mahasiswa mahasiswa = mahasiswaRepository.findByEmail(request.getEmail());
        ProfilResponse profilResponse = new ProfilResponse(mahasiswa.getNama(), mahasiswa.getNim());
        System.out.println("endpoint profile dipanggil");
        return profilResponse;
    }

    @PostMapping("/lokasiruang")
    public LokasiRuangResponse getLokasiRuang (@RequestBody LokasiRuangRequest request){
        Jadwal jadwal = jadwalRepository.findById(request.getIdJadwal()).get();
        Ruang ruang = ruangRepository.findById(jadwal.getRuang().getIdRuang()).get();
        LokasiRuangResponse lokasiRuangResponse = new LokasiRuangResponse(ruang.getLatitude(),ruang.getLongitude());
        System.out.println("endpoint lokasiruang dipanggil");
        return lokasiRuangResponse;


    }



}
