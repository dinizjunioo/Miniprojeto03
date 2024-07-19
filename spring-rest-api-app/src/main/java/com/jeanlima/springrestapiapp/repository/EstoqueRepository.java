package com.jeanlima.springrestapiapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jeanlima.springrestapiapp.model.Estoque;
import com.jeanlima.springrestapiapp.model.Produto;

public interface EstoqueRepository extends JpaRepository<Estoque,Integer>{
    Optional<Estoque> findByProduto(Produto produto);
}
