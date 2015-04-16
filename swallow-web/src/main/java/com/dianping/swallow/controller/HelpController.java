package com.dianping.swallow.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HelpController extends AbstractController{

	@RequestMapping(value = "/console/help")
	public ModelAndView allApps(HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("menu", "help");
		map.put("contextPath", "");
		return new ModelAndView("help/help", map);
	}

}
