package com.catiger.taxi.ui.notifications;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.catiger.taxi.data.LoginDataSource;
import com.catiger.taxi.data.LoginRepository;
import com.catiger.taxi.data.model.LoggedInUser;
import com.catiger.taxi.databinding.FragmentNotificationsBinding;
import com.catiger.taxi.ui.login.LoginActivity;

import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.internal.tls.BasicCertificateChainCleaner;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        TextView accountView = binding.account;
        if(hasLogged(this.getContext())){
            accountView.setText(getLoginAccount(this.getContext()));
        }else{
            startLoginActivity(this.getContext());
        }
        final TextView textView = binding.textNotifications;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private boolean hasLogged(Context context) {
        LoginRepository loginRepository = LoginRepository.getInstance(new LoginDataSource());
        LoggedInUser user = loginRepository.getUser();
        return user != null;
    }

    private String getLoginAccount(Context context) {
        LoginRepository loginRepository = LoginRepository.getInstance(new LoginDataSource());
        LoggedInUser user = loginRepository.getUser();
        return user.getDisplayName();
    }
    private void startLoginActivity(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        startActivityForResult(intent, 10);
    }
}