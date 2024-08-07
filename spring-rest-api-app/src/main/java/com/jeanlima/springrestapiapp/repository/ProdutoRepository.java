package com.jeanlima.springrestapiapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jeanlima.springrestapiapp.model.Produto;


public interface ProdutoRepository extends JpaRepository<Produto,Integer>{
    Optional<Produto> findByDescricao(String descricao);
}
