package com.firma.dev;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.firma.dev.letschat.R;

public class AlertDFragment extends DialogFragment {

    Button okBtn;
    TextView title;
    TextView desc;

    public String getTitleTxt() {
        return titleTxt;
    }

    public void setTitleTxt(String titleTxt) {
        this.titleTxt = titleTxt;
    }

    public String getDescTxt() {
        return descTxt;
    }

    public void setDescTxt(String descTxt) {
        this.descTxt = descTxt;
    }

    String titleTxt;
    String descTxt;


    public AlertDFragment(String titleTxt, String descTxt){
        this.titleTxt = titleTxt;
        this.descTxt = descTxt;
    }

    public AlertDFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.ok_dialog,container);


        okBtn = (Button) view.findViewById(R.id.okBtn);
        title = (TextView) view.findViewById(R.id.title);
        desc = (TextView) view.findViewById(R.id.desc);

        title.setText(titleTxt);
        desc.setText(descTxt);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }
}
