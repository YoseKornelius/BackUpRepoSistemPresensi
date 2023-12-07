package com.androidfaceauthentication.view.rvDetailKelas;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.androidfaceauthentication.R;

public class JadwalViewHolder extends RecyclerView.ViewHolder {

    public TextView ruang, sesi, tanggal;
    public CardView cardView;

    public JadwalViewHolder(@NonNull View itemView) {
        super(itemView);
        ruang = itemView.findViewById(R.id.ruang);
        sesi = itemView.findViewById(R.id.sesi);
        tanggal = itemView.findViewById(R.id.tanggal);
        cardView = itemView.findViewById(R.id.container_rv1);

    }
}
