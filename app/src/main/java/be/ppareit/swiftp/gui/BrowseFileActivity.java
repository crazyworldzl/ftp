package be.ppareit.swiftp.gui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.GridView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import be.ppareit.swiftp.FsSettings;
import be.ppareit.swiftp.R;
import be.ppareit.swiftp.Util;

/**
 * Created by ZhangLong on 2017/5/4.
 */

public class BrowseFileActivity extends AppCompatActivity {
    private GridView gv;
    private Adapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browsefile);
        setTitle(R.string.browse_file);
        initView();
    }

    public void initView() {
        gv = (GridView) findViewById(R.id.browsefile_gv);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = adapter.getFiles().get(position);
                if (file.isDirectory()) {
                    if (file.canRead()) {
                        if (file.listFiles().length > 0) {
                            List<File> files = Arrays.asList(file.listFiles());
                            adapter.addFiles(files);
                        } else {
                            adapter.addEmptyFile(file);
                        }
                    }
                } else {
                    Util.openFile(view.getContext(), file);
                }

            }
        });
        adapter = new Adapter();
        gv.setAdapter(adapter);
        File chrootDir = null;
        try {
            chrootDir = new File(FsSettings.getChrootDir().getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<File> files = Arrays.asList(chrootDir.listFiles());
        adapter.addFiles(files);
    }
    private void show(String file) {
        try {
            Uri uri = null;
            if (Build.VERSION.SDK_INT >= 24) {
                uri = FileProvider.getUriForFile(this,getPackageName()+".fileprovider",new File(file));
            } else {
                uri = Uri.fromFile(new File(file));
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri, "application/pdf");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

class Adapter extends BaseAdapter {
    List<File> fileList;
    List<Boolean> booleanList;

    public Adapter() {
        fileList = new ArrayList<File>();
        booleanList = new ArrayList<Boolean>();
    }

    public void addFiles(List<File> files) {
        File parent = files.get(0).getParentFile();
        if (parent.getParentFile() != null) {
            fileList.clear();
            fileList.add(parent.getParentFile());
            fileList.addAll(files);
            notifyDataSetChanged();
        }
    }

    public void addEmptyFile(File file) {
        if (file.getParentFile() != null) {
            fileList.clear();
            fileList.add(file.getParentFile());
            notifyDataSetChanged();
        }
    }

    public void addFile(File file) {
        fileList.add(file);
        notifyDataSetChanged();
    }

    public List<File> getFiles() {
        return fileList;
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public File getItem(int position) {
        return fileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void notifyDataSetChanged() {
        booleanList = new ArrayList<>(fileList.size());
        super.notifyDataSetChanged();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = new CheckedTextView(parent.getContext());
            holder = new ViewHolder();
            holder.ctv = (CheckedTextView) convertView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-1, -1);
        layoutParams.width = parent.getWidth() * 10 / 31;
        layoutParams.height = parent.getWidth() / 4;
        holder.ctv.setLayoutParams(layoutParams);
        holder.ctv.setBackgroundResource(R.drawable.browsefile_itembg);
        holder.ctv.setTextColor(0xff000000);
        holder.ctv.setPadding(5, 0, 5, 0);
        if (position == 0) {
            holder.ctv.setText("返回上一级");
        } else {
            holder.ctv.setText(fileList.get(position).getName());
        }
        Drawable drawable = null;
        if (!fileList.get(position).isFile()) {
            drawable = holder.ctv.getResources().getDrawable(R.mipmap.folder);
        } else {
            String mimeType = Util.getMIMEType(fileList.get(position));
            if (mimeType.contains("video")) {
                drawable = holder.ctv.getResources().getDrawable(R.mipmap.video);
            } else if (mimeType.contains("image")) {
                drawable = holder.ctv.getResources().getDrawable(R.mipmap.image);
            } else if (mimeType.contains("audio")) {
                drawable = holder.ctv.getResources().getDrawable(R.mipmap.audio);
            } else if (mimeType.contains("txt")) {
                drawable = holder.ctv.getResources().getDrawable(R.mipmap.txt);
            } else {
                drawable = holder.ctv.getResources().getDrawable(R.mipmap.unknow);
            }
        }
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        holder.ctv.setCompoundDrawables(null, drawable, null, null);
        holder.ctv.setGravity(Gravity.CENTER_HORIZONTAL);
        holder.ctv.setPadding(0,30,0,0);
        holder.ctv.setTextAlignment(CheckedTextView.TEXT_ALIGNMENT_CENTER);
        return convertView;
    }

    class ViewHolder {
        CheckedTextView ctv;
    }
}
