package com.berryspace.conjure;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.button.MaterialButton;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

public class OnboardingFragment extends Fragment {
    private final static String TAG = "OnboardingFragment";
    private int pageNumber = 0;

    public OnboardingFragment(int page){
        this.pageNumber = page;
    }

    private Boolean isTokenValid(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences("SPOTIFYAUTH", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("valid", false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    ViewGroup view;
    switch (pageNumber){
        case 0:
             view = (ViewGroup) inflater.inflate(
                    R.layout.fragment_onboarding_welcome, container, false);
            break;
        case 1:
            view = (ViewGroup) inflater.inflate(
                    R.layout.fragment_onboarding_setup, container, false);
            MaterialButton setup = view.findViewById(R.id.button_setup_spotify);
            AtomicReference<AppCompatTextView> statusMessage =
                    new AtomicReference<>(view.findViewById(R.id.onboarding_setup_status_message));
            setup.setOnClickListener(v->{
                statusMessage.set(view.findViewById(R.id.onboarding_setup_status_message));
                if(isTokenValid()){
                    statusMessage.get().setText(getText(R.string.onboarding_setup_status_success).toString());
                    setup.setVisibility(View.INVISIBLE);
                } else {
                    statusMessage.get().setText(getText(R.string.onboarding_setup_status_error).toString());
                    statusMessage.get().setTextColor(getResources().getColor(R.color.conjure_purple_really_dark));
                }
            });

            break;
        case 2:
             view = (ViewGroup) inflater.inflate(
                    R.layout.fragment_onboarding_scan, container, false);
            MaterialButton finish = view.findViewById(R.id.button_finish);
            finish.setOnClickListener(v -> {
                System.out.println("recording user finished onboarding");
                SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getSharedPreferences("NEWUSER",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("newUser", false);
                editor.apply();
                System.out.println("starting HomeActivity");
                Intent intent = new Intent(getActivity(), HomeActivity.class);
                startActivity(intent);
            });
            break;
        default:
            view = (ViewGroup) inflater.inflate(
                    R.layout.fragment_onboarding_welcome, container, false);
    }
        return view;
    }

}
