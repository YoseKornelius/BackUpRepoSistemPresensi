package com.androidfaceauthentication.view.rvKelas;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.androidfaceauthentication.R;

public class kelasViewHolder extends RecyclerView.ViewHolder {
    public TextView nama_matkul, jadwal_matkul;
    public CardView cardView;
    public kelasViewHolder(@NonNull View itemView) {
        super(itemView);
        nama_matkul = itemView.findViewById(R.id.nama_matkul);
        jadwal_matkul = itemView.findViewById(R.id. jadwal_matkul);
        cardView = itemView.findViewById(R.id.container_rv);
    }
}
