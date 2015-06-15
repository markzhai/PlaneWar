package com.mark.planewar.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.mark.planewar.GameConst;
import com.mark.planewar.GameService;
import com.mark.planewar.R;
import com.mark.planewar.model.Bullet;
import com.mark.planewar.model.Cloud;
import com.mark.planewar.model.EnemyPlane;
import com.mark.planewar.model.PlayerPlane;
import com.mark.planewar.utils.ViewUtils;

import java.util.ArrayList;
import java.util.LinkedList;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder mSurfaceHolder;

    // bitmap resources
    private ArrayList<Bitmap> mCloudBitmapList;
    private Bitmap mPlaneBitmap;
    private Bitmap mPlaneFireBitmap;
    private Bitmap mEnemyBitmap;
    private Bitmap mBulletBitmap;

    private float mLastTouchX;
    private float mLastTouchY;
    private Paint mPaint;
    private int mBackgroundColor;

    // Game objects
    private ArrayList<Cloud> mCloudList;
    private PlayerPlane mPlayerPlane;
    private LinkedList<EnemyPlane> mEnemyPlaneList;
    private LinkedList<Bullet> mPlayerBullet;
    private LinkedList<Bullet> mEnemyBullet;
    private int sumScore = 0;
    private boolean mIsTouchingPlane;    // 判断玩家是否按下屏幕

    private static final int TOUCH_THRESHOLD = 60;

    // Game logic
    private int mStage = 1;
    private boolean mGameStarted = true;
    private boolean mGameEnded = false;
    private int mCurrentFrame = 0;
    private int mEnemyLeft = 0;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSurfaceHolder = this.getHolder();
        mSurfaceHolder.addCallback(this);
        mPaint = new Paint();
        mBackgroundColor = context.getResources().getColor(R.color.background_color);
        mPaint.setTextSize(ViewUtils.dpToPx(getContext(), 30));
        mPaint.setColor(Color.YELLOW);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Bitmap mCloudBitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.cloud1);
        Bitmap mCloudBitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.cloud2);
        Bitmap mCloudBitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.cloud3);
        mCloudBitmapList = new ArrayList<>(3);
        mCloudBitmapList.add(mCloudBitmap1);
        mCloudBitmapList.add(mCloudBitmap2);
        mCloudBitmapList.add(mCloudBitmap3);
        mPlaneBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plane);
        mPlaneFireBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plane_rear_fire);
        mEnemyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.enemy);
        mBulletBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bullet);

        mPlayerPlane = GameService.getInstance().getPlayerPlane();
        mPlayerPlane.x = getWidth() / 2 - mPlaneBitmap.getWidth() / 2;
        mPlayerPlane.y = getHeight() - mPlaneBitmap.getHeight() - mPlaneFireBitmap.getHeight();

        mPlayerBullet = new LinkedList<>();
        mEnemyBullet = new LinkedList<>();
        mEnemyPlaneList = new LinkedList<>();
        mEnemyLeft = GameService.getInstance().getEnemyCount(mStage);

        mCloudList = GameService.getInstance().getOriginalCloudList(getWidth(), getHeight());

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mGameStarted = false;
        mGameEnded = true;
        if (!mEnemyBitmap.isRecycled()) {
            mEnemyBitmap.recycle();
        }
        if (!mBulletBitmap.isRecycled()) {
            mBulletBitmap.recycle();
        }
        if (!mPlaneBitmap.isRecycled()) {
            mPlaneBitmap.recycle();
        }
        if (!mPlaneFireBitmap.isRecycled()) {
            mPlaneFireBitmap.recycle();
        }
        for (Bitmap bitmap : mCloudBitmapList) {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    @Override
    public void run() {
        while (mGameStarted) {
            ++mCurrentFrame;
            drawGame();
            doGameLogic();
        }
    }

    /**
     * Move plane with touch event
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;

        if (event.getAction() == MotionEvent.ACTION_UP) {
            mIsTouchingPlane = false;
            handled = true;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            if (x > mPlayerPlane.x
                    && x < mPlayerPlane.x + mPlaneBitmap.getWidth()
                    && y > mPlayerPlane.y
                    && y < mPlayerPlane.y + mPlaneBitmap.getHeight()) {
                mIsTouchingPlane = mGameStarted;
                mLastTouchX = x;
                mLastTouchY = y;
                handled = true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && event.getPointerCount() == 1) {
            if (mIsTouchingPlane) {
                float x = event.getX();
                float y = event.getY();
                if (Math.abs(x - mLastTouchX) < TOUCH_THRESHOLD) {
                    mPlayerPlane.x = mPlayerPlane.x + x - mLastTouchX;
                }
                if (Math.abs(y - mLastTouchY) < TOUCH_THRESHOLD) {
                    mPlayerPlane.y = mPlayerPlane.y + y - mLastTouchY;
                }
                mLastTouchX = x;
                mLastTouchY = y;
                handled = true;
            }
        }
        return handled;
    }

    private void doGameLogic() {
        if (mGameStarted && !mGameEnded) {
            // make player plane's bullet
            if (mCurrentFrame - mPlayerPlane.frame > mPlayerPlane.bulletInterval) {
                Bullet bullet = GameService.getInstance().getPlaneBullet(mPlayerPlane);
                bullet.x = mPlayerPlane.x + mPlaneBitmap.getWidth() / 2 - mBulletBitmap.getWidth() / 2;
                bullet.y = mPlayerPlane.y - mBulletBitmap.getHeight();
                mPlayerBullet.add(bullet);
                mPlayerPlane.frame = mCurrentFrame;
            }
            // make enemy plane's bullet
            for (EnemyPlane plane : mEnemyPlaneList) {
                if (plane.frame == 0 || mCurrentFrame - plane.frame > plane.bulletInterval) {
                    Bullet bullet = GameService.getInstance().getEnemyBullet(plane);
                    bullet.x = plane.x + mEnemyBitmap.getWidth() / 2 - mBulletBitmap.getWidth() / 2;
                    bullet.y = plane.y + mEnemyBitmap.getHeight();
                    mEnemyBullet.add(bullet);
                    plane.frame = mCurrentFrame;
                }
            }
            // make enemy
            if (mEnemyLeft > 0) {
                // does not exceed max enemy on screen
                if (mEnemyPlaneList.size() < GameService.getInstance().getMaxEnemyCountOnScreen(mStage)) {
                    if (GameService.getInstance().getRandomNum(100) > 96) {
                        EnemyPlane plane = GameService.getInstance().getEnemyPlane(mStage, mEnemyLeft == 1);
                        plane.x = GameService.getInstance().getRandomNum(getWidth());
                        plane.y = 0;
                        mEnemyPlaneList.add(plane);
                        mEnemyLeft--;
                    }
                }
            } else if (mEnemyPlaneList.size() == 0) {
                // enemy clear
                mStage += 1;
                mEnemyLeft = GameService.getInstance().getEnemyCount(mStage);
            }
            // clear enemy out of screen
            for (int i = 0; i < mEnemyPlaneList.size(); ++i) {
                if (mEnemyPlaneList.get(i).y > getHeight()) {
                    mEnemyPlaneList.remove(i);
                }
            }

            // ============================= detect collision ==============================

            // between enemy bullet and player plane
            for (int i = 0; i < mEnemyBullet.size(); ++i) {
                Bullet bullet = mEnemyBullet.get(i);
                if (ViewUtils.isOverLapping(bullet.x, bullet.y, mBulletBitmap.getWidth(), mBulletBitmap.getHeight(),
                        mPlayerPlane.x, mPlayerPlane.y, mPlaneBitmap.getWidth(), mPlaneBitmap.getHeight())) {
                    mPlayerPlane.hp -= bullet.damage;
                    mEnemyBullet.remove(i);
                }
            }

            // between player bullet and enemy plane
            for (int i = 0; i < mPlayerBullet.size(); ++i) {
                Bullet bullet = mPlayerBullet.get(i);
                for (int j = 0; j < mEnemyPlaneList.size(); ++j) {
                    EnemyPlane plane = mEnemyPlaneList.get(j);
                    if (ViewUtils.isOverLapping(bullet.x, bullet.y, mBulletBitmap.getWidth(), mBulletBitmap.getHeight(),
                            plane.x, plane.y, mEnemyBitmap.getWidth(), mEnemyBitmap.getHeight())) {
                        plane.hp -= bullet.damage;
                        mPlayerBullet.remove(i);
                        if (plane.hp <= 0) {
                            sumScore += plane.score;
                            mEnemyPlaneList.remove(j);
                        }
                    }
                }
            }

            // between player and enemy
            for (int j = 0; j < mEnemyPlaneList.size(); ++j) {
                EnemyPlane plane = mEnemyPlaneList.get(j);
                if (ViewUtils.isOverLapping(mPlayerPlane.x, mPlayerPlane.y, mPlaneBitmap.getWidth(), mPlaneBitmap.getHeight(),
                        plane.x, plane.y, mEnemyBitmap.getWidth(), mEnemyBitmap.getHeight())) {
                    mPlayerPlane.hp -= 1;
                    sumScore += plane.score;

                    // crash is powerful
                    mEnemyPlaneList.remove(j);
                }
            }

            if (mStage > GameConst.MAX_STAGE || mPlayerPlane.hp <= 0) {
                // wow, game over...
                mGameEnded = true;
            }
        }
    }

    public void drawGame() {
        Canvas canvas = mSurfaceHolder.lockCanvas();

        if (canvas != null) {
            drawBackground(canvas);
            drawPlane(canvas);
            drawBullet(canvas);
            drawInformation(canvas);
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void drawBackground(Canvas canvas) {
        canvas.drawColor(mBackgroundColor);
        canvas.save();

        for (int i = 0; i < mCloudList.size(); ++i) {
            canvas.drawBitmap(mCloudBitmapList.get(i % 3), mCloudList.get(i).x, mCloudList.get(i).y, mPaint);
            Cloud cloud = mCloudList.get(i);
            cloud.y += cloud.speed;
            if (cloud.y >= getHeight()) {
                cloud.y = -ViewUtils.dpToPx(getContext(), 100);
                cloud.x = GameService.getInstance().getRandomNum(getWidth());
            }
        }

        canvas.restore();
    }

    public void drawPlane(Canvas canvas) {
        canvas.save();

        canvas.drawBitmap(mPlaneBitmap, mPlayerPlane.x, mPlayerPlane.y, mPaint);
        canvas.drawBitmap(mPlaneFireBitmap, mPlayerPlane.x, mPlayerPlane.y + mPlaneBitmap.getHeight(), mPaint);

        for (EnemyPlane plane : mEnemyPlaneList) {
            canvas.drawBitmap(mEnemyBitmap, plane.x, plane.y, mPaint);
            plane.y += plane.speed;
        }

        canvas.restore();
    }

    public void drawBullet(Canvas canvas) {
        canvas.save();

        for (Bullet bullet : mPlayerBullet) {
            bullet.y -= bullet.speed;
            canvas.drawBitmap(mBulletBitmap, bullet.x, bullet.y, mPaint);
        }
        for (Bullet bullet : mEnemyBullet) {
            bullet.y += bullet.speed;
            canvas.drawBitmap(mBulletBitmap, bullet.x, bullet.y, mPaint);
        }

        canvas.restore();
    }

    public void drawInformation(Canvas canvas) {
        canvas.save();
        canvas.drawText("Score:" + String.valueOf(sumScore), 30, 150, mPaint);
        canvas.drawText("HP:" + String.valueOf(mPlayerPlane.hp), 30, 300, mPaint);
        canvas.drawText("STAGE:" + String.valueOf(mStage), 30, 450, mPaint);

        if (mGameEnded) {
            mPaint.setColor(Color.RED);
            canvas.drawText("GAME OVER", getWidth() / 3, getHeight() / 2, mPaint);
        }
        canvas.restore();
    }
}
