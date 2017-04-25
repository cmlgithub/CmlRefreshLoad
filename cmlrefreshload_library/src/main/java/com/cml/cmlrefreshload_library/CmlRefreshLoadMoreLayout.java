package com.cml.cmlrefreshload_library;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorUpdateListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.Toast;

import static android.R.attr.y;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
import static android.view.MotionEvent.ACTION_MOVE;

/**
 * author：cml on 2017/4/25
 * github：https://github.com/cmlgithub
 */

public class CmlRefreshLoadMoreLayout extends FrameLayout {

    private static final int VERTICAL_PULL_DOWN = -1;
    private static final int VERTICAL_PULL_UP = 1;
    private static final int HEADERVIEW_HEIGHT_DP  = 50;
    private static final int FOOTERVIEW_HEIGHT_DP  = 50;

    private Context mContext;
    private View mChildView;

    private View mHeaderView;
    private View mFooterView;

    private int mHeaderViewHeight ;
    private int mFooterViewHeight ;
    private LayoutParams mHeaderLayoutParams;
    private LayoutParams mFooterLayoutParams;
    private int screeningHeight;
    private int mFraction;
    private int allowSlideHeight;
    private boolean isRefresh;
    private boolean isLoadMore;

    private CmlRefreshLoadListener cmlRefreshLoadListener;
    private ObjectAnimator mAnimator;


    public CmlRefreshLoadMoreLayout(@NonNull Context context) {
        this(context,null);
    }

    public CmlRefreshLoadMoreLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CmlRefreshLoadMoreLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mHeaderViewHeight = CmlUtils.dip2px(context,HEADERVIEW_HEIGHT_DP);
        mFooterViewHeight = CmlUtils.dip2px(context,FOOTERVIEW_HEIGHT_DP);
        screeningHeight = CmlUtils.getScreeningHeight(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mChildView = getChildAt(0);

        addHeaderView();

        addFooterView();

    }

    public void setCmlRefreshLoadMoreLayout(CmlRefreshLoadListener cmlRefreshLoadListener){
        this.cmlRefreshLoadListener = cmlRefreshLoadListener;
    }

    private void addHeaderView() {
        if(mHeaderLayoutParams == null){
            mHeaderLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mHeaderViewHeight);
        }
        goneView(mHeaderView);
        mHeaderView.setLayoutParams(mHeaderLayoutParams);
        addView(mHeaderView);
    }

    private void addFooterView() {
        if(mFooterView == null){
            return;
        }
        if(mFooterLayoutParams == null){
            mFooterLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mFooterViewHeight);
        }
        mFooterLayoutParams.gravity = Gravity.BOTTOM;
        goneView(mFooterView);
        mFooterView.setLayoutParams(mFooterLayoutParams);
        addView(mFooterView);
    }

    public void setHeaderView(@NonNull View headerView) {
        this.mHeaderView = headerView;
    }

    public void setFooterView(@NonNull View footerView) {
        this.mFooterView = footerView;
    }

    public void setHeaderView(@NonNull View headerView,@NonNull LayoutParams headerLayoutParams) {
        setHeaderView(headerView);
        this.mHeaderLayoutParams = headerLayoutParams;
    }

    public void setFooterView(@NonNull View footerView,@NonNull LayoutParams footerLayoutParams) {
        setFooterView(footerView);
        this.mFooterLayoutParams = footerLayoutParams;
    }

    public void setHeaderView(@NonNull View headerView,@NonNull ObjectAnimator animator) {
        setHeaderView(headerView);
        this.mAnimator = animator;
    }

    public void setFooterView(@NonNull View footerView,@NonNull ObjectAnimator animator) {
        setFooterView(footerView);
        this.mAnimator = animator;
    }

    public void setHeaderView(@NonNull View headerView,@NonNull LayoutParams headerLayoutParams,@NonNull ObjectAnimator animator) {
        setHeaderView(headerView,headerLayoutParams);
        this.mAnimator = animator;
    }

    public void setFooterView(@NonNull View footerView,@NonNull LayoutParams footerLayoutParams,@NonNull ObjectAnimator animator) {
        setFooterView(footerView,footerLayoutParams);
        this.mAnimator = animator;
    }

    private float downY ;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(isRefresh || isLoadMore){
            return true;
        }
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                downY = ev.getY();
                break;
            case ACTION_MOVE:
                float moveY = ev.getY();
                float dy = moveY - downY;
                if(dy > 0 && !canScrollVerticalPullDown(mChildView)){
                    if(mHeaderView == null){
                        return super.onInterceptTouchEvent(ev);
                    }
                    showView(mHeaderView);
                    onBegin(mHeaderView);
                    return true;
                }

                if(dy < 0 && !canScrollVerticalPullUp(mChildView)){
                    if(mFooterView == null){
                        return super.onInterceptTouchEvent(ev);
                    }
                    showView(mFooterView);
                    onBegin(mFooterView);
                    return true;
                }

                break;
        }

        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

       switch (event.getAction()){
           case ACTION_MOVE:
               float dy = event.getY() - downY;
               if(dy > 0){
                   setHeadOrFooterViewHeight(mHeaderView,dy,mHeaderViewHeight);
               }else if(dy < 0){
                   setHeadOrFooterViewHeight(mFooterView,-dy,mFooterViewHeight);
               }
               break;
           case MotionEvent.ACTION_UP:
               if(mFraction >= allowSlideHeight){
                   if(mHeaderView.getVisibility() == VISIBLE){
                       startAnim(mHeaderView);
                       isRefresh = true;
                   }else if(mFooterView.getVisibility() == VISIBLE){
                       startAnim(mFooterView);
                       isLoadMore = true;
                   }
                   if(cmlRefreshLoadListener != null){
                       if(isRefresh){
                           cmlRefreshLoadListener.onRefresh();
                       }else if(isLoadMore){
                           cmlRefreshLoadListener.onLoadMore();
                       }
                   }
               }else {
                   if(mHeaderView.getVisibility() == VISIBLE){
                       animatorTranslationY(mHeaderView,0);
                   }else if(mFooterView.getVisibility() == VISIBLE){
                       animatorTranslationY(mFooterView,0);
                   }
               }
               break;
        }

        return super.onTouchEvent(event);

    }

    public void finishRefreshOrLoadMore(){
        if(mAnimator != null && mAnimator.isRunning()){
            mAnimator.end();
        }
        if(isRefresh){
            animatorTranslationY(mHeaderView,0);
        }else if(isLoadMore){
            animatorTranslationY(mFooterView,0);
        }
        isRefresh = isLoadMore = false;
    }

    public void animatorTranslationY(final View view ,final float h) {
        ViewPropertyAnimatorCompat viewPropertyAnimatorCompat = ViewCompat.animate(view);
        viewPropertyAnimatorCompat.setDuration(250);
        viewPropertyAnimatorCompat.setInterpolator(new DecelerateInterpolator());
        viewPropertyAnimatorCompat.translationY(h);
        viewPropertyAnimatorCompat.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(GONE);
            }
        },250);
        viewPropertyAnimatorCompat.setUpdateListener(new ViewPropertyAnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(View view) {
                float height = ViewCompat.getTranslationY(view);
                view.getLayoutParams().height = (int) height;
                view.requestLayout();
            }

        });
    }

    private float getFraction(float dy ,int height){
        return height * Math.abs(dy) / screeningHeight;
    }

    private void setHeadOrFooterViewHeight(View view,float dy,int height){
        mFraction = (int) getFraction(dy,height);
        allowSlideHeight = height / 3;
        view.getLayoutParams().height = mFraction;
        view.requestLayout();
        onPull(view,dy);
    }

    private boolean canScrollVerticalPullDown(View view){
        if(view == null){
            return false;
        }

        if(Build.VERSION.SDK_INT < ICE_CREAM_SANDWICH){
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0 && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(view, VERTICAL_PULL_DOWN) || view.getScrollY() > 0;
            }
        }
        return ViewCompat.canScrollVertically(view,VERTICAL_PULL_DOWN);
    }

    private boolean canScrollVerticalPullUp(View view) {
        if(view == null){
            return false;
        }

        if(Build.VERSION.SDK_INT < ICE_CREAM_SANDWICH){
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                if (absListView.getChildCount() > 0) {
                    int lastChildBottom = absListView.getChildAt(absListView.getChildCount() - 1).getBottom();
                    return absListView.getLastVisiblePosition() == absListView.getAdapter().getCount() - 1 && lastChildBottom <= absListView.getMeasuredHeight();
                } else {
                    return false;
                }

            } else {
                return ViewCompat.canScrollVertically(view, VERTICAL_PULL_UP) || view.getScrollY() > 0;
            }
        }
        return ViewCompat.canScrollVertically(view,VERTICAL_PULL_UP);
    }

    private void showView(View view){
        view.setVisibility(VISIBLE);
    }

    private void goneView(View view){
        view.setVisibility(GONE);
    }

    private void onBegin(View view){
        ViewCompat.setScaleX(view, 0.001f);
        ViewCompat.setScaleY(view, 0.001f);
    }

    private void onPull(View view,float fra){
        float a = CmlUtils.limitValue(1, fra);
        ViewCompat.setScaleX(view, a);
        ViewCompat.setScaleY(view, a);
        ViewCompat.setAlpha(view, a);
    }

    private void startAnim(View view){
        mAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 720f);
        mAnimator.setDuration(7 * 1000);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        if (!mAnimator.isRunning())
            mAnimator.start();
    }

}
