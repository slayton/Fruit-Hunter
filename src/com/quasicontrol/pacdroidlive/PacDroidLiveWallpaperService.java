package com.quasicontrol.pacdroidlive;

import java.util.ArrayList;

import com.quasicontrol.live.Sprite;
import com.quasicontrol.live.TurningPoint;
import com.quasicontrol.live.WPUtil;
import com.quasicontrol.live.WallBlock;
import com.quasicontrol.live.Dot;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

public class PacDroidLiveWallpaperService extends WallpaperService {

	//public static final String SHARED_PREFS_NAME="wallpaperprefs";

	protected Resources resources;
	protected Context context; 

	public static final String PACDROID_THEME = "com.quasicontrol.pacdroidlive";
	public static final String PACMAN_THEME = "com.quasicontrol.pacdroidlive.classic";
	public static final String ZELDA_THEME = "com.quasicontrol.pacdroidlive.elfhunter";
	public static final String GOCART_THEME = "com.quasicontrol.pacdroidlive.gocarts";
	public static final String HONEYCOMB_THEME = "com.quasicontrol.pacdroidlive.honeycomb";

	

	@Override
	public void onCreate() {
		super.onCreate();
		WPUtil.logD("PacDroidWallpaperService.onCreate()");
		this.resources = this.getResources();
		context = this.getApplicationContext();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public Engine onCreateEngine() {
		return new PacDroidEngine(this, resources, context);
	}


	class PacDroidEngine extends Engine 
	implements SharedPreferences.OnSharedPreferenceChangeListener {

		protected WallpaperService wps;
		protected DisplayMetrics dMetrics;
		protected Display disp;
		protected Resources resources;
		protected Context context; 
		protected int currentOrientation = Surface.ROTATION_0;

		protected final Handler mHandler = new Handler();

		protected int tOff;
		protected int bOff;
		protected int lOff;
		protected int rOff;

		protected ArrayList<Dot> dots;
		protected Dot tempDot;
		protected int nDots;

		protected ArrayList<WallBlock> walls;
		protected int nWalls;

		protected ArrayList<TurningPoint> turningPoints;
		protected ArrayList<Sprite> monsters;
		protected int nMonsters = 4;
		protected Sprite tempMonster;

		protected Sprite pacdroid;
		protected ArrayList<TurningPoint> startLocations;

		protected int nRow = 4;
		protected int nCol = 4;
		
		protected String theme = PacDroidLiveWallpaperService.PACDROID_THEME;
		
		protected final Paint mPaint = new Paint();
		
		protected boolean showControls = false;
		protected ArrayList<Sprite> controls;

		protected final Runnable mDrawWallpaper = new Runnable() {
			public void run() {
				loop();
			}
		};
		protected boolean mVisible;
		protected SharedPreferences mPrefs;
		protected Rect wpBounds;


		protected int nEndGameFrames = 10;

		// Preference Variables
		protected boolean drawWalls = false;
		protected int wallColor =Color.BLUE;
		protected int wallWidth = 5;
		protected int bgColor = Color.BLACK;
		protected int dotColor = Color.WHITE;
		protected Paint dotPaint = new Paint();
		protected Paint wallPaint = new Paint();

		protected boolean drawDots = true;
		protected boolean eatMonsters;
		protected int gameSpeed;
		protected int runnerAi;
		protected int monsterAi;
		protected int winConditions;
		protected boolean customMonsterImg = false;

		protected boolean reseting = false;


		PacDroidEngine(WallpaperService wps, Resources r, Context c) {
			// Create a Paint to draw the lines for our cube
			this.wps = wps;
			this.resources = r; 
			this.context = c;  
			this.disp = ((WindowManager) wps.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();


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

			resetGame();
		}
		protected void loadPreferences()
		{

			WPUtil.logD("loading PREFERENCES!!!");
			eatMonsters = mPrefs.getBoolean("eat_monsters", true);
			gameSpeed = Integer.parseInt(mPrefs.getString("game_speed", "2"));
			runnerAi = Integer.parseInt(mPrefs.getString("runner_ai", "1"));
			monsterAi = Integer.parseInt(mPrefs.getString("monster_ai", "0"));
			gameSpeed = Integer.parseInt(mPrefs.getString("game_speed", "1"));
			nMonsters = Integer.parseInt(mPrefs.getString("n_monsters", "16"));
			drawWalls = mPrefs.getBoolean("draw_walls", false);

			wallColor = parseColorPref(Integer.parseInt(mPrefs.getString("wall_color", "1")));
			wallPaint.setColor(wallColor);
			wallPaint.setStyle(Style.STROKE);
			wallPaint.setStrokeWidth(this.wallWidth);

			dotColor = parseColorPref(Integer.parseInt(mPrefs.getString("dot_color", "6")));
			dotPaint.setColor(dotColor);

			bgColor = parseColorPref(Integer.parseInt(mPrefs.getString("bg_color", "0")));

			drawDots = mPrefs.getBoolean("draw_dots", false);
			customMonsterImg = mPrefs.getBoolean("custom_monster_img", false);
			nRow = Integer.parseInt(mPrefs.getString("n_rows", "4"));
			nCol = Integer.parseInt(mPrefs.getString("n_cols", "4"));

			this.showControls = mPrefs.getBoolean("show_controls", false);
			
			this.theme = mPrefs.getString("theme_pref", "com.quasicontrol.pacdroidlive");
			
			if (monsterAi==1)
				monsterAi = 0;

			if (nRow<4)
				nRow=4;
			if (nCol<4)
				nCol=4;       	

			switch(gameSpeed){
			case 0:
				gameSpeed = 1000/40;
				break;
			case 1:
				gameSpeed = 1000/20;
				break;
			case 2: 
				gameSpeed = 1000/15;
				break;
			}
			if (pacdroid==null)
				return;


			
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
			if (key.equalsIgnoreCase("n_monsters") || key.equalsIgnoreCase("n_rows") ||
					key.equalsIgnoreCase("n_cols") || key.equalsIgnoreCase("custom_monster_img") 
					|| key.equalsIgnoreCase("bg_color") || key.equalsIgnoreCase("wall_color")||
					key.equalsIgnoreCase("dot_color") || key.equalsIgnoreCase("theme_pref"))

				resetGame();
		}

		protected void loadDisplayInformation()
		{
			this.dMetrics = WPUtil.getDisplayMetrics(wps);
			//tOff = WPUtil.topOffset(dMetrics);
			//bOff = WPUtil.bottomOffset(dMetrics);
			//lOff = WPUtil.leftOffset(dMetrics);
			//rOff = WPUtil.rightOffset(dMetrics);
			this.currentOrientation = this.disp.getOrientation();   

			this.wpBounds = WPUtil.getDrawingBounds(this.disp, this.dMetrics);
			//wpBounds = new Rect(0+lOff, 0+tOff, dMetrics.widthPixels-rOff, dMetrics.heightPixels-bOff);

			WPUtil.logD("Screen Resolution:".concat(Integer.toString(dMetrics.widthPixels)).concat("x").concat(Integer.toString(dMetrics.heightPixels)));
			WPUtil.logD("Bounds for the wallpaper:".concat(wpBounds.toShortString()));
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
				if (!WPUtil.isThemeInstalled(this.context, this.theme)){
					this.theme = PacDroidLiveWallpaperService.PACDROID_THEME;
					resetGame();
				}
				if (this.reseting)
					resetGame();
				loop();
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
			loop();
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
			//loop();
		}

		/*
		 * Store the position of the touch event so we can use it for drawing later
		 */
		@Override
		public void onTouchEvent(MotionEvent event) {
			super.onTouchEvent(event);
			detectControlTouch(event);
		}

		/*
		 * Draw one frame of the animation. This method gets called repeatedly
		 * by posting a delayed Runnable. You can do any drawing you want in
		 * here. This example draws a wireframe cube.
		 */
		void detectControlTouch(MotionEvent e){
			WPUtil.logD("Touch Event:" + Integer.toString((int)e.getX()) + "x" + Integer.toString((int)e.getY()));

			Point p = new Point((int)e.getX(),(int)e.getY());
			PacDroid pac = (PacDroid)pacdroid;
			int nControls = this.controls.size();
			if (!this.showControls)
				return;
			for (int i=0; i<nControls; i++)
				if (this.controls.get(i).detectCollision(p, 30))
					pac.enqueueTurn(this.controls.get(i).getDir());	
//			drawTouchEvent(e);
		}
		void loop(){
			//android.os.Debug.startMethodTracing("PacDroidTrace");

			float dt = android.os.SystemClock.elapsedRealtime();
			if (this.currentOrientation != disp.getOrientation())
				this.resetGame();
			this.reseting = false;

			drawFrame();
			moveActors();
			detectActorCollisions();
			detectDotCollisions();
			checkForWin();

			mHandler.removeCallbacks(mDrawWallpaper);
			if (mVisible) {
				dt = android.os.SystemClock.elapsedRealtime()-dt;
				long waitTime = (long) (gameSpeed - dt);
				if (waitTime<0)
					waitTime = 0;
				mHandler.postDelayed(mDrawWallpaper, waitTime);
			}

			//android.os.Debug.stopMethodTracing();
		}
		void drawFrame() {

			final SurfaceHolder holder = getSurfaceHolder();
			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null) {
					clearCanvas(c);
					drawControls(c);
					drawDots(c);
					drawWalls(c);
					drawActors(c);
				}
			} 
			finally {
				if (c != null) holder.unlockCanvasAndPost(c);
			}
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
			for (int i=0; i<nDots; i++)
				dots.get(i).draw(c,dotPaint);
		}
		void drawWalls(Canvas c){
			if (!this.drawWalls)
				return;
			for (int i=0; i<nWalls; i++)
				walls.get(i).draw(c, wallPaint);
		}
		void drawActors(Canvas c)
		{
			for (int i=0; i<nMonsters; i++)
				this.monsters.get(i).draw(c,mPaint);
			this.pacdroid.draw(c, mPaint);
		}
		void drawControls(Canvas c)
		{
			if (!this.showControls)
				return;
			
			int nControls = this.controls.size();
			for (int i=0; i<nControls; i++)
				this.controls.get(i).draw(c, this.mPaint);			
		}
		void moveActors(){
			for (int i=0; i<nMonsters; i++)
				monsters.get(i).move();
			pacdroid.move();
		}
		void detectDotCollisions(){
			if (!this.drawDots)
				return;
			
			for (int i=0; i<nDots; i++){
				tempDot = dots.get(i);
				if (this.pacdroid.detectCollision(tempDot,3))
					tempDot.disableDot();
			}
		}
		
		void detectActorCollisions(){
			int collLen = 15;
			
			if (this.theme.equalsIgnoreCase(PacDroidLiveWallpaperService.ZELDA_THEME))
				collLen = 45;
			if (this.theme.equalsIgnoreCase(PacDroidLiveWallpaperService.GOCART_THEME))
				return;
				
			for (int i=0; i<nMonsters; i++){
				tempMonster = monsters.get(i);
				if (!tempMonster.enabled)
					continue;
				
				if (pacdroid.detectCollision(tempMonster, collLen))
					if (eatMonsters){
						tempMonster.setEnabled(false);
						if (this.theme.equalsIgnoreCase(PacDroidLiveWallpaperService.ZELDA_THEME))
		            		((ElfHunter)pacdroid).attackSprite(monsters.get(i));
						break;
					}
					else
						
						resetGame();
			}
		}
		protected void initActors()
		{
			WPUtil.logD("Stored AI for PacDroid:" + Integer.toString(runnerAi));

			//             TurningPoint tp = startLocations.get(0);
			initPacDroid();
			initMonsters();

			pacdroid.setTargetActors(monsters);
		}

		protected void initPacDroid(/*TurningPoint tp*/){

			if (this.theme.equalsIgnoreCase(PacDroidLiveWallpaperService.PACDROID_THEME))
				this.pacdroid = ActorLoader.initPacDroid(resources, context, nRow, nCol, turningPoints, wpBounds, runnerAi);
			else if (this.theme.equalsIgnoreCase(PacDroidLiveWallpaperService.HONEYCOMB_THEME))
				this.pacdroid = ActorLoader.initPacHoney(resources, context, nRow, nCol, turningPoints, wpBounds, runnerAi);
			else if (this.theme.equalsIgnoreCase(PacDroidLiveWallpaperService.PACMAN_THEME))
				this.pacdroid = ActorLoader.initClassicRunner(resources, context, nRow, nCol, turningPoints, wpBounds, runnerAi);
			else if (this.theme.equalsIgnoreCase(PacDroidLiveWallpaperService.ZELDA_THEME))
				this.pacdroid = ActorLoader.initElfHunter(resources, context, nRow, nCol, turningPoints, wpBounds, runnerAi);
			else if (this.theme.equalsIgnoreCase(PacDroidLiveWallpaperService.GOCART_THEME))
				this.pacdroid = ActorLoader.initMarioGoCart(resources, context, nRow, nCol, turningPoints, wpBounds, runnerAi);
			else
				this.pacdroid = ActorLoader.initPacDroid(resources, context, nRow, nCol, turningPoints, wpBounds, runnerAi);
		}
		protected void initMonsters(){
			
			if (this.theme.equalsIgnoreCase(PacDroidLiveWallpaperService.PACDROID_THEME))
				this.monsters = ActorLoader.initMonsters(resources, context, nMonsters, nRow, nCol, turningPoints, pacdroid, customMonsterImg, wpBounds, monsterAi, eatMonsters);
			else if (this.theme.equalsIgnoreCase(PacDroidLiveWallpaperService.HONEYCOMB_THEME))
				this.monsters = ActorLoader.initHoneyMonsters(resources, context, nMonsters, nRow, nCol, turningPoints, pacdroid, customMonsterImg, wpBounds, monsterAi, eatMonsters);
			else if (this.theme.equalsIgnoreCase(PacDroidLiveWallpaperService.PACMAN_THEME))
				this.monsters = ActorLoader.initClassicMonsters(resources, context, nMonsters, nRow, nCol, turningPoints, pacdroid, customMonsterImg, wpBounds, monsterAi, eatMonsters);
			else if (this.theme.equalsIgnoreCase(PacDroidLiveWallpaperService.ZELDA_THEME))
				this.monsters = ActorLoader.initSpitterMonsters(resources, context, nMonsters, nRow, nCol, turningPoints, pacdroid, customMonsterImg, wpBounds, monsterAi, eatMonsters);
			else if (this.theme.equalsIgnoreCase(PacDroidLiveWallpaperService.GOCART_THEME))
				this.monsters = ActorLoader.initGoCarts(resources, context, nMonsters, nRow, nCol, turningPoints, pacdroid, customMonsterImg, wpBounds, monsterAi, eatMonsters);
			else
				this.monsters = ActorLoader.initMonsters(resources, context, nMonsters, nRow, nCol, turningPoints, pacdroid, customMonsterImg, wpBounds, monsterAi, eatMonsters);
		}
		
		protected void initDotArray(){
			dots = new ArrayList<Dot>();

			WPUtil.logD("Initializing the dot array");
			int x,y;

			int minX = wpBounds.left;
			int maxX = wpBounds.right;
			int dx = (maxX-minX) / nRow;

			int minY = wpBounds.top;
			int maxY = wpBounds.bottom;
			int dy = (maxY - minY) / nCol;


			for (int i=0; i<=nRow; i++)
				for (int ii=0; ii<4; ii++)
					for (int j=0; j<=nCol; j++)
						for (int jj=0; jj<5; jj++)
						{
							x = minX + dx*i + dx/4 * ii;
							y = minY + dy*j + dy/5 * jj;
							if((ii>0 && jj>0) || (i==nRow && ii>0) || (j==nCol && jj>0))
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
			this.nDots = this.dots.size();
		}

		protected void initTurningPoints(){

			turningPoints = new ArrayList<TurningPoint>();
			WPUtil.logD("Initializing the turning points array");
			int x,y;

			int minX = wpBounds.left;
			int maxX = wpBounds.right;
			int dx = (maxX-minX) / nRow;

			int minY = wpBounds.top;
			int maxY = wpBounds.bottom;
			int dy = (maxY - minY) / nCol;

			for (int i=0; i<=nRow; i++)
				for (int j=0; j<=nCol; j++)
				{
					//WPUtil.logD(Integer.toString(i)+"x"+Integer.toString(j));
					x = minX + dx*i;
					y = minY + dy*j;
					TurningPoint tp = new TurningPoint(x,y);
					tp.left = i!=0;
					tp.up = j!=0;;
					tp.right = i!=nRow;
					tp.down = j!=nCol;

					turningPoints.add(tp);
				}         
		}
		protected void initWalls(){

			walls = new ArrayList<WallBlock>();
			WPUtil.logD("Initializing the wallblock objects array");
			int x,y;

			int minX = wpBounds.left;
			int maxX = wpBounds.right;
			int dx = (maxX-minX) / nRow;

			int minY = wpBounds.top;
			int maxY = wpBounds.bottom;
			int dy = (maxY - minY) / nCol;

			int r = 5;
			int wOffset = 40;

			for (int i=0; i<=nRow-1; i++)
				for (int j=0; j<=nCol-1; j++)
				{
					x = (int)(minX + dx*i) + dx/2;
					y = (int)(minY + dy*j) + dy/2;
					WallBlock w = new WallBlock(x,y, dx-wOffset, dy-wOffset, r);
					walls.add(w);
				}  
			nWalls = walls.size();
		}

		void initControls(){
			//if (!this.showControls)
			//	return;
			if (this.controls==null)
				this.controls = new ArrayList<Sprite>();
			
			TurningPoint tp = this.turningPoints.get(this.nCol*2+3);
			ArrayList<Bitmap> upList = new ArrayList<Bitmap>(); 
			upList.add(BitmapFactory.decodeResource(this.resources,	R.raw.arrow_right));
			Sprite upBtn = new Sprite(tp.x, tp.y,this.wpBounds, upList);
			upBtn.setDir(Sprite.MOVING_UP);
			
			tp = this.turningPoints.get(this.nCol*2+5);
			ArrayList<Bitmap> downList = new ArrayList<Bitmap>(); 
			downList.add(BitmapFactory.decodeResource(this.resources,	R.raw.arrow_right));
			Sprite downBtn = new Sprite(tp.x, tp.y,this.wpBounds, downList);
			downBtn.setDir(Sprite.MOVING_DOWN);
			
			tp = this.turningPoints.get(this.nCol*1+3);
			ArrayList<Bitmap> leftList = new ArrayList<Bitmap>(); 
			leftList.add(BitmapFactory.decodeResource(this.resources,	R.raw.arrow_right));
			Sprite leftBtn = new Sprite(tp.x, tp.y,this.wpBounds, leftList);
			leftBtn.setDir(Sprite.MOVING_LEFT);
			
			tp = this.turningPoints.get(this.nCol*3+5);
			ArrayList<Bitmap> rightList = new ArrayList<Bitmap>(); 
			rightList.add(BitmapFactory.decodeResource(this.resources,	R.raw.arrow_right));
			Sprite rightBtn = new Sprite(tp.x, tp.y,this.wpBounds, rightList);
			rightBtn.setDir(Sprite.MOVING_RIGHT);
			
			this.controls.add(upBtn);
			this.controls.add(downBtn);
			this.controls.add(leftBtn);
			this.controls.add(rightBtn);
			
		}
		void drawTouchEvent(MotionEvent e) {
			//if (mTouchX >=0 && mTouchY >= 0) {
            //    c.drawCircle(mTouchX, mTouchY, 10, mPaint);
            //}
			//this.drawCircle(e.getX(), e.getY(), 15, this.mPaint);
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

			this.loadDisplayInformation();
			this.reseting = true;
			pacdroid = null;
			monsters = null;
			turningPoints = null;
			dots = null;
			walls = null;
			controls = null;
			initDotArray();
			initTurningPoints();
			initWalls();
			initStartLocations();
			initControls();
			initActors();
		}
		void checkForWin()
		{
			for (int i=0; i<monsters.size(); i++)
				if (monsters.get(i).enabled)
					return;
			
			resetGame();
		}
	}
	public static ArrayList<String> getThemeList(){
		ArrayList<String> themeList = new ArrayList<String>();
		themeList.add(PacDroidLiveWallpaperService.PACDROID_THEME);
		themeList.add(PacDroidLiveWallpaperService.PACMAN_THEME);
		themeList.add(PacDroidLiveWallpaperService.ZELDA_THEME);
		themeList.add(PacDroidLiveWallpaperService.GOCART_THEME);
		themeList.add(PacDroidLiveWallpaperService.HONEYCOMB_THEME);
		return themeList;
	}
	public static String getThemeDisplayName(String theme){
		if (theme.equalsIgnoreCase(PacDroidLiveWallpaperService.PACDROID_THEME))
			return "PacDroid Theme";
		else if (theme.equalsIgnoreCase(PacDroidLiveWallpaperService.PACMAN_THEME))
			return "Classic Pac Theme";
		else if (theme.equalsIgnoreCase(PacDroidLiveWallpaperService.ZELDA_THEME))
			return "Legend of Zelda Theme";
		else if (theme.equalsIgnoreCase(PacDroidLiveWallpaperService.GOCART_THEME))
			return "Mario Kart Theme"; 
		else if (theme.equalsIgnoreCase(PacDroidLiveWallpaperService.HONEYCOMB_THEME))
			return "Honeycomb Theme"; 
		else
			return "Unknown Theme";
	}
}
