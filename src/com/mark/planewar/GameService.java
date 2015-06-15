package com.mark.planewar;

import com.mark.planewar.model.*;

import java.util.ArrayList;
import java.util.Random;

public class GameService {

    private Random mRandom = new Random(System.currentTimeMillis());
    private static GameService mGameService = null;

    public static GameService getInstance() {
        if (mGameService == null) {
            mGameService = new GameService();
        }
        return mGameService;
    }

    public int getRandomNum(int max) {
        return mRandom.nextInt(max);
    }

    /**
     * Generate cloud list to make a background
     */
    public ArrayList<Cloud> getOriginalCloudList(int width, int height) {
        ArrayList<Cloud> list = new ArrayList<>(GameConst.MAX_CLOUD);

        for (int i = 0; i < GameConst.MAX_CLOUD; ++i) {
            Cloud cloud = new Cloud();
            cloud.x = getRandomNum(width);
            cloud.y = getRandomNum(height);
            list.add(cloud);
        }

        return list;
    }

    /**
     * Generate normal enemy plane by stage
     */
    public EnemyPlane getEnemyPlane(int stage) {
        return getEnemyPlane(stage, false);
    }

    public EnemyPlane getEnemyPlane(int stage, boolean isBoss) {
        EnemyPlane plane = new EnemyPlane();
        plane.attack = stage;
        plane.hp = stage;
        plane.speed = GameConst.PLANE_SPEED;
        plane.bulletInterval = 250;

        if (isBoss) {
            plane.isBoss = true;
            plane.hp *= 2;
            plane.attack *= 2;
            plane.bulletInterval = 150;
        }

        plane.score = 1000 * plane.attack * plane.hp;

        return plane;
    }

    /**
     * Give you a plane with HP 5, ATT 5
     */
    public PlayerPlane getPlayerPlane() {
        PlayerPlane plane = new PlayerPlane();
        plane.hp = 5;
        plane.attack = 1;
        plane.speed = GameConst.PLANE_SPEED;
        return plane;
    }

    public Bullet getPlaneBullet(PlayerPlane playerPlane) {
        Bullet bullet = new Bullet();
        bullet.direction = GameConst.DIRECTION_UP;
        bullet.damage = playerPlane.attack;
        bullet.speed = 5;
        return bullet;
    }

    public Bullet getEnemyBullet(EnemyPlane enemyPlane) {
        Bullet bullet = new Bullet();
        bullet.direction = GameConst.DIRECTION_DOWN;
        bullet.damage = enemyPlane.attack;
        bullet.speed = 8;
        return bullet;
    }

    public int getMaxEnemyCountOnScreen(int stage) {
        return stage * 3;
    }

    public int getEnemyCount(int stage) {
        int count = 0;
        switch (stage) {
            case 1:
                count = 5;
                break;
            case 2:
                count = 15;
                break;
            case 3:
                count = 30;
                break;
            default:
                break;
        }
        return count;
    }
}
