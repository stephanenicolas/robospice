package com.octo.android.robospice.sample.ui.spicelist.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tweet {

    private String text;
    private String profile_image_url;
    private String from_user;

    public String getText() {
        return text;
    }

    public void setText( String text ) {
        this.text = text;
    }

    public String getProfile_image_url() {
        return profile_image_url;
    }

    public void setProfile_image_url( String profile_image_url ) {
        this.profile_image_url = profile_image_url;
    }

    public String getFrom_user() {
        return from_user;
    }

    public void setFrom_user( String from_user ) {
        this.from_user = from_user;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( from_user == null ? 0 : from_user.hashCode() );
        result = prime * result + ( text == null ? 0 : text.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        Tweet other = (Tweet) obj;
        if ( from_user == null ) {
            if ( other.from_user != null ) {
                return false;
            }
        } else if ( !from_user.equals( other.from_user ) ) {
            return false;
        }
        if ( text == null ) {
            if ( other.text != null ) {
                return false;
            }
        } else if ( !text.equals( other.text ) ) {
            return false;
        }
        return true;
    }

}