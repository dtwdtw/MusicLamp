package com.imt.musiclamp.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.imt.musiclamp.R;

/**
 * Created by dtw on 15/3/6.
 */
public class ValueChangeFrag extends DialogFragment{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.devicevelue,null);
        return  v;
    }
}
