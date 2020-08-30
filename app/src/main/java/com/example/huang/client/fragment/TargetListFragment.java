package com.example.huang.client.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.huang.client.activity.TargetDataActivity;
import com.example.huang.client.R;

import com.example.huang.client.adapter.TargetListAdapter;
import com.example.huang.client.config.App2;

public class TargetListFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.fragment_data, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        ListView listViewTargetList = (ListView) view.findViewById(R.id.listView_targetList);
        TargetListAdapter targetListAdapter = new TargetListAdapter(getContext());
        listViewTargetList.setAdapter(targetListAdapter);
        listViewTargetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                App2.focusTarget = App2.targetList.get(position);
                Intent intent = new Intent(getActivity(), TargetDataActivity.class);
                TargetListFragment.this.startActivity(intent);
            }
        });
    }
}
