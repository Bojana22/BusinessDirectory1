package com.example.businessdirectory1;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface CompanyDao {

    // Оваа функција ќе ја користиш за снимање на новата компанија
    @Insert
    void insert(Company company);

    // Оваа функција ги влече сите компании од базата
    @Query("SELECT * FROM companies")
    List<Company> getAllCompanies();

    // Оваа функција е клучна за табовите - филтрира по категорија
    @Query("SELECT * FROM companies WHERE category LIKE '%' || :categoryName || '%'")
    List<Company> getCompaniesByCategory(String categoryName);

    // Оваа функција ќе ни треба за пребарувањето на дното на екранот
    @Query("SELECT * FROM companies WHERE name LIKE '%' || :searchQuery || '%' AND category LIKE '%' || :categoryName || '%'")
    List<Company> searchCompanies(String searchQuery, String categoryName);
}