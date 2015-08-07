package com.brucetoo.autoswiperefesh;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.widget.TextView;

public class DotsTextView extends TextView {
    private static final String TAG = DotsTextView.class.getSimpleName();

    private JumpingSpan dotOne;
    private JumpingSpan dotTwo;
    private JumpingSpan dotThree;

    private int jumpHeight;
    private int period;

    private AnimatorSet mAnimatorSet = new AnimatorSet();

    public DotsTextView(Context context) {
        super(context);
        init(context, null);
    }

    public DotsTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DotsTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        period = 1000;
        jumpHeight = (int) (getTextSize() / 4);

        dotOne = new JumpingSpan();
        dotTwo = new JumpingSpan();
        dotThree = new JumpingSpan();

        SpannableString spannable = new SpannableString("...");
        //SPAN_EXCLUSIVE_EXCLUSIVE 前后不包括，经常用这个flag
        spannable.setSpan(dotOne, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(dotTwo, 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(dotThree, 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        setText(spannable, BufferType.SPANNABLE);

        ObjectAnimator dotOneJumpAnimator = createDotJumpAnimator(dotOne, 0);
        //监听绘制每一帧图片时候
        dotOneJumpAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //此句话可不要，直接重绘就OK
                Number number = (Number) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        //每一个dot执行动画的延迟时间分别时 0，1/6,1/3
        mAnimatorSet.playTogether(dotOneJumpAnimator, createDotJumpAnimator(dotTwo,
                period / 6), createDotJumpAnimator(dotThree, period * 2 / 6));
//
        start();
    }

    public void start() {
        for (Animator animator : mAnimatorSet.getChildAnimations()) {
            if (animator instanceof ObjectAnimator) {
                ((ObjectAnimator) animator).setRepeatCount(ValueAnimator.INFINITE);
            }
        }
        mAnimatorSet.start();
    }

    private ObjectAnimator createDotJumpAnimator(JumpingSpan jumpingSpan, long delay) {
        ObjectAnimator jumpAnimator = ObjectAnimator.ofFloat(jumpingSpan, "translationY", 0, -jumpHeight);
        /**
         * TimeInterpolator 插值器：计算动画运动时一个跟当前运动到得时间t 和 duration的一个比例因子
         * TypeEvaluator 利用 timeInterpolator计算出的因子算出当前运动的位置
         * 从TimeInterpolator的getInterpolation中获取到根据当前时间t/duration 的一个比例因子 fraction
         * 例如: LinearInterpolator 匀速运动时只是返回未做任何处理
         public float getInterpolation(float input) {
         return input;
         }
         而 AccelerateDecelerateInterpolator 会利用余弦函数计算比例因子，才实现了先加速和减速的效果
         public float getInterpolation(float input) {
         return (float)(Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
         }
         所有的******Interpolator都是实现了TimeInterpolator类中得getInterpolation方法就能设置对应的因子值
         TypeEvaluator会接收返回的fraction
         正弦函数轨迹执行每个 dot 动画
         返回的值 会 在addUpdateListener的回调函数中执行
         */
        jumpAnimator.setEvaluator(new TypeEvaluator<Number>() {
            @Override
            public Number evaluate(float fraction, Number from, Number to) {
//                Log.e(TAG,"from:"+from.floatValue());
//                Log.e(TAG,"to:"+to.floatValue());
                return Math.max(0, Math.sin(fraction * Math.PI * 2)) * (to.floatValue() - from.floatValue());
            }
        });
        jumpAnimator.setDuration(period);
        jumpAnimator.setStartDelay(delay);
        jumpAnimator.setRepeatCount(ValueAnimator.INFINITE);
        jumpAnimator.setRepeatMode(ValueAnimator.RESTART);

        return jumpAnimator;
    }

    public void stop() {
        setAllAnimationsRepeatCount(0);
    }

    private void setAllAnimationsRepeatCount(int repeatCount) {
        for (Animator animator : mAnimatorSet.getChildAnimations()) {
            if (animator instanceof ObjectAnimator) {
                ((ObjectAnimator) animator).setRepeatCount(repeatCount);
            }
        }
    }

    public class JumpingSpan extends ReplacementSpan {

        private float translationX = 0;
        private float translationY = 0;

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fontMetricsInt) {
            return (int) paint.measureText(text, start, end);
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            canvas.drawText(text, start, end, x + translationX, y + translationY, paint);
        }

        public void setTranslationX(float translationX) {
            this.translationX = translationX;
        }

        public void setTranslationY(float translationY) {
            this.translationY = translationY;
        }
    }

}
