package com.zhuchao.android.car.cartype;

import com.common.util.MachineConfig;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.bagoo.AlphaBagoo;
import com.zhuchao.android.car.cartype.bagoo.AudiBagoo;
import com.zhuchao.android.car.cartype.bagoo.BenzBagoo;
import com.zhuchao.android.car.cartype.bagoo.CarPSABagoo;
import com.zhuchao.android.car.cartype.bagoo.FiatBagoo;
import com.zhuchao.android.car.cartype.baogu.DaChengE20;
import com.zhuchao.android.car.cartype.baogu.Megane3;
import com.zhuchao.android.car.cartype.binarytek.AccordBinarytek;
import com.zhuchao.android.car.cartype.binarytek.CarOBDBinarytek;
import com.zhuchao.android.car.cartype.binarytek.CarToyotaBinarytek;
import com.zhuchao.android.car.cartype.binarytek.ChangAnBNR;
import com.zhuchao.android.car.cartype.binarytek.ChangChengBNR;
import com.zhuchao.android.car.cartype.binarytek.ChuanQiBNR;
import com.zhuchao.android.car.cartype.binarytek.FordBinarytek;
import com.zhuchao.android.car.cartype.binarytek.GMBinarytek;
import com.zhuchao.android.car.cartype.binarytek.GuanZhiBNR;
import com.zhuchao.android.car.cartype.binarytek.HondaDABNR;
import com.zhuchao.android.car.cartype.binarytek.HuaTaiBNR;
import com.zhuchao.android.car.cartype.binarytek.HyBNR;
import com.zhuchao.android.car.cartype.binarytek.JaingLingBNR;
import com.zhuchao.android.car.cartype.binarytek.JeepBNR;
import com.zhuchao.android.car.cartype.binarytek.JeepBNR2;
import com.zhuchao.android.car.cartype.binarytek.NissanBinarytek;
import com.zhuchao.android.car.cartype.binarytek.Odyssey09_14BNR;
import com.zhuchao.android.car.cartype.binarytek.RenaultBNR;
import com.zhuchao.android.car.cartype.binarytek.VWBNR;
import com.zhuchao.android.car.cartype.binarytek.VWMQBBNR;
import com.zhuchao.android.car.cartype.binarytek.ZongTaiBNR;
import com.zhuchao.android.car.cartype.changyuantong.Accord7ChangYuanTong;
import com.zhuchao.android.car.cartype.changyuantong.Accord7ChangYuanTong9600;
import com.zhuchao.android.car.cartype.changyuantong.BydCYT;
import com.zhuchao.android.car.cartype.changyuantong.Mazda6ChangYuanTong;
import com.zhuchao.android.car.cartype.daojun.Accord7DaoJun;
import com.zhuchao.android.car.cartype.daojun.BydF6DaoJun;
import com.zhuchao.android.car.cartype.daojun.BydG6DaoJun;
import com.zhuchao.android.car.cartype.daojun.BydM6DaoJun;
import com.zhuchao.android.car.cartype.daojun.BydS6DaoJun;
import com.zhuchao.android.car.cartype.daojun.CaloraDaoJun;
import com.zhuchao.android.car.cartype.daojun.ChangChengDaoJun;
import com.zhuchao.android.car.cartype.daojun.DaoQiDaoJun;
import com.zhuchao.android.car.cartype.daojun.DongNanDX7DaoJun;
import com.zhuchao.android.car.cartype.daojun.FiestaDaojun;
import com.zhuchao.android.car.cartype.daojun.GMDaoJun;
import com.zhuchao.android.car.cartype.daojun.JiangHuaiDaoJun;
import com.zhuchao.android.car.cartype.daojun.JinBeiDaoJun;
import com.zhuchao.android.car.cartype.daojun.MondeoDaojun;
import com.zhuchao.android.car.cartype.daojun.NissanDaoJun;
import com.zhuchao.android.car.cartype.daojun.RongWeiI5DaoJun;
import com.zhuchao.android.car.cartype.daojun.Sorento13DaoJun;
import com.zhuchao.android.car.cartype.daojun.SpiriorDaoJun;
import com.zhuchao.android.car.cartype.hiworld.AudiA3Hiworld;
import com.zhuchao.android.car.cartype.hiworld.AudiQ5Hiworld;
import com.zhuchao.android.car.cartype.hiworld.BMW001Hiworld;
import com.zhuchao.android.car.cartype.hiworld.BMW002Hiworld;
import com.zhuchao.android.car.cartype.hiworld.BYDHiworld;
import com.zhuchao.android.car.cartype.hiworld.BeiQiBAP001Hiworld;
import com.zhuchao.android.car.cartype.hiworld.BeiQiBAP002Hiworld;
import com.zhuchao.android.car.cartype.hiworld.BeiQiBAP003Hiworld;
import com.zhuchao.android.car.cartype.hiworld.BentengFWP003Hiworld;
import com.zhuchao.android.car.cartype.hiworld.BentengFWP005Hiworld;
import com.zhuchao.android.car.cartype.hiworld.BentengFWP006Hiworld;
import com.zhuchao.android.car.cartype.hiworld.BentengFWP007Hiworld;
import com.zhuchao.android.car.cartype.hiworld.BentengFWP008Hiworld;
import com.zhuchao.android.car.cartype.hiworld.BentengFWP009Hiworld;
import com.zhuchao.android.car.cartype.hiworld.BentengFWP00AHiworld;
import com.zhuchao.android.car.cartype.hiworld.BenzB200Hiworld;
import com.zhuchao.android.car.cartype.hiworld.BenzG350Hiworld;
import com.zhuchao.android.car.cartype.hiworld.BenzMetrisHiworld;
import com.zhuchao.android.car.cartype.hiworld.ChangAnCNP001Hiworld;
import com.zhuchao.android.car.cartype.hiworld.ChangAnCNP002Hiworld;
import com.zhuchao.android.car.cartype.hiworld.ChangAnCNP004Hiworld;
import com.zhuchao.android.car.cartype.hiworld.ChangAnCNP005Hiworld;
import com.zhuchao.android.car.cartype.hiworld.ChangChengH2Hiworld;
import com.zhuchao.android.car.cartype.hiworld.ChangChengH9Hiworld;
import com.zhuchao.android.car.cartype.hiworld.ChangChengHiworld;
import com.zhuchao.android.car.cartype.hiworld.ChangChengHiworldCCP003;
import com.zhuchao.android.car.cartype.hiworld.ChuanQiGA3Hiworld;
import com.zhuchao.android.car.cartype.hiworld.ChuanQiHiworld;
import com.zhuchao.android.car.cartype.hiworld.DongFeng002Hiworld;
import com.zhuchao.android.car.cartype.hiworld.DongFeng003Hiworld;
import com.zhuchao.android.car.cartype.hiworld.DongFeng005Hiworld;
import com.zhuchao.android.car.cartype.hiworld.DongFeng007Hiworld;
import com.zhuchao.android.car.cartype.hiworld.DongFeng008Hiworld;
import com.zhuchao.android.car.cartype.hiworld.FiatHiworld;
import com.zhuchao.android.car.cartype.hiworld.Ford003Hiworld;
import com.zhuchao.android.car.cartype.hiworld.Ford005Hiworld;
import com.zhuchao.android.car.cartype.hiworld.FordFDP007Hiworld;
import com.zhuchao.android.car.cartype.hiworld.FordHiworld;
import com.zhuchao.android.car.cartype.hiworld.GMHiworld;
import com.zhuchao.android.car.cartype.hiworld.HYHiworld;
import com.zhuchao.android.car.cartype.hiworld.Haima001Hiworld;
import com.zhuchao.android.car.cartype.hiworld.Haima003Hiworld;
import com.zhuchao.android.car.cartype.hiworld.Honda003Hiworld;
import com.zhuchao.android.car.cartype.hiworld.HondaDAHiworld;
import com.zhuchao.android.car.cartype.hiworld.IVT001Hiworld;
import com.zhuchao.android.car.cartype.hiworld.Infiniti001Hiworld;
import com.zhuchao.android.car.cartype.hiworld.Jeep002Hiworld;
import com.zhuchao.android.car.cartype.hiworld.JeepHiworld;
import com.zhuchao.android.car.cartype.hiworld.JiLiHiworld;
import com.zhuchao.android.car.cartype.hiworld.JiangHuaiRuiFengHiworld;
import com.zhuchao.android.car.cartype.hiworld.KeyPannelHiworld;
import com.zhuchao.android.car.cartype.hiworld.MAP001Hiworld;
import com.zhuchao.android.car.cartype.hiworld.MazdaHiworld;
import com.zhuchao.android.car.cartype.hiworld.MiniHiword;
import com.zhuchao.android.car.cartype.hiworld.NissanHiworld;
import com.zhuchao.android.car.cartype.hiworld.NissanTeana08Hiworld;
import com.zhuchao.android.car.cartype.hiworld.Odyssey04Hiworld;
import com.zhuchao.android.car.cartype.hiworld.OutlanderHiworld;
import com.zhuchao.android.car.cartype.hiworld.PSAHiworld;
import com.zhuchao.android.car.cartype.hiworld.QiChengT90Hiworld;
import com.zhuchao.android.car.cartype.hiworld.QiRuiHiworld;
import com.zhuchao.android.car.cartype.hiworld.QiRuiJieTuHiworld;
import com.zhuchao.android.car.cartype.hiworld.RanualtHiworld;
import com.zhuchao.android.car.cartype.hiworld.SaiOu3Hiworld;
import com.zhuchao.android.car.cartype.hiworld.ShangQiBaoJunHiworld;
import com.zhuchao.android.car.cartype.hiworld.ShangQiSAP001Hiworld;
import com.zhuchao.android.car.cartype.hiworld.ShangQiSAP003Hiworld;
import com.zhuchao.android.car.cartype.hiworld.ShangQiSAP004Hiworld;
import com.zhuchao.android.car.cartype.hiworld.ShangQiSAP005Hiworld;
import com.zhuchao.android.car.cartype.hiworld.ShangQiSAP006Hiworld;
import com.zhuchao.android.car.cartype.hiworld.ShangQiSAP007Hiworld;
import com.zhuchao.android.car.cartype.hiworld.TataTAP001Hiworld;
import com.zhuchao.android.car.cartype.hiworld.TouaregHiworld;
import com.zhuchao.android.car.cartype.hiworld.Toyota002Hiworld;
import com.zhuchao.android.car.cartype.hiworld.ToyotaHiworld;
import com.zhuchao.android.car.cartype.hiworld.UAZ001Hiworld;
import com.zhuchao.android.car.cartype.hiworld.VWHiworld;
import com.zhuchao.android.car.cartype.hiworld.VWMQBHiworld;
import com.zhuchao.android.car.cartype.hiworld.WeiChaiJMP001Hiworld;
import com.zhuchao.android.car.cartype.hiworld.ZTP001Hiworld;
import com.zhuchao.android.car.cartype.huachengyu.BydHCY;
import com.zhuchao.android.car.cartype.luzheng.BMWE46LuZheng;
import com.zhuchao.android.car.cartype.luzheng.HondaHaoZheng;
import com.zhuchao.android.car.cartype.luzheng.LandRoverHaozheng;
import com.zhuchao.android.car.cartype.luzheng.Mazda6LuZheng;
import com.zhuchao.android.car.cartype.luzheng.MiniHaoZheng;
import com.zhuchao.android.car.cartype.luzheng.OpelHaoZheng;
import com.zhuchao.android.car.cartype.luzheng.RX330HaoZheng;
import com.zhuchao.android.car.cartype.luzheng.SmartHaozheng;
import com.zhuchao.android.car.cartype.luzheng.SubaruHaoZheng;
import com.zhuchao.android.car.cartype.luzheng.ToyotaLuZheng;
import com.zhuchao.android.car.cartype.ods.BMWNbtEvo;
import com.zhuchao.android.car.cartype.ods.BiSuOD;
import com.zhuchao.android.car.cartype.ods.BydODS;
import com.zhuchao.android.car.cartype.ods.CadillacKaiLeiDeOD;
import com.zhuchao.android.car.cartype.ods.ChangChengH9OD;
import com.zhuchao.android.car.cartype.ods.CheryOD;
import com.zhuchao.android.car.cartype.ods.DongFengOD;
import com.zhuchao.android.car.cartype.ods.DongFengXinNengYuanOD;
import com.zhuchao.android.car.cartype.ods.DongNanA5OD;
import com.zhuchao.android.car.cartype.ods.DongNanDX7OD;
import com.zhuchao.android.car.cartype.ods.FordQuanXunOD;
import com.zhuchao.android.car.cartype.ods.FutianOD;
import com.zhuchao.android.car.cartype.ods.GMOD;
import com.zhuchao.android.car.cartype.ods.HummerODS;
import com.zhuchao.android.car.cartype.ods.JiLiBoRuiOD;
import com.zhuchao.android.car.cartype.ods.JiangHuaiOD;
import com.zhuchao.android.car.cartype.ods.LuFengOD;
import com.zhuchao.android.car.cartype.ods.LuxgenOD;
import com.zhuchao.android.car.cartype.ods.NaZhaOD;
import com.zhuchao.android.car.cartype.ods.OpelOD;
import com.zhuchao.android.car.cartype.ods.QQiceScreamOD;
import com.zhuchao.android.car.cartype.ods.SaicOD;
import com.zhuchao.android.car.cartype.ods.SkyworthET5;
import com.zhuchao.android.car.cartype.ods.SubrauODS;
import com.zhuchao.android.car.cartype.ods.VolvoXC60;
import com.zhuchao.android.car.cartype.ods.WeiChaiU70OD;
import com.zhuchao.android.car.cartype.ods.YeMaOD;
import com.zhuchao.android.car.cartype.ods.ZHONGXINGOD;
import com.zhuchao.android.car.cartype.ods.ZhidouOD;
import com.zhuchao.android.car.cartype.other.BeiqiDianDongCheOther;
import com.zhuchao.android.car.cartype.other.TestKLD;
import com.zhuchao.android.car.cartype.raise.AudiRaise;
import com.zhuchao.android.car.cartype.raise.BMWRaise;
import com.zhuchao.android.car.cartype.raise.BaoJunRaise;
import com.zhuchao.android.car.cartype.raise.BeiQiEC180Raise;
import com.zhuchao.android.car.cartype.raise.BeiQiM200Raise;
import com.zhuchao.android.car.cartype.raise.BeiQiRaise;
import com.zhuchao.android.car.cartype.raise.BenTengRaise;
import com.zhuchao.android.car.cartype.raise.BenzRaise;
import com.zhuchao.android.car.cartype.raise.BiSuRaise;
import com.zhuchao.android.car.cartype.raise.CarFordRaise;
import com.zhuchao.android.car.cartype.raise.CarMazda;
import com.zhuchao.android.car.cartype.raise.CarX80;
import com.zhuchao.android.car.cartype.raise.ChangChengC30Raise;
import com.zhuchao.android.car.cartype.raise.ChangChengFengJun6Raise;
import com.zhuchao.android.car.cartype.raise.ChangChengRaise;
import com.zhuchao.android.car.cartype.raise.ChuanQiRaise;
import com.zhuchao.android.car.cartype.raise.DaTongRaise;
import com.zhuchao.android.car.cartype.raise.DongFengFengShenAX7Raise;
import com.zhuchao.android.car.cartype.raise.DongFengJingYiX5Raise;
import com.zhuchao.android.car.cartype.raise.DongFengRaise;
import com.zhuchao.android.car.cartype.raise.DongFengS560Raise;
import com.zhuchao.android.car.cartype.raise.DongNanRaise;
import com.zhuchao.android.car.cartype.raise.FiatEGEARaise;
import com.zhuchao.android.car.cartype.raise.GMRaise;
import com.zhuchao.android.car.cartype.raise.HYRaise;
import com.zhuchao.android.car.cartype.raise.HaiMaFuLaiMeiRaise;
import com.zhuchao.android.car.cartype.raise.HaiMaM8Raise;
import com.zhuchao.android.car.cartype.raise.HaiMaRaise;
import com.zhuchao.android.car.cartype.raise.HanTengRaise;
import com.zhuchao.android.car.cartype.raise.HondaRaise;
import com.zhuchao.android.car.cartype.raise.InfinitiQX50;
import com.zhuchao.android.car.cartype.raise.JeepRaise;
import com.zhuchao.android.car.cartype.raise.JiLiRaise;
import com.zhuchao.android.car.cartype.raise.JiangHuaiRaise;
import com.zhuchao.android.car.cartype.raise.KadjarRaise;
import com.zhuchao.android.car.cartype.raise.KeyPannel1;
import com.zhuchao.android.car.cartype.raise.LiFanRaise;
import com.zhuchao.android.car.cartype.raise.LuFengRaise;
import com.zhuchao.android.car.cartype.raise.MazdaRaise;
import com.zhuchao.android.car.cartype.raise.MinJueRongWeiRaise;
import com.zhuchao.android.car.cartype.raise.MitsubishiRaise;
import com.zhuchao.android.car.cartype.raise.NaZhiJieU6Raise;
import com.zhuchao.android.car.cartype.raise.NissanRaise;
import com.zhuchao.android.car.cartype.raise.OuShangRaise;
import com.zhuchao.android.car.cartype.raise.PetgeoRaise;
import com.zhuchao.android.car.cartype.raise.PetgeoScreenRaise;
import com.zhuchao.android.car.cartype.raise.Q3Raise;
import com.zhuchao.android.car.cartype.raise.QiChengRaise;
import com.zhuchao.android.car.cartype.raise.QiRuiRaise;
import com.zhuchao.android.car.cartype.raise.RongWeiRaise;
import com.zhuchao.android.car.cartype.raise.SiWeiRaise;
import com.zhuchao.android.car.cartype.raise.ToyotaRaise;
import com.zhuchao.android.car.cartype.raise.TuoLaJiRaise;
import com.zhuchao.android.car.cartype.raise.VWMQBRaise;
import com.zhuchao.android.car.cartype.raise.VolvoRaise;
import com.zhuchao.android.car.cartype.raise.X30Raise;
import com.zhuchao.android.car.cartype.raise.YueXiangV7;
import com.zhuchao.android.car.cartype.raise.ZhongHuaRaise;
import com.zhuchao.android.car.cartype.raise.ZongTaiRaise;
import com.zhuchao.android.car.cartype.simple.Accord2013Simple;
import com.zhuchao.android.car.cartype.simple.AudiA3Simple;
import com.zhuchao.android.car.cartype.simple.BravoUnionSimple;
import com.zhuchao.android.car.cartype.simple.CRV12Simple;
import com.zhuchao.android.car.cartype.simple.CarBenzVito;
import com.zhuchao.android.car.cartype.simple.CarFordSimple;
import com.zhuchao.android.car.cartype.simple.CarGMSimple;
import com.zhuchao.android.car.cartype.simple.CarHY;
import com.zhuchao.android.car.cartype.simple.CarHondaDASimple;
import com.zhuchao.android.car.cartype.simple.CarMazdaBT50Simple;
import com.zhuchao.android.car.cartype.simple.CarOPEL;
import com.zhuchao.android.car.cartype.simple.CarTEANA;
import com.zhuchao.android.car.cartype.simple.CarToyota2013;
import com.zhuchao.android.car.cartype.simple.CarToyota2013Low;
import com.zhuchao.android.car.cartype.simple.CarVW;
import com.zhuchao.android.car.cartype.simple.ChryslerSimple;
import com.zhuchao.android.car.cartype.simple.DaciaSimple;
import com.zhuchao.android.car.cartype.simple.FIATSimple;
import com.zhuchao.android.car.cartype.simple.FordExplorerSimple;
import com.zhuchao.android.car.cartype.simple.FordMondeoSimple;
import com.zhuchao.android.car.cartype.simple.GMCSimple;
import com.zhuchao.android.car.cartype.simple.IsuzuSimple;
import com.zhuchao.android.car.cartype.simple.IvecoSimple;
import com.zhuchao.android.car.cartype.simple.JeepSimple;
import com.zhuchao.android.car.cartype.simple.Mazda3BinarytekSimple;
import com.zhuchao.android.car.cartype.simple.Mazda3Simple;
import com.zhuchao.android.car.cartype.simple.MazdaCX5Simple;
import com.zhuchao.android.car.cartype.simple.MitsubishiOutLanderSimple;
import com.zhuchao.android.car.cartype.simple.Nissan2013Simple;
import com.zhuchao.android.car.cartype.simple.PSASimple;
import com.zhuchao.android.car.cartype.simple.Peugeot206307OldSimple;
import com.zhuchao.android.car.cartype.simple.Peugeot206Simple;
import com.zhuchao.android.car.cartype.simple.PorscheUnionSimple;
import com.zhuchao.android.car.cartype.simple.RamFIATSimple;
import com.zhuchao.android.car.cartype.simple.RenaultMeganeFluenceSimple;
import com.zhuchao.android.car.cartype.simple.SubaruSimple;
import com.zhuchao.android.car.cartype.simple.VWGolfSimple;
import com.zhuchao.android.car.cartype.td.MitsubishiTD;
import com.zhuchao.android.car.cartype.td.ToyotaTD;
import com.zhuchao.android.car.cartype.union.BMWE90X1Union;
import com.zhuchao.android.car.cartype.union.BeiQiH3HeChi;
import com.zhuchao.android.car.cartype.union.BenzB200Union;
import com.zhuchao.android.car.cartype.union.PetgeoScreenUnion;
import com.zhuchao.android.car.cartype.xinbasi.Accord924Xinbasi;
import com.zhuchao.android.car.cartype.xinbasi.Accord9Xinbasi;
import com.zhuchao.android.car.cartype.xinbasi.BydSongRaise;
import com.zhuchao.android.car.cartype.xinbasi.CarMazdaXinbas;
import com.zhuchao.android.car.cartype.xinbasi.FengJun6Xinbas;
import com.zhuchao.android.car.cartype.xinbasi.FordXinbasi;
import com.zhuchao.android.car.cartype.xinbasi.HaferH3Xinbas;
import com.zhuchao.android.car.cartype.xinbasi.HoldenXinbasi;
import com.zhuchao.android.car.cartype.xinbasi.HondaDAXinbasi;
import com.zhuchao.android.car.cartype.xinbasi.HyXinbasi;
import com.zhuchao.android.car.cartype.xinbasi.JeepXinbas;
import com.zhuchao.android.car.cartype.xinbasi.Mazda6Xinbas;
import com.zhuchao.android.car.cartype.xinbasi.NissanXinbas;
import com.zhuchao.android.car.cartype.xinbasi.X80Xinbas;
import com.zhuchao.android.car.cartype.xinchi.Accord8XinChi;
import com.zhuchao.android.car.cartype.xinchi.FordXinChi;
import com.zhuchao.android.car.cartype.xinchi.FremontXinChi;
import com.zhuchao.android.car.cartype.xinchi.GMXinChi;
import com.zhuchao.android.car.cartype.xinchi.LC100XinChi;
import com.zhuchao.android.car.cartype.xinchi.Sorento13XinChi;
import com.zhuchao.android.car.cartype.xinchi.Teana2005XinChi;
import com.zhuchao.android.car.cartype.xinchi.Teana2008XinChi;
import com.zhuchao.android.car.cartype.xinchi.TeanaXinChi;
import com.zhuchao.android.car.cartype.xinfeiyang.HondaXinFeiYang;
import com.zhuchao.android.car.cartype.xinfeiyang.NissanXinFeiYang;
import com.zhuchao.android.car.cartype.xinfeiyang.RenaultXinFeiYang;
import com.zhuchao.android.car.cartype.xinfeiyang.SubaruX3XinFeiYang;
import com.zhuchao.android.car.cartype.xinfeiyang.ToyotaXinFeiYang;

public class CanboxToPro {


    //this index is the same as CanBus说明文档*.*
    private final static Class<?>[] mAllCanbox = {
            CarFordSimple.class,//1
            CarToyota2013.class, CarMazda.class, CarX80.class, CarTEANA.class, CarOPEL.class, CarVW.class, MitsubishiOutLanderSimple.class, CarHY.class, CarPSABagoo.class,//10
            CarGMSimple.class, CarHondaDASimple.class, VWGolfSimple.class, RamFIATSimple.class, RenaultMeganeFluenceSimple.class, BMWE90X1Union.class, FIATSimple.class, FordMondeoSimple.class,
            PSASimple.class, BenzBagoo.class,//20
            KadjarRaise.class, GMCSimple.class, BenzB200Union.class, CarMazdaBT50Simple.class, JeepSimple.class, Accord7ChangYuanTong.class, CarToyotaBinarytek.class, CarMazdaXinbas.class,
            Peugeot206Simple.class, Accord2013Simple.class,//30
            Nissan2013Simple.class, PorscheUnionSimple.class, Mazda3BinarytekSimple.class, BravoUnionSimple.class, TouaregHiworld.class, DaciaSimple.class, NissanRaise.class, PetgeoRaise.class,
            FordExplorerSimple.class, AccordBinarytek.class,//40
            AudiA3Simple.class, SubrauODS.class, MiniHiword.class, NissanBinarytek.class, CarBenzVito.class, VWMQBRaise.class, CheryOD.class, ChryslerSimple.class, Mazda3Simple.class,
            CarOBDBinarytek.class,//50
            HaferH3Xinbas.class, HondaRaise.class, PetgeoScreenRaise.class, CarFordRaise.class, SmartHaozheng.class, LandRoverHaozheng.class, PetgeoScreenUnion.class, MazdaCX5Simple.class,
            RX330HaoZheng.class, Peugeot206307OldSimple.class,//60
            X30Raise.class, MondeoDaojun.class, JeepXinbas.class, OuShangRaise.class, FiatEGEARaise.class, HYRaise.class, AlphaBagoo.class, ToyotaRaise.class, MiniHaoZheng.class, SubaruSimple.class,
            //70
            GMOD.class, MazdaRaise.class, GMRaise.class, AudiRaise.class, Q3Raise.class,//75
            JeepRaise.class, LiFanRaise.class, BeiQiRaise.class, BeiQiM200Raise.class, ChangChengRaise.class,//80
            ChangChengC30Raise.class, ChangChengFengJun6Raise.class, HaiMaRaise.class, HaiMaM8Raise.class, BiSuRaise.class,//85
            BenzRaise.class, SiWeiRaise.class, ChuanQiRaise.class, MitsubishiRaise.class, QiRuiRaise.class,//90
            BenTengRaise.class, JiLiRaise.class, YueXiangV7.class, BaoJunRaise.class, DaTongRaise.class,//95
            RongWeiRaise.class, MinJueRongWeiRaise.class, QiChengRaise.class, JiangHuaiRaise.class, DongFengRaise.class,//100
            DongFengFengShenAX7Raise.class, DongFengJingYiX5Raise.class, DongFengS560Raise.class, VWHiworld.class, ZongTaiRaise.class,//105
            VWMQBHiworld.class, ToyotaHiworld.class, HYHiworld.class, HondaDAHiworld.class, NissanHiworld.class, //110
            GMHiworld.class, ZhongHuaRaise.class, LuFengRaise.class, HanTengRaise.class, DongNanRaise.class, //115
            InfinitiQX50.class, FordHiworld.class, VolvoRaise.class, JeepHiworld.class, Jeep002Hiworld.class, //120
            PSAHiworld.class, MazdaHiworld.class, QiRuiHiworld.class, QiRuiJieTuHiworld.class, ChangChengHiworld.class, //125
            ChangChengH2Hiworld.class, ShangQiBaoJunHiworld.class, ShangQiSAP006Hiworld.class, ShangQiSAP004Hiworld.class, ShangQiSAP003Hiworld.class,//130
            ShangQiSAP005Hiworld.class, ShangQiSAP007Hiworld.class, ShangQiSAP001Hiworld.class, ChuanQiHiworld.class, FiatHiworld.class,//135
            IvecoSimple.class, JiLiHiworld.class, BYDHiworld.class, JiangHuaiRuiFengHiworld.class, HaiMaFuLaiMeiRaise.class,//140
            BeiQiEC180Raise.class, Accord7DaoJun.class, VolvoXC60.class, PG360Test.class, AudiBagoo.class,//145
            BMWNbtEvo.class, RanualtHiworld.class, HondaHaoZheng.class, LC100XinChi.class, DaoQiDaoJun.class,//150
            FordQuanXunOD.class, Accord9Xinbasi.class, Accord924Xinbasi.class, FordXinChi.class, Accord8XinChi.class, //155
            Sorento13XinChi.class, TeanaXinChi.class, HondaDAXinbasi.class, SaicOD.class, BydODS.class, //160
            FordXinbasi.class, JaingLingBNR.class, HuaTaiBNR.class, HummerODS.class, OutlanderHiworld.class,//165
            BenzB200Hiworld.class, QiChengT90Hiworld.class, BeiQiBAP002Hiworld.class, BeiQiBAP001Hiworld.class, ChuanQiGA3Hiworld.class,//170
            DongFeng002Hiworld.class, DongFeng003Hiworld.class, NissanDaoJun.class, SpiriorDaoJun.class, GMDaoJun.class,//175
            BMW001Hiworld.class, Teana2008XinChi.class, ChangAnCNP005Hiworld.class, LuxgenOD.class, DongFeng005Hiworld.class,//180
            DongFeng007Hiworld.class, DongFeng008Hiworld.class, Odyssey04Hiworld.class, ChangChengH9OD.class, CRV12Simple.class,//185
            JeepBNR.class, BMW002Hiworld.class, Ford003Hiworld.class, Honda003Hiworld.class, LuFengOD.class,//190
            BenzG350Hiworld.class, ZTP001Hiworld.class, BeiQiBAP003Hiworld.class, AudiA3Hiworld.class, AudiQ5Hiworld.class,//195
            Odyssey09_14BNR.class, FordBinarytek.class, BydS6DaoJun.class, OpelHaoZheng.class, ChangChengH9Hiworld.class, //200
            GuanZhiBNR.class, DaChengE20.class, BydF6DaoJun.class, BydM6DaoJun.class, BydG6DaoJun.class,//205
            BydHCY.class, NissanTeana08Hiworld.class, BenzMetrisHiworld.class, FordFDP007Hiworld.class, VWBNR.class,//210
            VWMQBBNR.class, Mazda6ChangYuanTong.class, ChangChengBNR.class, Haima003Hiworld.class, Haima001Hiworld.class,//215
            Infiniti001Hiworld.class, ChangAnCNP004Hiworld.class, ChangAnCNP001Hiworld.class, CadillacKaiLeiDeOD.class, RongWeiI5DaoJun.class,//220
            ChangAnCNP002Hiworld.class, HondaDABNR.class, Toyota002Hiworld.class, TuoLaJiRaise.class, JiangHuaiOD.class,//225
            Mazda6Xinbas.class, ToyotaLuZheng.class, SubaruHaoZheng.class, Ford005Hiworld.class, BydCYT.class,//230
            Mazda6LuZheng.class, BentengFWP006Hiworld.class, BentengFWP007Hiworld.class, BentengFWP009Hiworld.class, BentengFWP005Hiworld.class,//235
            BentengFWP00AHiworld.class, BentengFWP003Hiworld.class, BentengFWP008Hiworld.class, ChangChengHiworldCCP003.class, CaloraDaoJun.class,//240
            NissanXinbas.class, DongNanDX7OD.class, DongNanA5OD.class, GMBinarytek.class, Sorento13DaoJun.class,//245
            JinBeiDaoJun.class, FiestaDaojun.class, YeMaOD.class, DongNanDX7DaoJun.class, ZongTaiBNR.class,//250
            OpelOD.class, UAZ001Hiworld.class, FremontXinChi.class, GMXinChi.class, HyBNR.class,//255
            HyXinbasi.class, FiatBagoo.class, ToyotaTD.class, BMWRaise.class, WeiChaiJMP001Hiworld.class,//260
            TataTAP001Hiworld.class, RenaultBNR.class, IVT001Hiworld.class, MAP001Hiworld.class, Accord7ChangYuanTong9600.class,//265
            IsuzuSimple.class, BiSuOD.class, ZhidouOD.class, NaZhaOD.class, FutianOD.class,//270
            FengJun6Xinbas.class, JiangHuaiDaoJun.class, X80Xinbas.class, ChangChengDaoJun.class, ChangAnBNR.class,//275
            HoldenXinbasi.class, BeiQiH3HeChi.class, BydSongRaise.class, ChuanQiBNR.class, MitsubishiTD.class,//280
            SubaruX3XinFeiYang.class, RenaultXinFeiYang.class, HondaXinFeiYang.class, NissanXinFeiYang.class, ToyotaXinFeiYang.class,//285
            KeyPannel1.class, KeyPannelHiworld.class, KeyPannelHiworld.class, SkyworthET5.class,
            /*from 90*/
            BeiqiDianDongCheOther.class,//290
            QQiceScreamOD.class, WeiChaiU70OD.class, JeepBNR2.class, DongFengOD.class, CarMazda.class, // 3->295 (38400)
            NaZhiJieU6Raise.class, JiLiBoRuiOD.class, DongFengXinNengYuanOD.class, SaiOu3Hiworld.class, TestKLD.class, //300
            BMWE46LuZheng.class, Megane3.class, Teana2005XinChi.class, ZHONGXINGOD.class
    };

    private final static String[] RETURN_TYPE = {
            "3,4,6,231,295",
            "299,287,288,252,264,263,261,260,239,238,237,236,235,234,233,232,229,223,221,218,217,216,214,209,104,106,107,108,109,110,111,117,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,137,138,139,147,165,166,167,168,169,170,171,172,174,176,178,180,181,182,183,187,188,189,191,192,193,194,195,200,207,208",
            "215", "302",
    };

    public final static Canbox getPro(String mCanboxType, int version, int index) {
        Canbox mCanbox = null;

        if (version >= 3) {
            try {
                if (index > 0 && index <= mAllCanbox.length) {
                    index--;
                    mCanbox = (Canbox) mAllCanbox[index].newInstance();
                }
            } catch (Exception e) {
            }
        }

        if (mCanbox == null) {
            if (mCanboxType != null) {
                if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_FORD_SIMPLE)) {
                    mCanbox = new CarFordSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_TOYOTA)) {
                    mCanbox = new CarToyota2013();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_MAZDA)) {
                    mCanbox = new CarMazda();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_BESTURN_X80)) {
                    mCanbox = new CarX80();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_TEANA_2013)) {
                    mCanbox = new CarTEANA();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_OPEL)) {
                    mCanbox = new CarOPEL();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_VW)) {
                    mCanbox = new CarVW();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_MITSUBISHI_OUTLANDER_SIMPLE)) {
                    mCanbox = new MitsubishiOutLanderSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_HY)) {
                    mCanbox = new CarHY();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_PSA_BAGOO)) { //10
                    mCanbox = new CarPSABagoo();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_GM_SIMPLE)) {
                    mCanbox = new CarGMSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_GM_RAISE)) {
                    mCanbox = new GMRaise();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_HONDA_DA_SIMPLE)) {
                    mCanbox = new CarHondaDASimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_VW_GOLF_SIMPLE)) {
                    mCanbox = new VWGolfSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_RAM_FIAT)) {
                    mCanbox = new RamFIATSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_RENAULT_MEGANE_FLUENCE_SMPLE)) {
                    mCanbox = new RenaultMeganeFluenceSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_BMW_E90X1_UNION)) {
                    mCanbox = new BMWE90X1Union();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_FIAT)) {
                    mCanbox = new FIATSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_FORD_MONDEO)) {
                    mCanbox = new FordMondeoSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_PSA)) {
                    mCanbox = new PSASimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_BENZ_BAGOO)) {
                    mCanbox = new BenzBagoo();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_KADJAR_RAISE)) {
                    mCanbox = new KadjarRaise();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_GMC_SIMPLE)) {
                    mCanbox = new GMCSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_BENZ_B200_UNION)) {
                    mCanbox = new BenzB200Union();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_MAZDA_BT50_SIMPLE)) {
                    mCanbox = new CarMazdaBT50Simple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_JEEP_SIMPLE)) {
                    mCanbox = new JeepSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_ACCORD7_CHANGYUANTONG)) {
                    mCanbox = new Accord7ChangYuanTong();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_TOYOTA_BINARYTEK)) {
                    mCanbox = new CarToyotaBinarytek();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_MAZDA_XINBAS)) {
                    mCanbox = new CarMazdaXinbas();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_PEUGEOT206)) {
                    mCanbox = new Peugeot206Simple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_ACCORD2013)) {
                    mCanbox = new Accord2013Simple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_NISSAN2013)) {
                    mCanbox = new Nissan2013Simple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_PORSCHE_UNION)) {
                    mCanbox = new PorscheUnionSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_MAZDA3_BINARYTEK)) {
                    mCanbox = new Mazda3BinarytekSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_BRAVO_UNION)) {
                    mCanbox = new BravoUnionSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_TOUAREG_HIWORLD)) {
                    mCanbox = new TouaregHiworld();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_DACIA_SIMPLE)) {
                    mCanbox = new DaciaSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_NISSAN_RAISE)) {
                    mCanbox = new NissanRaise();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_PETGEO_RAISE)) {
                    mCanbox = new PetgeoRaise();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_FORD_EXPLORER_SIMPLE)) {
                    mCanbox = new FordExplorerSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_ACCORD_BINARYTEK)) {
                    mCanbox = new AccordBinarytek();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_AUDI_SIMPLE)) {
                    mCanbox = new AudiA3Simple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_SUBARU_ODS)) {
                    mCanbox = new SubrauODS();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_MINI_HIWORD)) {
                    mCanbox = new MiniHiword();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_NISSAN_BINARYTEK)) {
                    mCanbox = new NissanBinarytek();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_BENZ_VITO_SIMPLE)) {
                    mCanbox = new CarBenzVito();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_VW_MQB_RAISE)) {
                    mCanbox = new VWMQBRaise();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_CHERY_OD)) {
                    mCanbox = new CheryOD();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_CHRYSLER_SIMPLE)) {
                    mCanbox = new ChryslerSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_MAZDA3_SIMPLE)) {
                    mCanbox = new Mazda3Simple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_OBD_BINARUI)) {
                    mCanbox = new CarOBDBinarytek();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_HAFER_H2)) {
                    mCanbox = new HaferH3Xinbas();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_HONDA_RAISE)) {
                    mCanbox = new HondaRaise();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_PETGEO_SCREEN_RAISE)) {
                    mCanbox = new PetgeoScreenRaise();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_FORD_RAISE)) {
                    mCanbox = new CarFordRaise();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_SMART_HAOZHENG)) {
                    mCanbox = new SmartHaozheng();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_LANDROVER_HAOZHENG)) {
                    mCanbox = new LandRoverHaozheng();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_PEUGEOT307_UNION)) {
                    mCanbox = new PetgeoScreenUnion();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_MAZDA_CX5_SIMPLE)) {
                    mCanbox = new MazdaCX5Simple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_RX330_HAOZHENG)) {
                    mCanbox = new RX330HaoZheng();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_PSA206_SIMPLE)) {
                    mCanbox = new Peugeot206307OldSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_X30_RAISE)) {
                    mCanbox = new X30Raise();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_MONDEO_DAOJUN)) {
                    mCanbox = new MondeoDaojun();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_JEEP_XINBAS)) {
                    mCanbox = new JeepXinbas();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_OUSHANG_RAISE)) {
                    mCanbox = new OuShangRaise();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_FIAT_EGEA_RAISE)) {
                    mCanbox = new FiatEGEARaise();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_HY_RAISE)) {
                    mCanbox = new HYRaise();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_ALPHA_BAGOO)) {
                    mCanbox = new AlphaBagoo();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_TOYOTA_RAISE)) {
                    mCanbox = new ToyotaRaise();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_MINI_HAOZHENG)) {
                    mCanbox = new MiniHaoZheng();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_SUBARU_SIMPLE)) {
                    mCanbox = new SubaruSimple();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_GM_OD)) {
                    mCanbox = new GMOD();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_MAZDA_RAISE)) {
                    mCanbox = new MazdaRaise();

                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_TOYOTA_LOW)) {
                    mCanbox = new CarToyota2013Low();
                } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_ZHONGXING_OD)) {
                    mCanbox = new ZHONGXINGOD();
                } else {
                    mCanboxType = null;
                    mCanbox = new CarNone();
                    mCanbox = null;
                }
            } else {
                mCanboxType = null;
                mCanbox = new CarNone();
                mCanbox = null;
            }
        }

        return mCanbox;
    }


    public final static int getReturnMsgType(String mCanboxType, int version, int index) {
        int type = 0;

        if (version >= 3) {

            if (index > 0 && index <= mAllCanbox.length) {
                for (int i = 0; i < RETURN_TYPE.length; ++i) {
                    String[] ss = RETURN_TYPE[i].split(",");
                    for (String s : ss) {
                        if (s.equals(String.valueOf(index))) {
                            return i + 1;
                        }
                    }
                    //					if ((index == RETURN_TYPE[i][0])) {
                    //						return RETURN_TYPE[i][1];
                    //					}
                }
            }

        } else if (mCanboxType != null) {

            if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_MAZDA) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_OPEL) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_BESTURN_X80)) {
                type = 1;
            }
        }
        return type;
    }
}
