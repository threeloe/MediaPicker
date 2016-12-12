package com.pngfi.mediapicker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.pngfi.mediapicker.R;
import com.pngfi.mediapicker.engine.ImageLoader;
import com.pngfi.mediapicker.entity.Image;
import com.pngfi.mediapicker.entity.Media;
import com.pngfi.mediapicker.utils.Utils;

import java.util.ArrayList;

/**
 * Created by pngfi on 2016/11/30.
 */
public class GridAdapter extends BaseAdapter {

    private static final int ITEM_TYPE_CAMERA = 0;  //第一个条目是相机
    private static final int ITEM_TYPE_NORMAL = 1;  //第一个条目不是相机

    private Context context;
    private ArrayList<Image> photos;       //当前需要显示的所有的图片数据

    private int mImageSize;               //每个条目的大小
    private OnSelectedChangeListener listener;   //图片被点击的监听

    private boolean showCamera = true;         //是否显示拍照按钮


    private ArrayList<Media> selectedImages;

    private int imageFolderPosition;//图片文件夹在List中的位置


    public ArrayList<Media> getSelectedImages() {
        return selectedImages;
    }

    public void setSelectedImages(ArrayList<Media> selectedImages) {
        this.selectedImages = selectedImages;
    }

    private void setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
    }

    private boolean showCamera() {
        return showCamera && imageFolderPosition == 0;
    }

    public GridAdapter(Context context, ArrayList<Image> images) {
        this.context = context;
        if (images == null || images.size() == 0)
            this.photos = new ArrayList<>();
        else
            this.photos = images;

        mImageSize = Utils.getImageItemWidth(context);
    }


    public GridAdapter(Context context) {
        this(context, null);
    }


    public void setImageFolderPosition(int position) {
        imageFolderPosition = position;
    }

    public void refreshData(ArrayList<Image> images) {
        if (images == null || images.size() == 0) this.photos = new ArrayList<>();
        else this.photos = images;
        notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (showCamera())
            return position == 0 ? ITEM_TYPE_CAMERA : ITEM_TYPE_NORMAL;
        return ITEM_TYPE_NORMAL;
    }

    @Override
    public int getCount() {
        return showCamera() ? photos.size() + 1 : photos.size();
    }

    @Override
    public Image getItem(int position) {
        if (showCamera()) {
            if (position == 0) return null;
            return photos.get(position - 1);
        } else {
            return photos.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        int itemViewType = getItemViewType(position);
        if (itemViewType == ITEM_TYPE_CAMERA) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_camera, parent, false);
            convertView.setLayoutParams(new AbsListView.LayoutParams(mImageSize, mImageSize));

        } else {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_normal_image, parent, false);
                //让图片是个正方形
                convertView.setLayoutParams(new AbsListView.LayoutParams(mImageSize, mImageSize));
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final Image photo = getItem(position);

            holder.cbSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (listener != null) {
                        listener.onSelectedChange(holder.cbSelect, holder.mask, photo);
                    }
                }
            });

            boolean checked = getSelectedImages().contains(photo);
            if (checked) {
                holder.mask.setVisibility(View.VISIBLE);
                holder.cbSelect.setChecked(true);
            } else {
                holder.mask.setVisibility(View.GONE);
                holder.cbSelect.setChecked(false);
            }
            ImageLoader.loadImage(context, holder.ivPhoto, photo.getPath(), mImageSize, mImageSize); //显示图片
        }
        return convertView;
    }

    private class ViewHolder {
        public View rootView;
        public ImageView ivPhoto;
        public View mask;
        public CheckBox cbSelect;

        public ViewHolder(View view) {
            rootView = view;
            ivPhoto = (ImageView) view.findViewById(R.id.iv_photo);
            mask = view.findViewById(R.id.mask);
            cbSelect = (CheckBox) view.findViewById(R.id.cb_select);
        }
    }

    public void setOnSelectedChangeListener(OnSelectedChangeListener listener) {
        this.listener = listener;
    }

    public interface OnSelectedChangeListener {
        void onSelectedChange(CheckBox select, View mask, Media media);
    }
}