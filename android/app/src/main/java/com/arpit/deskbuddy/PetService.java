package com.arpit.deskbuddy;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import android.graphics.drawable.GradientDrawable;
import java.io.InputStream;
import java.util.ArrayList;

public class PetService extends Service implements PetView.Listener {
    private static final String CHANNEL_ID = "sage_pet";
    private static final int NOTIFICATION_ID = 100;
    private static final int PET_SIZE_DP = 150;
    private static final int SPRITE_HEIGHT_DP = 118;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private PetView petView;
    private View menuView;

    private final ArrayList<Bitmap> idle = new ArrayList<>();
    private final ArrayList<Bitmap> petted = new ArrayList<>();
    private final ArrayList<Bitmap> walk = new ArrayList<>();
    private final ArrayList<Bitmap> sleep = new ArrayList<>();

    private enum State { IDLE, PETTED, WALK, SLEEP }
    private State state = State.IDLE;
    private int frame = 0;
    private long lastFrame = 0, idleMs = 0, pettedMs = 0, lastTap = 0;
    private float speed = 1f, targetX, targetY;
    private boolean facingLeft, sleeping, pinned, gravity = true;
    private float downX, downY;
    private int downWinX, downWinY;
    private boolean dragging, longPressed;

    private final Runnable longPress = () -> {
        if (!dragging) { longPressed = true; showMenu(); }
    };

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            updateLogic();
            handler.postDelayed(this, 30);
        }
    };

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(NOTIFICATION_ID, notification());
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return; }
        loadAssets();
        createOverlay();
        handler.post(tick);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }

    private void createOverlay() {
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        int size = dp(PET_SIZE_DP);
        params = new WindowManager.LayoutParams(
                size, size,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;

        int[] s = screen();
        params.x = Math.max(0, s[0] - size - dp(16));
        params.y = Math.max(0, s[1] - size - navigationBar());
        targetX = params.x; targetY = params.y;

        petView = new PetView(this, this);
        wm.addView(petView, params);
        updateFrame();
    }

    private void loadAssets() {
        load("sage_idle_", 4, idle);
        if (idle.size() >= 4) {
            petted.add(idle.get(2)); petted.add(idle.get(3));
            idle.subList(2, idle.size()).clear();
        }
        load("sage_walk_", 6, walk);
        load("sage_sleep_", 1, sleep);
        if (idle.isEmpty() || walk.isEmpty() || sleep.isEmpty())
            throw new IllegalStateException("SAGE assets are missing");
    }

    private void load(String prefix, int count, ArrayList<Bitmap> out) {
        for (int i = 1; i <= count; i++) {
            String name = prefix + i + ".png";
            try (InputStream in = getAssets().open(name)) {
                Bitmap b = BitmapFactory.decodeStream(in);
                if (b != null) out.add(scale(b, dp(SPRITE_HEIGHT_DP)));
            } catch (Exception ignored) {}
        }
    }

    private Bitmap scale(Bitmap b, int h) {
        float sc = h / (float)b.getHeight();
        return Bitmap.createScaledBitmap(b, Math.max(1, Math.round(b.getWidth()*sc)), h, true);
    }

    private void updateLogic() {
        if (petView == null || wm == null) return;
        long now = System.currentTimeMillis();

        if (sleeping) {
            if (now - lastFrame >= 2400) { lastFrame = now; next(sleep); }
            return;
        }

        if (state == State.PETTED) {
            pettedMs += 30;
            if (pettedMs >= 1200) setState(State.IDLE);
        }

        if (state == State.WALK) {
            float dx = targetX - params.x, dy = targetY - params.y;
            float dist = (float)Math.hypot(dx, dy);
            float step = dp(4) * speed;

            if (dist > step) {
                params.x += Math.round(dx/dist*step);
                params.y += Math.round(dy/dist*step);
                if (Math.abs(dx) > dp(1)) facingLeft = dx < 0;
                wm.updateViewLayout(petView, params);
            } else {
                params.x = Math.round(targetX); params.y = Math.round(targetY);
                wm.updateViewLayout(petView, params);
                speed = 1f; setState(State.IDLE);
            }

            long delay = Math.max(60, (long)(300/speed));
            if (now-lastFrame >= delay) { lastFrame=now; next(walk); }
            idleMs=0;
            return;
        }

        if (state == State.IDLE) {
            idleMs += 30;
            if (!pinned && idleMs >= 15000) { sleeping=true; setState(State.SLEEP); return; }
            if (now-lastFrame >= 1500) { lastFrame=now; next(idle); }
        }
    }

    private void setState(State s) {
        if (state == s) return;
        state=s; frame=0; lastFrame=0;
        if (s==State.IDLE) idleMs=0;
        updateFrame();
    }

    private void next(ArrayList<Bitmap> frames) {
        if (frames.size()>1) frame=(frame+1)%frames.size(); else frame=0;
        updateFrame();
    }

    private void updateFrame() {
        if (petView==null) return;
        ArrayList<Bitmap> f = state==State.WALK ? walk :
                state==State.PETTED ? petted :
                state==State.SLEEP ? sleep : idle;
        if (!f.isEmpty()) petView.setFrame(f.get(Math.min(frame,f.size()-1)), facingLeft);
    }

    @Override public void onPetDown(MotionEvent e) {
        downX=e.getRawX(); downY=e.getRawY();
        downWinX=params.x; downWinY=params.y;
        dragging=false; longPressed=false;
        handler.removeCallbacks(longPress);
        handler.postDelayed(longPress,600);
        if (sleeping) { sleeping=false; setState(State.IDLE); }
    }

    @Override public void onPetMove(MotionEvent e) {
        float dx=e.getRawX()-downX, dy=e.getRawY()-downY;
        if (Math.hypot(dx,dy)>dp(8)) { dragging=true; handler.removeCallbacks(longPress); }
        if (dragging) {
            int[] s=screen();
            params.x=clamp(downWinX+Math.round(dx),0,s[0]-params.width);
            params.y=clamp(downWinY+Math.round(dy),0,floorY());
            wm.updateViewLayout(petView,params);
            if (Math.abs(dx)>dp(2)) facingLeft=dx<0;
            updateFrame();
        }
    }

    @Override public void onPetUp(MotionEvent e) {
        handler.removeCallbacks(longPress);
        if (longPressed) return;
        if (!dragging) {
            sleeping=false; setState(State.PETTED); pettedMs=0; return;
        }
        if (!pinned && gravity && params.y<floorY()) {
            targetX=params.x; targetY=floorY(); setState(State.WALK);
        } else setState(State.IDLE);
    }

    @Override public void onOutsideTap(float x,float y) {
        if (menuView!=null) { hideMenu(); return; }
        if (sleeping || pinned) return;
        long now=System.currentTimeMillis();
        speed=(now-lastTap<500)?Math.min(speed+1f,4f):1f;
        lastTap=now;
        targetX=clamp(x-params.width/2f,0,screen()[0]-params.width);
        targetY=clamp(y-params.height/2f,0,floorY());
        idleMs=0; setState(State.WALK);
    }

    @Override public void onLongPress() { showMenu(); }

    private void showMenu() {
        if (menuView!=null || wm==null) return;
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(8),dp(8),dp(8),dp(8));
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.WHITE); bg.setCornerRadius(dp(14)); bg.setStroke(dp(1),Color.LTGRAY);
        box.setBackground(bg);

        Button pin=button(pinned?"Unpin from Position":"Pin to Position");
        Button grav=button(gravity?"Disable Gravity":"Enable Gravity");
        Button sl=button(sleeping?"Wake Up":"Sleep Mode");
        Button quit=button("Shutdown SAGE");
        box.addView(pin); box.addView(grav); box.addView(sl); box.addView(quit);

        pin.setOnClickListener(v->{pinned=!pinned;if(pinned)setState(State.IDLE);hideMenu();});
        grav.setOnClickListener(v->{gravity=!gravity;hideMenu();});
        sl.setOnClickListener(v->{sleeping=!sleeping;setState(sleeping?State.SLEEP:State.IDLE);hideMenu();});
        quit.setOnClickListener(v->{hideMenu();stopSelf();});

        menuView=box;
        WindowManager.LayoutParams mp=new WindowManager.LayoutParams(
                dp(230),WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        mp.gravity=Gravity.TOP|Gravity.START;
        mp.x=clamp(params.x,0,screen()[0]-dp(230));
        mp.y=Math.max(dp(8),params.y-dp(220));
        wm.addView(menuView,mp);
    }

    private Button button(String text) {
        Button b=new Button(this); b.setText(text); b.setAllCaps(false); b.setMinHeight(dp(46)); return b;
    }

    private void hideMenu() {
        if(menuView!=null&&wm!=null) try{wm.removeView(menuView);}catch(Exception ignored){}
        menuView=null;
    }

    private Notification notification() {
        Intent i=new Intent(this,MainActivity.class);
        PendingIntent p=PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this,CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentTitle("SAGE is active")
                .setContentText("Your floating pet is running.")
                .setOngoing(true).setContentIntent(p).build();
    }

    private void createChannel() {
        if(Build.VERSION.SDK_INT>=26) {
            NotificationChannel c=new NotificationChannel(CHANNEL_ID,"SAGE Pet",NotificationManager.IMPORTANCE_LOW);
            NotificationManager m=getSystemService(NotificationManager.class);
            if(m!=null)m.createNotificationChannel(c);
        }
    }

    private int[] screen() {
        android.util.DisplayMetrics dm=new android.util.DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        return new int[]{dm.widthPixels,dm.heightPixels};
    }

    private int navigationBar() {
        int id=getResources().getIdentifier("navigation_bar_height","dimen","android");
        return id>0?getResources().getDimensionPixelSize(id):dp(48);
    }

    private int floorY() { return Math.max(0,screen()[1]-params.height-navigationBar()); }
    private int dp(int v) { return Math.round(v*getResources().getDisplayMetrics().density); }
    private int clamp(int v,int min,int max) { return Math.max(min,Math.min(max,v)); }
    private float clamp(float v,float min,float max) { return Math.max(min,Math.min(max,v)); }

    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        hideMenu();
        if(petView!=null&&wm!=null) try{wm.removeView(petView);}catch(Exception ignored){}
        petView=null;
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent){return null;}
}
