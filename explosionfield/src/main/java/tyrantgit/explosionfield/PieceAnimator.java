package tyrantgit.explosionfield;/*
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

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.Random;

public class PieceAnimator extends ExplosionAnimator {

    private static long DEFAULT_DURATION = 1200;
    private static float END_VALUE = 1.3f;
    private static final Interpolator KEEP_FINISH_INTERPOLATOR = new KeepFinishInterpolator(0.6f, 0.6f, 0.7f);
    private static final Interpolator DEFAULT_INTERPOLATOR = new DecelerateInterpolator(0.6f);
    private static final int COLUMN_COUNT = 15;
    private static float START_ROTATE = 360;
    private static float END_ROTATE = 360;

    private Paint mPaint;
    private Particle[] mParticles;
    private Rect mBound;
    private View mContainer;
    private Matrix matrix = new Matrix();
    private PaintFlagsDrawFilter mFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG);

    public static class KeepFinishInterpolator extends DecelerateInterpolator {

        private float mStartFinishFactor = 0f;
        private float mKeepFinishFactor = 0.7f;

        public KeepFinishInterpolator(float factor, float startFinish, float keepFinish) {
            super(factor);
            mStartFinishFactor = startFinish;
            mKeepFinishFactor = keepFinish;
        }

        @Override
        public float getInterpolation(float input) {
            if (input < mStartFinishFactor) {
                return 0;
            } else if (input < mKeepFinishFactor) {
                return 0.4f * super.getInterpolation(
                        (input - mStartFinishFactor) / (mKeepFinishFactor - mStartFinishFactor));
            } else if (input < 1) {
                return 0.4f + 0.6f * super.getInterpolation((input - mKeepFinishFactor) / (1 - mKeepFinishFactor));
            } else {
                return 1;
            }
        }
    }

    public PieceAnimator(View container, Bitmap bitmap, Rect bound) {
        this(container, bitmap, bound, false);
    }

    public PieceAnimator(View container, Bitmap bitmap, Rect bound, boolean supportGravity) {
        super(container, bitmap, bound);

        mPaint = new Paint();
        mBound = new Rect(bound);
        mBound.inset((mBound.width() - bitmap.getWidth()) / 2, (mBound.height() - bitmap.getHeight()) / 2);

        Random random = new Random(System.currentTimeMillis());

        float radius = bitmap.getWidth() / COLUMN_COUNT;
        int partLineCount = (int) (bitmap.getHeight() / radius);
        if (partLineCount == 0) {
            radius = bitmap.getHeight() / 4;
            partLineCount = (int) (bitmap.getHeight() / radius);
        }
        int partColumnCount = (int) (bitmap.getWidth() / radius);

        mParticles = new Particle[partLineCount * partColumnCount];
        for (int i = 0; i < partLineCount; i++) {
            for (int j = 0; j < partColumnCount; j++) {
                mParticles[(i * partColumnCount) + j]
                        = generateParticle(i, j, (int) radius, supportGravity, bitmap, random);
            }
        }

        mContainer = container;

        setFloatValues(0, END_VALUE);
        setInterpolator(DEFAULT_INTERPOLATOR);
        setDuration(DEFAULT_DURATION);
    }

    private Particle generateParticle(int line, int column, int radius, boolean supportGravity,
                                      Bitmap bitmap, Random random) {
        Particle particle = new Particle();
        particle.piece = Bitmap.createBitmap(bitmap,
                column * radius,
                line * radius,
                radius,
                radius,
                null, true);

        matrix.reset();
        matrix.setTranslate(-mBound.centerX(), -mBound.centerY());
        matrix.postScale(END_VALUE, END_VALUE, 0, 0);
        matrix.postTranslate(mBound.centerX(), mBound.centerY());

        particle.srcRectF = new RectF();
        particle.srcRectF.left = mBound.left + column * radius;
        particle.srcRectF.top = mBound.top + line * radius;
        particle.srcRectF.right = particle.srcRectF.left + radius;
        particle.srcRectF.bottom = particle.srcRectF.top + radius;

        float src[] = new float[]{particle.srcRectF.left, particle.srcRectF.top};
        float dst[] = new float[2];
        matrix.mapPoints(dst, src);

        particle.dstRectF = new RectF(particle.srcRectF);
        particle.dstRectF.offset(dst[0] - src[0], dst[1] - src[1]);

        particle.rectF = new RectF(particle.srcRectF);

        if (supportGravity) {
            particle.gravity = particle.srcRectF.height() * (END_VALUE - 1) / 0.02f;
        }

        particle.startRotateX = START_ROTATE * random.nextFloat();
        particle.startRotateY = START_ROTATE * random.nextFloat();
        particle.startRotateZ = START_ROTATE * random.nextFloat();

        particle.endRotateX = random.nextFloat() < 0.3f ? END_ROTATE * random.nextFloat() :
                random.nextFloat() < 0.7f ? END_ROTATE + 180 * random.nextFloat() :
                        END_ROTATE * 2 + 180 * random.nextFloat();
        particle.endRotateY = random.nextFloat() < 0.3f ? END_ROTATE * random.nextFloat() :
                random.nextFloat() < 0.7f ? END_ROTATE + 180 * random.nextFloat() :
                        END_ROTATE * 2 + 180 * random.nextFloat();
        particle.endRotateZ = random.nextFloat() < 0.3f ? END_ROTATE * random.nextFloat() :
                random.nextFloat() < 0.7f ? END_ROTATE + 180 * random.nextFloat() :
                        END_ROTATE * 2 + 180 * random.nextFloat();

        particle.life = 0f;
        particle.overflow = 0f;
        particle.alpha = 1f;

        return particle;
    }

    @Override
    public void start() {
        super.start();
        mContainer.invalidate(mBound);
    }

    public boolean draw(Canvas canvas) {
        if (!isStarted()) {
            return false;
        }

        canvas.setDrawFilter(mFilter);
        for (Particle particle : mParticles) {
            particle.advance();
            if (particle.alpha > 0) {
                mPaint.setAlpha((int) (255 * particle.alpha));
                canvas.save();
                canvas.concat(particle.matrix);
                canvas.drawBitmap(particle.piece, null, particle.rectF, mPaint);
                canvas.restore();
            }
        }
        mContainer.invalidate();
        return true;
    }

    private class Particle {
        RectF srcRectF;
        RectF dstRectF;
        RectF rectF;
        float gravity;

        float startRotateX;
        float startRotateY;
        float startRotateZ;
        float endRotateX;
        float endRotateY;
        float endRotateZ;
        float rotateX;
        float rotateY;
        float rotateZ;

        float life;
        float overflow;

        Bitmap piece;
        float alpha;
        final Matrix matrix = new Matrix();
        final Camera camera = new Camera();

        public float getValue(float start, float end, float k) {
            return start + (end - start) * k;
        }

        public void advance() {
            float normalization = getAnimatedFraction();
            if (normalization < life || normalization > 1f - overflow) {
                alpha = 0f;
                return;
            }

            normalization = (normalization - life) / (1f - life - overflow);
            alpha = 1f - KEEP_FINISH_INTERPOLATOR.getInterpolation(normalization);

            rectF.left = getValue(srcRectF.left, dstRectF.left, normalization);
            rectF.top = (float) (getValue(srcRectF.top, dstRectF.top, normalization)
                    + gravity * Math.pow(normalization - 0.3f, 2));
            rectF.right = rectF.left + srcRectF.width();
            rectF.bottom = rectF.top + srcRectF.width();

            rotateX = getValue(startRotateX, endRotateX, normalization);
            rotateY = getValue(startRotateY, endRotateY, normalization);
            rotateZ = getValue(startRotateZ, endRotateZ, normalization);

            matrix.reset();
            camera.save();
            camera.rotateX(rotateX);
            camera.rotateY(rotateY);
            camera.rotateZ(-rotateZ);
            camera.getMatrix(matrix);
            camera.restore();

            matrix.preTranslate(-rectF.centerX(), -rectF.centerY());
            matrix.postTranslate(rectF.centerX(), rectF.centerY());
        }

    }
}
