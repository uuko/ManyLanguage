package com.example.capturevideoandpictureandsaveandchoose.ui.choosedevice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capturevideoandpictureandsaveandchoose.R;

import java.util.ArrayList;

public class ChooseDeviceAdapter extends RecyclerView.Adapter {
    private ArrayList<ChooseDeviceItemData> dataList;
    private int CurrentPosition=0;
    public void setDataList(ArrayList<ChooseDeviceItemData> dataList){
        this.dataList=dataList;
        notifyDataSetChanged();

    }
    public int getCurrentPosition(){
        return CurrentPosition;
    }
    public void addDataToDataList(ChooseDeviceItemData mChooseDeviceItemData){
        dataList.add(mChooseDeviceItemData);
        notifyDataSetChanged();
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CurrentPosition=position;
                dataList.get(position).setBackgroundChange(!dataList.get(position).isBackgroundChange());
//                notifyDataSetChanged();
            }
        });
        if (holder instanceof ChooseDeviceAdapterViewHolder) {
            ((ChooseDeviceAdapterViewHolder) holder).getItemDevice().setText(dataList.get(position).getEQNO());
            ((ChooseDeviceAdapterViewHolder) holder).getNumber().setText(""+position);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view, parent, false);
        return new ChooseDeviceAdapterViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
    public ArrayList<ChooseDeviceItemData> getDataList(){
        return dataList;
    }
}