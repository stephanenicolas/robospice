package com.octo.android.robospice.motivations.robospice.image;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;

import com.jakewharton.notificationcompat2.NotificationCompat2;
import com.octo.android.robospice.motivations.R;
import com.octo.android.robospice.notification.SpiceNotificationService;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestProgress;

public class ImageSpiceNotificationService extends SpiceNotificationService {

    private static final float MAX_PROGRESS = 100;

    @Override
    public Notification onCreateNotificationForRequestFailure( SpiceException ex ) {
        return createCustomSpiceNotification( "Failure" );
    }

    @Override
    public Notification onCreateNotificationForRequestSuccess() {
        return createCustomSpiceNotification( "Success" );
    }

    @Override
    public Notification onCreateNotificationForRequestProgress( RequestProgress progress ) {
        return createCustomSpiceNotification( "download in progress", Math.round( progress.getProgress() * MAX_PROGRESS ) );
    }

    private Notification createCustomSpiceNotification( String text ) {
        return createCustomSpiceNotification( text, 0 );
    }

    private Notification createCustomSpiceNotification( String text, int progress ) {
        Intent intent = new Intent( ImageSpiceNotificationService.this, ImageSpiceActivity.class );
        intent.addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP );
        PendingIntent pendingIntent = PendingIntent.getActivity( ImageSpiceNotificationService.this, 06, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        NotificationCompat2.Builder builder = new NotificationCompat2.Builder( ImageSpiceNotificationService.this )//
                .setContentTitle( "Earth image download" )//
                .setContentIntent( pendingIntent )//
                .setContentText( text )//
                .setAutoCancel( true )//
                .setSmallIcon( R.drawable.spice );

        if ( progress != 0 ) {
            builder.setProgress( 100, progress, false );
        }
        return builder.build();
    }

}
