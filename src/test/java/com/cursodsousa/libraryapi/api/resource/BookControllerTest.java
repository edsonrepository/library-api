package com.cursodsousa.libraryapi.api.resource;

import com.cursodsousa.libraryapi.api.dto.BookDTO;
import com.cursodsousa.libraryapi.api.entity.Book;
import com.cursodsousa.libraryapi.exception.BusinessException;
import com.cursodsousa.libraryapi.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

@ExtendWith( SpringExtension.class )
@ActiveProfiles( "test" )
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    @Test
    @DisplayName( "Deve criar um livro com sucesso." )
    public void createBookTest() throws Exception {

        BookDTO bookDTO = createNewBook();

        Book savedBook = Book.builder()
                .id( 10L )
                .author( "Arthur" )
                .title( "As aventuras" )
                .isbn( "123456" )
                .build();

        BDDMockito.given( service
                        .save( Mockito.any( Book.class ) ) )
                .willReturn( savedBook );

        String json = new ObjectMapper().writeValueAsString( bookDTO );

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post( BOOK_API )
                .contentType( MediaType.APPLICATION_JSON )
                .accept( MediaType.APPLICATION_JSON )
                .content( json );

        mvc
                .perform( request )
                .andExpect( MockMvcResultMatchers.status().isCreated() )
                .andExpect( MockMvcResultMatchers.jsonPath( "id" ).isNotEmpty() )
                .andExpect( MockMvcResultMatchers.jsonPath( "title" ).value( bookDTO.getTitle() ) )
                .andExpect( MockMvcResultMatchers.jsonPath( "author" ).value( bookDTO.getAuthor() ) )
                .andExpect( MockMvcResultMatchers.jsonPath( "isbn" ).value( bookDTO.getIsbn() ) )
        ;

    }

    @Test
    @DisplayName( "Deve lançar erro de validação quando não houver dados suficiente para criação do livro." )
    public void createInvalidBookTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString( new BookDTO() );

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post( BOOK_API )
                .contentType( MediaType.APPLICATION_JSON )
                .accept( MediaType.APPLICATION_JSON )
                .content( json );

        mvc.perform( request )
                .andExpect( MockMvcResultMatchers.status().isBadRequest() )
                .andExpect( MockMvcResultMatchers.jsonPath( "errors", Matchers.hasSize( 3 ) ) );

    }

    @Test
    @DisplayName( "Deve lançar erro ao tentar cadastrar um livro com isbn já utilizado por outro." )
    public void createBookWithDuplicatedIsbn() throws Exception {

        BookDTO dto = createNewBook();
        String json = new ObjectMapper().writeValueAsString( dto );
        String msgErro = "Isbn já cadastrado";
        BDDMockito.given( service.save( Mockito.any(Book.class) ) )
                .willThrow( new BusinessException( msgErro ) );

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post( BOOK_API )
                .contentType( MediaType.APPLICATION_JSON )
                .accept( MediaType.APPLICATION_JSON )
                .content( json );

        mvc.perform( request )
                .andExpect( MockMvcResultMatchers.status().isBadRequest() )
                .andExpect( MockMvcResultMatchers.jsonPath( "errors", Matchers.hasSize( 1 ) ) )
                .andExpect( MockMvcResultMatchers.jsonPath( "errors[0]" ).value( msgErro )  );

    }

    private BookDTO createNewBook() {
        return BookDTO.builder()
                .author( "Arthur" )
                .title( "As aventuras" )
                .isbn( "123456" )
                .build();
    }

    @Test
    @DisplayName("Deve obter informacoes de um livro.")
    public void getBookDetailsTest() throws Exception {
        //cenario (given)
        Long id = 1l;

        Book book = Book.builder()
                .id(id)
                .title(createNewBook().getTitle())
                .author(createNewBook().getAuthor())
                .isbn(createNewBook().getIsbn())
                .build();

        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        //execucao (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect( MockMvcResultMatchers.jsonPath( "id" ).value(id) )
                .andExpect( MockMvcResultMatchers.jsonPath( "title" ).value( createNewBook().getTitle() ) )
                .andExpect( MockMvcResultMatchers.jsonPath( "author" ).value( createNewBook().getAuthor() ) )
                .andExpect( MockMvcResultMatchers.jsonPath( "isbn" ).value( createNewBook().getIsbn() ) )
                ;
    }

    @Test
    @DisplayName("Deve retornar resource not found quando o livro procurado nao existir")
    public void bookNotFoundTest() throws Exception {

        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() throws Exception {

        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder().id(1l).build()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform( request )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

    }

    @Test
    @DisplayName("Deve retornar resouce not found quando não encontrar o livro para deletar")
    public void deleteInexistentBookTest() throws Exception {

        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform( request )
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() throws Exception {
        Long id = 1l;
        Book updatingBook = Book.builder().id(id).title("Teste Atualizando").author("Edson").isbn("123456").build();
        String json = new ObjectMapper().writeValueAsString(updatingBook);

        BDDMockito.given(service.getById(id)).willReturn(Optional.of(updatingBook));
        Book updatedBook = Book.builder().id(id).author("Edson").title("Teste Atualizando").isbn("123456").build();
        BDDMockito.given(service.update(updatingBook)).willReturn(updatedBook);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc
                .perform( request )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect( MockMvcResultMatchers.jsonPath( "id" ).value(id) )
                .andExpect( MockMvcResultMatchers.jsonPath( "title" ).value( updatedBook.getTitle() ) )
                .andExpect( MockMvcResultMatchers.jsonPath( "author" ).value( updatedBook.getAuthor() ) )
                .andExpect( MockMvcResultMatchers.jsonPath( "isbn" ).value( "123456" ))
        ;

    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar atualizar um livro inexistente")
    public void updateInexistentBookTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString(createNewBook());
        BDDMockito.given(service.getById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc
                .perform( request )
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

}