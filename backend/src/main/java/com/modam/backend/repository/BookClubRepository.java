package com.modam.backend.repository;

import com.modam.backend.model.BookClub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookClubRepository extends JpaRepository<BookClub, Integer> {

    List<BookClub> findByBookId(int bookId);  // 필드명이 book_id로 변경됨
}
