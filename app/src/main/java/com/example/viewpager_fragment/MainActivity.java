package com.example.viewpager_fragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        BLESPPUtils.OnBluetoothAction {
    //ViewPager+Fragmnt 布局
    private TextView item_msg, item_set, item_more;
    private ViewPager vp;
    private MsgFragment msgFragment;
    private SetFragment setFragment;
    private MoreFragment moreFragment;
    private List<Fragment> mFragmentList = new ArrayList<Fragment>(3);
    private FragmentAdapter mFragmentAdapter;
    String titles[] = new String[]{"实时状态", "参数设置", "更多"};
    //蓝牙工具类实例
    private BLESPPUtils mBLESPPUtils;
    //保存搜索到的设备
    private List<BluetoothDevice> mDevicesList = new ArrayList<>();
    //接受信息结束标识
    String stopString = "\r\n";
    //设备搜索对话框
    private DeviceDialogCtrl mDeviceDialogCtrl;
    //数据视图
    private TextView totalV, I, SOC, Battery, T1, T2, T3, T4;
    private TextView SV1, SV2, SV3, SV4, SV5, SV6, SV7, SV8;
    private TextView SV9, SV10, SV11, SV12, SV13, SV14, SV15, SV16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除工具栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_main);
        //初始化主视图
        initViews();
        // 绑定数据视图
        bindView();

        mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), 0, mFragmentList);
        vp.setOffscreenPageLimit(3);//页面缓存最多3帧
        vp.setAdapter(mFragmentAdapter);
        vp.setCurrentItem(0);//初始设置ViewPager选中第一帧
        item_msg.setTextColor(Color.parseColor("#5FD348"));

        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeTextColor(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
               /*此方法是在状态改变的时候调用，其中arg0这个参数有三种状态（0，1，2）。
                arg0 ==1的时辰默示正在滑动，
                arg0==2的时辰默示滑动完毕了，
                arg0==0的时辰默示什么都没做。*/
            }
        });
        //申请定位权限
        initPermissions();
        postShowToast("请手动将位置服务打开");
        // 初始化蓝牙工具
        mBLESPPUtils = new BLESPPUtils(this, this);
        // 启用日志输出
        mBLESPPUtils.setEnableLogOut();
        // 启用蓝牙
        mBLESPPUtils.enableBluetooth();
        // 设置接收停止标志位字符串
        mBLESPPUtils.setStopString("\r\n");
        // 用户没有开启蓝牙的话打开蓝牙
        if (!mBLESPPUtils.isBluetoothEnable()) mBLESPPUtils.enableBluetooth();
        // 启动工具类
        mBLESPPUtils.onCreate();
        //初始化设备搜索对话框
        mDeviceDialogCtrl = new DeviceDialogCtrl(this);
        mDeviceDialogCtrl.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.item_msg:
                vp.setCurrentItem(0, true);
                break;
            case R.id.item_set:
                vp.setCurrentItem(1, true);
                break;
            case R.id.item_more:
                vp.setCurrentItem(2, true);
                break;
            default:
                break;
        }
    }

    private void initViews() {
        item_msg = findViewById(R.id.item_msg);
        item_set = findViewById(R.id.item_set);
        item_more = findViewById(R.id.item_more);

        item_msg.setOnClickListener(this);
        item_set.setOnClickListener(this);
        item_more.setOnClickListener(this);

        vp = findViewById(R.id.mainViewPager);
        msgFragment = new MsgFragment();
        setFragment = new SetFragment();
        moreFragment = new MoreFragment();
        //给FragmentList添加数据
        mFragmentList.add(msgFragment);
        mFragmentList.add(setFragment);
        mFragmentList.add(moreFragment);
    }

    private void initPermissions() {
        if (ContextCompat.checkSelfPermission(
                this, "android.permission-group.LOCATION") != 0) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            "android.permission.ACCESS_FINE_LOCATION",
                            "android.permission.ACCESS_COARSE_LOCATION",
                            "android.permission.ACCESS_WIFI_STATE"},
                    1
            );
        }
    }

    private void bindView() {
        TextView Battery = (TextView) findViewById(R.id.Battery);
        TextView totalV = (TextView) findViewById(R.id.totalV);
        TextView I = (TextView) findViewById(R.id.I);
        TextView SOC = (TextView) findViewById(R.id.SOC);
        TextView T1 = (TextView) findViewById(R.id.T1);
        TextView T2 = (TextView) findViewById(R.id.T2);
        TextView T3 = (TextView) findViewById(R.id.T3);
        TextView T4 = (TextView) findViewById(R.id.T4);
        TextView SV1 = (TextView) findViewById(R.id.SV1);
        TextView SV2 = (TextView) findViewById(R.id.SV2);
        TextView SV3 = (TextView) findViewById(R.id.SV3);
        TextView SV4 = (TextView) findViewById(R.id.SV4);
        TextView SV5 = (TextView) findViewById(R.id.SV5);
        TextView SV6 = (TextView) findViewById(R.id.SV6);
        TextView SV7 = (TextView) findViewById(R.id.SV7);
        TextView SV8 = (TextView) findViewById(R.id.SV8);
        TextView SV9 = (TextView) findViewById(R.id.SV9);
        TextView SV10 = (TextView) findViewById(R.id.SV10);
        TextView SV11 = (TextView) findViewById(R.id.SV11);
        TextView SV12 = (TextView) findViewById(R.id.SV12);
        TextView SV13 = (TextView) findViewById(R.id.SV13);
        TextView SV14 = (TextView) findViewById(R.id.SV14);
        TextView SV15 = (TextView) findViewById(R.id.SV15);
        TextView SV16 = (TextView) findViewById(R.id.SV16);
    }

    @Override
    public void onFoundDevice(BluetoothDevice device) {
        postShowToast("发现设备 " + device.getName());
        //判断是不是重复的
        for (int i = 0; i < mDevicesList.size(); i++) {
            if (mDevicesList.get(i).getAddress().equals(device.getAddress())) return;
        }
        //添加蓝牙设备
        mDevicesList.add(device);
        //添加标签到UI并设置点击事件
        mDeviceDialogCtrl.addDevice(device, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothDevice clickDecvice = (BluetoothDevice) view.getTag();
                postShowToast("开始连接设备" + clickDecvice.getAddress());
                mBLESPPUtils.connect(clickDecvice);
            }
        });
    }

    @Override
    public void onConnectSuccess(BluetoothDevice device) {
        postShowToast("连接成功", new DoSthAfterPost() {
            @Override
            public void doIt() {
                mDeviceDialogCtrl.dismiss();
            }
        });
    }

    @Override
    public void onConnectFailed(String msg) {
        postShowToast(msg);
    }

    @Override
    public void onReceiveBytes(byte[] bytes) {
        receiveMyMsg(bytes);
    }

    @Override
    public void onSendBytes(byte[] bytes) {
        postShowToast("发送数据:" + new String(bytes), new DoSthAfterPost() {
            @SuppressLint("SetTextI18n")
            @Override
            public void doIt() {
            }
        });
    }

    @Override
    public void onFinishFoundDevice() {
        postShowToast("结束搜索");
    }

    public class FragmentAdapter extends FragmentPagerAdapter {

        List<Fragment> fragmentList = new ArrayList<>();

        public FragmentAdapter(@NonNull FragmentManager fm, int behavior, List<Fragment> fragmentList) {
            super(fm, behavior);
            this.fragmentList = fragmentList;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }

    /*
     *由ViewPager的滑动修改底部导航Text的颜色
     */
    private void changeTextColor(int position) {
        if (position == 0) {
            item_msg.setTextColor(Color.parseColor("#5FD348"));
            item_set.setTextColor(Color.parseColor("#000000"));
            item_more.setTextColor(Color.parseColor("#000000"));
        } else if (position == 1) {
            item_msg.setTextColor(Color.parseColor("#000000"));
            item_set.setTextColor(Color.parseColor("#5FD348"));
            item_more.setTextColor(Color.parseColor("#000000"));
        } else if (position == 2) {
            item_msg.setTextColor(Color.parseColor("#000000"));
            item_set.setTextColor(Color.parseColor("#000000"));
            item_more.setTextColor(Color.parseColor("#5FD348"));
        }
    }

    /*设备搜索对话框控制*/
    private class DeviceDialogCtrl {
        private LinearLayout mDialogRootView;
        private ProgressBar mProgressBar;
        private AlertDialog mDeviceConnectDialog;

        DeviceDialogCtrl(Context context) {
            //搜索进度条
            mProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
            mProgressBar.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            50
                    )
            );
            //根布局
            mDialogRootView = new LinearLayout(context);
            mDialogRootView.setMinimumHeight(700);
            mDialogRootView.setOrientation(LinearLayout.VERTICAL);
            mDialogRootView.addView(mProgressBar);
            //容器布局 滚动
            ScrollView scrollView = new ScrollView(context);
            scrollView.addView(mDialogRootView,
                    new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            700
                    )
            );
            //构建对话框
            mDeviceConnectDialog = new AlertDialog
                    .Builder(context)
                    .setNegativeButton("刷新", null)
                    .setPositiveButton("退出", null)
                    .create();
            mDeviceConnectDialog.setTitle("选择蓝牙设备");
            mDeviceConnectDialog.setView(scrollView);
            mDeviceConnectDialog.setCancelable(false);
        }

        /*显示对话框并开始搜索设备*/
        void show() {
            postShowToast("开始搜索设备");
            mBLESPPUtils.startDiscovery();
            mDeviceConnectDialog.show();
            mDeviceConnectDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnLongClickListener(
                    new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            mDeviceConnectDialog.dismiss();
                            return false;
                        }
                    }
            );
            mDeviceConnectDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mDeviceConnectDialog.dismiss();
                            finish();
                        }
                    }
            );
            mDeviceConnectDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mDialogRootView.removeAllViews();
                            mDialogRootView.addView(mProgressBar);
                            mDevicesList.clear();
                            mBLESPPUtils.startDiscovery();
                            postShowToast("刷新搜索");
                        }
                    }
            );
        }

        /*取消对话框*/
        void dismiss() {
            mDeviceConnectDialog.dismiss();
        }

        /**
         * 添加一个设备到列表
         *
         * @param device          设备
         * @param onClickListener 点击回调
         */
        private void addDevice(final BluetoothDevice device, final View.OnClickListener onClickListener) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView devTag = new TextView(MainActivity.this);
                    devTag.setClickable(true);
                    devTag.setPadding(20, 20, 20, 20);
                    devTag.setBackgroundResource(R.drawable.rect_round_button_ripple);
                    devTag.setText(device.getName() + "\nMAC:" + device.getAddress());
                    devTag.setTextColor(Color.WHITE);
                    devTag.setLayoutParams(
                            new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                    );
                    ((LinearLayout.LayoutParams) devTag.getLayoutParams()).setMargins(
                            20, 20, 20, 20
                    );
                    devTag.setTag(device);
                    devTag.setOnClickListener(onClickListener);
                    mDialogRootView.addView(devTag);
                }
            });
        }
    }

    /**
     * 在主线程弹出 Toast
     *
     * @param msg 信息
     */
    private void postShowToast(final String msg) {
        postShowToast(msg, null);
    }

    /**
     * 在主线程弹出 Toast
     *
     * @param msg            信息
     * @param doSthAfterPost 在弹出后做点什么
     */
    private void postShowToast(final String msg, final DoSthAfterPost doSthAfterPost) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                if (doSthAfterPost != null) doSthAfterPost.doIt();
            }
        });
    }

    private interface DoSthAfterPost {
        void doIt();
    }

    public static String byte2Hex(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String hash = formatter.toString();
        formatter.close();
        return hash;
    }

    public String convertMyMsg(Byte dataH, Byte dataL, int Decimal, Boolean isUnsigned) {
        float resultF = 0;
        String resultStr, returnStr = null;
        int HH = dataH & 0xff;
        int LL = dataL & 0xff;
        int result = HH * 256 + LL;
        short resultShort = (short) result;
        if (isUnsigned) {
            if (Decimal == 0) {
                returnStr = String.valueOf(result);
            } else if (Decimal == 1) {
                resultF = result * 0.1f;
                returnStr = String.format("%.1f", resultF);
            } else if (Decimal == 2) {
                resultF = result * 0.01f;
                returnStr = String.format("%.2f", resultF);
            }
        } else {
            if (Decimal == 0) {
                returnStr = String.valueOf(resultShort);
            } else if (Decimal == 1) {
                resultF = resultShort * 0.1f;
                returnStr = String.format("%.1f", resultF);
            } else if (Decimal == 2) {
                resultF = resultShort * 0.01f;
                returnStr = String.format("%.2f", resultF);
            }
        }
        return returnStr;
    }

    private void receiveMyMsg(byte[] bytes) {
        // 绑定数据视图
        TextView Battery = (TextView) findViewById(R.id.Battery);
        TextView totalV = (TextView) findViewById(R.id.totalV);
        TextView I = (TextView) findViewById(R.id.I);
        TextView SOC = (TextView) findViewById(R.id.SOC);
        TextView T1 = (TextView) findViewById(R.id.T1);
        TextView T2 = (TextView) findViewById(R.id.T2);
        TextView T3 = (TextView) findViewById(R.id.T3);
        TextView T4 = (TextView) findViewById(R.id.T4);
        TextView SV1 = (TextView) findViewById(R.id.SV1);
        TextView SV2 = (TextView) findViewById(R.id.SV2);
        TextView SV3 = (TextView) findViewById(R.id.SV3);
        TextView SV4 = (TextView) findViewById(R.id.SV4);
        TextView SV5 = (TextView) findViewById(R.id.SV5);
        TextView SV6 = (TextView) findViewById(R.id.SV6);
        TextView SV7 = (TextView) findViewById(R.id.SV7);
        TextView SV8 = (TextView) findViewById(R.id.SV8);
        TextView SV9 = (TextView) findViewById(R.id.SV9);
        TextView SV10 = (TextView) findViewById(R.id.SV10);
        TextView SV11 = (TextView) findViewById(R.id.SV11);
        TextView SV12 = (TextView) findViewById(R.id.SV12);
        TextView SV13 = (TextView) findViewById(R.id.SV13);
        TextView SV14 = (TextView) findViewById(R.id.SV14);
        TextView SV15 = (TextView) findViewById(R.id.SV15);
        TextView SV16 = (TextView) findViewById(R.id.SV16);

        int xorCnt = 0;
        for (int i = 0; i < bytes.length - 3; i++) xorCnt ^= bytes[i];
        if ((bytes[0] == 0x01) && (bytes[17] == xorCnt)) {
            Battery.setText(convertMyMsg(bytes[1], bytes[2], 0, true));
            totalV.setText(convertMyMsg(bytes[3], bytes[4], 1, true));
            I.setText(convertMyMsg(bytes[5], bytes[6], 1, false));
            SOC.setText(convertMyMsg(bytes[7], bytes[8], 1, true));
            T1.setText(convertMyMsg(bytes[9], bytes[10], 1, true));
            T2.setText(convertMyMsg(bytes[11], bytes[12], 1, true));
            T3.setText(convertMyMsg(bytes[13], bytes[14], 1, true));
            T4.setText(convertMyMsg(bytes[15], bytes[16], 1, true));
        }

        if ((bytes[0] == 0x03) && (bytes[17] == xorCnt)) {
            SV1.setText(convertMyMsg(bytes[1], bytes[2], 2, true));
            SV2.setText(convertMyMsg(bytes[3], bytes[4], 2, true));
            SV3.setText(convertMyMsg(bytes[5], bytes[6], 2, true));
            SV4.setText(convertMyMsg(bytes[7], bytes[8], 2, true));
            SV5.setText(convertMyMsg(bytes[9], bytes[10], 2, true));
            SV6.setText(convertMyMsg(bytes[11], bytes[12], 2, true));
            SV7.setText(convertMyMsg(bytes[13], bytes[14], 2, true));
            SV8.setText(convertMyMsg(bytes[15], bytes[16], 2, true));
        }

        if ((bytes[0] == 0x04) && (bytes[17] == xorCnt)) {
            SV9.setText(convertMyMsg(bytes[1], bytes[2], 2, true));
            SV10.setText(convertMyMsg(bytes[3], bytes[4], 2, true));
            SV11.setText(convertMyMsg(bytes[5], bytes[6], 2, true));
            SV12.setText(convertMyMsg(bytes[7], bytes[8], 2, true));
            SV13.setText(convertMyMsg(bytes[9], bytes[10], 2, true));
            SV14.setText(convertMyMsg(bytes[11], bytes[12], 2, true));
            SV15.setText(convertMyMsg(bytes[13], bytes[14], 2, true));
            SV16.setText(convertMyMsg(bytes[15], bytes[16], 2, true));
        }
    }
}