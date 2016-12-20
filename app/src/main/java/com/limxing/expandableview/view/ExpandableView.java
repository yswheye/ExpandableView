

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
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
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

    private String mTitleName;// title 标题
    private static final int DEFAULT_ANIM_DURATION = 300;
    private static final float DEFAULT_ANIM_ALPHA_START = 0f;
    protected ImageView mArrowImg; // Button to expand/collapse
    private boolean displayArrow = true;
    private boolean mCollapsed = true; // Show short version as default.
    private boolean isCollapsed = true; // 是否折叠

    private Drawable mExpandDrawable;
    private Drawable mCollapseDrawable;

    private int mAnimationDuration;

    private float mAnimAlphaStart;

    private boolean mAnimating;
    /**
     * custom
     */
    private float mTitleHeight = 50;//title height
    private int mTitleLeftRightPadding = 35;// title左右边距

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
            setTitleArrow();
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
//                applyAlphaAnimation(mTv, mAnimAlphaStart,mAnimationDuration);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    clearAnimation();
                    mAnimating = false;
                    if (mListener != null) {
//                    mListener.onExpandStateChanged(mTv, !mCollapsed);
                    }
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
        isCollapsed = typedArray.getBoolean(R.styleable.ExpandableView_isCollapse, isCollapsed);
        mTitleImage = typedArray.getDrawable(R.styleable.ExpandableView_viewTitleImage);

//        if (mExpandDrawable == null) {
//            mExpandDrawable = getDrawable(getContext(), R.drawable.arrow_down);
//        }
//        if (mCollapseDrawable == null) {
//            mCollapseDrawable = getDrawable(getContext(), R.drawable.arrow_right);
//        }

        setOrientation(LinearLayout.VERTICAL);
        title = new RelativeLayout(context);
        int color = typedArray.getColor(R.styleable.ExpandableView_viewTitleBacColor, Color.WHITE);
        title.setOnClickListener(this);
        title.setBackgroundColor(color);
        //title.setGravity(Gravity.CENTER_VERTICAL);
        title.setVerticalGravity(Gravity.CENTER_VERTICAL);
        mTitleHeight = typedArray.getDimension(R.styleable.ExpandableView_titleHeight, mTitleHeight);
        RelativeLayout.LayoutParams titleLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) mTitleHeight);
        title.setLayoutParams(titleLayoutParams);

        displayArrow = typedArray.getBoolean(R.styleable.ExpandableView_titleArrow, true);
        if (mTitleImage != null) {
            initTitleImage();
        }
        mTitleName = typedArray.getString(R.styleable.ExpandableView_titleName);
        //可以自行调节字体的大小,sp转px貌似很大啊,所以使用px转dp
        title_size = typedArray.getDimension(R.styleable.ExpandableView_titleNameSize, 16);
        title_color = typedArray.getColor(R.styleable.ExpandableView_titleNameColor, ContextCompat.getColor(mContext, R.color.title_color));
        line_color = typedArray.getColor(R.styleable.ExpandableView_viewTitleLineColor, ContextCompat.getColor(mContext, R.color.line_color));
        if (mTitleName != null) {
            initTitleText();
        }
        typedArray.recycle();
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);//定义一个LayoutParams
        layoutParams.setMargins(0, DisplayUtil.dip2px(context, 10), 0, 0);
        setLayoutParams(layoutParams);

        if (isCollapsed) {
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
                            setTitleArrow();
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

    private void setTitleArrow() {
        if (mArrowImg != null && mExpandDrawable != null && mCollapseDrawable != null) {
            mArrowImg.setImageDrawable(mCollapsed ? mExpandDrawable : mCollapseDrawable);
        }
    }

    /**
     * 初始化标题题目
     */
    private void initTitleText() {
        int padLeft = DisplayUtil.dip2px(mContext, 25);
        int h = 0;
        if (mTitleImage != null) {
            h = mTitleImage.getMinimumWidth();
        }
        TextView tv = new TextView(mContext);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, title_size);
        tv.setTextColor(title_color);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        ViewGroup.LayoutParams titleNameLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        tv.setLayoutParams(titleNameLayoutParams);
        tv.setText(mTitleName);
        int padTop = DisplayUtil.dip2px(mContext, 10);
        tv.setPadding(padLeft + h, padTop, 0, padTop);
        title.addView(tv);

        if (displayArrow) {
            mArrowImg = new ImageView(mContext);
            mArrowImg.setScaleType(ImageView.ScaleType.CENTER);
            mArrowImg.setBackgroundColor(Color.WHITE);
            setTitleArrow();
            MarginLayoutParams mp = new MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT, MarginLayoutParams.WRAP_CONTENT);  //item的宽高
            mp.setMargins(0, 0, mTitleLeftRightPadding, 0);//30 arrow的右边距
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mp);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            mArrowImg.setLayoutParams(params);
            mArrowImg.setOnClickListener(this);
            title.addView(mArrowImg);
        }
//        title.measure(0, 0);

        View line = new View(mContext);
        line.setBackgroundColor(line_color);
        h = DisplayUtil.dip2px(mContext, 0.3f);
        MarginLayoutParams mpLine = new MarginLayoutParams(LayoutParams.MATCH_PARENT, h);  //item的宽高
        title.measure(0, 0);
        h = title.getMeasuredHeight() - h;
        mpLine.setMargins(0, h, 0, 0);//分别是margin_top那四个属性
        RelativeLayout.LayoutParams mpLineparams = new RelativeLayout.LayoutParams(mpLine);
        line.setLayoutParams(mpLineparams);
        title.addView(line);

        View line1 = new View(mContext);
        line1.setBackgroundColor(line_color);
        h = DisplayUtil.dip2px(mContext, 0.3f);
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
        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageDrawable(mTitleImage);
        MarginLayoutParams mp = new MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT, MarginLayoutParams.WRAP_CONTENT);  //item的宽高
        mp.setMargins(mTitleLeftRightPadding, 0, 0, 0);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mp);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        imageView.setLayoutParams(params);
        title.addView(imageView);
    }

    public void setmTitlt(String mTitlt) {
        this.mTitleName = mTitlt;
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
            setMinimumHeight(newHeight);
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