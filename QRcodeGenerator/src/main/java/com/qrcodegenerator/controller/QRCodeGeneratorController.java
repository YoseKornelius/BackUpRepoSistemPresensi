package com.qrcodegenerator.controller;

import com.google.zxing.WriterException;
import com.qrcodegenerator.entity.Jadwal;
import com.qrcodegenerator.entity.QRData;
import com.qrcodegenerator.repository.JadwalRepository;
import com.qrcodegenerator.request.QRCodeScanResultRequest;
import com.qrcodegenerator.response.QrCodeScanResult;
import com.qrcodegenerator.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class QRCodeGeneratorController {

    @Autowired
    private QRCodeService qrCodeService;

    private final List<SseEmitter> sseEmitter = new LinkedList<>();

        //in second
    private int TIME_LIMIT = 15;
    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
    SimpleDateFormat sdfTimeRemaining = new SimpleDateFormat("mm:ss");

    @Autowired
    JadwalRepository jadwalRepository;

    Timestamp timestart;
    Timestamp timeend;

    String formattedStartTime;
    String formattedEndTime;

    ScheduledThreadPoolExecutor scheduledPool;
    String qrCode;
    String qrValue;

    @GetMapping("/")
    public String home() {
        return "qr-view";
    }

    @PostMapping("/generateqr")
    public String generateQRCode(@RequestParam("qrValue") String qrValue,
                                 Model model) throws IOException, WriterException {
        timestart = new Timestamp(System.currentTimeMillis());
        timeend = new Timestamp(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(TIME_LIMIT));
        formattedStartTime = sdfTime.format(timestart);
        formattedEndTime = sdfTime.format(timeend);
        this.qrValue = qrValue + timestart;
        if (qrValue == null || qrValue.isBlank() || qrValue.isEmpty()) {
            return "redirect:/";
        }
        qrCode = qrCodeService.getQRCode(this.qrValue);
//        scheduledPool = Executors.newScheduledThreadPool(1);
        if (scheduledPool != null) {
            scheduledPool.shutdown();
        }

        scheduledPool = new ScheduledThreadPoolExecutor(1);
        scheduledPool.scheduleWithFixedDelay(changeTime, 0, 1, TimeUnit.SECONDS);
//        scheduledPool.scheduleWithFixedDelay(new ChangeTimeThread(timestart, timeend, qrCode, qrValue, sseEmitter), 0, 1, TimeUnit.SECONDS);

        model.addAttribute("qrvalue", this.qrValue);
        /*
        model.addAttribute("timestart", sdfTime.format(timestart));
        model.addAttribute("timeend", sdfTime.format(timeend));
        model.addAttribute("qrcode", qrCode);
*/
        byte[] qrImg = qrCodeService.getQRCodeImage(qrValue, 250, 250);
        System.out.println(qrCodeService.decodeQR(qrImg));
//        return "qr-view";
        return "qrcode :: qr_code";
    }

    public RedirectView recreateQR() {
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("http://localhost:8080/generate");
        return redirectView;
    }

    @GetMapping(path = "/registertime", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter registertime() {
        System.out.println("Registering a time.");
//        log.info("Registering a stream.");

        SseEmitter emitter = new SseEmitter();
        synchronized (sseEmitter) {
            sseEmitter.add(emitter);
        }
        emitter.onCompletion(() -> sseEmitter.remove(emitter));

        return emitter;
    }

    @PostMapping("/verifyqrcode")
    public ResponseEntity<QrCodeScanResult> verifyQRCode(@RequestBody QRCodeScanResultRequest scanResult) {
        // Lakukan verifikasi QR code disini
        QrCodeScanResult qrCodeScanResult = new QrCodeScanResult("invalid qrcode");
        System.out.println("id jadwal : " + scanResult.getKodeJadwal());
        Jadwal jadwal = jadwalRepository.findById(scanResult.getKodeJadwal()).get();

        String textPenguji = jadwal.getKataKunciQR();
        System.out.println(textPenguji + " Patokan");
        System.out.println(scanResult.getQrCodeData() + " raw result dari android");

//        LocalTime startTime = LocalTime.parse(formattedStartTime);
//        LocalTime endTime = LocalTime.parse(formattedEndTime);
//        System.out.println(startTime + " waktu dimulai QR");
//        System.out.println(endTime + " waktu kelar QR");
//        String pattern = "^(.*?)(\\d{4}-\\d{2}-\\d{2})\\s(\\d{2}:\\d{2}:\\d{2})(\\.\\d+)?$";
//        Pattern regex = Pattern.compile(pattern);
//        Matcher matcher = regex.matcher(scanResult.getQrCodeData());
//        if (matcher.find()) {
//            String a = matcher.group(1);
//            String b = matcher.group(2);
//            String c = matcher.group(3);
//            String d = matcher.group(4);
//
//            System.out.println(a + " kelas");
//            System.out.println(b + " tanggal");
//            System.out.println(c + " waktu");
//            System.out.println(d + " buntut");
//
//            LocalTime waktu = LocalTime.parse(c);
//            LocalTime waktuPlusSatuMenit = waktu.plusMinutes(1);
//            System.out.println(waktuPlusSatuMenit + " percobaan ditambah satumenit");
//            Boolean isBetweenStartAndStopStrictlySpeaking =
//                    (waktuPlusSatuMenit.isAfter(startTime) && waktuPlusSatuMenit.isBefore(endTime));
//            System.out.println("hasil percobaan boolean " + isBetweenStartAndStopStrictlySpeaking);
//
//            if (textPenguji.equals(scanResult.getQrCodeData())) {
//                System.out.println("tepat waktu");
//                qrCodeScanResult.setQrCodeData("verify qrcode berhasil");
//                return ResponseEntity.ok(qrCodeScanResult);
//            }
//        }
        if (textPenguji.isEmpty()){
            return ResponseEntity.ok(qrCodeScanResult);

        }
        if (textPenguji.equals(scanResult.getQrCodeData())){
            qrCodeScanResult.setQrCodeData("verify qrcode berhasil");
            System.out.println("VERIFY QRCODE BERHASIL");
            return ResponseEntity.ok(qrCodeScanResult);
        }
        return ResponseEntity.ok(qrCodeScanResult);
    }

    @RequestMapping(value = "/test_ajax", method = RequestMethod.GET)
    public String sendHtml(Model map) {
        //map.addAttribute("foo", "bar");
        return "testajax";
    }

    @RequestMapping(value = "/test_ajax_frag", method = RequestMethod.POST)
    public String sendHtmlFragment(Model map) {
        //map.addAttribute("foo", "bar");
        return "testajaxfragment :: test_frag";
    }

    private final Runnable changeTime = () -> {
        long timeRemain = timeend.getTime() - System.currentTimeMillis();
        QRData qrData = new QRData(sdfTimeRemaining.format(new Date(timeRemain)),
                sdfTime.format(new Date(timestart.getTime())),
                sdfTime.format(new Date(timeend.getTime())),
                qrCode, qrValue
        );
        System.out.println("Sending timer message : " + qrData.getTimeRemain());
        synchronized (sseEmitter) {
            sseEmitter.forEach((SseEmitter emitter) -> {
                if (timeRemain <= 0) {
                    emitter.complete();
                    System.out.println("times out. close thread!");

                    scheduledPool.shutdownNow();
                } else {
                    try {
                        emitter.send(qrData, MediaType.APPLICATION_JSON);
                    } catch (IOException e) {
                        emitter.complete();
                        sseEmitter.remove(emitter);
                    }
                }
            });

        }
    };

}
