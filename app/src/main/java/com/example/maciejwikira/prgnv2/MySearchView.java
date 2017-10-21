package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.support.v7.widget.SearchView;

/**
 * Created by Maciej on 2017-10-21.
 */

public class MySearchView extends SearchView {

    OnSearchViewCollapsedEventListener mSearchViewCollapsedEventListener;
    OnSearchViewExpandedEventListener mOnSearchViewExpandedEventListener;

    public MySearchView(Context context) {
        super(context);
    }

    @Override
    public void onActionViewCollapsed() {
        if (mSearchViewCollapsedEventListener != null)
            mSearchViewCollapsedEventListener.onSearchViewCollapsed();
        super.onActionViewCollapsed();
    }

    @Override
    public void onActionViewExpanded() {
        if (mOnSearchViewExpandedEventListener != null)
            mOnSearchViewExpandedEventListener.onSearchViewExpanded();
        super.onActionViewExpanded();
    }

    public interface OnSearchViewCollapsedEventListener {
        public void onSearchViewCollapsed();
    }

    public interface OnSearchViewExpandedEventListener {
        public void onSearchViewExpanded();
    }

    public void setOnSearchViewCollapsedEventListener(OnSearchViewCollapsedEventListener eventListener) {
        mSearchViewCollapsedEventListener = eventListener;
    }

    public void setOnSearchViewExpandedEventListener(OnSearchViewExpandedEventListener eventListener) {
        mOnSearchViewExpandedEventListener = eventListener;
    }


}