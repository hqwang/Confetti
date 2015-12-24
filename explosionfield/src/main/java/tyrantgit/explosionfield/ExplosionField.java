/*
 * Copyright (C) 2015 tyrantgit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tyrantgit.explosionfield;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ExplosionField extends View {

    private List<ExplosionAnimator> mExplosions = new ArrayList<>();
    private int[] mExpandInset = new int[2];

    public static final int MODE_EXPLOSION = 0;
    public static final int MODE_CONFETTI = 1;
    private int mMode = MODE_CONFETTI;

    public static final int FLAG_SUPPORT_GRAVITY = 0x1;
    private int mFlag;

    public ExplosionField(Context context) {
        super(context);
        init();
    }

    public ExplosionField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExplosionField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Arrays.fill(mExpandInset, Utils.dp2Px(32));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (ExplosionAnimator explosion : mExplosions) {
            explosion.draw(canvas);
        }
    }

    public void expandExplosionBound(int dx, int dy) {
        mExpandInset[0] = dx;
        mExpandInset[1] = dy;
    }

    public void setMode(int mode) {
        if (this.mMode != mode) {
            this.mMode = mode;
        }
    }

    public void explode(ExplosionAnimator explosion, long startDelay, long duration) {
        explosion.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mExplosions.remove(animation);
            }
        });
        explosion.setStartDelay(startDelay);
        explosion.setDuration(duration);
        mExplosions.add(explosion);
        explosion.start();
    }

    public void setFlag(int flag) {
        this.mFlag |= flag;
    }

    public boolean checkFlag(int flag) {
        return (this.mFlag & flag) > 0;
    }

    public void explode(final View view) {
        Rect r = new Rect();
        view.getGlobalVisibleRect(r);
        int[] location = new int[2];
        getLocationOnScreen(location);
        r.offset(-location[0], -location[1]);
        r.inset(-mExpandInset[0], -mExpandInset[1]);

        ExplosionAnimator explosion = null;
        long delay = 0;

        if (mMode == MODE_EXPLOSION) {
            explosion = new ExplosionAnimator(this, Utils.createBitmapFromView(view), r);
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(150);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                Random random = new Random();

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    view.setTranslationX((random.nextFloat() - 0.5f) * view.getWidth() * 0.05f);
                    view.setTranslationY((random.nextFloat() - 0.5f) * view.getHeight() * 0.05f);

                }
            });
            animator.start();

            view.animate().setDuration(150).setStartDelay(animator.getDuration())
                    .scaleX(0f).scaleY(0f).alpha(0f).start();

            delay = animator.getDuration();

        } else if (mMode == MODE_CONFETTI) {
            explosion = new PieceAnimator(this, Utils.createBitmapFromView(view), r, checkFlag(FLAG_SUPPORT_GRAVITY));
            view.animate().setDuration(50).alpha(0f).start();
            delay = 25;
        }

        if (explosion != null) {
            explode(explosion, delay, explosion.getDuration());
        }
    }

    public void clear() {
        mExplosions.clear();
        invalidate();
    }

    public static ExplosionField attach2Window(Activity activity) {
        ViewGroup rootView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        ExplosionField explosionField = new ExplosionField(activity);
        rootView.addView(explosionField, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return explosionField;
    }

    public void addListener(View root) {
        if (root instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) root;
            for (int i = 0; i < parent.getChildCount(); i++) {
                addListener(parent.getChildAt(i));
            }
        } else {
            root.setClickable(true);
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    explode(v);
                    v.setOnClickListener(null);
                }
            });
        }
    }

    public void reset(View root) {
        if (root instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) root;
            for (int i = 0; i < parent.getChildCount(); i++) {
                reset(parent.getChildAt(i));
            }
        } else {
            root.setScaleX(1);
            root.setScaleY(1);
            root.setAlpha(1);
        }
    }

}
