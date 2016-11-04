package douzi.android.view;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * LrcView can display LRC file and Seek it.
 * @author douzifly
 *
 */
public class LrcView extends View implements ILrcView{

	public final static String TAG = "LrcView";

	/** normal display mode*/
	public final static int DISPLAY_MODE_NORMAL = 0;
	/** seek display mode */
	public final static int DISPLAY_MODE_SEEK = 1;

	private List<LrcRow> mLrcRows; 	// all lrc rows of one lrc file
	private int mMinSeekFiredOffset = 10; // min offset for fire seek action, px;
	private int mHignlightRow = 0;   // current singing row , should be highlighted.
	private int mHignlightRowColor = Color.WHITE;
	private int mNormalRowColor = Color.GRAY;
	private int mSeekLineColor = Color.CYAN;
	private int mSeekLineTextColor = Color.CYAN;
	private int mSeekLineTextSize = 30;
	private int mLrcFontSize = 30; 	// font size of lrc
	private int mPaddingY = 10;		// padding of each row
	private int mSeekLinePaddingX = 0; // Seek line padding x
	private int mDisplayMode = DISPLAY_MODE_NORMAL;
	private LrcViewListener mLrcViewListener;

	private String mLoadingLrcTip = "Downloading lrc...";

	private Paint mPaint;

	public LrcView(Context context,AttributeSet attr){
		super(context,attr);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextSize(mLrcFontSize);
	}

	public void setListener(LrcViewListener l){
		mLrcViewListener = l;
	}

	public void setLoadingTipText(String text){
		mLoadingLrcTip = text;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final int height = getHeight(); // height of this view
		final int width = getWidth() ; // width of this view
		if(mLrcRows == null || mLrcRows.size() == 0){
			if(mLoadingLrcTip != null){
				// draw tip when no lrc.
				mPaint.setColor(mHignlightRowColor);
				mPaint.setTextSize(mLrcFontSize);
//				mPaint.setTextAlign(Align.CENTER);
				canvas.drawText(mLoadingLrcTip, 0, height / 2 - mLrcFontSize, mPaint);
			}
			return;
		}

		int rowY = 0; // vertical point of each row.
		final int rowX = width / 2;
		int rowNum = 0;

		// 1, draw highlight row at center.
		// 2, draw rows above highlight row.
		// 3, draw rows below highlight row.

		// 1 highlight row
		String highlightText = mLrcRows.get(mHignlightRow).content;
		int highlightRowY = height / 2 - mLrcFontSize;
		mPaint.setColor(mHignlightRowColor);
		mPaint.setTextSize(mLrcFontSize);
//		mPaint.setTextAlign(Align.CENTER);
		canvas.drawText(highlightText, 0, highlightRowY, mPaint);

		if(mDisplayMode == DISPLAY_MODE_SEEK){
			// draw Seek line and current time when moving.
			mPaint.setColor(mSeekLineColor);
			canvas.drawLine(mSeekLinePaddingX, highlightRowY, width - mSeekLinePaddingX, highlightRowY, mPaint);
			mPaint.setColor(mSeekLineTextColor);
			mPaint.setTextSize(mSeekLineTextSize);
//			mPaint.setTextAlign(Align.CENTER);
			canvas.drawText(mLrcRows.get(mHignlightRow).strTime, 0, highlightRowY, mPaint);
		}

		// 2 above rows
		mPaint.setColor(mNormalRowColor);
		mPaint.setTextSize(mLrcFontSize);
//		mPaint.setTextAlign(Align.CENTER);
		rowNum = mHignlightRow - 1;
		rowY = highlightRowY - mPaddingY - mLrcFontSize;
		while( rowY > -mLrcFontSize && rowNum >= 0){
			String text = mLrcRows.get(rowNum).content;
			canvas.drawText(text, 0, rowY, mPaint);
			rowY -=  (mPaddingY + mLrcFontSize);
			rowNum --;
		}

		// 3 below rows
		rowNum = mHignlightRow + 1;
		rowY = highlightRowY + mPaddingY + mLrcFontSize;
		while( rowY < height && rowNum < mLrcRows.size()){
			String text = mLrcRows.get(rowNum).content;
			canvas.drawText(text, 0, rowY, mPaint);
			rowY += (mPaddingY + mLrcFontSize);
			rowNum ++;
		}
	}

	public void seekLrc(int position, boolean cb) {
	    if(mLrcRows == null || position < 0 || position > mLrcRows.size()) {
	        return;
	    }
		LrcRow lrcRow = mLrcRows.get(position);
		mHignlightRow = position;
		invalidate();
		if(mLrcViewListener != null && cb){
			mLrcViewListener.onLrcSeeked(position, lrcRow);
		}
	}

	private float mLastMotionY;

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if(mLrcRows == null || mLrcRows.size() == 0){
			return super.onTouchEvent(event);
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
//			Log.d(TAG,"down,mLastMotionY:"+mLastMotionY);
			mLastMotionY = event.getY();
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			doSeek(event);
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if(mDisplayMode == DISPLAY_MODE_SEEK){
				seekLrc(mHignlightRow, true);
			}
			mDisplayMode = DISPLAY_MODE_NORMAL;
			invalidate();
			break;
		}
		return true;
	}

	private void doSeek(MotionEvent event) {
		float y = event.getY();
		float offsetY = y - mLastMotionY; // touch offset.
		if(Math.abs(offsetY) < mMinSeekFiredOffset){
			// move to short ,do not fire seek action
			return;
		}
		mDisplayMode = DISPLAY_MODE_SEEK;
		int rowOffset = Math.abs((int) offsetY / mLrcFontSize); // highlight row offset.
//		Log.d(TAG, "move new hightlightrow : " + mHignlightRow + " offsetY: " + offsetY + " rowOffset:" + rowOffset);
		if(offsetY < 0){
			// finger move up
			mHignlightRow += rowOffset;
		}else if(offsetY > 0){
			// finger move down
			mHignlightRow -= rowOffset;
		}
		mHignlightRow = Math.max(0, mHignlightRow);
		mHignlightRow = Math.min(mHignlightRow, mLrcRows.size() - 1);

		if(rowOffset > 0){
			mLastMotionY = y;
			invalidate();
		}
	}

    public void setLrc(List<LrcRow> lrcRows) {
        mLrcRows = lrcRows;
        invalidate();
    }

    public void seekLrcToTime(long time) {
        if(mLrcRows == null || mLrcRows.size() == 0) {
            return;
        }

        if(mDisplayMode != DISPLAY_MODE_NORMAL) {
            // touching
            return;
        }

//        Log.d(TAG, "seekLrcToTime:" + time);
        // find row
        for(int i = 0; i < mLrcRows.size(); i++) {
            LrcRow current = mLrcRows.get(i);
            LrcRow next = i + 1 == mLrcRows.size() ? null : mLrcRows.get(i + 1);

            if((time >= current.time && next != null && time < next.time)
                    || (time > current.time && next == null)) {
                seekLrc(i, false);
                return;
            }
        }
    }
}
