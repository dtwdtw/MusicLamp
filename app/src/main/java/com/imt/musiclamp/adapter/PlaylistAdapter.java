package com.imt.musiclamp.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.model.OnlinePlaylist;

import java.util.List;

public class PlaylistAdapter extends BaseAdapter {

    private Context context;
    private List<OnlinePlaylist> playlists;
    private LayoutInflater inflater;
    private MyApplication myApplication;

    public PlaylistAdapter(Context context, List<OnlinePlaylist> playlists) {
        this.context = context;
        this.playlists = playlists;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return playlists.size();
    }

    @Override
    public Object getItem(int position) {
        return playlists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        OnlinePlaylist playlist = playlists.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_playlist, null, false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            viewHolder.textViewTitle = (TextView) convertView.findViewById(R.id.textView_title);
            viewHolder.textViewTitle.setTextColor(0xdd000000);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final ViewHolder finalViewHolder = viewHolder;
        Glide.with(context)
                .load(playlist.getImg())
                .centerCrop()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

                        finalViewHolder.imageView.setImageResource(R.drawable.ic_audiotrack);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .crossFade()
                .into(viewHolder.imageView);
        viewHolder.textViewTitle.setText(playlist.getName());
        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView textViewTitle;
    }

    /**
     * 动态调整每个item高度，以便刚好适应整个屏幕
     */
    private void makeItemFitParentHeight(ViewGroup parent, View itemView) {
        Resources resources = parent.getContext().getResources();
        // 网格列数
        int numColumns = 3;
        // 网格行数
        int numRows = (int) Math.ceil(1.0 * getCount() / numColumns);
        // 网格垂直分割线的宽度
        int verticalSpacingHeight = (int) resources
                .getDimension(android.R.dimen.notification_large_icon_height);
        // 父容器的总高度
        int parentHeight = ((ViewGroup) parent.getParent()).getHeight();
        // 计算出条目应有的高度
        int itemHeight = (int) (1.0 * parentHeight / numRows);
        // 减去网格分割线的高度
        itemHeight = itemHeight - verticalSpacingHeight * (numRows - 1);
        // 设置条目的高度
        if (itemHeight > 0) {
            ListView.LayoutParams lp = new ListView.LayoutParams(
                    ListView.LayoutParams.MATCH_PARENT, itemHeight);
            itemView.setLayoutParams(lp);
        }
    }
}
