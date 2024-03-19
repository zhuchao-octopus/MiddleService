package com.my.manager.key;


public interface IKeyCallback {

	/**
	 * @param pressType 按键按下类型
	 * @param keyCode 按键键值
	 */
	public void onGetKeyPress(byte pressType, byte keyCode);

	/**
	 * 返回当前方控状态
	 * @param isStudyMode true:在方控学习状态
	 */
	public void onGetSwcMode(boolean isStudyMode);

	/**
	 * 当前正在学习的方控键值
	 * @param keyCode
	 */
	public void onGetSwcStudyKeycode(byte keyCode);

	/**
	 * 已经学习的所有方控按键
	 * @param alreadyStudySwcKeys
	 */
	public void onGetAlreadyStudySwcKeys(byte[] alreadyStudySwcKeys);
	
	/**
	 * 返回当前面板状态
	 * @param isStudyMode true:在面板学习状态
	 */
	public void onGetPanelMode(boolean isStudyMode);
	
	/**
	 * 当前正在学习的面板键值
	 * @param keyCode
	 */
	public void onGetPanelStudyKeycode(byte keyCode);
	
	/**
	 * 已经学习的所有面板按键
	 * @param alreadyStudyPanelKeys
	 */
	public void onGetAlreadyStudyPanelKeys(byte[] alreadyStudyPanelKeys);
	
	/**
	 * 返回当前触摸按键状态
	 * @param isStudyMode true:在触摸按键学习状态
	 */
	public void onGetTouchMode(boolean isStudyMode);
	
	/**
	 * 当前正在学习的触摸按键键值
	 * @param keyCode
	 */
	public void onGetTouchStudyKeycode(byte keyCode);
	
	/**
	 * 已经学习的所有触摸按键
	 * @param alreadyStudyTouchKeys
	 */
	public void onGetAlreadyStudyTouchKeys(byte[] alreadyStudyTouchKeys);
	
	
}
