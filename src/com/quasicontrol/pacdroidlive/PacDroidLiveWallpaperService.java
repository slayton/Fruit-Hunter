package com.quasicontrol.pacdroidlive;

import java.util.ArrayList;

import com.quasicontrol.live.Sprite;
import com.quasicontrol.live.TurningPoint;
import com.quasicontrol.live.WPUtil;
import com.quasicontrol.live.WallBlock;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class PacDroidLiveWallpaperService extends WallpaperService {

    //public static final String SHARED_PREFS_NAME="wallpaperprefs";

	private DisplayMetrics dMetrics;
	private Resources resources;
	private Context context; 
	
    @Override
    public void onCreate() {
        super.onCreate();
        this.dMetrics = WPUtil.getDisplayMetrics(this);
	    this.resources = this.getResources();
	    context = this.getApplicationContext();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        return new PacDroidEngine(dMetrics, resources, context);
    }


    class PacDroidEngine extends Engine 
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    	private DisplayMetrics dMetrics;
    	private Resources resources;
    	private Context context; 
    	
        private final Handler mHandler = new Handler();

        private int tOff;
        private int bOff;
        private int lOff;
        private int rOff;
        
        private ArrayList<Dot> dots;
        private ArrayList<TurningPoint> turningPoints;
        private ArrayList<Sprite> monsters;
        private Sprite pacdroid;
        private ArrayList<TurningPoint> startLocations;
        private ArrayList<WallBlock> walls;
        
        private int nCol = 4;
    	private int nRow = 4;
    	private int nMonsters = 4;
        
        private final Paint mPaint = new Paint();
                
        private final Runnable mDrawWallpaper = new Runnable() {
            public void run() {
                drawFrame();
            }
        };
        private boolean mVisible;
        private SharedPreferences mPrefs;
        private Rect wpBounds;
        
        
        private int nEndGameFrames = 10;

        // Preference Variables
        protected boolean drawWalls = false;
        protected int wallColor =Color.BLUE;
        protected int bgColor = Color.BLACK;
        protected int dotColor = Color.WHITE;
        protected boolean drawDots = true;
        private boolean eatMonsters;
        private int gameSpeed;
        private int runnerAi;
        private int monsterAi;
        private int winConditions;
        
        
        PacDroidEngine(DisplayMetrics m, Resources r, Context c) {
            // Create a Paint to draw the lines for our cube
        	
        	this.dMetrics = m;
        	this.resources = r; 
        	this.context = c;  
        	
        	mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            mPrefs.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(mPrefs, null);
            
        	this.loadPreferences();
        	
            final Paint paint = mPaint;
            paint.setColor(0xffffffff);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(2);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStyle(Paint.Style.STROKE);
            
         
            tOff = WPUtil.topOffset(dMetrics);
            bOff = WPUtil.bottomOffset(dMetrics);
            lOff = WPUtil.leftOffset(dMetrics);
            rOff = WPUtil.rightOffset(dMetrics);

            wpBounds = new Rect(0+lOff, 0+tOff, dMetrics.widthPixels-rOff, dMetrics.heightPixels-bOff);
            
            WPUtil.logD("Screen Resolution:".concat(Integer.toString(dMetrics.widthPixels)).concat("x").concat(Integer.toString(dMetrics.heightPixels)));
            WPUtil.logD("Bounds for the wallpaper:".concat(wpBounds.toShortString()));
            
            //initDotArray();
   		 	//initTurningPoints();
   		 	//initWalls();
   		 	//initStartLocations();
            //initActors();
           
            resetGame();
            
            //ghosts.add(new Ghost(100, 300, wpBounds, BitmapFactory.decodeResource(resources, R.raw.g5)))

            //mPrefs = PacManWallpaperService.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
        }
        private void loadPreferences()
        {
            
        	WPUtil.logD("loading PREFERENCES!!!");
        	eatMonsters = mPrefs.getBoolean("eat_monsters", true);
        	gameSpeed = Integer.parseInt(mPrefs.getString("game_speed", "2"));
        	runnerAi = Integer.parseInt(mPrefs.getString("runner_ai", "2"));
        	monsterAi = Integer.parseInt(mPrefs.getString("monster_ai", "1"));
        	gameSpeed = Integer.parseInt(mPrefs.getString("game_speed", "1"));
        	nMonsters = Integer.parseInt(mPrefs.getString("n_monsters", "16"));
        	drawWalls = mPrefs.getBoolean("draw_walls", false);
        	wallColor = parseColorPref(Integer.parseInt(mPrefs.getString("wall_color", "1")));
        	bgColor = parseColorPref(Integer.parseInt(mPrefs.getString("bg_color", "0")));
        	dotColor = parseColorPref(Integer.parseInt(mPrefs.getString("dot_color", "6")));
        	drawDots = mPrefs.getBoolean("draw_dots", false);
        	nCol = Integer.parseInt(mPrefs.getString("n_cols", "4"));
        	nRow = Integer.parseInt(mPrefs.getString("n_rows", "4")); 
        	
        	
        	
        	switch(gameSpeed){
        	case 0:
        		gameSpeed = 1000/80;
        		break;
        	case 1:
        		gameSpeed = 1000/50;
        		break;
        	case 2: 
        		gameSpeed = 1000/24;
        		break;
        	}
        	if (pacdroid==null)
        		return;
        	
        	WPUtil.logD("loaded AI for PacDroid:" + Integer.toString(runnerAi));
        	
        	pacdroid.setAiType(runnerAi);
        	
        	for (int i=0; i<monsters.size(); i++)
        	{
        		monsters.get(i).setAiType(monsterAi);
        		((PacDroidMonster)monsters.get(i)).vulnerable = eatMonsters;
        	}
        }
        protected int parseColorPref(int c)
        {
        	switch (c){
        	case 0:
        		c = Color.BLACK;
        		break;
        	case 1:
        		c = Color.BLUE;
        		break;
        	case 2:	
        		c = Color.GRAY;
        		break;
        	case 3:
        		c = Color.GREEN;
        		break;
        	case 4:
        		c = Color.MAGENTA;
        		break;
        	case 5:
        		c = Color.RED;
        		break;
        	case 6:
        		c = Color.YELLOW;
        		break;
        	case 7:
        		c = Color.WHITE;
        		break;
        	case 8:
        		c = Color.CYAN;
        		break;
    		default:
    			c = Color.BLACK;
        	}
        	return c;
        	
        }
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        	
        	loadPreferences();

        	if (pacdroid==null)
        		return;
        	
        	WPUtil.logD("New Droid AI:" + Integer.toString(runnerAi));
        	pacdroid.setAiType(runnerAi);
        	for (int i=0; i<monsters.size(); i++)
        		monsters.get(i).setAiType(monsterAi);
        	if (key.compareToIgnoreCase("n_monsters")==0 || key.compareToIgnoreCase("n_rows")==0 || key.compareToIgnoreCase("n_cols")==0)
        		resetGame();
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(true);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mHandler.removeCallbacks(mDrawWallpaper);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            if (visible) {
                drawFrame();
            } else {
                mHandler.removeCallbacks(mDrawWallpaper);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            // store the center of the surface, so we can draw the cube in the right spot
            //wpWidth = width;
            //wpHeight = height;
            //mCenterX = width/2.0f;
            //mCenterY = height/2.0f;
            drawFrame();
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            mHandler.removeCallbacks(mDrawWallpaper);
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xStep, float yStep, int xPixels, int yPixels) {	
            //drawFrame();
        }

        /*
         * Store the position of the touch event so we can use it for drawing later
         */
        @Override
        public void onTouchEvent(MotionEvent event) {
          /*  if (event.getAction() == MotionEvent.ACTION_MOVE) {
                mTouchX = event.getX();
                mTouchY = event.getY();
            } else {
                mTouchX = -1;
                mTouchY = -1;
            }*/
            super.onTouchEvent(event);
        }

        /*
         * Draw one frame of the animation. This method gets called repeatedly
         * by posting a delayed Runnable. You can do any drawing you want in
         * here. This example draws a wireframe cube.
         */
        void drawFrame() {
        	 final SurfaceHolder holder = getSurfaceHolder();
             
        	 //this.w = frame.width();
        	 //this.h = frame.height();
           
            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                    // draw something
                    //c.save();
                	clearCanvas(c);
                    drawDots(c);
                    if (this.drawWalls)
                    	drawWalls(c);
                    drawActors(c);
                    //c.restore();
                }
            } 
            finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }

            mHandler.removeCallbacks(mDrawWallpaper);
            if (mVisible) {
                mHandler.postDelayed(mDrawWallpaper, gameSpeed);
            }
            checkForWin();
        }
        void clearCanvas(Canvas c){
        	// Allow other colors
        	// Allow images
        	// setup classic blue background?
        	c.drawColor(this.bgColor);
        	//WPUtil.logD("This.bgColor =" + Integer.toString(this.bgColor));
        }

        void drawDots(Canvas c) {
        	if (!this.drawDots)
        		return;
            
        	Paint p = new Paint(mPaint);
        	p.setColor(this.dotColor);
        	for (int i=0; i<dots.size(); i++)
            {
            	if (this.pacdroid.detectCollision(dots.get(i)))
            		dots.get(i).disableDot();
            	dots.get(i).draw(c, p);
            }
        }
        
        void drawWalls(Canvas c){
        	int wallWidth = 5;
        	 for (int i=0; i<walls.size(); i++)
             {
        		 Paint p = new Paint(mPaint);
        		 p.setStyle(Style.STROKE);
        		 p.setStrokeWidth(wallWidth);
        		 p.setColor(this.wallColor);
             		walls.get(i).draw(c, p);
             }
        }
        void drawActors(Canvas c)
        {
        	for (int i=0; i<monsters.size(); i++)
            {
            	monsters.get(i).draw(c, mPaint);
            	if (pacdroid.detectCollision(monsters.get(i), 15))
            		if (eatMonsters)
            			monsters.get(i).enabled = false;
            		else
            		resetGame();
            }
        	this.pacdroid.draw(c, mPaint);
        }

        private void initActors()
        {
        	 WPUtil.logD("Stored AI for PacDroid:" + Integer.toString(runnerAi));
          
//             TurningPoint tp = startLocations.get(0);
             initPacDroid();
             initMonsters();
             
             pacdroid.setTargetActors(monsters);
        }
        
        private void initPacDroid(/*TurningPoint tp*/){
        	 
        	TurningPoint sp = turningPoints.get((nRow+1)*(nCol/2)+1);
            ArrayList<Bitmap> runnerImgList = new ArrayList<Bitmap>();
            runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.pd_android1));
            runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.pd_android2));
            runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.pd_android3));
            runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.pd_android4));

            pacdroid = new PacDroid(sp.x, sp.y, wpBounds, runnerImgList);
            pacdroid.turnRight();
            pacdroid.tag = 1;
            WPUtil.logD("creating pacdroid with AI:" + Integer.toString(runnerAi));
            pacdroid.setAiType(runnerAi);
        	
        }
        private void initMonsters(){
        	ArrayList<Sprite> runnerList = new ArrayList<Sprite>();
            runnerList.add(pacdroid);
            
            ArrayList<Bitmap> mImgList = new ArrayList<Bitmap>();
            mImgList.add(BitmapFactory.decodeResource(resources, R.raw.pd_apple));
            
            monsters = new ArrayList<Sprite>();
            TurningPoint sp;
            for (int i=0; i<nMonsters; i++)
            {

            	if (i < ((nRow+1)*(nCol/2)+1))
            		sp = turningPoints.get(i);
            	else
            		sp = turningPoints.get(i+1); 			
            			
            	
            	monsters.add(new PacDroidMonster(sp.x,sp.y, wpBounds, mImgList));
            }
            WPUtil.logD("Initialized " + Integer.toString(monsters.size()) + " monsters");            
            
            pacdroid.setTurningPoints(this.turningPoints);
            for (int i=0; i<monsters.size(); i++){
            	monsters.get(i).setTurningPoints(this.turningPoints);
            	monsters.get(i).setAiType(monsterAi);
            	monsters.get(i).setTargetActors(runnerList);
            	monsters.get(i).tag = i+1;
            	((PacDroidMonster)monsters.get(i)).vulnerable = eatMonsters;
            }
        }
        private void initDotArray(){
            dots = new ArrayList<Dot>();
       	
        	WPUtil.logD("Initializing the dot array");
        	int x,y;
        	
        	int minX = wpBounds.left;
            int maxX = wpBounds.right;
            int dx = (maxX-minX) / nCol;

            int minY = wpBounds.top;
            int maxY = wpBounds.bottom;
            int dy = (maxY - minY) / nRow;
            

            for (int i=0; i<=nCol; i++)
              	for (int ii=0; ii<4; ii++)
	            	for (int j=0; j<=nRow; j++)
	            		for (int jj=0; jj<5; jj++)
		            	{
	            			x = minX + dx*i + dx/4 * ii;
	            			y = minY + dy*j + dy/5 * jj;
	            			if((ii>0 && jj>0) || (i==nCol && ii>0) || (j==nRow && jj>0))
	            				continue;
	            			int r;
	            			switch(dMetrics.densityDpi){
	            			case DisplayMetrics.DENSITY_LOW:
	            				r=1;
	            				break;
	            			case DisplayMetrics.DENSITY_MEDIUM:
	            				r=2;
	            				break;
	            			case DisplayMetrics.DENSITY_HIGH:
	            				r=3;
	            				break;
	            			default: //DENSITY_DEFAULT
	            				r=2;
	            		    }	
	            			dots.add(new Dot(x,y, r));
	            			//WPUtil.logD(Integer.toString(x).concat("x").concat(Integer.toString(y)));
		            	}  
        }

        private void initTurningPoints(){
        	
        	turningPoints = new ArrayList<TurningPoint>();
        	WPUtil.logD("Initializing the turning points array");
        	int x,y;
        	
        	int minX = wpBounds.left;
            int maxX = wpBounds.right;
            int dx = (maxX-minX) / nCol;

            int minY = wpBounds.top;
            int maxY = wpBounds.bottom;
            int dy = (maxY - minY) / nRow;
            
            for (int i=0; i<=nCol; i++)
            	for (int j=0; j<=nRow; j++)
            	{
        			x = minX + dx*i;
        			y = minY + dy*j;
        			TurningPoint tp = new TurningPoint(x,y);
        			tp.left = i!=0;
        			tp.up = j!=0;;
        			tp.right = i!=nCol;
        			tp.down = j!=nRow;

        			turningPoints.add(tp);
        			
            	}  
        }
        private void initWalls(){
        	
            walls = new ArrayList<WallBlock>();
        	WPUtil.logD("Initializing the wallblock objects array");
        	int x,y;
        	
        	int minX = wpBounds.left;
            int maxX = wpBounds.right;
            int dx = (maxX-minX) / nCol;

            int minY = wpBounds.top;
            int maxY = wpBounds.bottom;
            int dy = (maxY - minY) / nRow;
            
            int r = 5;
            int wOffset = 40;
            
            for (int i=0; i<=nCol-1; i++)
            	for (int j=0; j<=nRow-1; j++)
            	{
        			x = (int)(minX + dx*i) + dx/2;
        			y = (int)(minY + dy*j) + dy/2;
        			WallBlock w = new WallBlock(x,y, dx-wOffset, dy-wOffset, r);
        			//WPUtil.logD("WallBlock:".concat(Integer.toString(x)).concat("x"));
        			walls.add(w);
            	}  
        }

        void drawTouchPoint(Canvas c) {
          /*  if (mTouchX >=0 && mTouchY >= 0) {
                c.drawCircle(mTouchX, mTouchY, 80, mPaint);
            }*/
        }
        void initStartLocations(){

        	WPUtil.logD("N Turning points".concat(Integer.toString(turningPoints.size())));
        	//Pac Man
        	/*startLocations = new ArrayList<TurningPoint> ();
        	startLocations.add(turningPoints.get((nRow*(nCol+1)+nRow)/2));
        	
        	//The 4 ghosts
        	startLocations.add(turningPoints.get(0));
        	startLocations.add(turningPoints.get(nRow));
        	startLocations.add(turningPoints.get(nRow*(nCol+1)));
        	startLocations.add(turningPoints.get(nRow*(nCol+1)+nRow));
        	startLocations.add(turningPoints.get(nRow+1));
        	startLocations.add(turningPoints.get((nRow*2)-1));
        	startLocations.add(turningPoints.get(nRow*(nCol+1)-1));
        	startLocations.add(turningPoints.get(nRow*(nCol+1)-nRow+1));
        	startLocations.add(turningPoints.get(nRow));
        	startLocations.add(turningPoints.get(nRow*(nCol+2)));
        	startLocations.add(turningPoints.get(nRow*(nCol+1)+nRow-3));
        	startLocations.add(turningPoints.get(nRow+2));
        	startLocations.add(turningPoints.get((nRow*2)-2));
        	startLocations.add(turningPoints.get(nRow*(nCol+1)-2));
        	startLocations.add(turningPoints.get(nRow*(nCol+1)-nRow+2));
        	startLocations.add(turningPoints.get(nRow*(nCol+1)-nRow+3));*/
        }
        void resetGame()
        {
        	        	
        	pacdroid = null;
        	monsters = null;
        	turningPoints = null;
        	dots = null;
        	walls = null;
        	initDotArray();
        	initTurningPoints();
        	initWalls();
        	initStartLocations();
        	initActors();
        }
        void checkForWin()
        {
        	for (int i=0; i<monsters.size(); i++)
        		if (monsters.get(i).enabled)
        			return;
        	//for (int i=0; i<dots.size(); i++)
        	//	if(dots.get(i).enabled)
        	//		return;
        	resetGame();
        }
    }
}
