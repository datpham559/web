package com.shopme.admin.user.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.admin.FileUploadUtil;
import com.shopme.admin.export.UserCsvExporter;
import com.shopme.admin.export.UserExcelExporter;
import com.shopme.admin.user.UserNotFoundException;
import com.shopme.admin.user.UserService;
import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

@Controller
public class UserController {

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/users")
	public String listFirstPage(Model model) {
		return listByPage(1, model, "firstName", "asc",null);// listAll thành list.. vì trang user sẽ là trang đầu (page=1)
	}

	@RequestMapping(value = "/users/page/{pageNum}")
	public String listByPage(@PathVariable(name = "pageNum") int pageNum, Model model,
			@Param("sortField") String sortField, @Param("sortDir") String sortDir,@Param("keyword") String keyword) {
		System.out.println("Sort Field " + sortField);
		System.out.println("Sort Dir " + sortDir);
		Page<User> page = userService.listByPage(pageNum, sortField, sortDir,keyword);
		List<User> users = page.getContent();
		long startCount = (pageNum - 1) * UserService.USERS_PER_PAGE + 1;// 0*4 + 1 =1 la phan tu dau tien khi o page 1
		long endCount = startCount + UserService.USERS_PER_PAGE - 1; // 1+4-1 => bang 4 khi o page 1
		if (endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}
		String reverseSortDir = sortDir.equals("asc")  ? "desc" : "asc";
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", page.getTotalElements());
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("users", users);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", reverseSortDir);
		model.addAttribute("keyword", keyword);

		return "users/users";
	}

	@RequestMapping(value = "/users/new")
	public String newUser(Model model) {
		List<Role> listRoles = userService.listRoles();
		User user = new User();
		user.setEnabled(true);
		model.addAttribute("user", user);
		model.addAttribute("listRoles", listRoles);
		model.addAttribute("pageTitle", "Create New User");
		return "users/user_form";
	}

	@RequestMapping(value = "/users/save")
	public String saveUser(User user, RedirectAttributes redirectAttributes,
			@RequestParam("image") MultipartFile multipartFile) throws IOException {
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());// cleanpath là loại bỏ các
																							// chuỗi như path/... để đơn
																							// giản hóa
			user.setPhotos(fileName);
			User saveUser = userService.save(user);
			String uploadDir = "user-photos/" + saveUser.getId();
			FileUploadUtil.cleanDirectory(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		} else {
			if (user.getPhotos().isEmpty())
				user.setPhotos(null);
			userService.save(user);
		}

		redirectAttributes.addFlashAttribute("message", "User have been save");
		return getRedirectURLtoAffectedUser(user);
	}

	private String getRedirectURLtoAffectedUser(User user) {
		String firstPartOfEmail =user.getEmail().split("@")[0];
		return "redirect:/users/page/1?sortField=id&sortDir=asc&keyword="+firstPartOfEmail;
	}

	@RequestMapping(value = "/users/edit/{id}")
	public String userEdit(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes, Model model) {
		try {
			User user = userService.get(id);
			List<Role> listRoles = userService.listRoles();
			model.addAttribute("user", user);
			model.addAttribute("pageTitle", "Edit User (ID " + id + ")");
			model.addAttribute("listRoles", listRoles);
			return "users/user_form";
		} catch (UserNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
			return "redirect:/users";
		}
	}

	@RequestMapping(value = "/users/delete/{id}")
	public String deleteUser(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
		try {
			User user = userService.get(id);
			userService.delete(id);
			redirectAttributes.addFlashAttribute("message", "User " + user.getEmail() + " have been delete");
		} catch (UserNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
		}
		return "redirect:/users";
	}

	@RequestMapping(value = "/users/{id}/enabled/{status}")
	public String updateUserEnabledStatus(@PathVariable("id") Integer id, @PathVariable("status") boolean enabled,
			RedirectAttributes redirectAttributes) {
		userService.updateEnableUser(id, enabled);
		String status = enabled ? "enabled" : "disabled";
		String message = "The User ID " + id + " has been " + status;
		redirectAttributes.addFlashAttribute("message", message);
		return "redirect:/users";

	}
	
	@RequestMapping(value="users/export/csv")
	public void exportToCSV(HttpServletResponse response) throws IOException {
		List<User> users = userService.listAll();
		UserCsvExporter exporter = new UserCsvExporter();
		exporter.export(users, response);
	}
	
	@RequestMapping(value="users/export/excel")
	public void exportToExcel(HttpServletResponse response) throws IOException {
		List<User> users = userService.listAll();
		UserExcelExporter exporter = new UserExcelExporter();
		exporter.export(users, response);
	}
	
	
	
	
	
	
	
//	@RequestMapping(value="/users/{id}/enabled/{status}")
//	public String updateEnable(@PathVariable("id")Integer id,
//							   @PathVariable("status") boolean enabled,
//							   RedirectAttributes redirectAttributes) {
//		
//		userService.updateEnableUser(id, enabled);
//		String status = enabled ? "enabled":"disabled";
//		String message = "The user ID " +id +" have been" + status;
//		redirectAttributes.addFlashAttribute("message", message);
//		
//		return "redirect:/users";
//	}
//	

}
