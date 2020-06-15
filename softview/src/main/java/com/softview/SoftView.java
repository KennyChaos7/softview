package com.softview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;


/**
 * 仅支持2个textView,待扩展
 * 有一个背景view绘制大小跟每个textView一样, 动画移动时移动此view
 */
public class SoftView extends FrameLayout {
    public SoftView(@NonNull Context context) {
        super(context);
        initAttr(context, null, 0, 0);
    }

    public SoftView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs, 0, 0);
    }

    public SoftView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SoftView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttr(context, attrs, defStyleAttr, defStyleRes);
    }

    private boolean isInit = false;

    //  content_background
    private int _contentBackground = 0;

    //  类型数目
//    private final static int DEFAULT_TYPE_COUNT = 2;
//    private int _typeCount;
    private String _textLeft = null;
    private String _textRight = null;

    //  字号
    private final static int DEFAULT_TEXT_SIZE = 14;
    private float _textSize;

    //  textView Padding
    private int _textPaddingLeft = 0;
    private int _textPaddingRight = 0;

    //  边界大小
    private int _bolderSize = 0;

    //  字颜色
    private int _textColorSelected = 0;
    private int _textColorUnSelected = 0;

    //  drawable
    private int _leftTextViewDrawableLeft = 0;
    private int _leftTextViewDrawableRight = 0;
    private int _rightTextViewDrawableLeft = 0;
    private int _rightTextViewDrawableRight = 0;


    //  drawable padding
    private int _textDrawablePadding = 0;


    private ArrayList<TextView> typeTexViewList = new ArrayList<>();
    private OnSoftTextViewClickListener _listener;

    //  状态
    private int isSelectIndex = 0;
    private View animView = null;
    private float animDistance = 0;
    //  动画状态,只有动画运行完毕后才可以执行
    private boolean isInAnim = false;
    //  模拟器中出现textView获取的width/height为0的情况,需监听addOnGlobalLayoutListener后再初始化
    private boolean isAnimReady = false;

    private void initAttr(Context context, AttributeSet attr, int attrStyle, int attrStyleRes) {
        if (!isInit) {
            TypedArray typedArray = null;
            try {
                typedArray = context.getTheme().obtainStyledAttributes(attr, R.styleable.SoftView, attrStyle, attrStyleRes);
                _contentBackground = typedArray.getResourceId(R.styleable.SoftView_content_background, 0);
                _textColorSelected = typedArray.getColor(R.styleable.SoftView_text_color_selected, getResources().getColor(android.R.color.black));
                _textColorUnSelected = typedArray.getColor(R.styleable.SoftView_text_color_unselected, getResources().getColor(android.R.color.darker_gray));
                _textLeft = typedArray.getString(R.styleable.SoftView_soft_text_1);
                _textRight = typedArray.getString(R.styleable.SoftView_soft_text_2);
                _textPaddingLeft = (int) typedArray.getDimension(R.styleable.SoftView_text_padding_left, 0);
                _textPaddingRight = (int) typedArray.getDimension(R.styleable.SoftView_text_padding_right, 0);
                _leftTextViewDrawableLeft = typedArray.getResourceId(R.styleable.SoftView_left_text_drawable_left, 0);
                _leftTextViewDrawableRight = typedArray.getResourceId(R.styleable.SoftView_left_text_drawable_right, 0);
                _rightTextViewDrawableLeft = typedArray.getResourceId(R.styleable.SoftView_right_text_drawable_left, 0);
                _rightTextViewDrawableRight = typedArray.getResourceId(R.styleable.SoftView_right_text_drawable_right, 0);
                _textDrawablePadding = (int) typedArray.getDimension(R.styleable.SoftView_drawable_padding, 0);
                _textSize = (int) typedArray.getDimension(R.styleable.SoftView_text_size, DEFAULT_TEXT_SIZE);
                _bolderSize = (int) typedArray.getDimension(R.styleable.SoftView_bolder_size, 0);
            } finally {
                if (typedArray != null) {
                    typedArray.recycle();
                    typedArray = null;
                }
            }

            //  animView
            animView = new View(context);
            addView(animView);
            animView.setBackgroundResource(_contentBackground == 0 ? android.R.color.black : _contentBackground);
            LayoutParams animViewLp = (LayoutParams) animView.getLayoutParams();
            animViewLp.bottomMargin = _bolderSize;
            animViewLp.topMargin = _bolderSize;
            animViewLp.leftMargin = _bolderSize;
            animViewLp.rightMargin = _bolderSize;
            animView.setLayoutParams(animViewLp);

            //  textView
            LinearLayout linearLayout = new LinearLayout(context);
            addView(linearLayout);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            LayoutParams layoutParams = (LayoutParams) linearLayout.getLayoutParams();
            layoutParams.width = LayoutParams.WRAP_CONTENT;
            layoutParams.height = LayoutParams.MATCH_PARENT;
            linearLayout.setLayoutParams(layoutParams);

            initTextView(context, linearLayout, _textLeft);
            initTextView(context, linearLayout, _textRight);

            initAnimView();
        }
        isInit = true;
    }

    private void initTextView(Context context, LinearLayout parent, String text) {
        TextView tv = new TextView(context);
        parent.addView(tv);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tv.getLayoutParams();
        layoutParams.height = LayoutParams.MATCH_PARENT;
        tv.setLayoutParams(layoutParams);
        tv.setText(text);
        tv.setTextColor(_textColorUnSelected);
        tv.setTextSize(_textSize);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(
                _textPaddingLeft,
                0,
                _textPaddingRight,
                0
        );
        tv.setCompoundDrawablePadding(_textDrawablePadding);
        typeTexViewList.add(tv);
        if (tv == typeTexViewList.get(0)) {
            tv.setCompoundDrawablesWithIntrinsicBounds(
                    _leftTextViewDrawableLeft != 0 ? ContextCompat.getDrawable(context, _leftTextViewDrawableLeft) : null,
                    null,
                    _leftTextViewDrawableRight != 0 ? ContextCompat.getDrawable(context, _leftTextViewDrawableRight) : null,
                    null
            );

            tv.setOnClickListener(v -> changeTextStatus(0));
        }else {
            tv.setCompoundDrawablesWithIntrinsicBounds(
                    _rightTextViewDrawableLeft != 0 ? ContextCompat.getDrawable(context, _rightTextViewDrawableLeft) : null,
                    null,
                    _rightTextViewDrawableRight != 0 ? ContextCompat.getDrawable(context, _rightTextViewDrawableRight) : null,
                    null
            );
            tv.setOnClickListener(v -> changeTextStatus(1));
        }
    }

    private void initAnimView() {
        typeTexViewList.get(0).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!isAnimReady) {
                    LayoutParams layoutParams = (LayoutParams) animView.getLayoutParams();
                    layoutParams.width = typeTexViewList.get(0).getWidth() - 2 * dip2px(getContext(), _bolderSize);
                    layoutParams.height = typeTexViewList.get(0).getHeight() - 2 * dip2px(getContext(), _bolderSize);
                    if (layoutParams.width > 0 && layoutParams.height > 0) {
                        isAnimReady = true;
                        layoutParams.topMargin = dip2px(getContext(), _bolderSize);
                        layoutParams.bottomMargin = dip2px(getContext(), _bolderSize);
                        animDistance = 0;
                        layoutParams.rightMargin = 0;
                        layoutParams.leftMargin = dip2px(getContext(), _bolderSize);
                        animView.setLayoutParams(layoutParams);
                    }
                    typeTexViewList.get(0).setTextColor(_textColorSelected);
                }
            }
        });
    }

    /**
     * 计算{@link #animView}的大小和{@link #animDistance}
     */
    private void calculateAnimView(int index) {
        LayoutParams layoutParams = (LayoutParams) animView.getLayoutParams();
        layoutParams.width = typeTexViewList.get(index).getWidth() - 2 * dip2px(getContext(), _bolderSize);
        layoutParams.height = typeTexViewList.get(index).getHeight() - 2 * dip2px(getContext(), _bolderSize);
        layoutParams.topMargin = dip2px(getContext(), _bolderSize);
        layoutParams.bottomMargin = dip2px(getContext(), _bolderSize);
        //  如果index为0,则向右
        if (index == 0){
            animDistance = 0;
            layoutParams.rightMargin = 0;
            layoutParams.leftMargin = dip2px(getContext(), _bolderSize);
        }
        else {
            animDistance = (typeTexViewList.get(0).getWidth() + dip2px(getContext(), 0));
            layoutParams.rightMargin = dip2px(getContext(), _bolderSize);
            layoutParams.leftMargin = 0;
        }
        animView.setLayoutParams(layoutParams);
    }

    private void changeTextStatus(int targetIndex) {
        if (isSelectIndex != targetIndex && !isInAnim) {
            if (_listener != null && isInit)
            {
                _listener.onClick(targetIndex);
            }
            TextView tv1, tv2;
            tv1 = typeTexViewList.get(0);
            tv2 = typeTexViewList.get(1);
            if (targetIndex == 0) {
                isSelectIndex = 0;
                tv1.setTextColor(_textColorSelected);
                tv2.setTextColor(_textColorUnSelected);
            }
            else {
                isSelectIndex = 1;
                tv1.setTextColor(_textColorUnSelected);
                tv2.setTextColor(_textColorSelected);
            }
            //  将animView改造为对应的tv大小
            calculateAnimView(isSelectIndex);
            animTo();
        }
    }

    private void animTo() {
        if (!isInAnim) {
//            ViewCompat.animate(animView).translationX(animDistance).setListener(new ViewPropertyAnimatorListener() {
//                @Override
//                public void onAnimationStart(View view) {
//                    isInAnim = true;
//                    if (_listener != null) _listener.onAnimStart(isSelectIndex);
//                }
//
//                @Override
//                public void onAnimationEnd(View view) {
//                    isInAnim = false;
//                    if (_listener != null) _listener.onAnimEnd();
//                }
//
//                @Override
//                public void onAnimationCancel(View view) {
//                    isInAnim = false;
//                }
//            }).setUpdateListener(new ViewPropertyAnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(View view) {
//
//                }
//            }).setDuration(500).start();
            ObjectAnimator circlePtAnim = ObjectAnimator.ofFloat(animView, "translationX", animDistance);
            circlePtAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isInAnim = false;
                    if (_listener != null) _listener.onAnimEnd();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    super.onAnimationRepeat(animation);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    isInAnim = true;
                    if (_listener != null) _listener.onAnimStart(isSelectIndex);
                }

                @Override
                public void onAnimationPause(Animator animation) {
                    super.onAnimationPause(animation);
                }

                @Override
                public void onAnimationResume(Animator animation) {
                    super.onAnimationResume(animation);
                }
            });
            circlePtAnim.setDuration(500);
            circlePtAnim.start();
        }
    }


    private int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public void setOnSoftTextViewClickListener(OnSoftTextViewClickListener listener) {
        _listener = listener;
    }

    public interface OnSoftTextViewClickListener {
        void onClick(int targetIndex);
        void onAnimStart(int targetIndex);
        void onAnimEnd();
    }
}
