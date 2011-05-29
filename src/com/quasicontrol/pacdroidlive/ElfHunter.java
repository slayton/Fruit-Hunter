package com.quasicontrol.pacdroidlive;

import java.util.ArrayList;

import com.quasicontrol.live.Sprite;
import com.quasicontrol.live.WPUtil;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

public class ElfHunter extends Sprite{

	protected int prevDir = -1;
	protected boolean attacking = false;
	protected boolean firstAttackFrame = true;
	
	protected boolean holdImage = true;
	int tempX = 0;
	int tempY = 0;
	
	ElfHunter(int x, int y, Rect bounds, ArrayList<Bitmap> imageList) {
		super(x, y, bounds,  imageList);
		
		this.imageArray = imageList;
		this.image = imageArray.get(0);
		this.imgIdx=0;
		this.vel = 2;
	}
	ElfHunter(int x, int y, Rect bounds, ArrayList<Bitmap> imageList, ArrayList<Sprite> targets) {
		this(x, y, bounds, imageList);
		
		this.targets = targets;	
		this.vel=2;

	}
	
	public void move(){
		if (this.attacking)
			return;
		else
			super.move();
	}
	public void draw(final Canvas c, final Paint mPaint)
	{	
		if (!attacking)
		{
			if (holdImage)
				holdImage = false;
			else
			{
				holdImage = true;
				imgIdx  += ((imgIdx % 3 == 0) ? 1 : -1);
			}
			
			drawMe = imageArray.get(imgIdx);//Bitmap.createBitmap(imageArray.get(imgIdx), 0, 0, image.getWidth(), image.getHeight(), m, true);
			c.drawBitmap(drawMe, this.x-image.getWidth()/2, this.y-image.getHeight()/2, mPaint);
			//attacking = (rand.nextInt(1000)<20);
		}
		else
		{
			
			//WPUtil.logD("Attacking:" + Boolean.toString(attacking) + " imgIdx:" + Integer.toString(imgIdx) + " attackIdx:"+ this.dir*3);
			
			int xOffAttack =0;
			int yOffAttack =0;
		
			imgIdx = this.dir*3+2;
			
			if (this.dir == Sprite.MOVING_UP)
				yOffAttack=-27;
			if (this.dir == Sprite.MOVING_LEFT)
				xOffAttack=-27;
			
			drawMe = imageArray.get(imgIdx);
			c.drawBitmap(drawMe, x-image.getWidth()/2+xOffAttack, y-image.getHeight()/2+yOffAttack, mPaint);
			
			if (this.firstAttackFrame)
				this.firstAttackFrame = false;
			else
			{
				this.attacking = false;
				this.setDir(prevDir);
				this.imgIdx = this.dir*3;
			}
		}		
	}
	public void attackSprite(Sprite target)
	{
		WPUtil.logD("Attacking this:" + Integer.toString(this.x) + "x" + Integer.toString(this.y)  + " & Target " + Integer.toString(target.x) + "x" + Integer.toString(target.y));
		this.prevDir = this.dir;
		this.firstAttackFrame = true;

		
		if (Math.abs(this.x-target.x) > Math.abs(this.y-target.y)) // on same y level
			if (this.x < target.x){ // elf is to the left of monster
				WPUtil.logD("attacking right");
				turnRight();	
			}
			else{
				WPUtil.logD("attacking left");
				turnLeft();
			}
			else
			if (this.y < target.y)// elf is above
				turnDown();
			else
				turnUp();
		
		attacking = true;
	}
	public void turnUp(){
		super.turnUp();
		this.imgIdx = this.dir*3;
	}
	public void turnDown(){
		super.turnDown();
		this.imgIdx = this.dir*3;
	}
	public void turnLeft(){
		super.turnLeft();
		this.imgIdx = this.dir*3;
	}
	public void turnRight(){
		super.turnRight();
		this.imgIdx = this.dir*3;
	}
	public boolean detectCollision(Point target, int len){
		return !this.attacking && super.detectLinearCollision(target,len);
	}
	
}
