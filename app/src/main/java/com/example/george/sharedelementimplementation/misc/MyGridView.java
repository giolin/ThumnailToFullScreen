package com.example.george.sharedelementimplementation.misc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

public class MyGridView extends GridView {
    public MyGridView(Context context) {
        super(context);
    }

    public MyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public View getViewByPosition(int position) {
        int firstPosition = this.getFirstVisiblePosition();
        int lastPosition = this.getLastVisiblePosition();

        if ((position < firstPosition) || (position > lastPosition))
            return null;

        return this.getChildAt(position - firstPosition);
    }

}
