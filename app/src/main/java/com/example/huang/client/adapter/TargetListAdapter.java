package com.example.huang.client.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.huang.client.R;

import com.example.huang.client.config.App2;
import com.example.huang.client.entity.Target;

public class TargetListAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;

    public TargetListAdapter(Context context) {
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return App2.targetList.size();
    }

    @Override
    public Object getItem(int position) {
        return App2.targetList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Target target = (Target) getItem(position);
        @SuppressLint({"ViewHolder", "InflateParams"})
        View view = layoutInflater.inflate(R.layout.layout_target_item, null);
        //照片
        ImageView imageViewTargetIcon = (ImageView) view.findViewById(R.id.target_item_imageView_targetIcon);
        if(target.getIconUri()!=null)
            imageViewTargetIcon.setImageURI(Uri.parse(target.getIconUri()));
        //名字
        TextView textViewTargetName = (TextView) view.findViewById(R.id.target_item_textView_targetName);
        textViewTargetName.setText(target.getName());
        //身份证号
        TextView textViewTargetIdNumber = (TextView) view.findViewById(R.id.target_item_textView_targetIdNumber);
        textViewTargetIdNumber.setText(target.getIdNumber());
        return view;
    }
}
