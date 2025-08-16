//#ifndef OLD_ABOUT_SCREEN
package com.gtrxac.discord;

import java.util.*;

public class AboutScreenParticle {
    public int x;
    public int y;
    public int velX;
    public int velY;
//#ifdef ABOUT_SCREEN_COLOR_PARTICLES
    public int color;
//#endif

    private static int rand() {
        int result;
        do {
            result = AboutScreen.rng.nextInt()&255;
        }
        while (result > 250);
        return result - 125;
    }

    public AboutScreenParticle() {
        reset();
    }

    public void reset() {
        this.x = 5000;
        this.y = 5000;

        int speed;
        do {
            this.velX = rand();
            this.velY = rand();
            speed = Math.abs(this.velX) + Math.abs(this.velY);
        }
        while (speed < 50);

//#ifdef ABOUT_SCREEN_COLOR_PARTICLES 
        speed = Math.min(Math.max(speed, 80), 224);
        color = speed | (speed << 8) | (speed << 16);
//#endif

        this.x += this.velX*15;
        this.y += this.velY*15;
    }
}
//#endif