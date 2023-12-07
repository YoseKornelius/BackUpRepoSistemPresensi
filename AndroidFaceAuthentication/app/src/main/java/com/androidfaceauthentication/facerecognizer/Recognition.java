package com.androidfaceauthentication.facerecognizer;

/**
 * Project: MobileFaceNetAndroid
 * Package: com.mobilefacenetandroid.tflite
 * <p>
 * User: dendy
 * Date: 27/04/2021
 * Time: 6:11
 * <p>
 * Description : An immutable result object returned by a Classifier describing what was recognized.
 * Moved from SimilarityClassifier for ease of code structure
 */

import android.graphics.Bitmap;
import android.graphics.RectF;

public class Recognition {
    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    private String id;

    /**
     * Display name for the recognition.
     */
    private String title;

    /**
     * A sortable score for how good the recognition is relative to others. Lower should be better.
     */
    private Float distance;

    /**
     * Whether or not the model features quantized or float weights.
     */
    private boolean quant;

    private float[] embeddings;

    /**
     * Optional location within the source image for the location of the recognized object.
     */
    private RectF location;
    private Integer color;
    private Bitmap crop;

    public Recognition(final String id, final String title, final Float confidence, final boolean quant) {
        this.id = id;
        this.title = title;
        this.distance = confidence;
        this.quant = quant;
    }

    public Recognition(final String id, final String title, final Float distance, final RectF location) {
        this.id = id;
        this.title = title;
        this.distance = distance;
        this.location = location;
        this.color = null;
        this.embeddings = null;
        this.crop = null;
    }

    public void setEmbeddings(float[] embeddings) {
        this.embeddings = embeddings;
    }

    public float[] getEmbeddings() {
        return this.embeddings;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public RectF getLocation() {
        return this.location;
    }

    public void setLocation(RectF location) {
        this.location = location;
    }

    @Override
    public String toString() {
        String resultString = "";
        if (id != null) {
            resultString += "[" + id + "] ";
        }

        if (title != null) {
            resultString += title + " ";
        }

        if (distance != null) {
            resultString += String.format("(%.1f%%) ", distance * 100.0f);
        }

        if (location != null) {
            resultString += location + " ";
        }

        return resultString.trim();
    }

    public Integer getColor() {
        return this.color;
    }

    public void setCrop(Bitmap crop) {
        this.crop = crop;
    }

    public Bitmap getCrop() {
        return this.crop;
    }
}
