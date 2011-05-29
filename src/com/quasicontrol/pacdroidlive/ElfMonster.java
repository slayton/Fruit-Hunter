package com.quasicontrol.pacdroidlive;

import java.util.ArrayList;

import com.quasicontrol.live.Sprite;
import com.quasicontrol.live.WPUtil;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class ElfMonster extends PacDroidMonster{

	protected int imgPerDir = 2;
	protected int nImage = 0;
	protected boolean disabledFlag = false;

	ElfMonster(int x, int y, Rect bounds, ArrayList<Bitmap> imageList) {
		super(x, y, bounds,  imageList);
		
		this.imageArray = imageList;
		this.image = imageArray.get(0);
		this.imgIdx=0;
		this.vel = 2;
	}
	ElfMonster(int x, int y, Rect bounds, ArrayList<Bitmap> imageList, ArrayList<Sprite> targets) {
		super(x, y, bounds, imageList);
		
		this.targets = targets;	
		this.vel = 2;
	}
	
	
	public void draw(final Canvas c, final Paint mPaint)
	{	
		
		if (!enabled)
			return;
		this.move();
		
		if (this.nImageArray==1)
			imgIdx = 0;
		else
			imgIdx  += ((imgIdx % imgPerDir == 0) ? 1 : -1);
		//imgIdx = 0;
		drawMe = imageArray.get(imgIdx);//Bitmap.createBitmap(imageArray.get(imgIdx), 0, 0, image.getWidth(), image.getHeight(), m, true);
		c.drawBitmap(drawMe, this.x-image.getWidth()/2, this.y-image.getHeight()/2, mPaint);
		if (this.disabledFlag)
			this.enabled = false;
	}
	
	public void turnUp(){
		super.turnUp();
		this.imgIdx = this.dir*imgPerDir;
	}
	public void turnDown(){
		super.turnDown();
		this.imgIdx = this.dir*imgPerDir;
	}
	public void turnLeft(){
		super.turnLeft();
		this.imgIdx = this.dir*imgPerDir;
	}
	public void turnRight(){
		super.turnRight();
		this.imgIdx = this.dir*imgPerDir;
	}
	public void setEnabled(boolean newVal){
		if (!newVal)
			this.disabledFlag = true;
		else
		{
			this.disabledFlag = false;
			this.enabled = newVal;
		}
	}
}
