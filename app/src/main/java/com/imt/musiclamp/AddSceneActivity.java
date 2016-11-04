package com.imt.musiclamp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.imt.musiclamp.fragment.FragmentAddScene0;
import com.imt.musiclamp.fragment.FragmentAddScene1;
import com.imt.musiclamp.fragment.FragmentAddScene2;
import com.imt.musiclamp.fragment.IndexFragment;
import com.imt.musiclamp.model.Scene;
import com.imt.musiclamp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class AddSceneActivity extends FragmentActivity {

    int position = 0;

    private Fragment[] fragments = {new FragmentAddScene1(), new FragmentAddScene2(), new FragmentAddScene0()};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scene);
        ButterKnife.inject(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new FragmentAddScene1(), "0")
                .commit();

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < 10; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("songID", 1);
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.e("getAddSceneJson", Utils.getAddSceneJson("", 1, 1, 1, 1, jsonArray));
    }

    private EditText editTextDialogName;
    private TextView textViewDialogContent;
    private FragmentAddScene1 fragment0;
    private FragmentAddScene0 fragment1;
    private FragmentAddScene2 fragment2;

    @OnClick({R.id.imageView_back, R.id.textView_next})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_back:
                if (position == 0) {
                    finish();
                } else if (position == 1) {
                    switchToFragment0();
                } else if (position == 2) {
                    backToFragment1();
                }
                position -= 1;
                break;
            case R.id.textView_next:
                if (position == 0) {
                    switchToFragment1();
                } else if (position == 1) {
                    switchToFragment2();
                } else if (position == 2) {
                    MaterialDialog dialog = new MaterialDialog.Builder(AddSceneActivity.this)
                            .customView(R.layout.dialog_add_scene, false)
                            .positiveText("保存")
                            .negativeText(R.string.cancel)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    Scene scene = new Scene();
                                    scene.setName(editTextDialogName.getText().toString());
                                    scene.setJsonArrayMusicList(fragment1.getMusicList().toString());

                                    Bundle bundle = fragment0.getPickerData();
                                    scene.setR(bundle.getInt("r"));
                                    scene.setG(bundle.getInt("g"));
                                    scene.setB(bundle.getInt("b"));
                                    scene.setBrightness(bundle.getInt("brightness"));
                                    scene.setVolume(bundle.getInt("volume"));
                                    scene.setColor(bundle.getBoolean("isColor"));

                                    bundle = fragment2.getData();
                                    scene.setTiming(bundle.getBoolean("isTiming"));
                                    scene.setHour(bundle.getInt("hour"));
                                    scene.setMinute(bundle.getInt("minute"));

                                    scene.save();
                                    Toast.makeText(AddSceneActivity.this, R.string.add_successfully, Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                            .build();
                    fragment0 = (FragmentAddScene1) getSupportFragmentManager().findFragmentByTag("0");
                    fragment1 = (FragmentAddScene0) getSupportFragmentManager().findFragmentByTag("1");
                    fragment2 = (FragmentAddScene2) getSupportFragmentManager().findFragmentByTag("2");
                    editTextDialogName = (EditText) dialog.getCustomView().findViewById(R.id.editText_name);
                    textViewDialogContent = (TextView) dialog.getCustomView().findViewById(R.id.textView_content);
                    textViewDialogContent.setText(String.format("R:%s,G:%s,B:%s,brightness:%s," + getResources().getString(R.string.timing) + ":%s,%s" + getResources().getString(R.string.hours)
                            + ":%s" + getResources().getString(R.string.minutes),
                            fragment0.getPickerData().getInt("r"),
                            fragment0.getPickerData().getInt("g"),
                            fragment0.getPickerData().getInt("b"),
                            fragment0.getPickerData().getInt("brightness"),
                            fragment2.getData().getBoolean("isTiming"),
                            fragment2.getData().getInt("hour"),
                            fragment2.getData().getInt("minute")));
                    dialog.show();
                }
                position += 1;
                break;
        }
    }

    private void switchToFragment0() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        fragment0 = (FragmentAddScene1) getSupportFragmentManager().findFragmentByTag("0");
        fragment1 = (FragmentAddScene0) getSupportFragmentManager().findFragmentByTag("1");

        if (!fragment0.isAdded()) {
            transaction.hide(fragment1).add(R.id.content_frame, fragment0, "0");
        } else {
            Log.e("colorPickerFragment", "isAdd");
            transaction.hide(fragment1).show(fragment0);
        }
        transaction.commit();
    }

    private void switchToFragment1() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        fragment0 = (FragmentAddScene1) getSupportFragmentManager().findFragmentByTag("0");
        fragment1 = (FragmentAddScene0) getSupportFragmentManager().findFragmentByTag("1");

        if (fragment1 == null) {
            fragment1 = new FragmentAddScene0();
        }
        if (!fragment1.isAdded()) {
            transaction.hide(fragment0).add(R.id.content_frame, fragment1, "1");
        } else {
            Log.e("colorPickerFragment", "isAdd");
            transaction.hide(fragment0).show(fragment1);
        }
        transaction.commit();
    }

    private void backToFragment1() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        fragment1 = (FragmentAddScene0) getSupportFragmentManager().findFragmentByTag("1");
        fragment2 = (FragmentAddScene2) getSupportFragmentManager().findFragmentByTag("2");

        if (fragment1 == null) {
            fragment1 = new FragmentAddScene0();
        }
        if (!fragment1.isAdded()) {
            transaction.hide(fragment2).add(R.id.content_frame, fragment1, "1");
        } else {
            Log.e("colorPickerFragment", "isAdd");
            transaction.hide(fragment2).show(fragment1);
        }
        transaction.commit();
    }

    private void switchToFragment2() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        fragment1 = (FragmentAddScene0) getSupportFragmentManager().findFragmentByTag("1");
        fragment2 = (FragmentAddScene2) getSupportFragmentManager().findFragmentByTag("2");

        if (fragment2 == null) {
            fragment2 = new FragmentAddScene2();
        }
        if (!fragment2.isAdded()) {
            transaction.hide(fragment1).add(R.id.content_frame, fragment2, "2");
        } else {
            Log.e("colorPickerFragment", "isAdd");
            transaction.hide(fragment1).show(fragment2);
        }
        transaction.commit();
    }


}
