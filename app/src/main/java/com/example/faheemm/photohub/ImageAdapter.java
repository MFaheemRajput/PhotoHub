package com.example.faheemm.photohub;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by faheem.m on 07/04/2015.
 */
public class ImageAdapter extends BaseAdapter {
    private Context context;
    private int itemBackground;
    private Integer[] imgArray;

    private Cursor cursor;
    private int columnIndex;
    File[] sdDirFiles;
    //List<File> sdDirFiles = new ArrayList;

    public ImageAdapter(Context c,Integer[] imgArrayParam ,View fragmentParam)
    {
        context = c;



        File sdDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES),"PhotoHub");

        if (!sdDir.exists()) {

            sdDir.mkdirs();

        }

        try {
            sdDirFiles = sdDir.listFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }




// request only the image ID to be returned


        imgArray = imgArrayParam;
        TypedArray a = c.obtainStyledAttributes(R.styleable.MyGallery);
        itemBackground = a.getResourceId(R.styleable.MyGallery_android_galleryItemBackground, 0);
        a.recycle();
    }
    // returns the number of image
    //
    // s
    public int getCount() {

        if (sdDirFiles != null)
        return sdDirFiles.length;
        else return 0;

    }

    // returns the ID of an item
    public Object getItem(int position) {
        return position;
    }
    // returns the ID of an item
    public long getItemId(int position) {
        return position;
    }
    // returns an ImageView view
    public View getView(int position, View convertView, ViewGroup parent) {
//        ImageView imageView = new ImageView(context);
//        imageView.setImageResource(imgArray[position]);
//        imageView.setLayoutParams(new Gallery.LayoutParams(200, 200));
//        imageView.setBackgroundResource(itemBackground);

//        return imageView;

        ImageView i = new ImageView(context);
//        // Move cursor to current position
//        cursor.moveToPosition(position);
//        // Get the current value for the requested column
//        int imageID = cursor.getInt(columnIndex);
//        // obtain the image URI
//        Uri uri = Uri.withAppendedPath( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(imageID) );
//        String url = uri.toString();
//        // Set the content of the image based on the image URI
//        int originalImageId = Integer.parseInt(url.substring(url.lastIndexOf("/") + 1, url.length()));
//        Bitmap b = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(),
//                originalImageId, MediaStore.Images.Thumbnails.MINI_KIND, null);
//        i.setImageBitmap(b);
//        i.setLayoutParams(new Gallery.LayoutParams(150, 100));
//        i.setScaleType(ImageView.ScaleType.FIT_XY);
//        i.setBackgroundResource(itemBackground);
//        return i;



            File  singleFile = sdDirFiles[position];
            ImageView myImageView = new ImageView(context);
            //myImageView.setImageDrawable(Drawable.createFromPath(singleFile.getAbsolutePath()));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPurgeable=false;

            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(singleFile.getAbsolutePath()), 150, 100);

            myImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            myImageView.setImageBitmap(ThumbImage);

            myImageView.setLayoutParams(new Gallery.LayoutParams(200, 200));
            myImageView.setBackgroundResource(itemBackground);

        //mThumbIds = (Integer[])drawablesId.toArray(new Integer[0]);
        return  myImageView;
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

}