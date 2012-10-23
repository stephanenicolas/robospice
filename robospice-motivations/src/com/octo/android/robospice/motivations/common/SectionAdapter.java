package com.octo.android.robospice.motivations.common;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.octo.android.robospice.motivations.R;

public class SectionAdapter extends ArrayAdapter< String > {

    private final static int VIEW_TYPE_HEADER = 0;
    private final static int VIEW_TYPE_ITEM = 1;
    private final static int VIEW_TYPE_COUNT = 2;

    private final int layoutHeaderResId;
    private final int layoutItemResId;
    private List< SectionItem > listSectionItems = new ArrayList< SectionItem >();

    public SectionAdapter( Context context, int layoutHeaderResId, int layoutItemResId ) {
        super( context, 0 );
        this.layoutHeaderResId = layoutHeaderResId;
        this.layoutItemResId = layoutItemResId;
    }

    public void addSection( String section, List< String > listItemPerSection ) {
        addSection( section );
        addItems( listItemPerSection );
    }

    public void addSection( String section, String[] listItemPerSection ) {
        addSection( section );
        addItems( listItemPerSection );
    }

    public void addItems( List< String > listItemPerSection ) {
        for ( String item : listItemPerSection ) {
            addItem( item );
        }
    }

    public void addItems( String[] listItemPerSection ) {
        for ( String item : listItemPerSection ) {
            addItem( item );
        }
    }

    public void addSection( String section ) {
        listSectionItems.add( new SectionItem( section, true ) );
    }

    public void addItem( String item ) {
        listSectionItems.add( new SectionItem( item, false ) );
    }

    @Override
    public void add( String item ) {
        addItem( item );
    }

    /*
     * @Override public void addAll( String... items ) { addItems( items ); }
     */

    @Override
    public int getCount() {
        return listSectionItems.size();
    }

    @Override
    public String getItem( int position ) {
        return listSectionItems.get( position ).title;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType( int position ) {
        return listSectionItems.get( position ).isTitle ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;

    }

    public boolean areAllItemsSelectable() {
        return false;
    }

    @Override
    public boolean isEnabled( int position ) {
        return getItemViewType( position ) == VIEW_TYPE_ITEM;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        SectionItem sectionItem = listSectionItems.get( position );
        int layoutResId = sectionItem.isTitle ? layoutHeaderResId : layoutItemResId;
        if ( convertView == null ) {
            convertView = LayoutInflater.from( getContext() ).inflate( layoutResId, parent, false );
        }
        TextView textView = (TextView) convertView.findViewById( android.R.id.text1 );
        String text = listSectionItems.get( position ).title;
        boolean spicy = !sectionItem.isTitle && text.contains( "RoboSpice" );
        textView.setText( text );
        textView.setCompoundDrawablesWithIntrinsicBounds( spicy ? R.drawable.spice : 0, 0, 0, 0 );
        return convertView;
    }

    @Override
    public long getItemId( int position ) {
        return position;
    }

    private class SectionItem {
        private String title;
        private boolean isTitle;

        public SectionItem( String title, boolean isTitle ) {
            this.title = title;
            this.isTitle = isTitle;
        }
    }

}
