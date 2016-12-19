

package com.limxing.expandableview.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.limxing.expandableview.R;


public class ExpandableView extends LinearLayout implements View.OnClickListener {
    private Context mContext;

    private String mTitlt;
    private static final int DEFAULT_ANIM_DURATION = 300;

    private static final float DEFAULT_ANIM_ALPHA_START = 0f;
    private static final boolean DEFAULT_SHOW = true;
    protected ImageButton mButton; // Button to expand/collapse
    private boolean mCollapsed = true; // Show short version as default.
    private boolean isShow = true;// 是否显示全部

    private Drawable mExpandDrawable;

    private Drawable mCollapseDrawable;

    private int mAnimationDuration;

    private float mAnimAlphaStart;

    private boolean mAnimating;

    /* Listener for callback */
    private OnExpandStateChangeListener mListener;
    private int mHeight;//需要改变的高度
    private int mMinHeight;//最小距离
    private Drawable mTitleImage;
    private RelativeLayout title;
    private float title_size;
    private int title_color;
    private int line_color;
    private boolean addFlag = false;
    private boolean measureFlag = true;
    private int childHeight;


    public ExpandableView(Context context) {
        this(context, null);
    }

    public ExpandableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ExpandableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @Override
    public void setOrientation(int orientation) {
        if (LinearLayout.HORIZONTAL == orientation) {
            throw new IllegalArgumentException("ExpandableTextView only supports Vertical Orientation.");
        }
        super.setOrientation(orientation);
    }

    @Override
    public void onClick(View view) {
        if (!mAnimating) {
            mCollapsed = !mCollapsed;
            mButton.setImageDrawable(mCollapsed ? mExpandDrawable : mCollapseDrawable);
            mAnimating = true;
            Animation animation;
            if (mCollapsed) {
                //false
                animation = new ExpandCollapseAnimation(this, mMinHeight, mHeight);//kuo
            } else {
                animation = new ExpandCollapseAnimation(this, mHeight, mMinHeight);//suo
            }

            animation.setFillAfter(true);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    clearAnimation();
                    mAnimating = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            clearAnimation();
            startAnimation(animation);

        }
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return mAnimating;
//    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findViews();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mCollapsed && addFlag) {
            super.onMeasure(widthMeasureSpec, mHeight);
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //如果展开
        if (mCollapsed) {
            mHeight = getMeasuredHeight();
        }
    }


    public void addView(View child, int index, boolean b) {
        if (b) {
            addFlag = true;
            child.measure(0, 0);
            childHeight = child.getMeasuredHeight();
//            LogUtils.i(mHeight + "==child：" + childHeight);
            mHeight = mHeight + childHeight;

        }
        super.addView(child, index);
    }

    @Override
    public void removeViewAt(int index) {
        addFlag = true;
        mHeight = mHeight - childHeight;
        super.removeViewAt(index);
    }

    public void setOnExpandStateChangeListener(@Nullable OnExpandStateChangeListener listener) {
        mListener = listener;
    }


    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ExpandableView);
        mAnimationDuration = typedArray.getInt(R.styleable.ExpandableView_animDuration, DEFAULT_ANIM_DURATION);
        mAnimAlphaStart = typedArray.getFloat(R.styleable.ExpandableView_animAlphaStart, DEFAULT_ANIM_ALPHA_START);
        mExpandDrawable = typedArray.getDrawable(R.styleable.ExpandableView_expandDrawable);
        mCollapseDrawable = typedArray.getDrawable(R.styleable.ExpandableView_collapseDrawable);
        isShow = typedArray.getBoolean(R.styleable.ExpandableView_viewTitleShow, DEFAULT_SHOW);
        mTitleImage = typedArray.getDrawable(R.styleable.ExpandableView_viewTitleImage);

        if (mExpandDrawable == null) {
            mExpandDrawable = getDrawable(getContext(), R.drawable.zhedie_zhankai);
        }
        if (mCollapseDrawable == null) {
            mCollapseDrawable = getDrawable(getContext(), R.drawable.zhedie_shouqi);
        }

        setOrientation(LinearLayout.VERTICAL);
        title = new RelativeLayout(context);
        int color = typedArray.getColor(R.styleable.ExpandableView_viewTitleBacColor, Color.WHITE);
        title.setOnClickListener(this);
        title.setBackgroundColor(color);
//        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setVerticalGravity(Gravity.CENTER_VERTICAL);
        if (mTitleImage != null) {
            initTitleImage();
        }
        mTitlt = typedArray.getString(R.styleable.ExpandableView_viewTitle);
        //可以自行调节字体的大小,sp转px貌似很大啊,所以使用px转dp
        title_size = typedArray.getDimension(R.styleable.ExpandableView_viewTitleSize,
                50);
        title_size = DisplayUtil.px2dip(mContext, title_size);
        title_color = typedArray.getColor(R.styleable.ExpandableView_viewTitleColor, getResources().getColor(R.color.color_909090));
        line_color = typedArray.getColor(R.styleable.ExpandableView_viewTitleLineColor, getResources().getColor(R.color.color_bbbbbb));
        if (mTitlt != null) {
            initTitleText();
        }
        typedArray.recycle();
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);//定义一个LayoutParams

        layoutParams.setMargins(0, DisplayUtil.dip2px(context, 10), 0, 0);
        setLayoutParams(layoutParams);

        if (!isShow) {
            // 默认折叠
            addOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                    removeOnLayoutChangeListener(this);
                    Animation animation;
                    animation = new ExpandCollapseAnimation(ExpandableView.this, mHeight, mMinHeight);//suo
                    animation.setFillAfter(true);
                    animation.setDuration(0);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mAnimating = false;
                            mCollapsed = false;
                            if (mButton != null) {
                                mButton.setImageDrawable(mCollapsed ? mExpandDrawable : mCollapseDrawable);
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    clearAnimation();
                    ExpandableView.this.startAnimation(animation);
                }
            });
        }
    }

    /**
     * 初始化标题题目
     */
    private void initTitleText() {
        int padLeft = DisplayUtil.dip2px(mContext, 25);
        int h = 0;
        if (mTitleImage != null) {
            h = mTitleImage.getMinimumHeight();
        }
        TextView tv = new TextView(mContext);
        tv.setTextSize(title_size);
        tv.setTextColor(title_color);
        tv.setText(mTitlt);
        int padTop = DisplayUtil.dip2px(mContext, 10);
        tv.setPadding(padLeft + h, padTop, 0, padTop);
        title.addView(tv);
        mButton = new ImageButton(mContext);
        mButton.setBackgroundColor(Color.WHITE);
        mButton.setImageDrawable(mCollapsed ? mExpandDrawable : mCollapseDrawable);
        h = mExpandDrawable.getMinimumHeight();
        MarginLayoutParams mp = new MarginLayoutParams(h, h);  //item的宽高
        mp.setMargins(0, 0, padLeft, 0);//分别是margin_top那四个属性
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mp);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        mButton.setLayoutParams(params);
        mButton.setOnClickListener(this);
        title.addView(mButton);
//        title.measure(0, 0);

        View line = new View(mContext);
        line.setBackgroundColor(line_color);
        h = DisplayUtil.dip2px(mContext, 1);
        MarginLayoutParams mpLine = new MarginLayoutParams(LayoutParams.MATCH_PARENT, h);  //item的宽高
        title.measure(0, 0);
        h = title.getMeasuredHeight() - h;
        mpLine.setMargins(0, h, 0, 0);//分别是margin_top那四个属性
        RelativeLayout.LayoutParams mpLineparams = new RelativeLayout.LayoutParams(mpLine);
        line.setLayoutParams(mpLineparams);
        title.addView(line);

        View line1 = new View(mContext);
        line1.setBackgroundColor(line_color);
        h = DisplayUtil.dip2px(mContext, 1);
        MarginLayoutParams mpLine1 = new MarginLayoutParams(LayoutParams.MATCH_PARENT, h);  //item的宽高
        RelativeLayout.LayoutParams mpLineparams1 = new RelativeLayout.LayoutParams(mpLine1);
        line1.setLayoutParams(mpLineparams1);
        title.addView(line1);


        mMinHeight = title.getMeasuredHeight();
        addView(title);
    }

    /**
     * 初始化标题图片
     */
    private void initTitleImage() {
        int padLeft = DisplayUtil.dip2px(mContext, 20);
        int h = mTitleImage.getMinimumHeight();
        ImageView imageView = new ImageView(mContext);
        imageView.setImageDrawable(mTitleImage);
        MarginLayoutParams mp = new MarginLayoutParams(h, h);  //item的宽高
        mp.setMargins(padLeft, 0, 0, 0);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mp);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        imageView.setLayoutParams(params);
        title.addView(imageView);
    }

    public void setmTitlt(String mTitlt) {
        this.mTitlt = mTitlt;
        initTitleText();
    }

    public void setmTitleImage(Drawable mTitleImage) {
        this.mTitleImage = mTitleImage;
        initTitleImage();
    }

    private void findViews() {
//        mTv = (LinearLayout) findViewById(R.id.expandable_text);

    }

    private static boolean isPostHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    private static boolean isPostLolipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void applyAlphaAnimation(View view, float alpha, int duration) {
        if (isPostHoneycomb()) {
            view.setAlpha(1);
        } else {
            AlphaAnimation alphaAnimation = new AlphaAnimation(1, alpha);
            // make it instant
            alphaAnimation.setDuration(duration);
            alphaAnimation.setFillAfter(true);
            view.startAnimation(alphaAnimation);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Drawable getDrawable(@NonNull Context context, @DrawableRes int resId) {
        Resources resources = context.getResources();
        if (isPostLolipop()) {
            return resources.getDrawable(resId, context.getTheme());
        } else {
            return resources.getDrawable(resId);
        }
    }


    class ExpandCollapseAnimation extends Animation {
        private final View mTargetView;
        private final int mStartHeight;
        private final int mEndHeight;

        public ExpandCollapseAnimation(View view, int startHeight, int endHeight) {
            mTargetView = view;
            mStartHeight = startHeight;
            mEndHeight = endHeight;
            setDuration(mAnimationDuration);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            final int newHeight = (int) ((mEndHeight - mStartHeight) * interpolatedTime + mStartHeight);
//            mTv.setMaxHeight(newHeight - mMarginBetweenTxtAndBottom);
            setMinimumHeight(newHeight);
//            if (Float.compare(mAnimAlphaStart, 1.0f) != 0) {
//                applyAlphaAnimation(mTv, mAnimAlphaStart + interpolatedTime * (1.0f - mAnimAlphaStart),mAnimationDuration);
//            }
            mTargetView.getLayoutParams().height = newHeight;
            mTargetView.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }

    public interface OnExpandStateChangeListener {
        /**
         * Called when the expand/collapse animation has been finished
         *
         * @param textView   - TextView being expanded/collapsed
         * @param isExpanded - true if the TextView has been expanded
         */
        void onExpandStateChanged(LinearLayout textView, boolean isExpanded);
    }

}