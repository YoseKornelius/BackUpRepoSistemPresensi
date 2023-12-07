package com.qrcodegenerator.response;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
public class KelasDTOResponse {
    private String kodeKelas;
    private String matakuliah;
    private String semester;
    private String dosen;
    private String grup;

    public KelasDTOResponse(String kodeKelas, String matakuliah, String semester,String dosen, String
                             grup) {
        this.kodeKelas = kodeKelas;
        this.matakuliah = matakuliah;
        this.semester = semester;
        this.dosen = dosen;
        this.grup = grup;
    }

}
