package com.so.myfm.ui.activity;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.so.myfm.R;
import com.so.myfm.ui.adapter.FMAdapter;
import com.so.myfm.utils.LogUtil;
import com.so.myfm.utils.PermissionsUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

public class FMActivity extends AppCompatActivity {
    /**
     * 当前全部文件数组
     */
    private ArrayList<File> mCurFileList;
    /**
     * 存放选择文件的路径
     */
    private ArrayList<String> mSelectPath;
    /**
     * 选择文件个数记录
     */
    private int mSelectCount = 0;
    /**
     * 路径栈
     */
    private Stack<String> mCurPathStack;
    /**
     * 前一次点击返回按钮的时间
     */
    private long lastBackPressed = 0;
    /**
     * sdcard路径
     */
    private String mSdcardPath;
    /**
     * rv
     */
    private RecyclerView mRvContent;
    /**
     * 路径显示
     */
    private TextView mTvPath;
    /**
     * rv适配器
     */
    private FMAdapter mFMAdapter;
    /**
     * 全选点击记录
     */
    private int mAll = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fm);

        // 权限获取
        PermissionsUtil.verifyStoragePermissions(this);

        initSysUI();
        initUI();
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        final GridLayoutManager gm = new GridLayoutManager(this, 1);
        mRvContent.setLayoutManager(gm);

        // 获取sdcard目录
        mSdcardPath = Environment.getExternalStorageDirectory().toString();

        mSelectPath = new ArrayList<>();

        mCurPathStack = new Stack<>();

        mCurFileList = new ArrayList<>();

        File[] files = Environment.getExternalStorageDirectory().listFiles();
        if (files != null) {
            for (File f : files) {
                mCurFileList.add(f);
            }
        }

        mCurPathStack.push(mSdcardPath);
        mTvPath.setText(getCurPath());

        mFMAdapter = new FMAdapter(this, mCurFileList);

        mRvContent.setAdapter(mFMAdapter);
        mFMAdapter.setOnItemClickListener(new FMAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final File file = mCurFileList.get(position);
                if (file.isDirectory()) {
                    // 是文件夹
                    mCurPathStack.push("/" + file.getName());

                    // 根据路径刷新数据
                    refreshData(getCurPath());
                } else {
                    // 是文件
                    mSelectCount = mFMAdapter.refreshSelect(position);
                    getSupportActionBar()
                            .setTitle(String.format(getResources()
                                    .getString(R.string.selected_str), mSelectCount));

                    // 将选中的文件加入文件路径数组
                    if (!mSelectPath.contains(file.getAbsolutePath())) {
                        mSelectPath.add(file.getAbsolutePath());
                    } else {
                        mSelectPath.remove(file.getAbsolutePath());
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        mRvContent = (RecyclerView) findViewById(R.id.rv_content);
        mTvPath = (TextView) findViewById(R.id.tv_path);

        mRvContent.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL));
    }

    /**
     * 初始化系统UI
     */
    private void initSysUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        actionBar.setTitle(String.format(getResources().getString(
                R.string.selected_str), mSelectCount));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                android.support.v7.app.AlertDialog.Builder confirmBuilder
                        = new android.support.v7.app.AlertDialog.Builder(FMActivity.this);

                confirmBuilder.setIcon(R.drawable.ic_info);
                String[] strConfirmPath = mSelectPath.toArray(new String[0]);
                if (mSelectPath.size() != 0) {
                    confirmBuilder.setTitle(R.string.con_file_selected);
                    confirmBuilder.setItems(strConfirmPath, null);
                    confirmBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 清空重选
                            mSelectPath.clear();
                            mSelectCount = mFMAdapter.refreshSelect(-1);
                            dialog.dismiss();

                            // 显示提示信息
                            getSupportActionBar().setTitle(
                                    String.format(getResources().getString(
                                            R.string.selected_str), mSelectCount));
                        }
                    });
                    confirmBuilder.setPositiveButton(R.string.yes, null);
                } else {
                    confirmBuilder.setTitle(R.string.select_empty);
                    confirmBuilder.setPositiveButton(R.string.confirm, null);
                }

                confirmBuilder.setCancelable(false);
                AlertDialog confirmDialog = confirmBuilder.create();
                confirmDialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if ((mCurPathStack.peek()).equals(mSdcardPath)) {
                    // 已经到达根目录, 两秒内连续点击则退出
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastBackPressed < 2000) {
                        super.onBackPressed();
                    } else {
                        Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    }
                    lastBackPressed = currentTime;
                } else {
                    // 未到根目录则返回上一层
                    mCurPathStack.pop();
                    refreshData(getCurPath());
                }
                break;
            case R.id.all:
                if (mAll % 2 == 0) {
                    mSelectPath.clear();
                    mSelectCount = 0;
                    String curPath = getCurPath();
                    File[] files = new File(curPath).listFiles();
                    for (File f : files) {
                        if (f.isFile()) {
                            mSelectPath.add(f.getAbsolutePath());
                            mSelectCount++;
                        }
                    }
                    mFMAdapter.refreshSelect(-2);
                    getSupportActionBar().setTitle(
                            String.format(getResources().getString(
                                    R.string.selected_str), mSelectCount));
                    mAll++;
                } else {
                    mSelectPath.clear();
                    mSelectCount = 0;
                    mFMAdapter.refreshSelect(-1);
                    getSupportActionBar().setTitle(
                            String.format(getResources().getString(
                                    R.string.selected_str), mSelectCount));
                    mAll++;
                }
                break;
        }
        return true;
    }

    /**
     * 刷新数据
     *
     * @param path 当前路径
     */
    private void refreshData(String path) {
        mTvPath.setText(path);
        mCurFileList.clear();

        File[] files = new File(path).listFiles();
        for (File f : files) {
            mCurFileList.add(f);
        }

        mFMAdapter.refreshData(mCurFileList);
    }

    /**
     * @return 当前路径
     */
    private String getCurPath() {
        Stack<String> tmpStack = new Stack<>();
        tmpStack.addAll(mCurPathStack);

        String curPath = "";
        while (tmpStack.size() != 0) {
            curPath = tmpStack.pop() + curPath;
        }
        return curPath;
    }

    /**
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 授予结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LogUtil.i("成功获取权限");
                    // 隐藏虚拟按键栏
//                    new NavigationBarUtil(this).detectNavigationBar();
                } else {
                    Toast.makeText(this, "拒绝权限, 将无法使用程序.", Toast.LENGTH_LONG).show();
                    finish();
                    // TODO: 24/04/2018 帮助用户打开
                }
                break;
            default:
        }
    }
}
