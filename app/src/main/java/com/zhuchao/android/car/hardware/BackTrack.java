package com.zhuchao.android.car.hardware;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

// move C to java, by allen
public class BackTrack {
    /*
     * static { System.loadLibrary("libBacktrack"); }
     *
     *
     * private native final void set_track_param(double a, double h, double b,
     * double l, double w, double d); private native final void do_track(double
     * angle, byte[] left, byte[] right);
     */

    // 推导出锯齿
    public final static int SAWTOOTH_NUM = 7;
    // #define TRACK_DEBG
    public final static int SAWTOOTH_INTERVERL = 80;
    public final static int SAWTOOTH_INTERVERL2 = 140;
    static final double PI = 3.1415926;
    /*

     */
    private final static String TAG = "BackTrack";
    /*
     */
    private final static int DEFAULT_CAMERA_H = 800;
    private static final String SAVE_DATA = "BackTrack";
    private static final String SAVE_DATA_TIME = "time";
    private static final String SAVE_FIRST_BOOT = "first_boot";
    private static final String SAVE_DATA_FIRST_SYSTEM_BOOT = "first_system_boot";
    public static int SAWTOOTH_STEP1 = 1400;
    public static int SAWTOOTH_STEP2 = 2800;
    public static int SAWTOOTH_STEP3 = 5800;
    static double angle_2a = (double) 120 / 2; // 摄像头可视角度2a,
    /*
     */

    //	static int screen_w_px = 800; // 屏幕输出的分辨率
    //	static int screen_h_px = 480; // 屏幕输出的分辨率
    static int camera_h = 800; // 摄像头距离地面距离h

    //static double angle_c = -280; // 前轮同水平方向的夹角
    static double angle_b = 25; // 摄像头中心线同水平面的夹角β
    static int screen_w = 1024; // 屏幕输出的宽度
    public static int show_l_num = screen_w;
    public static int show_r_num = screen_w;
    static int screen_h = 600; // 屏幕输出的高度
    static int car_l = 2600; // 汽车前后轮轴距L
    static int car_w = 1800; // 汽车轴长W，
    static int vision_back = 6000;// 轨迹可视最远距离, 太远的距离点计算进来的话,会导航轨迹成像不真实.
    static int show_w = screen_w / 2; // 显示轨迹的宽度
    static int show_h = screen_h; // 显示轨迹的高度
    static int caculate_x = car_w / 2; // 计算x点数
    // static float ratio = show_w/screen_w ; //显示比例

    //	private CPoint xy_l_real[];
    //	private CPoint xy_l_shadow[];
    //	public CPoint xy_l_screen[];
    //
    //	private CPoint xy_r_real[];
    //	private CPoint xy_r_shadow[];
    //	public CPoint xy_r_screen[];
    static double no_use_h;// = camera_h/Math.tan(radian_a+radian_b); //real y <
    // no_use_h is盲区
    static double no_use_h_shadow;
    static double angle_a = angle_2a / 2;
    static double radian_a = ANGLE_TO_RADIAN(angle_a);
    static double radian_b = ANGLE_TO_RADIAN(angle_b);
    static double screen_ratio_w = 1;// screen_w_px/screen_w;
    static double screen_ratio_h = 1;// screen_h_px/screen_h;
    static double static_tan_a = 0;
    private static int ext_y_move = 0;
    private final int[] sawtooth_steps = new int[]{SAWTOOTH_STEP1 - SAWTOOTH_INTERVERL, SAWTOOTH_STEP1, SAWTOOTH_STEP1 + SAWTOOTH_INTERVERL, SAWTOOTH_STEP2 - SAWTOOTH_INTERVERL2, SAWTOOTH_STEP2, SAWTOOTH_STEP2 + SAWTOOTH_INTERVERL2, SAWTOOTH_STEP3};
    private final double[] sawtooth_tan = new double[SAWTOOTH_NUM];
    public double[] xy_l_screen_x;
    public double[] xy_l_screen_y;
    public double[] xy_r_screen_x;
    public double[] xy_r_screen_y;
    public int[] sawtooth_l_point = new int[SAWTOOTH_NUM];
    public int[] sawtooth_r_point = new int[SAWTOOTH_NUM];
    double camera_k_x = 0.0000012;//畸变系数
    double camera_k_y = camera_k_x;//0.0000008;//畸变系数
    private int car_d = 500; // 后轮距离车尾的距离D
    private double[] xy_l_real_x;
    private double[] xy_l_real_y;
    private double[] xy_l_shadow_x;

    /*
     *
     * public
     * */
    private double[] xy_l_shadow_y;
    private double[] xy_r_real_x;
    private double[] xy_r_real_y;
    private double[] xy_r_shadow_x;
    private double[] xy_r_shadow_y;

    // #define ANGLE_TO_RADIAN(angle) (((angle)*PI)/180.0)
    public static double ANGLE_TO_RADIAN(double angle) {
        return (((angle) * PI) / 180.0);
    }

    public static int get_track_car_w() {
        return car_w;
    }

    public static void set_track_car_w(int w) {
        if (car_w > 2000) return;
        car_w = w;
    }

    public static int get_track_car_l() {
        return car_l;
    }

    public static void set_track_car_l(int w) {
        car_l = w;
    }

    //	public static int get_track_car_d()
    //	{
    //		return car_d;
    //	}
    public static int get_track_camera_h() {
        return camera_h;
    }

    public static void set_track_camera_h(int w) {
        camera_h = w;
        int v = DEFAULT_CAMERA_H - w;
        if (v < 0) {
            ext_y_move = (v / 10) * 4;
        } else {
            ext_y_move = (v / 10) * 6;
        }
    }

    public static int get_ext_y_move() {
        return ext_y_move;
    }

    //	public static void set_track_car_d(int w)
    //	{
    //		car_d = w;
    //	}
    public static void set_ext_y_move(int y) {
        ext_y_move = y;
    }

    public static double get_track_angle_2a() {
        return angle_2a;
    }

    public static void set_track_angle_2a(double d) {
        angle_2a = d;
        angle_a = angle_2a / 2;
        radian_a = ANGLE_TO_RADIAN(angle_a);
    }

    /*
     * for c -> java
     */

    public static double get_track_angle_b() {
        return angle_b;
    }

    public static void set_track_angle_b(double d) {
        angle_b = d;
        radian_b = ANGLE_TO_RADIAN(angle_b);
    }


    //以下这些计算是根据具体的UI效果而处理的,不属于通用轨迹算法

    public static int get_screen_w() {
        return screen_w;
    }

    public static void set_screen_w(int w) {
        screen_w = w;
    }

    private static void memset(double[] pIn, int data) {
        for (double p : pIn) {
            p = data;
        }
    }

    public static double getScreenH() {
        return screen_h;
    }

    public static void reset() {
        // angle_2a = 120 / 2; // 摄像头可视角度2a,
        camera_h = 800; // 摄像头距离地面距离h
        // angle_b = 25; // 摄像头中心线同水平面的夹角β
        screen_w = 1024; // 屏幕输出的宽度
        // screen_h = 600; // 屏幕输出的高度
        // car_l = 2600; // 汽车前后轮轴距L
        car_w = 1800; // 汽车轴长W，
        ext_y_move = 0;
    }

    public static void reloadConfig(Context c) {
        if (c != null) {
            long l;

            l = getData(c, "car_w");
            if (l != 0) {
                car_w = (int) l;
            }
            l = getData(c, "screen_w");
            if (l != 0) {
                screen_w = (int) l;
            }
            l = getData(c, "camera_h");
            if (l != 0) {
                camera_h = (int) l;
            }
            l = getData(c, "ext_y_move");
            //			if (l != 0) {
            ext_y_move = (int) l;
            //			}
        }
    }

    public static void saveConfig(Context c) {
        if (c != null) {
            saveData(c, "car_w", car_w);
            saveData(c, "screen_w", screen_w);
            saveData(c, "camera_h", camera_h);
            saveData(c, "ext_y_move", ext_y_move);
        }
    }

    private static void saveData(Context c, String s, long v) {
        SharedPreferences.Editor sharedata = c.getSharedPreferences(SAVE_DATA, 0).edit();

        sharedata.putLong(s, v);
        sharedata.commit();
    }

    private static long getData(Context c, String s) {
        SharedPreferences sharedata = c.getSharedPreferences(SAVE_DATA, 0);
        return sharedata.getLong(s, 0);
    }

    public void init() {
        int w = screen_w, h = screen_h;
        //		try {
        //
        //			String s = MachineConfig.getProperty(MachineConfig.KEY_SCREEN_W);
        //			w = Integer.parseInt(s);
        //			s = MachineConfig.getProperty(MachineConfig.KEY_SCREEN_H);
        //			h = Integer.parseInt(s);
        //
        //		} catch (Exception e) {
        //			w = 1024;
        //			h = 600;
        //		}

        init(w, h);
    }

    public void init(int width, int height) {
        screen_w = width;
        screen_h = height;

        show_w = screen_w / 2; // 显示轨迹的宽度
        show_h = screen_h; // 显示轨迹的高度

        show_l_num = screen_w;
        show_r_num = screen_w;

        no_use_h = camera_h / Math.tan(radian_a + radian_b); // real y <
        // no_use_h is盲区

        double h = (((double) show_w / 2) / Math.tan(radian_a));
        car_d -= h;

        xy_l_real_x = new double[show_l_num];
        xy_l_real_y = new double[show_l_num];

        xy_l_shadow_x = new double[show_l_num];
        xy_l_shadow_y = new double[show_l_num];

        xy_l_screen_x = new double[show_l_num];
        xy_l_screen_y = new double[show_l_num];


        xy_r_real_x = new double[show_l_num];
        xy_r_real_y = new double[show_l_num];

        xy_r_shadow_x = new double[show_l_num];
        xy_r_shadow_y = new double[show_l_num];

        xy_r_screen_x = new double[show_l_num];
        xy_r_screen_y = new double[show_l_num];

    }

    private void init_deult_x(double angle) {
        double step = (double) car_w * 2 / show_l_num;
        int i;

        memset(xy_l_real_x, 0);
        memset(xy_r_real_x, 0);
        memset(xy_l_shadow_x, 0);
        memset(xy_r_shadow_x, 0);
        memset(xy_l_screen_x, 0);
        memset(xy_r_screen_x, 0);

        memset(xy_l_real_y, 0);
        memset(xy_r_real_y, 0);
        memset(xy_l_shadow_y, 0);
        memset(xy_r_shadow_y, 0);
        memset(xy_l_screen_y, 0);
        memset(xy_r_screen_y, 0);

        double step_small;
        double step_small_num;

        if (angle < 10) {
            step_small = 0.04;
            step_small_num = (double) show_l_num / 4;
        } else {
            step_small = 0.1;
            step_small_num = (double) show_l_num / 40;
        }

        if (angle > 0) // 右转
        {

            for (i = 0; i < show_l_num; i++) {
                if (i < step_small_num) {
                    xy_l_real_x[i] = ((double) car_w / 2) - (i * step_small);
                } else {
                    xy_l_real_x[i] = ((double) car_w / 2) - (step_small_num * step_small) - ((i - step_small_num) * step);

                }
            }

            for (i = 0; i < show_l_num; i++) {
                if (i < step_small_num) {
                    xy_r_real_x[i] = 0 - i * step_small - ((double) car_w / 2);
                } else {
                    xy_r_real_x[i] = 0 - ((step_small_num * step_small) + ((i - step_small_num) * step)) - ((double) car_w / 2);
                }

            }
        } else if (angle == 0) {
            step_small = (double) SAWTOOTH_STEP3 / (show_l_num - 1);
            for (i = 0; i < show_l_num; i++) {
                xy_l_real_x[i] = ((double) car_w / 2);
                xy_r_real_x[i] = -((double) car_w / 2);
                xy_l_real_y[i] = xy_r_real_y[i] = 0 + step_small * i;
            }

        }
    }

    // 推导出显示的实际轨迹图像
    // 连锯齿一起计算
    private void caculate_real_point(double angle) {
        double X, Y, Z, R;
        int i;

        angle = Math.abs(angle);

        init_deult_x(angle);

        int sawtooth_l = 0;
        int sawtooth_r = 0;

        double tan_d, tan_d2;
        if (angle > 0) // 右转
        {
            R = (car_l / Math.tan(ANGLE_TO_RADIAN(angle)));
            Z = R + (car_w / 2);

            tan_d = (car_d + sawtooth_steps[sawtooth_l]) / R;

            for (i = 0; i < show_l_num; i++) {
                X = xy_l_real_x[i] + Z - (car_w / 2);
                Y = Math.sqrt(((Z * Z) - (X * X)));
                xy_l_real_y[i] = (Y - car_d);


                if (/* xy_l_real[i].y <= no_use_h || */xy_l_real_y[i] > vision_back) {
                    xy_l_real_y[i] = -1;
                } else {
                    tan_d2 = (car_d + xy_l_real_y[i]) / (xy_l_real_x[i] + R);

                    if (sawtooth_l < SAWTOOTH_NUM && tan_d2 >= tan_d) {
                        // Log.e(TAG, xy_l_real_x[i] + "  x:"+
                        // get_real_x_from_y(xy_l_real_y[i], Z, angle));

                        // xy_l_real_y[i] = sawtooth_steps[sawtooth_l];
                        // xy_l_real_x[i] = get_real_l_x_from_y(xy_l_real_y[i],
                        // Z);

                        if (tan_d2 > tan_d) {
                            //							Log.e(TAG, "  tan_d:" + tan_d + "  tan_d2:"
                            //									+ tan_d2 + " xy_l_real_x[" + i + "]:"
                            //									+ xy_l_real_x[i]);
                            for (int j = 0; j < 100; j++) {

                                xy_l_real_x[i] += 0.1;

                                X = xy_l_real_x[i] + Z - (car_w / 2);
                                Y = Math.sqrt(((Z * Z) - (X * X)));
                                xy_l_real_y[i] = (Y - car_d);

                                tan_d2 = (car_d + xy_l_real_y[i]) / (xy_l_real_x[i] + R);

                                if (tan_d2 <= tan_d) break;
                            }
                        }

                        //						Log.e(TAG, "22  tan_d:" + tan_d + "  tan_d2:" + tan_d2
                        //								+ " xy_l_real_x[" + i + "]:" + xy_l_real_x[i]);
                        sawtooth_l_point[sawtooth_l] = i;
                        sawtooth_l++;
                        if (sawtooth_l < SAWTOOTH_NUM) {
                            tan_d = (car_d + sawtooth_steps[sawtooth_l]) / R;
                        }

                    }
                }

            }

            Z = R - (car_w / 2);
            tan_d = (car_d + sawtooth_steps[sawtooth_r]) / R;

            for (i = 0; i < show_r_num; i++) {
                X = xy_r_real_x[i] + Z + (car_w / 2);
                Y = Math.sqrt(((Z * Z) - (X * X)));
                xy_r_real_y[i] = (Y - car_d);
                if (/* xy_r_real[i].y <= no_use_h || */xy_r_real_y[i] > vision_back) {
                    xy_r_real_y[i] = -1;
                } else {
                    tan_d2 = (car_d + xy_r_real_y[i]) / (xy_r_real_x[i] + R);

                    if (sawtooth_r < SAWTOOTH_NUM && tan_d2 >= tan_d) {

                        if (tan_d2 > tan_d) {
                            //	Log.e(TAG, "  tan_d:" + tan_d + "  tan_d2:"
                            //			+ tan_d2 + " xy_r_real_x[" + i + "]:"
                            //			+ xy_r_real_x[i]);
                            for (int j = 0; j < 100; j++) {

                                xy_r_real_x[i] += 0.1;

                                X = xy_r_real_x[i] + Z + (car_w / 2);
                                Y = Math.sqrt(((Z * Z) - (X * X)));
                                xy_r_real_y[i] = (Y - car_d);

                                tan_d2 = (car_d + xy_r_real_y[i]) / (xy_r_real_x[i] + R);

                                if (tan_d2 <= tan_d) break;
                            }
                        }

                        //	Log.e(TAG, "22  tan_d:" + tan_d + "  tan_d2:" + tan_d2
                        //			+ " xy_r_real_x[" + i + "]:" + xy_r_real_x[i]);
                        sawtooth_r_point[sawtooth_r] = i;
                        sawtooth_r++;
                        if (sawtooth_r < SAWTOOTH_NUM) {
                            tan_d = (car_d + sawtooth_steps[sawtooth_r]) / R;
                        }

                    }
                }
            }

        } else if (angle == 0) //
        {

        }

    }

    private double get_real_r_x_from_y(double y, double Z) {
        double x;

        y += car_d;
        x = Math.sqrt(((Z * Z) - (y * y)));
        x = x - Z - (car_w / 2);

        return x;
    }

    private double get_real_l_x_from_y(double y, double Z) {
        double x;

        y += car_d;
        x = Math.sqrt(((Z * Z) - (y * y)));
        x = x - Z + (car_w / 2);

        return x;
    }

    // 计算出显示屏幕上的坐标投影
    private void caculate_virtual_point(double angle) {
        double Yr, Xr, line_side, h;
        int i;
        double radian_c, radian_c1, radian_c2;

        double p_x, p_y;
        double temp_x, temp_y;
        h = ((show_w / 2) / Math.tan(radian_a));

        int no_use;
        int caculate_ext;
        no_use = 0;
        caculate_ext = 0;
        for (i = 0; i < show_l_num; i++) {

            if (xy_l_real_y[i] == -1) {
                xy_l_shadow_y[i] = -1;
                continue;
            }
            // getShadowPoint(angle, xy_l_real+i, &temp);
            p_x = xy_l_real_x[i];
            p_y = xy_l_real_y[i];

            radian_c = Math.atan(camera_h / p_y);

            radian_c1 = radian_a + radian_b - radian_c;
            radian_c2 = ANGLE_TO_RADIAN(90 - angle_a);

            line_side = show_h / 2 / Math.sin(radian_a);

            Yr = line_side * (Math.sin(radian_c1) / Math.sin(radian_c1 + radian_c2));

            Xr = (p_x / (p_y * (Math.tan(radian_a)))) * (show_w / 2);
            /*
             * if (temp.y < 0 || temp.x > screen_w/2) { // no_use++; //
             * continue; }
             *
             * if ( xy_l_shadow[0].x == 0 && xy_l_shadow[0].y == 0 && temp.y >
             * 1)//add caculate, first { int int_y = temp.y; CPoint point_ext;
             *
             * point_ext.y = temp.y; for (int j = 0; j < int_y; ++j) { }
             *
             * }
             */


            if (angle < 0)// do x change
            {
                Xr = -Xr;
            }

            //			if (Xr > screen_w/2)
            //			{
            //				Xr = -1;
            //			}

            /*
             * 增加畸变参数转换
             */

            //转换以Yr为屏幕中心为0的坐标.
            Yr -= screen_h / 2;

            Xr = Xr / ((Xr * Xr + Yr * Yr) * camera_k_x + 1);
            Yr = Yr / ((Xr * Xr + Yr * Yr) * camera_k_y + 1);

            Yr += screen_h / 2;

            //		Log.e(TAG, "Xr:"+Xr+"  Yr:"+Yr + "  Xk:"+Xk +"  Yk:"+Yk);

            xy_l_shadow_x[i] = Xr;
            xy_l_shadow_y[i] = Yr;
        }

        for (i = 0; i < show_r_num; i++) {
            if (xy_r_real_x[i] == -1) {
                xy_r_shadow_y[i] = -1;
                continue;
            }

            p_x = xy_r_real_x[i];
            p_y = xy_r_real_y[i];

            radian_c = Math.atan(camera_h / p_y);

            radian_c1 = radian_a + radian_b - radian_c;
            radian_c2 = ANGLE_TO_RADIAN(90 - angle_a);

            line_side = show_h / 2 / Math.sin(radian_a);
            Yr = line_side * (Math.sin(radian_c1) / Math.sin(radian_c1 + radian_c2));

            Xr = (p_x / (p_y * (Math.tan(radian_a)))) * (show_w / 2);

            if (angle < 0)// do x change
            {
                Xr = -Xr;
            }
            Yr -= screen_h / 2;

            Xr = Xr / ((Xr * Xr + Yr * Yr) * camera_k_x + 1);
            Yr = Yr / ((Xr * Xr + Yr * Yr) * camera_k_y + 1);

            Yr += screen_h / 2;

            xy_r_shadow_x[i] = Xr;
            xy_r_shadow_y[i] = Yr;
        }

        // 计算盲区
        radian_c = Math.atan(camera_h / no_use_h);

        radian_c1 = radian_a + radian_b - radian_c;
        radian_c2 = ANGLE_TO_RADIAN(90 - angle_a);

        line_side = show_h / 2 / Math.sin(radian_a);
        Yr = line_side * (Math.sin(radian_c1) / Math.sin(radian_c1 + radian_c2));

        no_use_h_shadow = line_side * (Math.sin(radian_c1) / Math.sin(radian_c1 + radian_c2));

    }

    // 计算出实际显示屏幕上的坐标
    private void caculate_screen_point(double angle) {
        int move;
        int move_y = 0;
        int i;

        int temp = 0;

        double no_use_h_;
        no_use_h_ = (no_use_h * car_w) / show_l_num;
        if (angle > 0) {
            move = (screen_w) / 2 + temp;

        } else {
            move = (screen_w) / 2 - temp;
        }

        // double ratio = 1.5;
        for (i = 0; i < show_l_num; i++) {
            if (xy_l_shadow_y[i] == -1) {
                xy_l_screen_y[i] = -1;
                continue;
            }

            xy_l_screen_x[i] = xy_l_shadow_x[i] + move;
            xy_l_screen_y[i] = show_h - xy_l_shadow_y[i] - move_y + ext_y_move;

            //xy_l_screen_x[i] *= screen_ratio_w;
            //xy_l_screen_y[i] *= screen_ratio_h;
        }

        if (angle < 0) {
            move = (screen_w) / 2 + temp;
        } else {
            move = (screen_w) / 2 - temp;
        }

        for (i = 0; i < show_r_num; i++) {
            if (xy_r_shadow_y[i] == -1) {
                xy_r_screen_y[i] = -1;
                continue;
            }

            xy_r_screen_x[i] = xy_r_shadow_x[i] + move;
            xy_r_screen_y[i] = show_h - xy_r_shadow_y[i] - move_y + ext_y_move;

            // check if correct data?

            //xy_r_screen_x[i] *= screen_ratio_w;
            //xy_r_screen_y[i] *= screen_ratio_h;

            //if (xy_r_screen[i].y >= show_h) {
            // xy_r_screen[i].x = -1;
            // xy_r_screen[i].y = -1;
            //}
        }
    }

    public void do_drack(double angle) {
        //	if (angle == 0)
        //		return;
        Log.e(TAG, "do_drack:" + angle);
        caculate_real_point(angle);
        caculate_virtual_point(angle);
        caculate_screen_point(angle);
        //	caculte_sawtooth_point(angle);
    }

    public void set_track_param(double a, double h, double b, double l, double w, double d) {
        /*
         * static double angle_2a = 120 / 2; //摄像头可视角度2a,
         * static int camera_h = * 800; //摄像头距离地面距离h
         * static double angle_b = 20; //摄像头中心线同水平面的夹角β static
         * int screen_w = 800; //屏幕输出的宽度
         * static int screen_h = 480; //屏幕输出的高度
         * static int car_l = 2600; //汽车前后轮轴距L
         * static int car_w = 1700; //汽车轴长W，
         * static int car_d = 400; //后轮距离车尾的距离D
         */

    }

    private double double_abs(double d) {
        if (d < 0) return -d;
        return d;
    }

    private void caculte_sawtooth_point(double angle) {
        int i, j;
        double tan_d, tan_d2 = 0, tan_d2_prev;
        double r = car_l / Math.tan(ANGLE_TO_RADIAN(angle));

        r = double_abs(r);
        angle = Math.abs(angle);

        // memset(sawtooth_l_point, 0, sizeof(sawtooth_l_point));
        // memset(sawtooth_r_point, 0, sizeof(sawtooth_r_point));
        //Log.e(TAG, ",");
        ///Log.e(TAG, ",");

        for (j = 0; j < SAWTOOTH_NUM; ++j) {
            tan_d = (car_d + sawtooth_steps[j]) / r; // 找到锯齿点角度
            tan_d2_prev = 0;
            for (i = 0; i < show_l_num; i++) {
                if (xy_l_real_y[i] < 0) {
                    continue;
                }

                tan_d2 = (car_d + xy_l_real_y[i]) / (xy_l_real_x[i] + r);

                if (tan_d2 >= tan_d) // 找到对应角度显示点
                {
                    break;
                }

                tan_d2_prev = tan_d2;
            }

            if (i < show_l_num) {
                if (i > 0) {
                    if (double_abs(tan_d2 - tan_d) > double_abs(tan_d2_prev - tan_d)) {
                        if (j == 0 || (i - 1) != sawtooth_l_point[j - 1]) {
                            --i;
                        }
                    }
                }
                //	Log.e(TAG, "  tan_d:"+tan_d+"  tan_d2:"+tan_d2+"  tan_d2:"+tan_d2_prev + "         i:"+i);
                //	Log.e(TAG, ",");
                sawtooth_l_point[j] = i;
            }

            for (i = 0; i < show_r_num; i++) {
                if (xy_r_real_y[i] < 0) {
                    continue;
                }

                tan_d2 = (car_d + xy_r_real_y[i]) / (xy_r_real_x[i] + r);

                if (tan_d2 >= tan_d) // 找到对应角度显示点
                {
                    break;
                }

                tan_d2_prev = tan_d2;
            }

            if (i < show_l_num) {
                if (i > 0) {
                    if (double_abs(tan_d2 - tan_d) > double_abs(tan_d2_prev - tan_d)) {

                        if (j == 0 || (i - 1) != sawtooth_r_point[j - 1]) {
                            --i;
                        }

                    }
                }
                //Log.e(TAG, "222222  tan_d:"+xy_l_real_x[i]+"  tan_d2:"+xy_l_real_x[(i-1)<0?0:(i-1)]+"  tan_d2:"+tan_d2_prev + "         i:"+i);
                //Log.e(TAG, ",");
                sawtooth_r_point[j] = i;
            }
        }
    }
}
