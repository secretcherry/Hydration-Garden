package com.example.hydrationgarden.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.hydrationgarden.R;
import com.example.hydrationgarden.databinding.FragmentWaterInputBinding;

public class WaterInputFragment extends DialogFragment {
    private FragmentWaterInputBinding binding;
    private OnWaterAddedListener onWaterAddedListener;

    // Predefined amounts
    private final int[] quickAmounts = {250, 500, 750, 1000};

    public interface OnWaterAddedListener {
        void onWaterAdded(int amount);
    }

    public void setOnWaterAddedListener(OnWaterAddedListener listener) {
        this.onWaterAddedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWaterInputBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupClickListeners();
        setupQuickButtons();
    }

    private void setupClickListeners() {
        binding.btnAdd.setOnClickListener(v -> {
            String amountStr = binding.etAmount.getText().toString().trim();

            if (amountStr.isEmpty()) {
                binding.etAmount.setError("Unesite količinu");
                return;
            }

            try {
                int amount = Integer.parseInt(amountStr);

                if (amount <= 0) {
                    binding.etAmount.setError("Količina mora biti veća od 0");
                    return;
                }

                if (amount > 2000) {
                    binding.etAmount.setError("Količina ne može biti veća od 2000ml");
                    return;
                }

                if (onWaterAddedListener != null) {
                    onWaterAddedListener.onWaterAdded(amount);
                }

                dismiss();

            } catch (NumberFormatException e) {
                binding.etAmount.setError("Unesite valjanu količinu");
            }
        });

        binding.btnCancel.setOnClickListener(v -> dismiss());
    }

    private void setupQuickButtons() {
        binding.btn250ml.setOnClickListener(v -> setAmount(250));
        binding.btn500ml.setOnClickListener(v -> setAmount(500));
        binding.btn750ml.setOnClickListener(v -> setAmount(750));
        binding.btn1000ml.setOnClickListener(v -> setAmount(1000));
    }

    private void setAmount(int amount) {
        binding.etAmount.setText(String.valueOf(amount));
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