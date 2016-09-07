package ex.com.filescanlister;


import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.CustomViewHolder> {
    private List<FileKrumb> feedItemList;
    private Context mContext;

    public FileListAdapter(Context context, List<FileKrumb> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public FileListAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        FileKrumb feedItem = feedItemList.get(position);
        if(feedItem.count == 0 && feedItem.length == 0) {
            holder.name.setText( feedItem.name);
            holder.len.setText("");
            holder.itemView.setBackgroundColor(Color.LTGRAY);
            Log.v("Filer Start Q","") ;
        } else if(feedItem.count > 0 ){
            holder.name.setText( feedItem.ext);
            holder.len.setText(Integer.toString(feedItem.count));
            holder.itemView.setBackgroundColor(Color.WHITE);
            Log.v("Filer Ext Q", "") ;

        } else if(feedItem.length > 0 ){
            holder.name.setText( feedItem.name);
            holder.len.setText(Long.toString(feedItem.length) + (feedItem.isAverage?"KB" :" MB"));
            holder.itemView.setBackgroundColor(Color.WHITE);
            Log.v("Filer File Q", "") ;
        }
        Log.v("Filer Result Q", feedItem.name + " " + feedItem.length + " " + feedItem.ext + " " + feedItem.count) ;

    }


    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected TextView len;

        public CustomViewHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.textView2);
            this.len = (TextView) view.findViewById(R.id.textView3);
        }
    }
}

