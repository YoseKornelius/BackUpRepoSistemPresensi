package com.androidfaceauthentication.view.rvDetailKelas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.regex.*;

import com.androidfaceauthentication.R;
import com.androidfaceauthentication.view.pojo.JadwalDTOResponse;
import com.androidfaceauthentication.view.rvKelas.SelectListener;

import java.util.List;

public class JadwalRVAdapter extends RecyclerView.Adapter<JadwalViewHolder> {

    private JadwalSelectListener listener;
    private Context context;
    private List<JadwalDTOResponse> jadwalDTOResponses;

    public JadwalRVAdapter(JadwalSelectListener listener, Context context, List<JadwalDTOResponse> jadwalDTOResponses) {
        this.listener = listener;
        this.context = context;
        this.jadwalDTOResponses = jadwalDTOResponses;
    }

    @NonNull
    @Override
    public JadwalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new JadwalViewHolder(LayoutInflater.from(context).inflate(R.layout.item_kelas, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull JadwalViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.tanggal.setText(formatString(jadwalDTOResponses.get(position).getJadwal()));
        holder.sesi.setText(jadwalDTOResponses.get(position).getSesi());
        holder.ruang.setText(jadwalDTOResponses.get(position).getTanggal());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClicked(jadwalDTOResponses.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return jadwalDTOResponses.size();
    }

    public static String formatString(String input) {
        String output = input.replaceAll("([a-z])([A-Z])", "$1 $2").replaceAll("(\\d+)$", " $1");
        output = output.replaceAll("(\\d+)", "-$1");
        return output;
    }


}
