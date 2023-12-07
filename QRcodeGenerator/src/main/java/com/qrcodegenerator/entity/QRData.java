/*
 * Copyright 2016 Jan-Philipp Kappmeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qrcodegenerator.entity;

/**
 * Some data for updating website.
 */
public class QRData {
    /** Some data. */
    private String timeRemain;
    private String timeStart;

    private String timeEnd;

    private String qrCode;

    private String qrValue;

    public QRData(String timeRemain, String timeStart, String timeEnd, String qrCode, String qrValue) {
        this.timeRemain = timeRemain;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.qrCode = qrCode;
        this.qrValue = qrValue;
    }

    public QRData() {

    }

    public void setTimeRemain(String timeRemain) {
        this.timeRemain = timeRemain;
    }

    public String getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(String timeStart) {
        this.timeStart = timeStart;
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(String timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getTimeRemain() {
        return timeRemain;
    }

    public String getQrValue() {
        return qrValue;
    }

    public void setQrValue(String qrValue) {
        this.qrValue = qrValue;
    }
}

