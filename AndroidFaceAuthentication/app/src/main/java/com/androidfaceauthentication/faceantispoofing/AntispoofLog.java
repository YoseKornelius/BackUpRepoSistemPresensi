package com.androidfaceauthentication.faceantispoofing;

public class AntispoofLog {
    private long timestamp;
    private String result;
    private double confidence;

    private float luxValue;

    public AntispoofLog(long timestamp, String result, double confidence, float luxValue) {
        this.setConfidence(confidence);
        this.setResult(result);
        this.setTimestamp(timestamp);
        this.setLuxValue(luxValue);
    }


    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getLuxValue() {
        return luxValue;
    }

    public void setLuxValue(float luxValue) {
        this.luxValue = luxValue;
    }
}
