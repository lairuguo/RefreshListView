package com.refreshlistview;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
    private int          mCount;
    private MyAdapter    mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        ButterKnife.bind(this);
        initListener();
        initData();
        initAdapter();
    }

    /**
     * 设置监听
     */
    private void initListener() {
        lvRefersh.addOnRefreshListener(new RefreshListView.OnRefreshListener() {
            @Override
            public void onRefreshing() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mInfos.clear();
                        mCount++;
                        for (int i = 0; i < 50; i++) {
                            mInfos.add("第" + mCount + "次,刷新后的数据" + i + "条");
                        }
                        mAdapter.notifyDataSetChanged();
                        lvRefersh.refreshFinish();
                    }
                }, 2000);
            }

            @Override
            public void onLoadingMore() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       List<String> data = new ArrayList();
                        for (int i = 0; i < 50; i++) {
                            data.add("更多"+i);
                        }
                        mInfos.addAll(data);
                        mAdapter.notifyDataSetChanged();
                        lvRefersh.refreshFinish();
                    }
                }, 2000);
            }
        });
    }

    private void initData() {
        ImageView view = new ImageView(this);
        view.setImageResource(R.mipmap.common_listview_headview_red_arrow);
        lvRefersh.addHeaderView(view);
        mInfos = new ArrayList<String>();
        for (int i = 0; i < 40; i++) {
            mInfos.add("第" + (i + 1) + "条数据");
        }
    }

    private void initAdapter() {
        mAdapter = new MyAdapter();
        lvRefersh.setAdapter(mAdapter);
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
            tv.setPadding(8, 8, 8, 8);
            return tv;
        }
    }
}
