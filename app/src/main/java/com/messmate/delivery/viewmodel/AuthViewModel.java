package com.messmate.delivery.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.messmate.delivery.models.LoginResponse;
import com.messmate.delivery.repository.AuthRepository;
import com.messmate.delivery.utils.Resource;

public class AuthViewModel extends ViewModel {

    private final AuthRepository repository;

    public AuthViewModel(AuthRepository repository) {
        this.repository = repository;
    }

    /* ============================================================
       🔥 NEW LOGIN (FIREBASE OTP)
    ============================================================ */

    public LiveData<Resource<LoginResponse>> firebaseLogin(String firebaseToken) {
        return repository.firebaseLogin(firebaseToken);
    }
}