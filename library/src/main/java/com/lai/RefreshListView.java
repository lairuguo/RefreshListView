package com.lai;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.library.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * @创建者 lai
 * @创建时间 2016/2/23
 * @packageName com.library
 * @更新时间 2016/2/23 19:40
 * @描述 ListView下拉刷新和加载更多
 */
public class RefreshListView extends ListView implements AbsListView.OnScrollListener {
    private boolean debug = true;
    private LayoutInflater mInflater;
    private View           mHeaderView;
    private float mDownY = -1;
    private int         mHeaderHeight;
    private ImageView   mHeaderIvArr;
    private TextView    mHeaderTvRefresh;
    private ProgressBar mHeaderPb;
    private TextView    mHeaderTvRefreshTime;

    private static final int STATE_PULL_STATE      = 0;//下拉
    private static final int STATE_RELAESE_REFRESH = 1;//释放
    private static final int STATE_REFRESHING      = 2;//正在刷新

    private int mCurrentState = STATE_PULL_STATE;//记录刷新的状态
    private RotateAnimation              mRAnimationUp;
    private RotateAnimation              mRAnimationDown;
    private ArrayList<OnRefreshListener> mListener;
    private boolean                      isLoadingMore;
    private View                         mFooterView;
    private int                          mFooterHeight;

    public RefreshListView(Context context) {
        this(context, null);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
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
        mListener = new ArrayList<>();
        this.setOnScrollListener(this);
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
        mFooterView.findViewById(R.id.footer_tv_loading);

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
        mFooterView = mInflater.inflate(R.layout.refersh_footer_view, null);
        addFooterView(mFooterView);
        mHeaderView.measure(0, 0);
        mFooterHeight = mFooterView.getMeasuredHeight();
    }

    private int mHiddenHeight = -1;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE: {
                if (mCurrentState == STATE_REFRESHING) {
                    break;
                }
                float moveY = ev.getRawY();
                float diffY = moveY - mDownY;
                if (mDownY == -1) {
                    mDownY = moveY;
                }

                if (mHiddenHeight == -1) {
                    int[] listViewLocation = new int[2];
                    this.getLocationOnScreen(listViewLocation);
                    //获取ListView第一个条目左上角的点
                    int[] firstItemLocation = new int[2];
                    this.getChildAt(0).getLocationOnScreen(firstItemLocation);
                    if (debug) {
                        Log.d("RefreshListView", "listViewLocation[1]:" + listViewLocation[1]);
                        Log.d("RefreshListView", "firstItemLocation[1]:" + firstItemLocation[1]);
                    }
                    mHiddenHeight = listViewLocation[1] - firstItemLocation[1];
                }

                //当第0个可见时,拖动时,需要刷新头可见
                //获取listView左上角的点
                int firstVisiblePosition = getFirstVisiblePosition();
                if (diffY > 0 && firstVisiblePosition == 0) {
                    int top = (int) (diffY - mHeaderHeight + .5f) - mHiddenHeight;
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
                    if (mHiddenHeight == -1) {
                        return true;
                    } else {
                        break;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mHiddenHeight = -1;
                // 判断是否为要释放刷新的状态
                if (mCurrentState == STATE_RELAESE_REFRESH) {
                    mCurrentState = STATE_REFRESHING;
                    refreshUI();
                    int start = mHeaderView.getPaddingTop();
                    int end = 0;
                    //执行动画
                    doHeaderAnimator(start, end, true);

                } else if (mCurrentState == STATE_PULL_STATE) {
                    doHeaderAnimator(mHeaderView.getPaddingTop(), -mHeaderHeight, false);
                }
                break;
        }

        return super.onTouchEvent(ev);

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mCurrentState == STATE_REFRESHING) {
            return;
        }
        if (isLoadingMore) {
            return;
        }
        int position = this.getLastVisiblePosition();
        int maxIndex = this.getAdapter().getCount() - 1;
        //判断是否是最后一个
        if (position == maxIndex) {
            isLoadingMore = true;
            //更新UI

            //滑动到最后一个加载更多
            //暴露接口
            notifyOnLoadingMore();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    /**
     * 添加下拉刷新的监听
     *
     * @param listener
     */
    public void addOnRefreshListener(OnRefreshListener listener) {
        if (mListener.contains(listener)) {
            return;
        }
        mListener.add(listener);
    }

    public void refreshFinish() {
        refreshFinish(true);
    }

    /**
     * 刷新结束
     */
    public void refreshFinish(boolean hasMore) {
        if (isLoadingMore) {
            //加载更多结束
            isLoadingMore = false;
            if (!hasMore) {
                //没有更多
                mFooterView.setPadding(0, -mFooterHeight, 0, 0);
            }
            return;
        }
        //改变刷新状态
        mCurrentState = STATE_PULL_STATE;
        //更新UI
        refreshUI();
        doHeaderAnimator(mHeaderView.getPaddingTop(), -mHeaderHeight, false);
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        mHeaderTvRefreshTime.setText("刷新时间:" + currentTime);
    }

    /**
     * 移除下拉刷新的监听
     *
     * @param listener
     */
    public void removeOnRefreshListener(OnRefreshListener listener) {
        mListener.remove(listener);
    }

    /**
     * 通知正在刷新
     */
    public void notifyOnRefreshListener() {
        Iterator<OnRefreshListener> iterator = mListener.iterator();
        while (iterator.hasNext()) {
            OnRefreshListener listener = iterator.next();
            listener.onRefreshing();
        }
    }

    /**
     * 通知正在刷新
     */
    public void notifyOnLoadingMore() {
        Iterator<OnRefreshListener> iterator = mListener.iterator();
        while (iterator.hasNext()) {
            OnRefreshListener listener = iterator.next();
            listener.onLoadingMore();
        }
    }

    /**
     * 头部动画
     *
     * @param start       开始位置
     * @param end         结束位置
     * @param needRefresh 是否刷新
     */
    private void doHeaderAnimator(int start, int end, final boolean needRefresh) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        int duration = Math.abs((end - start) * 10);
        if (duration > 600) {
            duration = 600;
        }
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                mHeaderView.setPadding(0, value, 0, 0);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (needRefresh) {
                    //加载数据
                    notifyOnRefreshListener();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    /**
     * 更新UI
     */
    private void refreshUI() {
        switch (mCurrentState) {
            case STATE_PULL_STATE:
                mHeaderTvRefresh.setText("下拉刷新");
                mHeaderIvArr.startAnimation(mRAnimationUp);
                mHeaderIvArr.setVisibility(View.VISIBLE);
                mHeaderPb.setVisibility(View.INVISIBLE);
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

    /**
     * 下拉刷新时的回调接口
     */
    public interface OnRefreshListener {
        /**
         * 正在刷新
         */
        void onRefreshing();

        /**
         * 加载更多
         */
        void onLoadingMore();
    }
}
