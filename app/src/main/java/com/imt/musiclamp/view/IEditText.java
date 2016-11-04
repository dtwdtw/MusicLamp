package com.imt.musiclamp.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class IEditText extends EditText implements TextWatcher, View.OnFocusChangeListener {
	
	private Drawable left, right, top, bottom;  
    private Rect rect;
	
	public IEditText(Context context) {
		super(context);

        init();
	}
	
	public IEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
	}

	public IEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init();
	}
	
	private void setDrawable() {
		if(0 == getText().toString().length())
			setCompoundDrawables(this.left, this.top, null, this.bottom);
		else
			setCompoundDrawables(this.left, this.top, this.right, this.bottom);
	}
	
	private void init() {
		setDrawable();
		addTextChangedListener(this);
        setOnFocusChangeListener(this);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		
		this.left = null;
		this.right = null;
		this.top = null;
		this.bottom = null;
		
		this.rect = null;
	}
	
	@Override
	public void setCompoundDrawables(Drawable left, Drawable top,
			Drawable right, Drawable bottom) {
		super.setCompoundDrawables(left, top, right, bottom);
		
		if(null != left)
			this.left = left;
		
		if(null != right)
			this.right = right;
		
		if(null != top)
			this.top = top;
		
		if(null != bottom)
			this.bottom = bottom;
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		
	}
	
	@Override
	public void onTextChanged(CharSequence text, int start,
			int lengthBefore, int lengthAfter) {
		IEditText.this.setDrawable();
	}
	
	@Override
	public void afterTextChanged(Editable arg0) {
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if((null != this.right) && (event.getAction() == MotionEvent.ACTION_UP)) {
			this.rect = this.right.getBounds();
			int i = (int) event.getRawX(); //The distance from the screen.
			
			if (i >= getRight() - this.rect.width()) {  
                setText("");  
                event.setAction(MotionEvent.ACTION_CANCEL);  
            }  
		}
		
		return super.onTouchEvent(event);
	}

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(this.hasFocus())
            setDrawable();
        else
            setCompoundDrawables(this.left, this.top, null, this.bottom);
    }
}
