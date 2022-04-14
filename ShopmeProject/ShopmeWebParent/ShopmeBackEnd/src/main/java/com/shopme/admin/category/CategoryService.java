package com.shopme.admin.category;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.Category;

@Service
public class CategoryService {
	
	@Autowired
	private CategoryRepository repo;
	
	public List<Category> getAll(){
		List<Category> rootCategories = repo.findRootCategories();
		return listHierarchicalCategories(rootCategories);
	}
	public List<Category> listHierarchicalCategories(List<Category> rootCategories){
		
		List<Category> hierarchicalCategories = new ArrayList<>();
		for(Category rootCategory : rootCategories) {
			
			hierarchicalCategories.add(Category.copyFull(rootCategory));
			
			Set<Category> children = rootCategory.getChildren();
			
			for(Category subCategory : children) {
				String name = "--" + subCategory.getName(); 
				hierarchicalCategories.add(Category.copyFull(subCategory, name));
				listSubHierarchicalCategories(hierarchicalCategories, subCategory, 1);
			}
			
		}
		return hierarchicalCategories;
	}
	
	private void listSubHierarchicalCategories(List<Category> hierarchicalCategories,
			Category parent,int subLevel) {
		
		Set<Category> children = parent.getChildren();
		int newSubLevel = subLevel + 1;
		for(Category subCategory : children) {
			String name ="";
			for(int i = 0;i < newSubLevel;i++) {
				name += "--";
			}
			name += subCategory.getName();
			hierarchicalCategories.add(Category.copyFull(subCategory, name));
			listSubHierarchicalCategories(hierarchicalCategories, subCategory, newSubLevel);
		}
	}
	public Category save(Category category) {
		return repo.save(category);
	}
	public Category updateCategory(Category categoryInForm) {
		Category categoryInDB = repo.findById(categoryInForm.getId()).get();
		categoryInForm.setAlias(categoryInDB.getAlias());
		categoryInForm.setName(categoryInDB.getName());
		categoryInForm.setParent(categoryInDB.getParent());
		if(categoryInDB.getImage() !=null)
		{
			categoryInDB.setImage(categoryInForm.getImage());
		}
		return repo.save(categoryInDB);
			
	}
	
	public void deleteCategory(Integer id) {
		 repo.deleteById(id);	
	}
	public List<Category> listCategoriesUsedForm(){
		List<Category> categoriesUsedInForm = new ArrayList<>();
		Iterable<Category> caetgoriesInDB = repo.findAll();
		
		for (Category category : caetgoriesInDB) {
			if(category.getParent() == null) 
			{
				categoriesUsedInForm.add(Category.copyIdAndName(category));
				Set<Category> children = category.getChildren(); 
				
				for (Category subCategory : children) {
					String name = "--" + subCategory.getName(); 
					categoriesUsedInForm.add(Category.copyIdAndName(subCategory.getId(), name));
					listSubCategoriesUsedInForm(categoriesUsedInForm,subCategory, 1);
				}
			}
		}
		
		return categoriesUsedInForm;	
	}
	private void listSubCategoriesUsedInForm(List<Category> categoriesUsedInForm,Category parent, int subLevel) {
		int newSubLevel = subLevel + 1;
		Set<Category> children = parent.getChildren();
		for (Category subCategory: children) {
			String name = "";
			for (int i = 0; i < newSubLevel; i++) {
				name += "--";
			}
			name += subCategory.getName();
			categoriesUsedInForm.add(Category.copyIdAndName(subCategory.getId(), name));
			listSubCategoriesUsedInForm(categoriesUsedInForm,subCategory, newSubLevel);
		}
	}
	public Category get(Integer id) throws CategoryNotFoundException{
		try {
			return repo.findById(id).get();
		} catch (NoSuchElementException e) {
			throw new CategoryNotFoundException("Could not find any category with ID " +id);
		}
	}
	
}
