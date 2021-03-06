package com.cursodsousa.libraryapi.service.impl;

import com.cursodsousa.libraryapi.api.entity.Book;
import com.cursodsousa.libraryapi.exception.BusinessException;
import com.cursodsousa.libraryapi.model.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookServiceImpl implements com.cursodsousa.libraryapi.service.BookService {

    private BookRepository repository;

    public BookServiceImpl( BookRepository repository ) {
        this.repository = repository;
    }

    @Override
    public Book save( Book book ) {
        if( repository.existsByIsbn( book.getIsbn() ) ) {
            throw new BusinessException( "Isbn já cadastrado." );
        }
        return repository.save( book );
    }

    @Override
    public Optional<Book> getById(Long id) {
        return Optional.empty();
    }

    @Override
    public void delete(Book book) {

    }

    @Override
    public Book update(Book book) {

        return null;
    }
}
