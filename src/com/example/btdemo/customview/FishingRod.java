package com.example.btdemo.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.btdemo.R;

public class FishingRod extends View {
	private static final int LEFT_MARGIN = 200;
	private static final int CAST_ANIMATION_DURATION = 1000;
	public static final int MAX_ROD_ANGLE = 25;
	public static final int MIN_ROD_ANGLE = -75;
	public static final int MIN_SHADOW_ANGLE = -60;
	public static final int MAX_SHADOW_ANGLE = 80;
	private static final int DEFAULT_ROD_ANGLE = 45;

	private static final int ROD_PREFERRED_HEIGHT = 150;
	private static final int ROD_PREFERRED_WIDTH = 150;

	public static final int ROD_ONLY_STATE = 0;
	public static final int CAST_LINE_STATE = 1;
	public static final int ROD_AND_LINE_STATE = 2;

	private volatile int state;

	public Bitmap rod;
	public Paint painter;
	private Matrix transMatrix;

	private PointF pivot;
	private PointF dropP;
	private PointF shadowP;
	private int startAngle;
	private int stopAngle;
	private double shadowAngle;
	private double fishWeight = 0.05;

	public FishingRod(Context context) {
		super(context);

		initView(context);
	}

	public void initView(Context context) {
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		this.setLayoutParams(lp);

		Resources res = context.getResources();
		Bitmap rod_tmp = BitmapFactory.decodeResource(res,
				R.drawable.fishing_rod);
		transMatrix = new Matrix();
		RectF src = new RectF(0, 0, rod_tmp.getWidth(), rod_tmp.getHeight());
		RectF dst = new RectF(0, 0, ROD_PREFERRED_WIDTH, ROD_PREFERRED_HEIGHT);
		transMatrix.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
		rod = Bitmap.createBitmap(rod_tmp, 0, 0, rod_tmp.getWidth(),
				rod_tmp.getHeight(), transMatrix, true);
		transMatrix.reset();

		startAngle = 0;
		stopAngle = 0;
		shadowAngle = 0;
		pivot = new PointF();
		dropP = new PointF();
		shadowP = new PointF();
		state = ROD_ONLY_STATE;

		painter = new Paint();
		painter.setColor(Color.BLACK);
	}

	public void setState(int state) {
		switch (state) {
		case ROD_ONLY_STATE:
		case CAST_LINE_STATE:
		case ROD_AND_LINE_STATE:
			this.state = state;
			break;
		default:
			this.state = ROD_ONLY_STATE;
		}
	}

	public int getState() {
		return state;
	}

	public void setWeight(double w) {
		if (w < 0) {
			w = 0;
		} else if (w > 1) {
			w = 1;
		}
		fishWeight = w;
	}

	public double getWeight() {
		return fishWeight;
	}

	public PointF getDefaultRodTip() {
		return new PointF(LEFT_MARGIN + rod.getWidth(), this.getHeight()
				- rod.getHeight());
	}

	public double getMaxDistance() {
		PointF start = new PointF();
		start.set(this.getDefaultRodTip());
		PointF stop = new PointF(this.getWidth(), 0);

		return Math.hypot(start.x - stop.x, start.y - stop.y);
	}

	public PointF getRodTip() {
		int h = rod.getHeight();
		int w = rod.getWidth();
		int left = LEFT_MARGIN;
		int top = this.getHeight() - h;
		double hypo = Math.sqrt(h * h + w * w);
		double ang = Math.atan2(h, w) - Math.toRadians((double) stopAngle);

		double nLeft = left + hypo * Math.cos(ang);
		double nTop = top + h - hypo * Math.sin(ang);
		return new PointF((float) nLeft, (float) nTop);
	}

	public void onMeasure(int wSpec, int hSpec) {
		this.setMeasuredDimension(MeasureSpec.getSize(wSpec),
				MeasureSpec.getSize(hSpec));
	}

	@SuppressLint("DrawAllocation")
	public void onDraw(Canvas canvas) {
		canvas.save();
		super.onDraw(canvas);
		Matrix m = new Matrix(transMatrix);
		transMatrix.preTranslate(LEFT_MARGIN,
				this.getHeight() - rod.getHeight());
		canvas.drawBitmap(rod, transMatrix, painter);
		transMatrix = m;
		canvas.restore();

		drawRodShadow(canvas);
		drawFishingLine(canvas);
	}

	public void drawRodShadow(Canvas canvas) {
		PointF start = new PointF(LEFT_MARGIN, this.getHeight());
		PointF stop = calcShadowPoint();

		canvas.drawLine(start.x, start.y, stop.x, stop.y, painter);
	}

	private PointF calcShadowPoint() {
		double rodLength = Math.hypot(rod.getHeight(), rod.getWidth());
		double ang = stopAngle + DEFAULT_ROD_ANGLE;
		double rod_ang = Math.toRadians(90.0 - Math.abs(ang));
		double tmp = rodLength * Math.cos(rod_ang);
		double shadow_ang = 0;

		if (ang < 0) {
			shadowAngle = ang * MIN_SHADOW_ANGLE / (MIN_ROD_ANGLE + 45);
			shadow_ang = Math.toRadians(90.0 + shadowAngle);

			shadowP.x = (float) (LEFT_MARGIN - tmp);
			shadowP.y = (float) (this.getHeight() - tmp * Math.tan(shadow_ang));
		} else if (ang > 0) {
			shadowAngle = (-4.0 / 245.0 * ang * ang + 16.0 / 7.0 * ang);
			shadow_ang = Math.toRadians(90.0 - shadowAngle);
			shadowP.x = (float) (LEFT_MARGIN + tmp);
			shadowP.y = (float) (this.getHeight() - tmp * Math.tan(shadow_ang));
		} else {
			shadowP.set(LEFT_MARGIN, 305);
		}
		return shadowP;
	}

	PointF startP = new PointF(0, 0);
	PointF stopP = new PointF(0, 0);

	public void drawFishingLine(Canvas canvas) {
		if (state != ROD_ONLY_STATE) {
			switch (state) {
			case CAST_LINE_STATE:
				// calculation done by CastAnimationThread
				break;
			case ROD_AND_LINE_STATE:
				startP = getRodTip();
				stopP.set(dropP);
				break;
			default:
				break;
			}
			canvas.drawLine(startP.x, startP.y, stopP.x, stopP.y, painter);
		}
	}

	public void rotate(int dAngle) {
		pivot.x = LEFT_MARGIN;
		pivot.y = this.getHeight();

		if (CAST_LINE_STATE != state) {
			stopAngle += dAngle;
			stopAngle = (stopAngle > MAX_ROD_ANGLE) ? MAX_ROD_ANGLE : stopAngle;
			stopAngle = (stopAngle < MIN_ROD_ANGLE) ? MIN_ROD_ANGLE : stopAngle;

			if (startAngle != stopAngle) {
				transMatrix.setRotate(stopAngle, pivot.x, pivot.y);

				if (state == ROD_AND_LINE_STATE) {
					float dx = shadowP.x - dropP.x;
					float dy = shadowP.y - dropP.y;
					if ((Math.abs(dx) < 10) && (Math.abs(dy) < 10)) {
						dropP.set(shadowP);
						setState(ROD_ONLY_STATE);
					} else {
						dropP.offset((float) (dx * fishWeight),
								(float) (dy * fishWeight));
					}
				}

				invalidate();
				startAngle = stopAngle;
			}
		}
	}

	public void castLine(double dist) {
		setState(CAST_LINE_STATE);
		dropP = calcDropPoint(dist);
		(new CastAnimationThread()).start();
	}

	private PointF calcDropPoint(double dist) {
		double bound = 0;
		int coX = 0;
		if (shadowAngle < 0) {
			coX = LEFT_MARGIN;
		} else if (shadowAngle >= 0) {
			coX = this.getWidth() - LEFT_MARGIN;
		}
		bound = Math.abs(coX / Math.sin(Math.toRadians(shadowAngle)));
		if (dist > bound) {
			dist = bound;
		}
		bound = Math.abs(this.getHeight()
				/ Math.cos(Math.toRadians(shadowAngle)));
		if (dist > bound) {
			dist = bound;
		}

		double x = dist * Math.sin(Math.toRadians(shadowAngle)) + LEFT_MARGIN;
		double y = this.getHeight() - dist
				* Math.cos(Math.toRadians(shadowAngle));
		return new PointF((float) x, (float) y);
	}

	class CastAnimationThread extends Thread {
		public void run() {
			startP = getRodTip();
			stopP = new PointF(startP.x, startP.y);

			int drawNum = CAST_ANIMATION_DURATION / 1000 * 50;
			long time = CAST_ANIMATION_DURATION / drawNum;

			double dx = dropP.x - stopP.x;
			double dy = dropP.y - stopP.y;
			double interpolatorX = dx / drawNum;
			double interpolatorY = dy / drawNum;

			while ((Math.abs(dx) > 0.5) && (Math.abs(dy) > 0.5)) {
				stopP.offset((float) interpolatorX, (float) interpolatorY);
				Log.d("CastAnimationThread", "startP(" + startP.x + ","
						+ startP.y + ") -> stopP(" + stopP.x + "," + stopP.y
						+ ")");
				postInvalidate();

				dx = dropP.x - stopP.x;
				dy = dropP.y - stopP.y;
				try {
					Thread.sleep(time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			stopP.set(dropP);
			postInvalidate();
			setState(ROD_AND_LINE_STATE);
		}
	}
}