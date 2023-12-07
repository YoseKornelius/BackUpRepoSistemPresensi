package com.qrcodegenerator;

import com.qrcodegenerator.entity.*;
import com.qrcodegenerator.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QRcodeGeneratorApplication implements CommandLineRunner {

    @Autowired
    DosenRepository dosenRepository;

    @Autowired
    JadwalRepository jadwalRepository;

    @Autowired
    KelasRepository kelasRepository;

    @Autowired
    KelasSesiRepository kelasSesiRepository;

    @Autowired
    MahasiswaRepository mahasiswaRepository;

    @Autowired
    MatakuliahRepository matakuliahRepository;

    @Autowired
    PresensiMahasiswaRepository presensiMahasiswaRepository;

    @Autowired
    RuangRepository ruangRepository;

    @Autowired
    SemesterRepository semesterRepository;

    public static void main(String[] args) {
        SpringApplication.run(QRcodeGeneratorApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // Load some data in db
        Dosen dendy = new Dosen("1", "123n123", "TI", "Dendy");
        dosenRepository.save(dendy);

        Mahasiswa yose = new Mahasiswa("123", "TI", "2017", "user pengujian");
        yose.setEmail("yose8161@gmail.com");
        mahasiswaRepository.save(yose);

        Matakuliah kelasAndroid = new Matakuliah("TI001", "Android", 3);
        matakuliahRepository.save(kelasAndroid);

        Matakuliah kelasSistemOperasi = new Matakuliah("TI002", "Sistem Operasi", 3);
        matakuliahRepository.save(kelasSistemOperasi);

        Semester genap2023 = new Semester("GN23", "Genap 2023", "2023", "13 januari 2023", "25 juli 2023");
        semesterRepository.save(genap2023);

        KelasSesi kelasSesi2 = new KelasSesi("Sesi2", "10.30", "12.30");
        kelasSesiRepository.save(kelasSesi2);

        KelasSesi kelasSesi3 = new KelasSesi("Sesi3", "13.30", "16.30");
        kelasSesiRepository.save(kelasSesi3);

        Ruang ruangLabA = new Ruang("R_LAB_A", "Lab A", "-7.7859162", "110.3782016");
        //-7.7871026  110.3781791 -7.7859195 110.3781444 110.3781849 -7.7869993 110.3783964 -7.7860575
        ruangRepository.save(ruangLabA);

        Ruang ruangChara31 = new Ruang("R_C_31", "Chara 3.1", "-7.7765973", "110.4120908");
        ruangRepository.save(ruangChara31);

        Kelas kelasAndroidA = new Kelas("TI001A", kelasAndroid, "A", genap2023, dendy);
        kelasRepository.save(kelasAndroidA);

        Kelas kelasSistemOperasiA = new Kelas("TI002A", kelasSistemOperasi, "A", genap2023, dendy);
        kelasRepository.save(kelasSistemOperasiA);

        Jadwal jadwalAndroidPertemuan1 = new Jadwal("AndroidPertemuan1",kelasAndroidA, kelasSesi2, ruangLabA, "1 Agustus 2023");
        jadwalRepository.save(jadwalAndroidPertemuan1);
        Jadwal jadwalAndroidPertemuan2 = new Jadwal("AndroidPertemuan2",kelasAndroidA, kelasSesi2, ruangLabA, "8 Agustus 2023");
        jadwalRepository.save(jadwalAndroidPertemuan2);
        Jadwal jadwalAndroidPertemuan3 = new Jadwal("AndroidPertemuan3",kelasAndroidA, kelasSesi2, ruangLabA, "15 Agustus 2023");
        jadwalRepository.save(jadwalAndroidPertemuan3);
        Jadwal jadwalAndroidPertemuan4 = new Jadwal("AndroidPertemuan4",kelasAndroidA, kelasSesi2, ruangLabA, "22 Agustus 2023");
        jadwalRepository.save(jadwalAndroidPertemuan4);
        Jadwal jadwalAndroidPertemuan5 = new Jadwal("AndroidPertemuan5",kelasAndroidA, kelasSesi2, ruangLabA, "29 Agustus 2023");
        jadwalRepository.save(jadwalAndroidPertemuan5);
        Jadwal jadwalAndroidPertemuan6 = new Jadwal("AndroidPertemuan6",kelasAndroidA, kelasSesi2, ruangLabA, "5 September 2023");
        jadwalRepository.save(jadwalAndroidPertemuan6);

        Jadwal jadwalSistemOperasiPertemuan1 = new Jadwal("SistemOperasiPertemuan1",kelasSistemOperasiA, kelasSesi3, ruangChara31, "1 Agustus 2023");
        jadwalRepository.save(jadwalSistemOperasiPertemuan1);
        Jadwal jadwalSistemOperasiPertemuan2 = new Jadwal("SistemOperasiPertemuan2",kelasSistemOperasiA, kelasSesi3, ruangChara31, "8 Agustus 2023");
        jadwalRepository.save(jadwalSistemOperasiPertemuan2);
        Jadwal jadwalSistemOperasiPertemuan3 = new Jadwal("SistemOperasiPertemuan3",kelasSistemOperasiA, kelasSesi3, ruangChara31, "15 Agustus 2023");
        jadwalRepository.save(jadwalSistemOperasiPertemuan3);
        Jadwal jadwalSistemOperasiPertemuan4 = new Jadwal("SistemOperasiPertemuan4",kelasSistemOperasiA, kelasSesi3, ruangChara31, "22 Agustus 2023");
        jadwalRepository.save(jadwalSistemOperasiPertemuan4);
        Jadwal jadwalSistemOperasiPertemuan5 = new Jadwal("SistemOperasiPertemuan5",kelasSistemOperasiA, kelasSesi3, ruangChara31, "29 Agustus 2023");
        jadwalRepository.save(jadwalSistemOperasiPertemuan5);
        Jadwal jadwalSistemOperasiPertemuan6 = new Jadwal("SistemOperasiPertemuan6",kelasSistemOperasiA, kelasSesi3, ruangChara31, "5 September 2023");
        jadwalRepository.save(jadwalSistemOperasiPertemuan6);

        PresensiMahasiswa presensiAndroidYose1 = new PresensiMahasiswa(jadwalAndroidPertemuan1, yose, false);
        presensiMahasiswaRepository.save(presensiAndroidYose1);
        PresensiMahasiswa presensiAndroidYose2 = new PresensiMahasiswa(jadwalAndroidPertemuan2, yose, false);
        presensiMahasiswaRepository.save(presensiAndroidYose2);
        PresensiMahasiswa presensiAndroidYose3 = new PresensiMahasiswa(jadwalAndroidPertemuan3, yose, false);
        presensiMahasiswaRepository.save(presensiAndroidYose3);
        PresensiMahasiswa presensiAndroidYose4 = new PresensiMahasiswa(jadwalAndroidPertemuan4, yose, false);
        presensiMahasiswaRepository.save(presensiAndroidYose4);
        PresensiMahasiswa presensiAndroidYose5 = new PresensiMahasiswa(jadwalAndroidPertemuan5, yose, false);
        presensiMahasiswaRepository.save(presensiAndroidYose5);
        PresensiMahasiswa presensiAndroidYose6 = new PresensiMahasiswa(jadwalAndroidPertemuan6, yose, false);
        presensiMahasiswaRepository.save(presensiAndroidYose6);

        PresensiMahasiswa presensiSistemOperasiYose1 = new PresensiMahasiswa(jadwalSistemOperasiPertemuan1, yose, false);
        presensiMahasiswaRepository.save(presensiSistemOperasiYose1);
        PresensiMahasiswa presensiSistemOperasiYose2 = new PresensiMahasiswa(jadwalSistemOperasiPertemuan2, yose, false);
        presensiMahasiswaRepository.save(presensiSistemOperasiYose2);
        PresensiMahasiswa presensiSistemOperasiYose3 = new PresensiMahasiswa(jadwalSistemOperasiPertemuan3, yose, false);
        presensiMahasiswaRepository.save(presensiSistemOperasiYose3);
        PresensiMahasiswa presensiSistemOperasiYose4 = new PresensiMahasiswa(jadwalSistemOperasiPertemuan4, yose, false);
        presensiMahasiswaRepository.save(presensiSistemOperasiYose4);
        PresensiMahasiswa presensiSistemOperasiYose5 = new PresensiMahasiswa(jadwalSistemOperasiPertemuan5, yose, false);
        presensiMahasiswaRepository.save(presensiSistemOperasiYose5);
        PresensiMahasiswa presensiSistemOperasiYose6 = new PresensiMahasiswa(jadwalSistemOperasiPertemuan6, yose, false);
        presensiMahasiswaRepository.save(presensiSistemOperasiYose6);
    }
}
