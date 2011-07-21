package com.quasicontrol.pacdroidlive;


import com.quasicontrol.live.Sprite;
import com.quasicontrol.live.TurningPoint;
import com.quasicontrol.live.WPUtil;

import java.util.ArrayList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class PacDroid extends Sprite{

	protected boolean closingMouth = false;
	protected int queuedDir = -1;
	protected boolean animateMouth = true;
	protected boolean rotateWithTurns = false;
	
	PacDroid(int x, int y, Rect bounds, ArrayList<Bitmap> imageList) {
		super(x, y, bounds,  imageList);
		
		//this.imageArray = imageList;
		//this.image = imageArray.get(1);
		this.tag = 555;
		this.vel = 2;
	}
	PacDroid(int x, int y, Rect bounds, ArrayList<Bitmap> imageList, ArrayList<Sprite> targets) {
		super(x, y, bounds, imageList);
		
		this.targets = targets;		
		this.tag = 555;
		this.vel = 2;
	}
	
	public void setAnimateMouth(boolean anim)
	{
		this.animateMouth = anim; 
	}
	
	public void draw(final Canvas c, final Paint mPaint)
	{			
		if (this.animateMouth){
				
			if (imgIdx ==0)
				closingMouth = false;
			else if (imgIdx == imageArray.size()-1)
				closingMouth = true;
			
			imgIdx  += ((closingMouth) ? -1 : 1);
		}
		else
			imgIdx = 0;
		drawMe = this.imageArray.get(imgIdx);
		
		c.drawBitmap(drawMe, this.x-image.getWidth()/2, this.y-image.getHeight()/2, mPaint);
	}
	public void enqueueTurn(int dir){
		this.queuedDir = dir;
		WPUtil.logD("Enqueued Turn: " + Integer.toString(dir));
		
		if (this.dir == Sprite.MOVING_UP && dir==Sprite.MOVING_DOWN)
			this.turnDown();
		else if (this.dir == Sprite.MOVING_LEFT && dir == Sprite.MOVING_RIGHT)
			this.turnRight();
		else if (this.dir == Sprite.MOVING_DOWN && dir == Sprite.MOVING_UP)
			this.turnUp();
		else if (this.dir ==Sprite.MOVING_RIGHT && dir == Sprite.MOVING_LEFT)
			this.turnLeft();
		
	}
	
	public void turnAtPoint(TurningPoint tp)
	{	
		if (!(this.queuedDir>=0 && this.queuedDir<=3)){ // if not a valid turn is enqueued
			super.turnAtPoint(tp);
			return;
		}
		if (!this.isValidTurn(tp, this.queuedDir))
		{
			super.turnAtPoint(tp);
			return;
		}
		super.turnAtPoint(tp, this.queuedDir);
		
		this.queuedDir = -1;
		return;
		
	}
	boolean isValidTurn(TurningPoint tp, int dir){
		switch(dir){
		case Sprite.MOVING_UP: return tp.up; 
		case Sprite.MOVING_DOWN: return tp.down;
		case Sprite.MOVING_LEFT: return tp.left; 
		case Sprite.MOVING_RIGHT: return tp.right; 
		default: return false;
		}
	}
}

