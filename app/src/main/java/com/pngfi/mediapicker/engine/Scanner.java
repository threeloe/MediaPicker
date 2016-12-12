package com.pngfi.mediapicker.engine;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.pngfi.mediapicker.entity.Image;
import com.pngfi.mediapicker.entity.ImageFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pngfi on 2016/11/30.
 * 扫描手机中的图片，视频
 */

public class Scanner implements LoaderManager.LoaderCallbacks<Cursor> {


    //loadType
    public static final int LOAD_TYPE_IMG = 1;
    public static final int LOAD_TYPE_VIDEO = 2;

    private FragmentActivity mContext;

    /**
     * 可选值：LOAD_TYPE_IMG
     * LOAD_TYPE_VIDEO
     */
    private int loadType;


    private ArrayList<ImageFolder> imageFolders;


    private OnLoadFishedListener mListenter;


    private final String[] IMG_PROJECTION = {
            MediaStore.Images.Media.DATA,         //路径
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_ADDED//添加时间，毫秒值
    };

    private final String[] VIDEO_PROJECTION = {
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED
    };

    public Scanner(FragmentActivity activity, int loadType) {
        this.loadType = loadType;
        mContext = activity;
        imageFolders = new ArrayList<>();
    }


    public void scan(OnLoadFishedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener can't be null");
        }
        mListenter = listener;
        mContext.getSupportLoaderManager().initLoader(loadType, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;
        if (id == LOAD_TYPE_IMG) {
            cursorLoader = new CursorLoader(mContext, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMG_PROJECTION, null, null, IMG_PROJECTION[3] + " DESC");
        } else if (loadType == LOAD_TYPE_VIDEO) {
            cursorLoader = new CursorLoader(mContext, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEO_PROJECTION, null, null, VIDEO_PROJECTION[3] + " DESC");
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.getCount() > 0) {
            ArrayList<Image> allImages = new ArrayList<>();

            if (loadType == LOAD_TYPE_IMG) {

                while (data.moveToNext()) {
                    String path = data.getString(data.getColumnIndex(IMG_PROJECTION[0]));
                    long size = data.getLong(data.getColumnIndex(IMG_PROJECTION[1]));
                    String mimeType = data.getString(data.getColumnIndex(IMG_PROJECTION[2]));
                    long addTime = data.getLong(data.getColumnIndex(IMG_PROJECTION[3]));
                    Image image = new Image();
                    image.setPath(path);
                    image.setSize(size);
                    image.setMimeType(mimeType);
                    image.setAddTime(addTime);
                    allImages.add(image);

                    File imageFile = new File(path);
                    File imageParentFile = imageFile.getParentFile();
                    ImageFolder imageFolder = new ImageFolder();
                    imageFolder.setName(imageParentFile.getName());
                    imageFolder.setPath(imageParentFile.getAbsolutePath());
                    if (!imageFolders.contains(imageFolder)) {
                        ArrayList<Image> images = new ArrayList<>();
                        images.add(image);
                        imageFolder.setCover(image);
                        imageFolder.setImages(images);
                        imageFolder.setName(imageParentFile.getName());
                        imageFolders.add(imageFolder);
                    } else {
                        imageFolders.get(imageFolders.indexOf(imageFolder)).getImages().add(image);
                    }

                }
                //构造所有图片的集合
                ImageFolder allImagesFolder = new ImageFolder();
                allImagesFolder.setName("所有图片");
                allImagesFolder.setPath("/");
                allImagesFolder.setCover(allImages.get(0));
                allImagesFolder.setImages(allImages);
                imageFolders.add(0, allImagesFolder);  //确保第一条是所有图片

            } else if (loadType == LOAD_TYPE_VIDEO) {


            }
        }

        mListenter.onLoadFinshed(imageFolders);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    public interface OnLoadFishedListener {
        void onLoadFinshed(List<ImageFolder> imageFolders);
    }
}