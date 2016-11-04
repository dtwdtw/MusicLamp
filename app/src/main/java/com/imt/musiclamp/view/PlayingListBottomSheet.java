package com.imt.musiclamp.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.adapter.LocalPlayingListAdapter;
import com.imt.musiclamp.adapter.OnlinePlayingListAdapter;
import com.imt.musiclamp.event.PlayingListPlayEvent;

import de.greenrobot.event.EventBus;


public class PlayingListBottomSheet extends Dialog implements DialogInterface {

    private Context context;
    private MyApplication myApplication;
    private boolean isLocal;

    public PlayingListBottomSheet(Context context, MyApplication myApplication, boolean isLocal) {
        super(context, com.cocosw.bottomsheet.R.style.BottomSheet_Dialog);
        this.context = context;
        this.myApplication = myApplication;
        this.isLocal = isLocal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCanceledOnTouchOutside(true);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.sheet_playing_list, null, false);
        ListView listView = (ListView) sheetView.findViewById(R.id.listView);
        if (isLocal) {
            LocalPlayingListAdapter adapter = new LocalPlayingListAdapter(context, myApplication);
            listView.setAdapter(adapter);
            listView.setSelection(myApplication.getCurrentLocalPosition());
        } else {
            OnlinePlayingListAdapter adapter = new OnlinePlayingListAdapter(context, myApplication);
            listView.setAdapter(adapter);
            listView.setSelection(myApplication.getCurrentOnlinePosition());
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlayingListPlayEvent playingListPlayEvent = new PlayingListPlayEvent();
                playingListPlayEvent.setPosition(position);
                playingListPlayEvent.setLocal(isLocal);
                EventBus.getDefault().post(playingListPlayEvent);
                PlayingListBottomSheet.this.dismiss();
            }
        });
        Button buttonClose = (Button) sheetView.findViewById(R.id.button_close);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayingListBottomSheet.this.dismiss();
            }
        });
        setContentView(sheetView);


        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;

        TypedArray a = getContext().obtainStyledAttributes(new int[]{android.R.attr.layout_width});
        try {
            params.width = a.getLayoutDimension(0, ViewGroup.LayoutParams.MATCH_PARENT);
        } finally {
            a.recycle();
        }

        getWindow().setAttributes(params);
    }


}
