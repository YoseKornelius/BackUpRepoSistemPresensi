package com.qrcodegenerator.controller;

import com.qrcodegenerator.entity.Jadwal;
import com.qrcodegenerator.entity.Kelas;
import com.qrcodegenerator.entity.PresensiMahasiswa;
import com.qrcodegenerator.repository.JadwalRepository;
import com.qrcodegenerator.repository.KelasRepository;
import com.qrcodegenerator.repository.KelasSesiRepository;
import com.qrcodegenerator.repository.PresensiMahasiswaRepository;
import com.qrcodegenerator.response.KelasDTOResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class KelasController {

    @Autowired
    KelasSesiRepository kelasSesiRepository;

    @Autowired
    KelasRepository kelasRepository;

    @Autowired
    JadwalRepository jadwalRepository;

    @Autowired
    PresensiMahasiswaRepository presensiMahasiswaRepository;


    @GetMapping("/daftarkelas")
    public String getDaftarKelas(Model model) {

        model.addAttribute("kelases", kelasRepository.findAll());
        return "daftarkelas";
    }



    @GetMapping("/daftarjadwal")
    public String getDaftarJadwalOfKelas(@RequestParam("kodekelas") String kodekelas, Model model) {
        Kelas kelas = kelasRepository.findById(kodekelas).orElseThrow();
        model.addAttribute("jadwals", jadwalRepository.findAllByKelas(kelas));
//        model.addAttribute("jadwal",jadwalRepository.findByKelas(kelas));
        return "daftarjadwalkelas";
    }

    @GetMapping("/jadwalpresensi")
    public String getPresensiOfJadwal(@RequestParam("kodejadwal") String kodejadwal, Model model) {
        Jadwal jadwal = jadwalRepository.findById(kodejadwal).orElseThrow();
        List<PresensiMahasiswa> daftarPresensiMahasiswa = presensiMahasiswaRepository
                .findAllByJadwal(jadwal);
        model.addAttribute("daftarpresensi", daftarPresensiMahasiswa);
        model.addAttribute("kodejadwal", kodejadwal);
//        model.addAttribute("jadwal",jadwalRepository.findByKelas(kelas));
        return "daftarpresensijadwal";
    }
}
