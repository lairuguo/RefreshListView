package com.lai;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.library.R;

/**
 * @创建者 lai
 * @创建时间 2016/2/23
 * @packageName com.library
 * @更新时间 2016/2/23 19:40
 * @描述 ListView下拉刷新和加载更多
 */
public class RefreshListView extends ListView {
    private boolean debug = true;
    private LayoutInflater mInflater;
    private View           mHeaderView;
    private float          mDownY;
    private int            mHeaderHeight;
    private ImageView      mHeaderIvArr;
    private TextView       mHeaderTvRefresh;
    private ProgressBar    mHeaderPb;
    private TextView       mHeaderTvRefreshTime;
    private Context        mContext;
    private Handler        mHandler;

    private static final int STATE_PULL_STATE      = 0;//下拉
    private static final int STATE_RELAESE_REFRESH = 1;//释放
    private static final int STATE_REFRESHING      = 2;//正在刷新

    private int mCurrentState = STATE_PULL_STATE;//记录刷新的状态
    private RotateAnimation mRAnimationUp;
    private RotateAnimation mRAnimationDown;

    public RefreshListView(Context context) {
        this(context, null);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mHandler = new Handler();
        initHeaderView();
        initFooterView();
        initView();
        initData();
    }

    private void initData() {
        mRAnimationUp = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRAnimationUp.setDuration(500);
        mRAnimationUp.setFillAfter(true);
        mRAnimationDown = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRAnimationDown.setDuration(500);
        mRAnimationDown.setFillAfter(true);
    }

    /**
     * 查找控件
     */
    private void initView() {
        //头部
        mHeaderIvArr = (ImageView) mHeaderView.findViewById(R.id.header_iv_arr);
        mHeaderTvRefresh = (TextView) mHeaderView.findViewById(R.id.header_tv_refresh);
        mHeaderPb = (ProgressBar) mHeaderView.findViewById(R.id.header_pb);
        mHeaderTvRefreshTime = (TextView) mHeaderView.findViewById(R.id.header_tv_refresh_time);
        //底部
        initViewState();

    }

    /**
     * 初始化控件
     */
    private void initViewState() {
        //把头部的progressBar隐藏
        mHeaderPb.setVisibility(View.INVISIBLE);
        mHeaderIvArr.setVisibility(View.VISIBLE);
        mHeaderTvRefresh.setText("下拉刷新");
    }

    /**
     * 初始化刷新头
     */
    private void initHeaderView() {
        mHeaderView = mInflater.inflate(R.layout.refersh_header_view, null);
        //        View headerView = View.inflate(context, R.layout.refersh_header_view,null);
        addHeaderView(mHeaderView);
        //隐藏头
        mHeaderView.measure(0, 0);
        mHeaderHeight = mHeaderView.getMeasuredHeight();
        // int headerWidth = headerView.getMeasuredWidth();
        int top = -mHeaderHeight;
        mHeaderView.setPadding(0, top, 0, 0);
    }

    private void initFooterView() {
        View footerView = mInflater.inflate(R.layout.refersh_footer_view, null);
        addFooterView(footerView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE: {
                float moveY = ev.getRawY();
                float diffY = moveY - mDownY;
                // mDownY = moveY;

                //当第0个可见时,拖动时,需要刷新头可见
                int firstVisiblePosition = getFirstVisiblePosition();
                if (diffY > 0 && firstVisiblePosition == 0) {
                    int top = (int) (diffY - mHeaderHeight + .5f);
                    mHeaderView.setPadding(0, top, 0, 0);
                    if (top >= 0 && mCurrentState != STATE_RELAESE_REFRESH) {
                        if (debug)
                            Log.d("RefreshListView", "下拉");
                        mCurrentState = STATE_RELAESE_REFRESH;
                        refreshUI();
                    } else if (top < 0 && mCurrentState != STATE_PULL_STATE) {
                        mCurrentState = STATE_PULL_STATE;
                        if (debug)
                            Log.d("RefreshListView", "");
                        refreshUI();
                    }
                    //

                    // mHeaderIvArr.setAnimation(animation);

                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_UP:
                // 判断是否为要释放刷新的状态
                if (mCurrentState == STATE_RELAESE_REFRESH) {
                    mCurrentState = STATE_REFRESHING;
                    refreshUI();
                    int start = mHeaderView.getPaddingTop();
                    int end = 0;
                    ValueAnimator animator = ValueAnimator.ofInt(start, end);
                    animator.setDuration(1000);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int value = (int) animation.getAnimatedValue();
                            mHeaderView.setPadding(0, value, 0, 0);
                        }
                    });
                    animator.start();
                } else {
                    mHeaderView.setPadding(0, 0, 0, 0);
                }
                break;
        }

        return super.onTouchEvent(ev);

    }

    /**
     * 跟新UI
     */
    private void refreshUI() {
        switch (mCurrentState) {
            case STATE_PULL_STATE:
                mHeaderTvRefresh.setText("下拉刷新");
                mHeaderIvArr.startAnimation(mRAnimationUp);
                break;
            case STATE_RELAESE_REFRESH:
                mHeaderTvRefresh.setText("释放刷新");
                mHeaderIvArr.startAnimation(mRAnimationDown);
                break;
            case STATE_REFRESHING:
                mHeaderTvRefresh.setText("正在刷新");
                mHeaderIvArr.clearAnimation();
                mHeaderIvArr.setVisibility(View.INVISIBLE);
                mHeaderPb.setVisibility(View.VISIBLE);
                break;
        }

    }

    //    /**
    //     * 下拉头部
    //     *
    //     * @param ev
    //     */
    //    private void pullHeader(MotionEvent ev) {
    //
    //        if (debug) {
    //            Log.d("RefreshListView", "diffX:" + diffY);
    //            Log.d("RefreshListView", "mDownX:" + mDownY);
    //            Log.d("RefreshListView", "moveX:" + moveY);
    //            Log.d("RefreshListView", "mTop:" + mTop);
    //            Log.d("RefreshListView", "refresh:" + refresh);
    //        }
    //
    //    }

    //    /**
    //     * 开始刷新
    //     */
    //    private void startRefresh() {
    //        mHeaderPb.setVisibility(View.VISIBLE);
    //        mHeaderIvArr.setVisibility(View.INVISIBLE);
    //        mHandler.postDelayed(new Runnable() {
    //            @Override
    //            public void run() {
    //                initViewState();
    //                //startBack();
    //            }
    //        }, 2000);
    //    }

    //    /**
    //     * 刷新动画
    //     */
    //    private void startBack() {
    //        mHandler.postDelayed(new Runnable() {
    //            @Override
    //            public void run() {
    //                while (mTop != -mHeaderHeight)
    //                    mHeaderView.setPadding(0, mTop--, 0, 0);
    //            }
    //        }, 30);
    //        initViewState();
    //    }
}