package com.my.cartype;

import com.common.util.MachineConfig;
import com.my.canbox.Canbox;
import com.my.cartype.bagoo.AlphaBagoo;
import com.my.cartype.bagoo.AudiBagoo;
import com.my.cartype.bagoo.BenzBagoo;
import com.my.cartype.bagoo.CarPSABagoo;
import com.my.cartype.bagoo.FiatBagoo;
import com.my.cartype.baogu.DaChengE20;
import com.my.cartype.baogu.Megane3;
import com.my.cartype.binarytek.AccordBinarytek;
import com.my.cartype.binarytek.CarOBDBinarytek;
import com.my.cartype.binarytek.CarToyotaBinarytek;
import com.my.cartype.binarytek.ChangAnBNR;
import com.my.cartype.binarytek.ChangChengBNR;
import com.my.cartype.binarytek.ChuanQiBNR;
import com.my.cartype.binarytek.FordBinarytek;
import com.my.cartype.binarytek.GMBinarytek;
import com.my.cartype.binarytek.GuanZhiBNR;
import com.my.cartype.binarytek.HondaDABNR;
import com.my.cartype.binarytek.HuaTaiBNR;
import com.my.cartype.binarytek.HyBNR;
import com.my.cartype.binarytek.JaingLingBNR;
import com.my.cartype.binarytek.JeepBNR;
import com.my.cartype.binarytek.JeepBNR2;
import com.my.cartype.binarytek.NissanBinarytek;
import com.my.cartype.binarytek.Odyssey09_14BNR;
import com.my.cartype.binarytek.RenaultBNR;
import com.my.cartype.binarytek.VWBNR;
import com.my.cartype.binarytek.VWMQBBNR;
import com.my.cartype.binarytek.ZongTaiBNR;
import com.my.cartype.changyuantong.Accord7ChangYuanTong;
import com.my.cartype.changyuantong.Accord7ChangYuanTong9600;
import com.my.cartype.changyuantong.BydCYT;
import com.my.cartype.changyuantong.Mazda6ChangYuanTong;
import com.my.cartype.daojun.Accord7DaoJun;
import com.my.cartype.daojun.BydF6DaoJun;
import com.my.cartype.daojun.BydG6DaoJun;
import com.my.cartype.daojun.BydM6DaoJun;
import com.my.cartype.daojun.BydS6DaoJun;
import com.my.cartype.daojun.CaloraDaoJun;
import com.my.cartype.daojun.ChangChengDaoJun;
import com.my.cartype.daojun.DaoQiDaoJun;
import com.my.cartype.daojun.DongNanDX7DaoJun;
import com.my.cartype.daojun.FiestaDaojun;
import com.my.cartype.daojun.GMDaoJun;
import com.my.cartype.daojun.JiangHuaiDaoJun;
import com.my.cartype.daojun.JinBeiDaoJun;
import com.my.cartype.daojun.MondeoDaojun;
import com.my.cartype.daojun.NissanDaoJun;
import com.my.cartype.daojun.RongWeiI5DaoJun;
import com.my.cartype.daojun.Sorento13DaoJun;
import com.my.cartype.daojun.SpiriorDaoJun;
import com.my.cartype.hiworld.AudiA3Hiworld;
import com.my.cartype.hiworld.AudiQ5Hiworld;
import com.my.cartype.hiworld.BMW001Hiworld;
import com.my.cartype.hiworld.BMW002Hiworld;
import com.my.cartype.hiworld.BYDHiworld;
import com.my.cartype.hiworld.BeiQiBAP001Hiworld;
import com.my.cartype.hiworld.BeiQiBAP002Hiworld;
import com.my.cartype.hiworld.BeiQiBAP003Hiworld;
import com.my.cartype.hiworld.BentengFWP003Hiworld;
import com.my.cartype.hiworld.BentengFWP005Hiworld;
import com.my.cartype.hiworld.BentengFWP006Hiworld;
import com.my.cartype.hiworld.BentengFWP007Hiworld;
import com.my.cartype.hiworld.BentengFWP008Hiworld;
import com.my.cartype.hiworld.BentengFWP009Hiworld;
import com.my.cartype.hiworld.BentengFWP00AHiworld;
import com.my.cartype.hiworld.BenzB200Hiworld;
import com.my.cartype.hiworld.BenzG350Hiworld;
import com.my.cartype.hiworld.BenzMetrisHiworld;
import com.my.cartype.hiworld.ChangAnCNP001Hiworld;
import com.my.cartype.hiworld.ChangAnCNP002Hiworld;
import com.my.cartype.hiworld.ChangAnCNP004Hiworld;
import com.my.cartype.hiworld.ChangAnCNP005Hiworld;
import com.my.cartype.hiworld.ChangChengH2Hiworld;
import com.my.cartype.hiworld.ChangChengH9Hiworld;
import com.my.cartype.hiworld.ChangChengHiworld;
import com.my.cartype.hiworld.ChangChengHiworldCCP003;
import com.my.cartype.hiworld.ChuanQiGA3Hiworld;
import com.my.cartype.hiworld.ChuanQiHiworld;
import com.my.cartype.hiworld.DongFeng002Hiworld;
import com.my.cartype.hiworld.DongFeng003Hiworld;
import com.my.cartype.hiworld.DongFeng005Hiworld;
import com.my.cartype.hiworld.DongFeng007Hiworld;
import com.my.cartype.hiworld.DongFeng008Hiworld;
import com.my.cartype.hiworld.FiatHiworld;
import com.my.cartype.hiworld.Ford003Hiworld;
import com.my.cartype.hiworld.Ford005Hiworld;
import com.my.cartype.hiworld.FordFDP007Hiworld;
import com.my.cartype.hiworld.FordHiworld;
import com.my.cartype.hiworld.GMHiworld;
import com.my.cartype.hiworld.HYHiworld;
import com.my.cartype.hiworld.Haima001Hiworld;
import com.my.cartype.hiworld.Haima003Hiworld;
import com.my.cartype.hiworld.Honda003Hiworld;
import com.my.cartype.hiworld.HondaDAHiworld;
import com.my.cartype.hiworld.IVT001Hiworld;
import com.my.cartype.hiworld.Infiniti001Hiworld;
import com.my.cartype.hiworld.Jeep002Hiworld;
import com.my.cartype.hiworld.JeepHiworld;
import com.my.cartype.hiworld.JiLiHiworld;
import com.my.cartype.hiworld.JiangHuaiRuiFengHiworld;
import com.my.cartype.hiworld.KeyPannelHiworld;
import com.my.cartype.hiworld.MAP001Hiworld;
import com.my.cartype.hiworld.MazdaHiworld;
import com.my.cartype.hiworld.MiniHiword;
import com.my.cartype.hiworld.NissanHiworld;
import com.my.cartype.hiworld.NissanTeana08Hiworld;
import com.my.cartype.hiworld.Odyssey04Hiworld;
import com.my.cartype.hiworld.OutlanderHiworld;
import com.my.cartype.hiworld.PSAHiworld;
import com.my.cartype.hiworld.QiChengT90Hiworld;
import com.my.cartype.hiworld.QiRuiHiworld;
import com.my.cartype.hiworld.QiRuiJieTuHiworld;
import com.my.cartype.hiworld.RanualtHiworld;
import com.my.cartype.hiworld.SaiOu3Hiworld;
import com.my.cartype.hiworld.ShangQiBaoJunHiworld;
import com.my.cartype.hiworld.ShangQiSAP001Hiworld;
import com.my.cartype.hiworld.ShangQiSAP003Hiworld;
import com.my.cartype.hiworld.ShangQiSAP004Hiworld;
import com.my.cartype.hiworld.ShangQiSAP005Hiworld;
import com.my.cartype.hiworld.ShangQiSAP006Hiworld;
import com.my.cartype.hiworld.ShangQiSAP007Hiworld;
import com.my.cartype.hiworld.TataTAP001Hiworld;
import com.my.cartype.hiworld.TouaregHiworld;
import com.my.cartype.hiworld.Toyota002Hiworld;
import com.my.cartype.hiworld.ToyotaHiworld;
import com.my.cartype.hiworld.UAZ001Hiworld;
import com.my.cartype.hiworld.VWHiworld;
import com.my.cartype.hiworld.VWMQBHiworld;
import com.my.cartype.hiworld.WeiChaiJMP001Hiworld;
import com.my.cartype.hiworld.ZTP001Hiworld;
import com.my.cartype.huachengyu.BydHCY;
import com.my.cartype.luzheng.BMWE46LuZheng;
import com.my.cartype.luzheng.HondaHaoZheng;
import com.my.cartype.luzheng.LandRoverHaozheng;
import com.my.cartype.luzheng.Mazda6LuZheng;
import com.my.cartype.luzheng.MiniHaoZheng;
import com.my.cartype.luzheng.OpelHaoZheng;
import com.my.cartype.luzheng.RX330HaoZheng;
import com.my.cartype.luzheng.SmartHaozheng;
import com.my.cartype.luzheng.SubaruHaoZheng;
import com.my.cartype.luzheng.ToyotaLuZheng;
import com.my.cartype.ods.BMWNbtEvo;
import com.my.cartype.ods.BiSuOD;
import com.my.cartype.ods.BydODS;
import com.my.cartype.ods.CadillacKaiLeiDeOD;
import com.my.cartype.ods.ChangChengH9OD;
import com.my.cartype.ods.CheryOD;
import com.my.cartype.ods.DongFengOD;
import com.my.cartype.ods.DongFengXinNengYuanOD;
import com.my.cartype.ods.DongNanA5OD;
import com.my.cartype.ods.DongNanDX7OD;
import com.my.cartype.ods.FordQuanXunOD;
import com.my.cartype.ods.FutianOD;
import com.my.cartype.ods.GMOD;
import com.my.cartype.ods.HummerODS;
import com.my.cartype.ods.JiLiBoRuiOD;
import com.my.cartype.ods.JiangHuaiOD;
import com.my.cartype.ods.LuFengOD;
import com.my.cartype.ods.LuxgenOD;
import com.my.cartype.ods.NaZhaOD;
import com.my.cartype.ods.OpelOD;
import com.my.cartype.ods.QQiceScreamOD;
import com.my.cartype.ods.SaicOD;
import com.my.cartype.ods.SkyworthET5;
import com.my.cartype.ods.SubrauODS;
import com.my.cartype.ods.VolvoXC60;
import com.my.cartype.ods.WeiChaiU70OD;
import com.my.cartype.ods.YeMaOD;
import com.my.cartype.ods.ZhidouOD;
import com.my.cartype.other.BeiqiDianDongCheOther;
import com.my.cartype.other.TestKLD;
import com.my.cartype.raise.AudiRaise;
import com.my.cartype.raise.BMWRaise;
import com.my.cartype.raise.BaoJunRaise;
import com.my.cartype.raise.BeiQiEC180Raise;
import com.my.cartype.raise.BeiQiM200Raise;
import com.my.cartype.raise.BeiQiRaise;
import com.my.cartype.raise.BenTengRaise;
import com.my.cartype.raise.BenzRaise;
import com.my.cartype.raise.BiSuRaise;
import com.my.cartype.raise.CarFordRaise;
import com.my.cartype.raise.CarMazda;
import com.my.cartype.raise.CarX80;
import com.my.cartype.raise.ChangChengC30Raise;
import com.my.cartype.raise.ChangChengFengJun6Raise;
import com.my.cartype.raise.ChangChengRaise;
import com.my.cartype.raise.ChangChengRaise_OuDi;
import com.my.cartype.raise.ChuanQiRaise;
import com.my.cartype.raise.DaTongRaise;
import com.my.cartype.raise.DongFengFengShenAX7Raise;
import com.my.cartype.raise.DongFengJingYiX5Raise;
import com.my.cartype.raise.DongFengRaise;
import com.my.cartype.raise.DongFengS560Raise;
import com.my.cartype.raise.DongNanRaise;
import com.my.cartype.raise.FiatEGEARaise;
import com.my.cartype.raise.GMRaise;
import com.my.cartype.raise.HYRaise;
import com.my.cartype.raise.HaiMaFuLaiMeiRaise;
import com.my.cartype.raise.HaiMaM8Raise;
import com.my.cartype.raise.HaiMaRaise;
import com.my.cartype.raise.HanTengRaise;
import com.my.cartype.raise.HondaRaise;
import com.my.cartype.raise.InfinitiQX50;
import com.my.cartype.raise.JeepRaise;
import com.my.cartype.raise.JiLiRaise;
import com.my.cartype.raise.JiangHuaiRaise;
import com.my.cartype.raise.KadjarRaise;
import com.my.cartype.raise.KeyPannel1;
import com.my.cartype.raise.LiFanRaise;
import com.my.cartype.raise.LuFengRaise;
import com.my.cartype.raise.MazdaRaise;
import com.my.cartype.raise.MinJueRongWeiRaise;
import com.my.cartype.raise.MitsubishiRaise;
import com.my.cartype.raise.NaZhiJieU6Raise;
import com.my.cartype.raise.NissanRaise;
import com.my.cartype.raise.OuShangRaise;
import com.my.cartype.raise.PetgeoRaise;
import com.my.cartype.raise.PetgeoScreenRaise;
import com.my.cartype.raise.Q3Raise;
import com.my.cartype.raise.QiChengRaise;
import com.my.cartype.raise.QiRuiRaise;
import com.my.cartype.raise.RongWeiRaise;
import com.my.cartype.raise.SiWeiRaise;
import com.my.cartype.raise.ToyotaRaise;
import com.my.cartype.raise.TuoLaJiRaise;
import com.my.cartype.raise.VWMQBRaise;
import com.my.cartype.raise.VolvoRaise;
import com.my.cartype.raise.X30Raise;
import com.my.cartype.raise.YueXiangV7;
import com.my.cartype.raise.ZhongHuaRaise;
import com.my.cartype.raise.ZongTaiRaise;
import com.my.cartype.simple.Accord2013Simple;
import com.my.cartype.simple.AudiA3Simple;
import com.my.cartype.simple.BravoUnionSimple;
import com.my.cartype.simple.CRV12Simple;
import com.my.cartype.simple.CarBenzVito;
import com.my.cartype.simple.CarFordSimple;
import com.my.cartype.simple.CarGMSimple;
import com.my.cartype.simple.CarHY;
import com.my.cartype.simple.CarHondaDASimple;
import com.my.cartype.simple.CarMazdaBT50Simple;
import com.my.cartype.simple.CarOPEL;
import com.my.cartype.simple.CarTEANA;
import com.my.cartype.simple.CarToyota2013;
import com.my.cartype.simple.CarToyota2013Low;
import com.my.cartype.simple.CarVW;
import com.my.cartype.simple.ChryslerSimple;
import com.my.cartype.simple.DaciaSimple;
import com.my.cartype.simple.FIATSimple;
import com.my.cartype.simple.FordExplorerSimple;
import com.my.cartype.simple.FordMondeoSimple;
import com.my.cartype.simple.GMCSimple;
import com.my.cartype.simple.IsuzuSimple;
import com.my.cartype.simple.IvecoSimple;
import com.my.cartype.simple.JeepSimple;
import com.my.cartype.simple.Mazda3BinarytekSimple;
import com.my.cartype.simple.Mazda3Simple;
import com.my.cartype.simple.MazdaCX5Simple;
import com.my.cartype.simple.MitsubishiOutLanderSimple;
import com.my.cartype.simple.Nissan2013Simple;
import com.my.cartype.simple.PSASimple;
import com.my.cartype.simple.Peugeot206307OldSimple;
import com.my.cartype.simple.Peugeot206Simple;
import com.my.cartype.simple.PorscheUnionSimple;
import com.my.cartype.simple.RamFIATSimple;
import com.my.cartype.simple.RenaultMeganeFluenceSimple;
import com.my.cartype.simple.SubaruSimple;
import com.my.cartype.simple.VWGolfSimple;
import com.my.cartype.td.MitsubishiTD;
import com.my.cartype.td.ToyotaTD;
import com.my.cartype.union.BMWE90X1Union;
import com.my.cartype.union.BeiQiH3HeChi;
import com.my.cartype.union.BenzB200Union;
import com.my.cartype.union.PetgeoScreenUnion;
import com.my.cartype.xinbasi.Accord924Xinbasi;
import com.my.cartype.xinbasi.Accord9Xinbasi;
import com.my.cartype.xinbasi.BydSongRaise;
import com.my.cartype.xinbasi.CarMazdaXinbas;
import com.my.cartype.xinbasi.FengJun6Xinbas;
import com.my.cartype.xinbasi.FordXinbasi;
import com.my.cartype.xinbasi.HaferH3Xinbas;
import com.my.cartype.xinbasi.HoldenXinbasi;
import com.my.cartype.xinbasi.HondaDAXinbasi;
import com.my.cartype.xinbasi.HyXinbasi;
import com.my.cartype.xinbasi.JeepXinbas;
import com.my.cartype.xinbasi.Mazda6Xinbas;
import com.my.cartype.xinbasi.NissanXinbas;
import com.my.cartype.xinbasi.X80Xinbas;
import com.my.cartype.xinchi.Accord8XinChi;
import com.my.cartype.xinchi.FordXinChi;
import com.my.cartype.xinchi.FremontXinChi;
import com.my.cartype.xinchi.GMXinChi;
import com.my.cartype.xinchi.LC100XinChi;
import com.my.cartype.xinchi.Sorento13XinChi;
import com.my.cartype.xinchi.Teana2005XinChi;
import com.my.cartype.xinchi.Teana2008XinChi;
import com.my.cartype.xinchi.TeanaXinChi;
import com.my.cartype.xinfeiyang.HondaXinFeiYang;
import com.my.cartype.xinfeiyang.NissanXinFeiYang;
import com.my.cartype.xinfeiyang.RenaultXinFeiYang;
import com.my.cartype.xinfeiyang.SubaruX3XinFeiYang;
import com.my.cartype.xinfeiyang.ToyotaXinFeiYang;

public class CanboxToPro {
	
		
	
	//this index is the same as CanBus说明文档*.*	
	private final static Class<?> []mAllCanbox = {
		CarFordSimple.class,//1
		CarToyota2013.class,
		CarMazda.class,
		CarX80.class,
		CarTEANA.class,
		CarOPEL.class,
		CarVW.class,
		MitsubishiOutLanderSimple.class,
		CarHY.class,
		CarPSABagoo.class,//10
		CarGMSimple.class,
		CarHondaDASimple.class,
		VWGolfSimple.class,
		RamFIATSimple.class,
		RenaultMeganeFluenceSimple.class,
		BMWE90X1Union.class,
		FIATSimple.class,
		FordMondeoSimple.class,
		PSASimple.class,
		BenzBagoo.class,//20
		KadjarRaise.class,
		GMCSimple.class,
		BenzB200Union.class,
		CarMazdaBT50Simple.class,
		JeepSimple.class,
		Accord7ChangYuanTong.class,
		CarToyotaBinarytek.class,
		CarMazdaXinbas.class,
		Peugeot206Simple.class,
		Accord2013Simple.class,//30
		Nissan2013Simple.class,
		PorscheUnionSimple.class,
		Mazda3BinarytekSimple.class,
		BravoUnionSimple.class,
		TouaregHiworld.class,
		DaciaSimple.class,
		NissanRaise.class,
		PetgeoRaise.class,
		FordExplorerSimple.class,
		AccordBinarytek.class,//40
		AudiA3Simple.class,
		SubrauODS.class,
		MiniHiword.class,
		NissanBinarytek.class,
		CarBenzVito.class,
		VWMQBRaise.class,
		CheryOD.class,
		ChryslerSimple.class,
		Mazda3Simple.class,
		CarOBDBinarytek.class,//50
		HaferH3Xinbas.class,
		HondaRaise.class,
		PetgeoScreenRaise.class,
		CarFordRaise.class,
		SmartHaozheng.class,
		LandRoverHaozheng.class,
		PetgeoScreenUnion.class,
		MazdaCX5Simple.class,
		RX330HaoZheng.class,
		Peugeot206307OldSimple.class,//60
		X30Raise.class,
		MondeoDaojun.class,
		JeepXinbas.class,
		OuShangRaise.class,
		FiatEGEARaise.class,
		HYRaise.class,
		AlphaBagoo.class,
		ToyotaRaise.class,
		MiniHaoZheng.class,
		SubaruSimple.class,//70
		GMOD.class,
		MazdaRaise.class,
		GMRaise.class,
		AudiRaise.class,
		Q3Raise.class,//75
		JeepRaise.class,
		LiFanRaise.class,
		BeiQiRaise.class,
		BeiQiM200Raise.class,
		ChangChengRaise.class,//80
		ChangChengC30Raise.class, 
		ChangChengFengJun6Raise.class,
		HaiMaRaise.class,
		HaiMaM8Raise.class,
		BiSuRaise.class,//85
		BenzRaise.class,
		SiWeiRaise.class,
		ChuanQiRaise.class,
		MitsubishiRaise.class,
		QiRuiRaise.class,//90
		BenTengRaise.class,
		JiLiRaise.class,
		YueXiangV7.class,
		BaoJunRaise.class,
		DaTongRaise.class,//95
		RongWeiRaise.class,
		MinJueRongWeiRaise.class,
		QiChengRaise.class,
		JiangHuaiRaise.class,
		DongFengRaise.class,//100
		DongFengFengShenAX7Raise.class,
		DongFengJingYiX5Raise.class,
		DongFengS560Raise.class,
		VWHiworld.class,
		ZongTaiRaise.class,//105
		VWMQBHiworld.class,
		ToyotaHiworld.class,
		HYHiworld.class,
		HondaDAHiworld.class,
		NissanHiworld.class, //110
		GMHiworld.class,
		ZhongHuaRaise.class,
		LuFengRaise.class,
		HanTengRaise.class,
		DongNanRaise.class, //115
		InfinitiQX50.class,
		FordHiworld.class,
		VolvoRaise.class,
		JeepHiworld.class,
		Jeep002Hiworld.class, //120
		PSAHiworld.class,
		MazdaHiworld.class,
		QiRuiHiworld.class,
		QiRuiJieTuHiworld.class,
		ChangChengHiworld.class, //125
		ChangChengH2Hiworld.class,
		ShangQiBaoJunHiworld.class,
		ShangQiSAP006Hiworld.class,
		ShangQiSAP004Hiworld.class,
		ShangQiSAP003Hiworld.class,//130
		ShangQiSAP005Hiworld.class,
		ShangQiSAP007Hiworld.class,
		ShangQiSAP001Hiworld.class,
		ChuanQiHiworld.class,
		FiatHiworld.class,//135
		IvecoSimple.class,
		JiLiHiworld.class,
		BYDHiworld.class,
		JiangHuaiRuiFengHiworld.class,
		HaiMaFuLaiMeiRaise.class,//140
		BeiQiEC180Raise.class,
		Accord7DaoJun.class,
		VolvoXC60.class,
		PG360Test.class,
		AudiBagoo.class,//145
		BMWNbtEvo.class,
		RanualtHiworld.class,
		HondaHaoZheng.class,
		LC100XinChi.class,
		DaoQiDaoJun.class,//150
		FordQuanXunOD.class,
		Accord9Xinbasi.class,
		Accord924Xinbasi.class,
		FordXinChi.class,
		Accord8XinChi.class, //155
		Sorento13XinChi.class,
		TeanaXinChi.class,
		HondaDAXinbasi.class,
		SaicOD.class,
		BydODS.class, //160
		FordXinbasi.class,
		JaingLingBNR.class,
		HuaTaiBNR.class,
		HummerODS.class,
		OutlanderHiworld.class,//165
		BenzB200Hiworld.class,
		QiChengT90Hiworld.class,
		BeiQiBAP002Hiworld.class,
		BeiQiBAP001Hiworld.class,
		ChuanQiGA3Hiworld.class,//170
		DongFeng002Hiworld.class,
		DongFeng003Hiworld.class,
		NissanDaoJun.class,
		SpiriorDaoJun.class,
		GMDaoJun.class,//175
		BMW001Hiworld.class,
		Teana2008XinChi.class,
		ChangAnCNP005Hiworld.class,
		LuxgenOD.class,
		DongFeng005Hiworld.class,//180
		DongFeng007Hiworld.class,
		DongFeng008Hiworld.class,
		Odyssey04Hiworld.class,
		ChangChengH9OD.class,
		CRV12Simple.class,//185
		JeepBNR.class,
		BMW002Hiworld.class,
		Ford003Hiworld.class,
		Honda003Hiworld.class,
		LuFengOD.class,//190
		BenzG350Hiworld.class,
		ZTP001Hiworld.class,
		BeiQiBAP003Hiworld.class,
		AudiA3Hiworld.class,
		AudiQ5Hiworld.class,//195
		Odyssey09_14BNR.class,
		FordBinarytek.class,
		BydS6DaoJun.class,
		OpelHaoZheng.class,
		ChangChengH9Hiworld.class, //200
		GuanZhiBNR.class,
		DaChengE20.class,
		BydF6DaoJun.class,
		BydM6DaoJun.class,
		BydG6DaoJun.class,//205
		BydHCY.class,
		NissanTeana08Hiworld.class,
		BenzMetrisHiworld.class,
		FordFDP007Hiworld.class,
		VWBNR.class,//210
		VWMQBBNR.class,
		Mazda6ChangYuanTong.class,
		ChangChengBNR.class,
		Haima003Hiworld.class,
		Haima001Hiworld.class,//215
		Infiniti001Hiworld.class,
		ChangAnCNP004Hiworld.class,
		ChangAnCNP001Hiworld.class,
		CadillacKaiLeiDeOD.class,
		RongWeiI5DaoJun.class,//220
		ChangAnCNP002Hiworld.class,
		HondaDABNR.class,
		Toyota002Hiworld.class,
		TuoLaJiRaise.class,
		JiangHuaiOD.class,//225
		Mazda6Xinbas.class,
		ToyotaLuZheng.class,
		SubaruHaoZheng.class,
		Ford005Hiworld.class,
		BydCYT.class,//230
		Mazda6LuZheng.class,
		BentengFWP006Hiworld.class,
		BentengFWP007Hiworld.class,
		BentengFWP009Hiworld.class,
		BentengFWP005Hiworld.class,//235
		BentengFWP00AHiworld.class,
		BentengFWP003Hiworld.class,
		BentengFWP008Hiworld.class,
		ChangChengHiworldCCP003.class,
		CaloraDaoJun.class,//240
		NissanXinbas.class,
		DongNanDX7OD.class,
		DongNanA5OD.class,
		GMBinarytek.class,
		Sorento13DaoJun.class,//245
		JinBeiDaoJun.class,
		FiestaDaojun.class,
		YeMaOD.class,
		DongNanDX7DaoJun.class,
		ZongTaiBNR.class,//250
		OpelOD.class,
		UAZ001Hiworld.class,
		FremontXinChi.class,
		GMXinChi.class,
		HyBNR.class,//255
		HyXinbasi.class,
		FiatBagoo.class,
		ToyotaTD.class,
		BMWRaise.class,
		WeiChaiJMP001Hiworld.class,//260
		TataTAP001Hiworld.class,
		RenaultBNR.class,
		IVT001Hiworld.class,
		MAP001Hiworld.class,
		Accord7ChangYuanTong9600.class,//265
		IsuzuSimple.class,
		BiSuOD.class,
		ZhidouOD.class,
		NaZhaOD.class,
		FutianOD.class,//270
		FengJun6Xinbas.class,
		JiangHuaiDaoJun.class,
		X80Xinbas.class,
		ChangChengDaoJun.class,
		ChangAnBNR.class,//275
		HoldenXinbasi.class,
		BeiQiH3HeChi.class,
		BydSongRaise.class,
		ChuanQiBNR.class,
		MitsubishiTD.class,//280
		SubaruX3XinFeiYang.class,
		RenaultXinFeiYang.class,
		HondaXinFeiYang.class,
		NissanXinFeiYang.class,
		ToyotaXinFeiYang.class,//285
		KeyPannel1.class,
		KeyPannelHiworld.class,
		KeyPannelHiworld.class,
		SkyworthET5.class,
		/*from 90*/
		BeiqiDianDongCheOther.class,//290 
		QQiceScreamOD.class,
		WeiChaiU70OD.class,
		JeepBNR2.class,
		DongFengOD.class,
		CarMazda.class, // 3->295 (38400)
		NaZhiJieU6Raise.class,
		JiLiBoRuiOD.class,
		DongFengXinNengYuanOD.class,
		SaiOu3Hiworld.class,
		TestKLD.class, //300
		BMWE46LuZheng.class,
		Megane3.class,
		Teana2005XinChi.class,
		ChangChengRaise_OuDi.class,
	};
	
	private final static String[] RETURN_TYPE = {
		"3,4,6,231,295",
		"299,287,288,252,264,263,261,260,239,238,237,236,235,234,233,232,229,223,221,218,217,216,214,209,104,106,107,108,109,110,111,117,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,137,138,139,147,165,166,167,168,169,170,171,172,174,176,178,180,181,182,183,187,188,189,191,192,193,194,195,200,207,208",
		"215",
		"302",
	};

	public final static Canbox getPro(String mCanboxType, int version, int index) {
		Canbox mCanbox = null;
		
		if (version >= 3) {
			try {
				if (index > 0 && index <= mAllCanbox.length) {
					index--;
					mCanbox = (Canbox) mAllCanbox[index].newInstance();
				} 
			} catch (Exception ignored) {
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
				} 		
				else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_HY)) {
					mCanbox = new CarHY();
				} else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_PSA_BAGOO)) { //10
					mCanbox = new CarPSABagoo();
				} else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_GM_SIMPLE)) {
					mCanbox = new CarGMSimple();
				} else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_GM_RAISE)) {
					mCanbox = new GMRaise();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_HONDA_DA_SIMPLE)) {
					mCanbox = new CarHondaDASimple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_VW_GOLF_SIMPLE)) {
					mCanbox = new VWGolfSimple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_RAM_FIAT)) {
					mCanbox = new RamFIATSimple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_RENAULT_MEGANE_FLUENCE_SMPLE)) {
					mCanbox = new RenaultMeganeFluenceSimple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_BMW_E90X1_UNION)) {
					mCanbox = new BMWE90X1Union();
				} else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_FIAT)) {
					mCanbox = new FIATSimple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_FORD_MONDEO)) {
					mCanbox = new FordMondeoSimple();
				} else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_PSA)) {
					mCanbox = new PSASimple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_BENZ_BAGOO)) {
					mCanbox = new BenzBagoo();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_KADJAR_RAISE)) {
					mCanbox = new KadjarRaise();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_GMC_SIMPLE)) {
					mCanbox = new GMCSimple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_BENZ_B200_UNION)) {
					mCanbox = new BenzB200Union();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_MAZDA_BT50_SIMPLE)) {
					mCanbox = new CarMazdaBT50Simple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_JEEP_SIMPLE)) {
					mCanbox = new JeepSimple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_ACCORD7_CHANGYUANTONG)) {
					mCanbox = new Accord7ChangYuanTong();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_TOYOTA_BINARYTEK)) {
					mCanbox = new CarToyotaBinarytek();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_MAZDA_XINBAS)) {
					mCanbox = new CarMazdaXinbas();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_PEUGEOT206)) {
					mCanbox = new Peugeot206Simple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_ACCORD2013)) {
					mCanbox = new Accord2013Simple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_NISSAN2013)) {
					mCanbox = new Nissan2013Simple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_PORSCHE_UNION)) {
					mCanbox = new PorscheUnionSimple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_MAZDA3_BINARYTEK)) {
					mCanbox = new Mazda3BinarytekSimple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_BRAVO_UNION)) {
					mCanbox = new BravoUnionSimple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_TOUAREG_HIWORLD)) {
					mCanbox = new TouaregHiworld();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_DACIA_SIMPLE)) {
					mCanbox = new DaciaSimple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_NISSAN_RAISE)) {
					mCanbox = new NissanRaise();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_PETGEO_RAISE)) {
					mCanbox = new PetgeoRaise();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_FORD_EXPLORER_SIMPLE)) {
					mCanbox = new FordExplorerSimple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_ACCORD_BINARYTEK)) {
					mCanbox = new AccordBinarytek();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_AUDI_SIMPLE)) {
					mCanbox = new AudiA3Simple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_SUBARU_ODS)) {
					mCanbox = new SubrauODS();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_MINI_HIWORD)) {
					mCanbox = new MiniHiword();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_NISSAN_BINARYTEK)) {
					mCanbox = new NissanBinarytek();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_BENZ_VITO_SIMPLE)) {
					mCanbox = new CarBenzVito();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_VW_MQB_RAISE)) {
					mCanbox = new VWMQBRaise();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_CHERY_OD)) {
					mCanbox = new CheryOD();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_CHRYSLER_SIMPLE)) {
					mCanbox = new ChryslerSimple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_MAZDA3_SIMPLE)) {
					mCanbox = new Mazda3Simple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_OBD_BINARUI)) {
					mCanbox = new CarOBDBinarytek();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_HAFER_H2)) {
					mCanbox = new HaferH3Xinbas();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_HONDA_RAISE)) {
					mCanbox = new HondaRaise();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_PETGEO_SCREEN_RAISE)) {
					mCanbox = new PetgeoScreenRaise();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_FORD_RAISE)) {
					mCanbox = new CarFordRaise();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_SMART_HAOZHENG)) {
					mCanbox = new SmartHaozheng();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_LANDROVER_HAOZHENG)) {
					mCanbox = new LandRoverHaozheng();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_PEUGEOT307_UNION)) {
					mCanbox = new PetgeoScreenUnion();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_MAZDA_CX5_SIMPLE)) {
					mCanbox = new MazdaCX5Simple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_RX330_HAOZHENG)) {
					mCanbox = new RX330HaoZheng();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_PSA206_SIMPLE)) {
					mCanbox = new Peugeot206307OldSimple();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_X30_RAISE)) {
					mCanbox = new X30Raise();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_MONDEO_DAOJUN)) {
					mCanbox = new MondeoDaojun();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_JEEP_XINBAS)) {
					mCanbox = new JeepXinbas();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_OUSHANG_RAISE)) {
					mCanbox = new OuShangRaise();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_FIAT_EGEA_RAISE)) {
					mCanbox = new FiatEGEARaise();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_HY_RAISE)) {
					mCanbox = new HYRaise();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_ALPHA_BAGOO)) {
					mCanbox = new AlphaBagoo();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_TOYOTA_RAISE)) {
					mCanbox = new ToyotaRaise();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_MINI_HAOZHENG)) {
					mCanbox = new MiniHaoZheng();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_SUBARU_SIMPLE)) {
					mCanbox = new SubaruSimple();
				} else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_GM_OD)) {
					mCanbox = new GMOD();
				} else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_MAZDA_RAISE)) {
					mCanbox = new MazdaRaise();
				}  else if (mCanboxType
						.equals(MachineConfig.VALUE_CANBOX_TOYOTA_LOW)) {
					mCanbox = new CarToyota2013Low();
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
	


	public final static int getReturnMsgType(String mCanboxType, int version,int index) {
		int type = 0;

		if (version >= 3) {

			if (index > 0 && index <= mAllCanbox.length) {
				for (int i = 0; i < RETURN_TYPE.length; ++i) {
					String []ss = RETURN_TYPE[i].split(",");
					for (String s : ss) {
						if (s.equals(index + "")) {
							return i + 1;
						}
					}
						///	if ((index == RETURN_TYPE[i][0])) {
						///		return RETURN_TYPE[i][1];
						///	}
				}
			}

		} else if (mCanboxType != null) {

			if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_MAZDA)
					|| mCanboxType.equals(MachineConfig.VALUE_CANBOX_OPEL)
					|| mCanboxType.equals(MachineConfig.VALUE_CANBOX_BESTURN_X80)) {
				type = 1;
			}
		}
		return type;
	}
}
