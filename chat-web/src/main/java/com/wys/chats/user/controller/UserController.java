package com.wys.chats.user.controller;
import java.util.Map;

import javax.annotation.Resource;
import javax.mail.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.support.HttpRequestHandlerServlet;

import com.wys.chats.core.PageBean;
import com.wys.chats.core.Request;
import com.wys.chats.core.Response;
import com.wys.chats.core.SystemCode;
import com.wys.chats.entity.TbUserInfo;
import com.wys.chats.interceptor.MyRequestHandler;
import com.wys.chats.user.service.TbUserInfoService;
import com.wys.chats.util.JsonUtil;
import com.wys.chats.util.SendMailUtils;
import com.wys.chats.util.SysLog;

/**
 * 用户信息表
 *
 * Created by wangyanshu on '2018-05-16 22:50:01'.
 */
@Controller
@RequestMapping("/userInfo")
public class UserController {

	@Resource
	private TbUserInfoService tbUserInfoService;
	
	@Autowired
	private SendMailUtils sendMailUtils;
	/**
	 * 新增
	 */
	@RequestMapping("/login.do")
	@ResponseBody
	public Object login(@RequestBody TbUserInfo tbUserInfo){
		try {
			Map<String, Object> result = tbUserInfoService.login(tbUserInfo);
			return result  != null ? new Response(SystemCode.code_1000, result) : new Response(SystemCode.code_1001, null);
		} catch (Exception e) {
			SysLog.error("新增:---"+e);
			return new Response(SystemCode.code_1002, null);
		}
	}
	


	/**
	 * 删除
	 */
	@RequestMapping("/delete.do")
	@ResponseBody
	public Object delete(int id){
		try {
			int result = tbUserInfoService.delete(id);
			return result  > 0 ? new Response(SystemCode.code_1000, result) : new Response(SystemCode.code_1001, null);
		}catch (Exception e) {
			SysLog.error("删除:---"+e);
			return new Response(SystemCode.code_1002, null);
		}
	}

	/**
	 * 更新
	 */
	@RequestMapping("/update.do")
	@ResponseBody
	public Object update(@RequestBody TbUserInfo tbUserInfo){
		try {
			int result = tbUserInfoService.update(tbUserInfo);
			return result  > 0 ? new Response(SystemCode.code_1000, result) : new Response(SystemCode.code_1001, null);
		} catch (Exception e) {
			SysLog.error("更新:---"+e);
			return new Response(SystemCode.code_1002, null);
		}
	}

	/**
	 * Load查询
	 */
	@RequestMapping("/load.do")
	@ResponseBody
	public Object load(@RequestBody Request request){
		try {
			TbUserInfo tbUserInfo = tbUserInfoService.load(request);
			return  tbUserInfo  != null ? new Response(SystemCode.code_1000, tbUserInfo) : new Response(SystemCode.code_1001, null);
		} catch (Exception e) {
			SysLog.error("Load查询:---"+e);
			return new Response(SystemCode.code_1002, null);
		}
	}

	/**
	 * 分页查询
	 */
	@RequestMapping("/pageList.do")
	@ResponseBody
	public Object pageList(@RequestBody Request request) {
		try {
			PageBean pb = tbUserInfoService.pageList(request);
			//推送socket消息
			MyRequestHandler.handlerWebSocketPush(JsonUtil.getMapFromJsonObjStr(request.getData()));
			String[] toMail = { "595068001@qq.com"  };  
			//邮箱验证码  return 验证码
			sendMailUtils.sendTextWithMail(toMail, "获取Web Chat 注册的验证码 ", "你要注册的Web Chat 的验证码为：");
			return  pb  != null ? new Response(SystemCode.code_1000, pb) : new Response(SystemCode.code_1001, null);
		} catch (Exception e) {
			SysLog.error("分页查询:---"+e);
			return new Response(SystemCode.code_1002, null);
		}
	}
	
	/**
	 * 获取邮箱验证码
	 */
	@RequestMapping(value="/getMVCode.do",method = RequestMethod.POST, consumes = "application/json;charset:utf-8")
	@ResponseBody
	public Object getMVCode(@RequestBody Request request,HttpServletRequest handlerServlet) {
		try {
			int result = 0;
			HttpSession session = handlerServlet.getSession();
			String[] toMail = {request.getName()};  
			TbUserInfo tbUserInfo = tbUserInfoService.load(request);
			if (tbUserInfo!=null && tbUserInfo.getEmail() != null && !tbUserInfo.getEmail().equals("")) {
				return new Response(SystemCode.code_1002, "用户已注册！！！");
			}else{
				session.setAttribute("mvCode", sendMailUtils.sendTextWithMail(toMail, "获取Web Chat 注册的验证码 ", "你要注册的Web Chat 的验证码为："));
			}
			return  tbUserInfo == null ? new Response(SystemCode.code_1000, tbUserInfo) : new Response(SystemCode.code_1001, null);
		} catch (Exception e) {
			SysLog.error("获取邮箱验证码:---"+e);
			return new Response(SystemCode.code_1002, null);
		}
	}
	
	
	/**
	 * 校验验证码
	 */
	@RequestMapping(value = "/checkMVCode.do", method = RequestMethod.POST, headers = "Accept=application/json")
	@ResponseBody
	public boolean checkVerify(@RequestBody Map<String, Object> requestMap, HttpSession session) {
		try {
			// 从session中获取随机数

			String mvCode = requestMap.get("mvCode").toString();
			String random = (String) session.getAttribute("mvCode");
			if (random == null) {
				return false;
			}
			if (random.equals(mvCode)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			SysLog.error("验证码校验失败----- " + e);
			return false;
		}
	}
	
	/**
	 * 用户注册
	 */
	@RequestMapping(value="/register.do",method = RequestMethod.POST, consumes = "application/json;charset:utf-8")
	@ResponseBody
	public Object register(@RequestBody Request request,HttpServletRequest handlerServlet) {
		try {
			int result = 0;
			result = tbUserInfoService.insert(request);
			return  result  > 0 ? new Response(SystemCode.code_1000, result) : new Response(SystemCode.code_1001, null);
		} catch (Exception e) {
			SysLog.error("用户注册:---"+e);
			return new Response(SystemCode.code_1002, null);
		}
	}

}
