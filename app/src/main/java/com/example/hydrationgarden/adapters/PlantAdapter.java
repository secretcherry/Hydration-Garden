package com.example.hydrationgarden.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hydrationgarden.R;
import com.example.hydrationgarden.models.Plant;
import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {
    private List<Plant> plantList;
    private OnPlantClickListener onPlantClickListener;

    public interface OnPlantClickListener {
        void onPlantClick(Plant plant);
    }

    public PlantAdapter(List<Plant> plantList, OnPlantClickListener listener) {
        this.plantList = plantList;
        this.onPlantClickListener = listener;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plantList.get(position);
        holder.bind(plant);
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    class PlantViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPlant;
        private TextView tvPlantName;
        private TextView tvPlantMessage;
        private View itemView;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            ivPlant = itemView.findViewById(R.id.ivPlant);
            tvPlantName = itemView.findViewById(R.id.tvPlantName);
            tvPlantMessage = itemView.findViewById(R.id.tvPlantMessage);
        }

        public void bind(Plant plant) {
            tvPlantName.setText(plant.getName());
            tvPlantMessage.setText(plant.getCurrentMessage());

            ivPlant.setImageResource(plant.getCurrentImageResId());

            if (plant.isHappy()) {
                itemView.setBackgroundColor(0xFFFFFFFF);
            } else {
                itemView.setBackgroundColor(0xFFFFFFFF);
            }

            itemView.setOnClickListener(v -> {
                if (onPlantClickListener != null) {
                    onPlantClickListener.onPlantClick(plant);
                }
            });
        }
    }
}