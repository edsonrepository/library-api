package com.cursodsousa.libraryapi.service;

import com.cursodsousa.libraryapi.api.entity.Book;

import java.util.Optional;

public interface BookService {

    Book save( Book book );

    Optional<Book> getById(Long id);

    void delete(Book book);

    Book update(Book book);
}
