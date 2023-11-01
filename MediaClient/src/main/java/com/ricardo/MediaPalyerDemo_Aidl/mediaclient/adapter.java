package com.ricardo.MediaPalyerDemo_Aidl.mediaclient;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.ricardo.MediaPalyerDemo_Aidl.mediaclient.R;

import java.util.ArrayList;
import java.util.List;

public class adapter extends RecyclerView.Adapter<adapter.ViewHolder> {
    private List<TextView> mTextViews;
    private List<String> songs = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemOnClick(View view, int pos);
    }

    public void setLIst(List<String> list) {
        songs.clear();
        //此处可以list.addAll()
//        list.forEach(item -> songs.add(item));
        songs.addAll(list);
//        Log.d("List : ", songs.toString());
//        Log.d("=======>", songs.toString());
    }


    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public adapter(List<TextView> textViews) {
        mTextViews = textViews;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (songs.size() == 0) {
            holder.mtextView.setText(String.valueOf(position));
        } else {
            String string = songs.get(position);
            holder.mtextView.setText(string.substring(string.indexOf("-") + 1, string.indexOf(".")));
        }
//        Log.d("TEST", songs.toString());

        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemOnClick(holder.itemView, position);
                }
            });
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mtextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mtextView = itemView.findViewById(R.id.item);
        }
    }

    @Override
    public int getItemCount() {
        return songs==null?0:songs.size();
    }
}
