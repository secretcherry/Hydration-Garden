package com.example.hydrationgarden.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.hydrationgarden.databinding.FragmentPlantDialogBinding;
import com.example.hydrationgarden.models.Plant;

public class PlantDialogFragment extends DialogFragment {
    private static final String ARG_PLANT_ID = "plant_id";
    private static final String ARG_PLANT_NAME = "plant_name";
    private static final String ARG_PLANT_TYPE = "plant_type";
    private static final String ARG_IS_HAPPY = "is_happy";
    private static final String ARG_HAPPY_MESSAGE = "happy_message";
    private static final String ARG_SAD_MESSAGE = "sad_message";
    private static final String ARG_HAPPY_IMAGE = "happy_image";
    private static final String ARG_SAD_IMAGE = "sad_image";

    private FragmentPlantDialogBinding binding;
    private Plant plant;

    public static PlantDialogFragment newInstance(Plant plant) {
        PlantDialogFragment fragment = new PlantDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLANT_ID, plant.getId());
        args.putString(ARG_PLANT_NAME, plant.getName());
        args.putString(ARG_PLANT_TYPE, plant.getType().getDisplayName());
        args.putBoolean(ARG_IS_HAPPY, plant.isHappy());
        args.putString(ARG_HAPPY_MESSAGE, plant.getHappyMessage());
        args.putString(ARG_SAD_MESSAGE, plant.getSadMessage());
        args.putInt(ARG_HAPPY_IMAGE, plant.getHappyImageResId());
        args.putInt(ARG_SAD_IMAGE, plant.getSadImageResId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            plant = new Plant();
            plant.setId(getArguments().getString(ARG_PLANT_ID));
            plant.setName(getArguments().getString(ARG_PLANT_NAME));
            plant.setHappy(getArguments().getBoolean(ARG_IS_HAPPY));
            plant.setHappyMessage(getArguments().getString(ARG_HAPPY_MESSAGE));
            plant.setSadMessage(getArguments().getString(ARG_SAD_MESSAGE));
            plant.setHappyImageResId(getArguments().getInt(ARG_HAPPY_IMAGE));
            plant.setSadImageResId(getArguments().getInt(ARG_SAD_IMAGE));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPlantDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (plant != null) {
            setupPlantInfo();
        }

        binding.btnClose.setOnClickListener(v -> dismiss());
    }

    private void setupPlantInfo() {
        binding.tvPlantName.setText(plant.getName());
        binding.tvPlantType.setText(getArguments().getString(ARG_PLANT_TYPE));
        binding.ivPlantLarge.setImageResource(plant.getCurrentImageResId());
        binding.tvPlantMessage.setText(plant.getCurrentMessage());

        // Set mood indicator
        if (plant.isHappy()) {
            binding.tvMoodIndicator.setText("Sretna");
            binding.tvMoodIndicator.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            binding.tvAdvice.setText("Nastavi ovako! Tvoja biljka je zadovoljna količinom vode.");
        } else {
            binding.tvMoodIndicator.setText("Tužna");
            binding.tvMoodIndicator.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            binding.tvAdvice.setText("Popij još malo vode da usrećiš svoju biljku!");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}