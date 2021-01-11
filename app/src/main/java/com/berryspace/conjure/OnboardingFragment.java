package com.berryspace.conjure;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import java.util.Objects;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OnboardingFragment extends Fragment {
    private final static String TAG = "OnboardingFragment";
    private int pageNumber = 0;
    private TextView statusMessage;
    private MaterialButton setup;

    public OnboardingFragment(int page){
        this.pageNumber = page;
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
            setup = view.findViewById(R.id.button_setup_spotify);
            statusMessage = view.findViewById(R.id.onboarding_setup_status_message) ;
            setup.setOnClickListener(v->{
                authenticateWithSpotify();
            });
            break;
        case 2:
             view = (ViewGroup) inflater.inflate(
                    R.layout.fragment_onboarding_scan, container, false);

            ImageView phone = view.findViewById(R.id.image_phone);


            ObjectAnimator animator = (ObjectAnimator) AnimatorInflater.loadAnimator(this.getActivity(),
                    R.animator.album_scan);
            animator.setTarget(phone);
            animator.start();

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

    private void authenticateWithSpotify(){
        Intent intent = new Intent(getActivity(), SpotifyAuthActivity.class);
        startActivityForResult(intent, 0);
    };

    private Boolean isTokenValid(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences("SPOTIFYAUTH", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("valid", false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "spotify auth process complete");

        if(isTokenValid()){
            statusMessage.setText(getText(R.string.onboarding_setup_status_success).toString());
            setup.setVisibility(View.INVISIBLE);
        } else {
            statusMessage.setText(getText(R.string.onboarding_setup_status_error).toString());
            setup.setVisibility(View.INVISIBLE);
        }
    }

}
