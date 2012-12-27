package com.octo.android.robospice.sample.ui.spicelist.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.octo.android.robospice.sample.ui.spicelist.R;
import com.octo.android.robospice.sample.ui.spicelist.model.Tweet;
import com.octo.android.robospice.spicelist.SpiceListItemView;

public class TweetListItemView extends RelativeLayout implements SpiceListItemView< Tweet > {

    private TextView userNameTextView;
    private TextView tweetContentTextView;
    private ImageView thumbImageView;
    private Tweet tweet;

    public TweetListItemView( Context context ) {
        super( context );
        inflateView( context );
    }

    private void inflateView( Context context ) {
        LayoutInflater.from( context ).inflate( R.layout.view_cell_tweet, this );

        this.userNameTextView = (TextView) this.findViewById( R.id.user_name_textview );
        this.tweetContentTextView = (TextView) this.findViewById( R.id.tweet_content_textview );
        this.thumbImageView = (ImageView) this.findViewById( R.id.octo_thumbnail_imageview );
    }

    public void updateView( Tweet tweet ) {
        this.tweet = tweet;

        userNameTextView.setText( tweet.getFrom_user() );
        tweetContentTextView.setText( tweet.getText() );
    }

    @Override
    public Tweet getData() {
        return tweet;
    }

    @Override
    public ImageView getImageView() {
        return thumbImageView;
    }

}
