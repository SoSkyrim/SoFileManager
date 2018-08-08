package com.so.myfm.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.so.myfm.R;
import com.so.myfm.utils.FileSizeUtil;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * FM适配器
 * Created by sorrower on 21/04/2018.
 */

public class FMAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * 当前全部文件数组
     */
    private ArrayList<File> mFiles;
    /**
     * 当前上下文
     */
    private Context mContext;
    /**
     * 勾选文件位置列表
     */
    private ArrayList<Integer> mSelectList;

    private final LayoutInflater mLayoutInflater;

    private enum ITEM_TYPE {
        ITEM_TYPE_IMAGE,
        ITEM_TYPE_TEXT
    }

    /**
     * @param ctx   上下文
     * @param files 当前目录下全部文件
     */
    public FMAdapter(Context ctx, ArrayList<File> files) {
        mContext = ctx;
        mLayoutInflater = LayoutInflater.from(ctx);
        mFiles = files;
        mSelectList = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyIVHolder(mLayoutInflater.inflate(R.layout.rv_file_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        File file = mFiles.get(position);
        if (file.isDirectory()) {
            ((MyIVHolder) holder).fileImage.setImageResource(R.drawable.dir);
            ((MyIVHolder) holder).fileSize.setText(R.string.file_system_dir);
        } else {
            if (!mSelectList.contains(position)) {
                ((MyIVHolder) holder).fileImage.setImageResource(R.drawable.file);
            } else {
                ((MyIVHolder) holder).fileImage.setImageResource(R.drawable.select);
            }
            ((MyIVHolder) holder).fileSize.setText(FileSizeUtil.generateSize(file.length()));
        }

        ((MyIVHolder) holder).fileName.setText(file.getName());

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        ((MyIVHolder) holder).fileTime.setText(format.format(new Date(file.lastModified())));

        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView, pos);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 0) {
            return ITEM_TYPE.ITEM_TYPE_TEXT.ordinal();
        } else {
            return ITEM_TYPE.ITEM_TYPE_IMAGE.ordinal();
        }
    }

    @Override
    public int getItemCount() {
        return mFiles == null ? 0 : mFiles.size();
    }

    private static class MyIVHolder extends RecyclerView.ViewHolder {
        ImageView fileImage;
        TextView fileName;
        TextView fileSize;
        TextView fileTime;

        MyIVHolder(View v) {
            super(v);
            fileImage = (ImageView) v.findViewById(R.id.iv_file_image);
            fileName = (TextView) v.findViewById(R.id.tv_file_name);
            fileSize = (TextView) v.findViewById(R.id.tv_file_size);
            fileTime = (TextView) v.findViewById(R.id.tv_file_time);
        }
    }

    public void refreshData(ArrayList<File> files) {
        mFiles = files;
        this.notifyDataSetChanged();
    }

    public int refreshSelect(int pos) {
        if (pos < 0) {
            if (pos == -1) {
                // 全不选
                mSelectList.clear();
            } else if (pos == -2) {
                // 全选
                for (int i = 0; i < mFiles.size(); i++) {
                    if (mFiles.get(i).isFile() && !mSelectList.contains(i)) {
                        mSelectList.add(i);
                    }
                }
            }
        } else {
            // 单选
            if (!mSelectList.contains(pos)) {
                mSelectList.add(pos);
            } else {
                mSelectList.remove((Integer) pos);
            }
        }
        this.notifyDataSetChanged();

        return mSelectList.size();
    }

    // 监听事件
    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
}
