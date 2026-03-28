package com.messmate.delivery.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.messmate.delivery.databinding.FragmentProfileBinding;
import com.messmate.delivery.socket.SocketManager;
import com.messmate.delivery.utils.SharedPreferencesManager;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SharedPreferencesManager prefsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsManager = new SharedPreferencesManager(requireContext());

        binding.tvProfileName.setText(prefsManager.getAgentName());
        binding.tvProfilePhone.setText("Agent ID: " + prefsManager.getAgentId());

        binding.btnLogout.setOnClickListener(v -> {
            prefsManager.clearAll();
            SocketManager.getInstance().disconnect();
            
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
