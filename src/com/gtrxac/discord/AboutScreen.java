//#ifndef OLD_ABOUT_SCREEN
package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import java.util.*;

public class AboutScreen extends KineticScrollingCanvas implements CommandListener, Strings, Runnable {
    private Image logo;
    private Command backCommand;

    public static final Random rng = new Random();

//#ifdef ABOUT_SCREEN_EASING
    private static final int[] easing = {
        17, 33, 50, 66, 83, 99, 115, 132, 123, 115, 105, 96, 87, 89, 92, 94, 96, 98, 101, 103, 105, 104, 103, 102, 101, 100, 99, 98, 98, 99, 99, 100, 100, 101, 101
    };

    private static final int[] easingSizes = {
        17, 33, 50, 66, 83, 87, 89, 92, 94, 96, 98, 99, 100, 101, 102, 103, 104, 105, 115, 123, 132
    };

    private Image[] scaledIcons;
//#endif

    int logoSize;
    int logoY;
    int easingTimer = 0;  // delay before icon starts moving upwards (also on versions without easing animation)

//#ifdef NOKIA_128PX
    private static final int PARTICLE_COUNT = 30;
//#else
    private static final int PARTICLE_COUNT = 40;
//#endif

    private AboutScreenParticle[] particles;
    private AboutScreenItem[] items;

//#ifdef ABOUT_SCREEN_BOUNCE
    static int bounceTimer = 0;
//#endif

//#ifdef ABOUT_SCREEN_SPARKLES
    private Image[] sparkles;
    int sparkleTimer = -10;
    int sparkleX;
    int sparkleY;
//#endif

    public static int maxScroll;

    public AboutScreen() {
        super();
        setTitle(Locale.get(ABOUT_TITLE));
        setCommandListener(this);
        backCommand = Locale.createCommand(BACK, Command.BACK, 0);
        addCommand(backCommand);

        logoSize = Settings.getBestMenuIconSize()*3;
        try {
            logo = Image.createImage("/icon.png");
            logo = Util.resizeImage(logo, logoSize, logoSize);
        }
        catch (Exception e) {}

//#ifdef ABOUT_SCREEN_EASING
        scaledIcons = new Image[easing.length];

        for (int i = 0; i < easingSizes.length; i++) {
            int scaledSize = logoSize*easingSizes[i]/100;
            Image scaled = Util.resizeImageBilinear(logo, scaledSize, scaledSize);

            for (int j = 0; j < easing.length; j++) {
                if (easing[j] == easingSizes[i]) {
                    scaledIcons[j] = scaled;
                }
            }
        }
//#endif

//#ifdef ABOUT_SCREEN_SPARKLES
        try {
            Spritesheet sparkle = new Spritesheet("/sparkle.png", 15);
            sparkle.blockSize = 15;
            sparkles = new Image[9];
            int sparkleSize = logoSize/48*15;

            for (int i = 0; i < 9; i++) {
                sparkles[i] = Util.resizeImage(sparkle.next(), sparkleSize, sparkleSize);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
//#endif

        particles = new AboutScreenParticle[PARTICLE_COUNT];
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles[i] = new AboutScreenParticle();
        }
        for (int i = 0; i < 50; i++) updateParticles();  // move particles a bit from their starting positions

        logoY = getHeight()/2 - logoSize/2;

        AboutScreenItem.titleColor = 0x000000;
        AboutScreenItem.contentColor = 0x000000;

        String versionStr =
            Locale.get(VERSION) +
            App.VERSION_NAME +
            Locale.get(LEFT_PAREN) +
            App.VERSION_VARIANT +
            Locale.get(RIGHT_PAREN);

        Vector i = new Vector();

        i.addElement(new AboutScreenItem("Discord J2ME", Font.SIZE_MEDIUM, 0, true, false));
        i.addElement(new AboutScreenItem(versionStr, Font.SIZE_SMALL, Util.fontSize, true, false));

        i.addElement(new AboutScreenItem(Locale.get(ABOUT_DEVELOPERS), Font.SIZE_MEDIUM, Util.fontSize/2, false, false));
        addDeveloper(i, "gtrxAC", LEAD_DEVELOPER, 2);
        addDeveloper(i, "Shinovon", WHAT_SHINOVON_DID, 2);
        addDeveloper(i, "AeroPurple", WHAT_AEROPURPLE_DID, 1);

        i.addElement(new AboutScreenItem(Locale.get(ABOUT_TRANSLATORS), Font.SIZE_MEDIUM, Util.fontSize/2, false, false));
        addDeveloper(i, "ACPI Fixed Feature Button", TRANSLATOR_VI, 2);
        addDeveloper(i, "AlanHudik", TRANSLATOR_HR, 2);
        addDeveloper(i, "Alex222", TRANSLATOR_SV, 2);
        addDeveloper(i, "cappuchi", TRANSLATOR_PL, 2);
        addDeveloper(i, "chair_senpai", TRANSLATOR_HU, 2);
        addDeveloper(i, "Dragan232", TRANSLATOR_CA, 2);
        i.addElement(new AboutScreenItem("ElHamexd", Font.SIZE_SMALL, 0, false, true));
        addDeveloper(i, "AlexisBlade2001", TRANSLATOR_ES, 2);
        addDeveloper(i, "EyadMahm0ud", TRANSLATOR_AR, 2);
        addDeveloper(i, "Kaiky Alexandre Souza", TRANSLATOR_PTBR, 2);
        addDeveloper(i, "Lennard_105", TRANSLATOR_DE, 2);
        addDeveloper(i, "logoffon", TRANSLATOR_TH, 2);
        addDeveloper(i, "mal0gen", TRANSLATOR_PT, 2);
        addDeveloper(i, "Misu", TRANSLATOR_ID, 2);
        addDeveloper(i, "Motorazr", TRANSLATOR_FR, 2);
        addDeveloper(i, "nativeshades", TRANSLATOR_BG, 2);
        i.addElement(new AboutScreenItem("MC-Nirvana", Font.SIZE_SMALL, 0, false, true));
        addDeveloper(i, "pdyq", TRANSLATOR_ZHTW_ZHHK, 2);
        addDeveloper(i, "proxion", TRANSLATOR_UK, 2);
        addDeveloper(i, "raul0028", TRANSLATOR_RO, 2);
        i.addElement(new AboutScreenItem("Meganium412", Font.SIZE_SMALL, 0, false, true));
        addDeveloper(i, "violent body", TRANSLATOR_TR, 2);
        i.addElement(new AboutScreenItem("SpiroWalkman", Font.SIZE_SMALL, 0, false, true));
        addDeveloper(i, "multiplemegapixels", TRANSLATOR_RU, 2);
        addDeveloper(i, "tsukihimeri6969", TRANSLATOR_JA, 2);
        addDeveloper(i, "MC-Nirvana", TRANSLATOR_ZHCN, 1);

        i.addElement(new AboutScreenItem(Locale.get(ABOUT_SUPPORT), Font.SIZE_MEDIUM, Util.fontSize/2, false, false));
        i.addElement(new AboutScreenItem("discord.gg/2GKuJjQagp", Font.SIZE_SMALL, Util.fontSize/2, false, false));
        i.addElement(new AboutScreenItem("t.me/dscforsymbian", Font.SIZE_SMALL, Util.fontSize/2, false, false));
        i.addElement(new AboutScreenItem("gtrxac.fi", Font.SIZE_SMALL, Util.fontSize/2, false, false));

        items = new AboutScreenItem[i.size()];
        i.copyInto(items);

        sizeChanged(0, 0);  // calculate word wrap for items

        threadIsForParticles = true;
        new Thread(this).start();
    }

    private void addDeveloper(Vector i, String name, int descKey, int marginDiv) {
        i.addElement(new AboutScreenItem(name, Font.SIZE_SMALL, 0, false, true));
        i.addElement(new AboutScreenItem(Locale.get(descKey), Font.SIZE_SMALL, Util.fontSize/marginDiv, false, false));
    }

    protected void sizeChanged(int w, int h) {
        int width = getWidth();
        maxScroll = logoSize + logoSize/3 + logoSize/8;

        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                items[i].recalc(width);
            }
        }
    }

    public void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        checkScrollInRange();

//#ifdef BLACKBERRY
        bbDrawTitle(g);
//#endif

        g.setColor(0x000000);
        g.fillRect(0, 0, width, height);

//#ifdef NOKIA_THEME_BACKGROUND
        // Fix white border rendering bug on Symbian 9.3 - 9.4
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
//#endif

//#ifndef ABOUT_SCREEN_COLOR_PARTICLES
        g.setColor(0x888888);
//#endif
        int particleSize = Math.max(1, (width+height)/240);

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            AboutScreenParticle p = particles[i];
//#ifdef ABOUT_SCREEN_COLOR_PARTICLES
            g.setColor(p.color);
//#endif
            g.fillRect(p.x*width/10000, p.y*height/10000, particleSize, particleSize);
        }

        g.translate(0, -scroll);
        int logoArea = logoSize + logoSize/3;
        if (scroll < logoArea) drawLogo(g, width);
        g.translate(0, logoArea + logoSize/8);

//#ifdef BLACKBERRY
        height = height*8/7;
//#endif

        for (int i = 0; i < items.length; i++) {
            items[i].draw(g, g.getTranslateY() < height && g.getTranslateY() > -items[i].height);
        }

//#ifdef BLACKBERRY
        g.translate(0, -g.getTranslateY() + bbTitleHeight);
//#else
        g.translate(0, -g.getTranslateY());
//#endif
        drawScrollbar(g);
    }

    public void drawLogo(Graphics g, int width) {
//#ifdef ABOUT_SCREEN_EASING
        boolean easingAnimRunning = (easingTimer/2 < easing.length);
//#else
        boolean easingAnimRunning = (easingTimer < 30);
//#endif

        g.drawImage(
//#ifdef ABOUT_SCREEN_EASING
            easingAnimRunning ? scaledIcons[easingTimer/2] :
//#endif
            logo,
            width/2,
            logoY + logoSize/2,
            Graphics.HCENTER | Graphics.VCENTER
        );

//#ifdef ABOUT_SCREEN_SPARKLES
        if (!easingAnimRunning && sparkleTimer >= 0 && sparkleTimer < 18) {
            g.drawImage(
                sparkles[sparkleTimer/2],
                width/2 - logoSize/2 + sparkleX,
                logoY + sparkleY,
                Graphics.HCENTER | Graphics.VCENTER
            );
        }
//#endif
    }

    private boolean threadIsForParticles;

    public void run() {
        if (threadIsForParticles) {
            threadIsForParticles = false;

            // Wait for this screen to show up
            while (App.disp.getCurrent() != this) {
                Util.sleep(10);
            }

            long updateTimer = 0;

            while (App.disp.getCurrent() == this) {
                long time = System.currentTimeMillis();
                boolean autoScroll =
                    AboutScreenItem.contentColor > 0xAAAAAA
//#ifdef TOUCH_SUPPORT
                    && velocity <= 1 && !usingScrollBar
//#endif
                    ;

                while (updateTimer > 20) {
                    update();
                    if (autoScroll) scroll += Math.max(1, getHeight()/200);
                    updateTimer -= 20;
                }

                repaint();
                Util.sleep(20);
                updateTimer += System.currentTimeMillis() - time;
            }
        }
//#ifdef TOUCH_SUPPORT
        // for kineticscrollingcanvas scroll thread
        else {
            super.run();
        }
//#endif
    }

    private void update() {
        updateParticles();

//#ifdef ABOUT_SCREEN_EASING
        if (easingTimer/2 < easing.length) {
//#else
        if (easingTimer < 30) {
//#endif
            easingTimer++;
        }
        else if (logoY > logoSize/3) {
            logoY -= logoSize/20;
        }
        else if (AboutScreenItem.titleColor < 0xFFFFFF) {
            AboutScreenItem.titleColor = Math.min(AboutScreenItem.titleColor + 0x030303, 0xFFFFFF);
        }
        else if (AboutScreenItem.contentColor < 0xEEEEEE) {
            AboutScreenItem.contentColor = Math.min(AboutScreenItem.contentColor + 0x030303, 0xEEEEEE);
        }

//#ifdef ABOUT_SCREEN_BOUNCE
        bounceTimer++;
        if (bounceTimer > 100) bounceTimer = 0;
//#endif

//#ifdef ABOUT_SCREEN_SPARKLES
        sparkleTimer++;
        if (sparkleTimer == 0) {
            sparkleX = logoSize*(rng.nextInt()&127)/128;
            sparkleY = logoSize*(rng.nextInt()&127)/128;
        }
        else if (sparkleTimer == 18) {
            sparkleTimer = -(rng.nextInt()&63) - 15;
        }
//#endif
    }

    private void updateParticles() {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            AboutScreenParticle p = particles[i];
            p.x += p.velX;
            p.y += p.velY;

            if (p.x < 0 || p.x > 10000 || p.y < 0 || p.y > 10000) {
                p.reset();
            }
        }
    }

    public void commandAction(Command c, Displayable d) {
        App.disp.setCurrent(MainMenu.get(false));
    }

    // make the app name and version number immediately visible
    private void skipAnimation() {
        while (AboutScreenItem.titleColor < 0xFFFFFF) update();
    }

    protected void keyAction(int keycode) {
        skipAnimation();
        switch (getGameAction(keycode)) {
            case UP: {
                scroll -= Util.fontSize*3;
                break;
            }
            case DOWN: {
                scroll += Util.fontSize*3;
                break;
            }
        }
        repaint();
    }

//#ifdef TOUCH_SUPPORT
    protected void pointerReleased(int x, int y) {
        skipAnimation();
        super.pointerReleased(x, y);
    }
//#endif

    protected int getMinScroll() { return 0; }
    protected int getMaxScroll() { return Math.max(maxScroll - getHeight(), 0); }
}
//#endif