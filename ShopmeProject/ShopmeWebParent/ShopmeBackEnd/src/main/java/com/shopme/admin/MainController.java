package com.shopme.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

		@RequestMapping("")
		public String viewHomePage() {
			return "index";
		}
		@RequestMapping(value="/login")
		public String login() {
			return "login";
		}
}
