package com.refreshlistview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lai.RefreshListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends Activity {

    @Bind(R.id.lv_refersh)
    RefreshListView lvRefersh;
    private List<String> mInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        ButterKnife.bind(this);
        initData();
        initAdapter();
    }

    private void initData() {
        mInfos = new ArrayList<String>();
        for (int i = 0; i < 40; i++) {
            mInfos.add("第" + (i + 1) + "条数据");
        }
    }

    private void initAdapter() {
        lvRefersh.setAdapter(new MyAdapter());
    }


    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mInfos != null) {
                return mInfos.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return mInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new TextView(MainActivity.this);
            }
            TextView tv = (TextView) convertView;
            tv.setText(mInfos.get(position));
            tv.setPadding(8,8,8,8);
            return tv;
        }
    }
}
