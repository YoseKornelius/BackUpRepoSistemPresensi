package com.androidfaceauthentication.view.rvKelas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.androidfaceauthentication.R;
import com.androidfaceauthentication.view.pojo.KelasDTOResponse;

import java.util.List;

public class KelasRVAdapter extends RecyclerView.Adapter<kelasViewHolder> {
    private Context context;
    private List <KelasDTOResponse> kelasDTORespons;
    private SelectListener listener;

    public KelasRVAdapter(Context context, List<KelasDTOResponse> kelasDTORespons, SelectListener listener) {
        this.context = context;
        this.kelasDTORespons = kelasDTORespons;
        this.listener = listener;
    }

    @NonNull
    @Override
    public kelasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new kelasViewHolder(LayoutInflater.from(context).inflate(R.layout.item_jadwal, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull kelasViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.nama_matkul.setText(kelasDTORespons.get(position).getMatakuliah() + " " + kelasDTORespons.get(position).getGrup());
        holder.jadwal_matkul.setText(kelasDTORespons.get(position).getDosen());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String jadwal = itemKelas.get(position).getJadwal();
//                String pattern = "^(\\w+)\\s(\\d{2}\\.\\d{2})\\s-\\s(\\d{2}\\.\\d{2})$";
//                Pattern regex = Pattern.compile(pattern);
//                Matcher matcher = regex.matcher(jadwal);
//                if (matcher.find()) {
//                    String a = matcher.group(1);
//                    LocalTime b = LocalTime.parse(matcher.group(2).replace(".", ":"));
//                    LocalTime c = LocalTime.parse(matcher.group(3).replace(".", ":"));
//
//                    boolean isCurrentTimeWithinRange = isTimeWithinRange(b, c);
//                    System.out.println("Current time is within the specified range: " + isCurrentTimeWithinRange);
//                }

                listener.onItemClicked(kelasDTORespons.get(position));

            }
        });

    }

    @Override
    public int getItemCount() {
        return kelasDTORespons.size();
    }

//    public static boolean isTimeWithinRange(LocalTime startTime, LocalTime endTime) {
//        LocalTime currentTime = LocalTime.now();
//
//        return (currentTime.isAfter(startTime) && currentTime.isBefore(endTime));
//    }
}
