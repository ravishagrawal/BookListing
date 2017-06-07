package com.example.android.booklisting;

/**
 * Created by hp on 05-06-2017.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import java.util.List;

public class LibraryAdapter extends ArrayAdapter<Library> {

    public LibraryAdapter(Context context, List<Library> objects) {
        super(context, 0, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_view, parent, false);

            /*
            Using a view holder class so that it is only necessary to use find view by id when view is inflated.
             */
            viewHolder = new ViewHolder();

            /*
            Finding views and assigning.
             */
            viewHolder.title = (TextView) listItemView.findViewById(R.id.title_text);
            viewHolder.author = (TextView) listItemView.findViewById(R.id.author_text);
            viewHolder.description = (TextView) listItemView.findViewById(R.id.description_text);


            /*
            setting a tag to the view for future reference instead of using find view by id each time.
             */
            listItemView.setTag(viewHolder);
        } else {

            /*
            here the tag is used to reference the view if it has already been inflated and a tag set to the holder.
             */
            viewHolder = (ViewHolder) listItemView.getTag();
        }

        /*
        Getting current object position in Array.
         */
        Library currentLibrary = getItem(position);

        /*
        Checking to see if object exists before setting data to viewHolders.
         */
        if (currentLibrary != null) {

            viewHolder.title.setText(currentLibrary.getTitle());

            /*
            Adding prefix (Author: ) to author string before setting string to viewHolder.
             */
            String author = getContext().getString(R.string.by)+" "+currentLibrary.getAuthor();
            viewHolder.author.setText(author);

            /*
            Checking if description exists for currentBookObject.  If none exists setting description viewHolder to default of
            * No description available. and reducing text size.
             */
            String description;
            if(currentLibrary.getDescription()==null || currentLibrary.getDescription().equals("")){
                viewHolder.description.setTextSize(8);
                description = "\n"+getContext().getResources().getString(R.string.no_description);
            }else {
                viewHolder.description.setTextSize(12);
                description = currentLibrary.getDescription();
            }
            viewHolder.description.setText(description);
        }
        return listItemView;
    }

    static class ViewHolder {

        TextView title;
        TextView author;
        TextView description;
    }

}
