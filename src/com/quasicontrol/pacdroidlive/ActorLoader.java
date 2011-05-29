package com.quasicontrol.pacdroidlive;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import com.quasicontrol.live.Sprite;
import com.quasicontrol.live.TurningPoint;
import com.quasicontrol.live.WPUtil;

public class ActorLoader {

	public static Sprite initPacDroid(Resources resources, Context context, int nRow, int nCol, ArrayList<TurningPoint> turningPoints,
			Rect wpBounds, int runnerAi){
		TurningPoint sp = turningPoints.get((nCol+1)*(nRow/2)+1);
		ArrayList<Bitmap> runnerImgList = new ArrayList<Bitmap>();
		runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.pd_android1));
		runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.pd_android2));
		runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.pd_android3));
		runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.pd_android4));

		PacDroid pacdroid = new PacDroid(sp.x, sp.y, wpBounds, runnerImgList);
		pacdroid.setTurningPoints(turningPoints);
		pacdroid.turnRight();
		pacdroid.tag = 1;
		WPUtil.logD("creating pacdroid with AI:" + Integer.toString(runnerAi));
		pacdroid.setAiType(runnerAi);
		
		return pacdroid;
	}
	
	public static Sprite initClassicRunner(Resources resources, Context context, int nRow, int nCol, ArrayList<TurningPoint> turningPoints,
			Rect wpBounds, int runnerAi){
		
		TurningPoint sp = turningPoints.get((nCol+1)*(nRow/2)+1);
		ArrayList<Bitmap> runnerImgList = new ArrayList<Bitmap>();
        runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.ar_runner0));
        runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.ar_runner1));
        runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.ar_runner2));
        runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.ar_runner3));
        runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.ar_runner4));

		ClassicRunner classicRunner = new ClassicRunner(sp.x, sp.y, wpBounds, runnerImgList);
		classicRunner.setTurningPoints(turningPoints);
		classicRunner.turnRight();
		classicRunner.tag = 1;
		WPUtil.logD("creating pacdroid with AI:" + Integer.toString(runnerAi));
		classicRunner.setAiType(runnerAi);
		
		return classicRunner;
	}
	
	public static Sprite initElfHunter(Resources resources, Context context, int nRow, int nCol, ArrayList<TurningPoint> turningPoints,
			Rect wpBounds, int runnerAi){
		
		TurningPoint sp = turningPoints.get((nCol+1)*(nRow/2)+1);
		ArrayList<Bitmap> runnerImgList = new ArrayList<Bitmap>();
        runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_hunter_up1));
        runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_hunter_up2));
        runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_hunter_up_sword));
    	runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_hunter_down1));
        runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_hunter_down2));
        runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_hunter_down_sword));
        runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_hunter_left1));
        runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_hunter_left2));
        runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_hunter_left_sword));
        runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_hunter_right1));
        runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_hunter_right2));
        runnerImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_hunter_right_sword));

		ElfHunter elf = new ElfHunter(sp.x, sp.y, wpBounds, runnerImgList);
		elf.setTurningPoints(turningPoints);
		elf.turnRight();
		elf.tag = 1;
		WPUtil.logD("creating pacdroid with AI:" + Integer.toString(runnerAi));
		elf.setAiType(runnerAi);
		
		return elf;
	}
	
		
	public static ArrayList<Sprite> initMonsters(Resources resources, Context context,  int nMonsters, int nRow, int nCol, 
			ArrayList<TurningPoint> turningPoints, Sprite pacdroid, boolean customMonsterImg, 
			Rect wpBounds, int monsterAi, boolean eatMonsters){
		
		
		ArrayList<Sprite> runnerList = new ArrayList<Sprite>();
		runnerList.add(pacdroid);

		ArrayList<Bitmap> mImgList = new ArrayList<Bitmap>();
		Bitmap monsterImg = null; 

		if (customMonsterImg)
			monsterImg = WPUtil.loadPrivateImage(context, WPUtil.MONSTER_FILE_PATH);

		if(monsterImg==null) // null from not set or no image exhists
			monsterImg = BitmapFactory.decodeResource(resources, R.raw.pd_apple);

		mImgList.add(monsterImg);

		ArrayList<Sprite> monsters = new ArrayList<Sprite>();
		TurningPoint sp;
		
		for (int i=0; i<nMonsters; i++)
		{
			if (i < ((nCol+1)*(nRow/2)+1))
				sp = turningPoints.get(i);
			else
				sp = turningPoints.get(i+1); 				
			monsters.add(new PacDroidMonster(sp.x,sp.y, wpBounds, mImgList));
		}
		
		//WPUtil.logD("Initialized " + Integer.toString(monsters.size()) + " monsters");            

		for (int i=0; i<monsters.size(); i++){
			monsters.get(i).setTurningPoints(turningPoints);
			monsters.get(i).setAiType(monsterAi);
			monsters.get(i).setTargetActors(runnerList);
			monsters.get(i).tag = i+1;
			((PacDroidMonster)monsters.get(i)).vulnerable = eatMonsters;
		}
		
		return monsters;
	}
	
	public static ArrayList<Sprite> initClassicMonsters(Resources resources, Context context,  int nMonsters, int nRow, int nCol, 
			ArrayList<TurningPoint> turningPoints, Sprite pacdroid, boolean customMonsterImg, 
			Rect wpBounds, int monsterAi, boolean eatMonsters){
		
		
		ArrayList<Sprite> runnerList = new ArrayList<Sprite>();
		runnerList.add(pacdroid);

		Bitmap monsterImg = null; 

		ArrayList<Bitmap> tmp = new ArrayList<Bitmap>();
		if (customMonsterImg)
			monsterImg = WPUtil.loadPrivateImage(context, WPUtil.MONSTER_FILE_PATH);

		if(monsterImg != null)
			tmp.add(monsterImg);
		
		else // null from not set or no image exhists
		{
			
			tmp.add(BitmapFactory.decodeResource(resources, R.raw.ar_ghost1));
			tmp.add(BitmapFactory.decodeResource(resources, R.raw.ar_ghost2));
			tmp.add(BitmapFactory.decodeResource(resources, R.raw.ar_ghost3));
			tmp.add(BitmapFactory.decodeResource(resources, R.raw.ar_ghost4));
		}
		
		int nTmp = tmp.size();
		ArrayList<Sprite> monsters = new ArrayList<Sprite>();
		TurningPoint sp;
		
		for (int i=0; i<nMonsters; i++)
		{
			if (i < ((nCol+1)*(nRow/2)+1))
				sp = turningPoints.get(i);
			else
				sp = turningPoints.get(i+1); 	
			ArrayList<Bitmap> imgList = new ArrayList<Bitmap>();
			imgList.add(tmp.get(i%nTmp));
			monsters.add(new PacDroidMonster(sp.x,sp.y, wpBounds, imgList));
		}
		
		//WPUtil.logD("Initialized " + Integer.toString(monsters.size()) + " monsters");            

		for (int i=0; i<monsters.size(); i++){
			monsters.get(i).setTurningPoints(turningPoints);
			monsters.get(i).setAiType(monsterAi);
			monsters.get(i).setTargetActors(runnerList);
			monsters.get(i).tag = i+1;
			((PacDroidMonster)monsters.get(i)).vulnerable = eatMonsters;
		}	
		return monsters;
	}
	public static ArrayList<Sprite> initSpitterMonsters(Resources resources, Context context,  int nMonsters, int nRow, int nCol, 
			ArrayList<TurningPoint> turningPoints, Sprite pacdroid, boolean customMonsterImg, 
			Rect wpBounds, int monsterAi, boolean eatMonsters){
		
		
		ArrayList<Sprite> runnerList = new ArrayList<Sprite>();
		runnerList.add(pacdroid);

		ArrayList<Bitmap> mImgList = new ArrayList<Bitmap>();
		Bitmap monsterImg = null; 

		if (customMonsterImg)
			monsterImg = WPUtil.loadPrivateImage(context, WPUtil.MONSTER_FILE_PATH);

		if(monsterImg!=null)
			mImgList.add(monsterImg);
		else
		{
        	mImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_spitter_u1));
        	mImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_spitter_u2));
        	mImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_spitter_d1));
        	mImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_spitter_d2));
        	mImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_spitter_l1));
        	mImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_spitter_l2));
        	mImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_spitter_r1));
        	mImgList.add(BitmapFactory.decodeResource(resources, R.raw.eh_spitter_r2));// null from not set or no image exhists
		}

		ArrayList<Sprite> monsters = new ArrayList<Sprite>();
		TurningPoint sp;
		
		for (int i=0; i<nMonsters; i++)
		{
			if (i < ((nCol+1)*(nRow/2)+1))
				sp = turningPoints.get(i);
			else
				sp = turningPoints.get(i+1); 				
			monsters.add(new ElfMonster(sp.x,sp.y, wpBounds, mImgList));
		}
		
		//WPUtil.logD("Initialized " + Integer.toString(monsters.size()) + " monsters");            

		for (int i=0; i<monsters.size(); i++){
			monsters.get(i).setTurningPoints(turningPoints);
			monsters.get(i).setAiType(monsterAi);
			monsters.get(i).setTargetActors(runnerList);
			monsters.get(i).tag = i+1;
			((PacDroidMonster)monsters.get(i)).vulnerable = eatMonsters;
		}
		
		return monsters;
	}
}
