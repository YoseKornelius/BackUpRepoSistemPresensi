package com.qrcodegenerator.controller;

import com.qrcodegenerator.entity.*;
import com.qrcodegenerator.repository.JadwalRepository;
import com.qrcodegenerator.repository.MahasiswaRepository;
import com.qrcodegenerator.repository.PresensiMahasiswaRepository;
import com.qrcodegenerator.request.PresensiMahasiswaRequest;

import com.qrcodegenerator.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

@Controller
public class PresensiMahasiswaController {

    @Autowired
    PresensiMahasiswaRepository presensiMahasiswaRepository;

    @Autowired
    MahasiswaRepository mahasiswaRepository;

    @Autowired
    JadwalRepository jadwalRepository;

    private final SseEmitters emitters = new SseEmitters();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    SimpleDateFormat sdfTimeRemaining = new SimpleDateFormat("mm:ss");
    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
    Map<String, TimerThread> timerThreadMap = new HashMap<>();

    @Autowired
    private QRCodeService qrCodeService;

    //in second
    private int TIME_LIMIT = 30;

    @PostMapping("/presensimhs")
    public ResponseEntity<Boolean> generateQRCode(@RequestBody PresensiMahasiswaRequest request) {

        if (jadwalRepository.findById(request.idJadwal).isPresent() &&
                mahasiswaRepository.findById(request.nim).isPresent()) {

            //finding jadwal
            Jadwal jadwal = jadwalRepository.findById(request.idJadwal).get();

            //finding mahasiswa
            Mahasiswa mahasiswa = mahasiswaRepository.findById(request.nim).get();

            PresensiMahasiswa daftarPresensiMahasiswa = presensiMahasiswaRepository
                    .findByJadwalAndMahasiswa(jadwal, mahasiswa);
            daftarPresensiMahasiswa.setHadir(true);
            presensiMahasiswaRepository.save(daftarPresensiMahasiswa);
           /* List<PresensiMahasiswa> presensiMahasiswaList = presensiMahasiswaRepository
                    .findAllByPresensiMahasiswaId(new PresensiMahasiswaId(jadwal, mahasiswa));*/
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/statuspresensimhs")
    public ResponseEntity<Boolean> cekStatusPresensiMhs(@RequestBody PresensiMahasiswaRequest request){

        if (jadwalRepository.findById(request.idJadwal).isPresent() &&
                mahasiswaRepository.findById(request.nim).isPresent()) {

            //finding jadwal
            Jadwal jadwal = jadwalRepository.findById(request.idJadwal).get();

            //finding mahasiswa
            Mahasiswa mahasiswa = mahasiswaRepository.findById(request.nim).get();

            PresensiMahasiswa daftarPresensiMahasiswa = presensiMahasiswaRepository
                    .findByJadwalAndMahasiswa(jadwal, mahasiswa);
            if (daftarPresensiMahasiswa.isHadir()){
                return ResponseEntity.ok(true);
            }
            else {
                return ResponseEntity.ok(false);
            }

        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(path = "/ssepresensi", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter getPerformance() {
        return emitters.add();
    }

    @PostMapping("/generatepresensiqr")
    public String generateQRCode(@RequestParam("kodeJadwal") String kodeJadwal,
                                 Model model) {

        Timestamp timestart = new Timestamp(System.currentTimeMillis());
        Timestamp timeend = new Timestamp(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(TIME_LIMIT));
        String theQr = kodeJadwal + timestart;
        String qrCode = qrCodeService.getQRCode(theQr);

        //make sure only one thread run for per kodejadwal
        if (!timerThreadMap.containsKey(kodeJadwal)) {
            TimerThread theThread = new TimerThread(kodeJadwal, timestart, timeend, theQr, qrCode);
            timerThreadMap.put(kodeJadwal, theThread);
            threadPool.submit(theThread);
            Jadwal jadwal = jadwalRepository.findById(kodeJadwal).get();
            jadwal.setKataKunciQR(theQr);
            jadwalRepository.save(jadwal);
            System.out.println("kata kunci qrcode = " + jadwal.getKataKunciQR());
        }
        //todo : still have problem with duplicated view when webmvc clicked
        return "qrcode :: qr_code";
    }

    public class TimerThread implements Runnable {

        String threadID;
        // to stop the thread
        private boolean exit = false;
        Timestamp timestart;
        Timestamp timeend;
        String theQr;
        String qrCode;

        public TimerThread(Timestamp timestart, Timestamp timeend, String theQr, String qrCode) {
            this.timestart = timestart;
            this.timeend = timeend;
            this.theQr = theQr;
            this.qrCode = qrCode;
        }

        public TimerThread(String threadID, Timestamp timestart, Timestamp timeend, String theQr, String qrCode) {
            this.threadID = threadID;
            this.timestart = timestart;
            this.timeend = timeend;
            this.theQr = theQr;
            this.qrCode = qrCode;
        }

        public void run() {
            while (!exit) {
                System.out.println("Now the thread is running ...");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Caught:" + e);
                }

                long timeRemain = timeend.getTime() - System.currentTimeMillis();

                if (timeRemain > 0) {
                    QRData qrData = new QRData(sdfTimeRemaining.format(new Date(timeRemain)),
                            sdfTime.format(new Date(timestart.getTime())),
                            sdfTime.format(new Date(timeend.getTime())),
                            qrCode, theQr
                    );
                    emitters.send(qrData);
                } else {
                    Jadwal jadwal = jadwalRepository.findById(threadID).get();
                    System.out.println("kata kunci qr code : " + jadwal.getKataKunciQR());
                    System.out.println("PENGHAPUSAN");
                    jadwal.setKataKunciQR(null);
                    jadwalRepository.save(jadwal);
                    System.out.println("kata kunci qr code : " + jadwal.getKataKunciQR());
                    timerThreadMap.remove(threadID);
                    exit = true;
                }
            }
        }
    }
}
