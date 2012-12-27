package com.octo.android.robospice.sample.ui.spicelist.adapter;

import java.io.File;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.octo.android.robospice.request.simple.BigBinaryRequest;
import com.octo.android.robospice.sample.ui.spicelist.model.ListTweets;
import com.octo.android.robospice.sample.ui.spicelist.model.Tweet;
import com.octo.android.robospice.sample.ui.spicelist.view.TweetListItemView;
import com.octo.android.robospice.spicelist.BigBinarySpiceManager;
import com.octo.android.robospice.spicelist.SpiceArrayAdapter;
import com.octo.android.robospice.spicelist.SpiceListItemView;

/**
 * 
 * @author jva
 * @author stp
 * @author sni
 * 
 */
public class ListTweetArrayAdapter extends SpiceArrayAdapter< Tweet > {

    // --------------------------------------------------------------------------------------------
    // CONSTRUCTOR
    // --------------------------------------------------------------------------------------------

    public ListTweetArrayAdapter( Context context, BigBinarySpiceManager spiceManagerBinary, ListTweets tweets ) {
        super( context, spiceManagerBinary, tweets.getResults() );
    }

    // --------------------------------------------------------------------------------------------
    // ArrayAdapter implementation
    // --------------------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        View view;

        Tweet currentItem = getItem( position );
        if ( convertView != null ) {
            view = convertView;
        } else {
            view = new TweetListItemView( getContext() );
        }
        ( (TweetListItemView) view ).updateView( currentItem );
        // this is the most important line. It will update views automatically
        // ----------------------------------------
        updateListItemViewAsynchronously( currentItem, (SpiceListItemView< Tweet >) view );
        // ----------------------------------------
        return view;
    }

    /**
     * Improve {@link ListView} performance while scrolling<br/>
     * 
     * <a href="ttp://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder">See doc</a>
     */
    static class ViewHolder {
        TextView fullName;
        TextView nickName;
        ImageView thumbnail;
    }

    @Override
    public BigBinaryRequest createRequest( Tweet data ) {
        Tweet tweet = data;
        File tempFile = new File( getContext().getCacheDir(), "THUMB_IMAGE_TEMP_" + tweet.getFrom_user() );
        return new BigBinaryRequest( tweet.getProfile_image_url(), tempFile );
    }

}
