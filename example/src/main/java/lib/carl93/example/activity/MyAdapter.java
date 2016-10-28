package lib.carl93.example.activity;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import lib.carl93.example.ItemEntity;
import lib.carl93.example.R;

/**
 * Created by Carl on 2016-09-10 010.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements View.OnClickListener {
    private List<ItemEntity> data;
    private Context mContext;
    private OnRecyclerViewItemClickListener itemClickListener;

    public MyAdapter(Context mContext, List<ItemEntity> data,OnRecyclerViewItemClickListener itemClickListener) {
        this.data = data;
        this.mContext = mContext;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_view, parent,false);
        view.setOnClickListener(this);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mTitleTextView.setText(data.get(position).getTitle());
        holder.mDescTextView.setText(data.get(position).getDesc());
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return null == data ? 0 : data.size();
    }

    @Override
    public void onClick(View v) {
        if(itemClickListener!=null)
            itemClickListener.onItemClick((Integer) v.getTag());
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mTitleTextView, mDescTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTitleTextView = (TextView) itemView.findViewById(R.id.item_title_tv);
            mDescTextView = (TextView) itemView.findViewById(R.id.item_desc_tv);
        }
    }
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(int position);
    }

}
