package com.shopme.admin.user.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.admin.FileUploadUtil;
import com.shopme.admin.security.ShopmeUserDetails;
import com.shopme.admin.user.UserService;
import com.shopme.common.entity.User;

@Controller
public class AccountController {
	@Autowired
	private UserService userService;
	
	@RequestMapping(value ="/account")
	public String viewDetails(@AuthenticationPrincipal ShopmeUserDetails loggedUser,Model model) {
		String email = loggedUser.getUsername();
	    User user= 	userService.getEmail(email);
	    model.addAttribute("user", user);
	    
		return "users/account_form";
	}
	@RequestMapping(value = "/account/update")
	public String saveDetails(User user, RedirectAttributes redirectAttributes,
			@RequestParam("image") MultipartFile multipartFile) throws IOException {
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());// cleanpath là loại bỏ các
																							// chuỗi như path/... để đơn
																							// giản hóa
			user.setPhotos(fileName);
			User saveUser = userService.updateAccount(user);
			String uploadDir = "user-photos/" + saveUser.getId();
			FileUploadUtil.cleanDirectory(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		} else {
			if (user.getPhotos().isEmpty())
				user.setPhotos(null);
			userService.updateAccount(user);
		}

		redirectAttributes.addFlashAttribute("message", "User account details have been updated");
		return "redirect:/account";
	}
}
