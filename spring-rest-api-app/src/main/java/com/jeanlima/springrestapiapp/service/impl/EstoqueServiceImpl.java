package com.jeanlima.springrestapiapp.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.jeanlima.springrestapiapp.exception.PedidoNaoEncontradoException;
import com.jeanlima.springrestapiapp.model.Estoque;
import com.jeanlima.springrestapiapp.model.Produto;
import com.jeanlima.springrestapiapp.repository.EstoqueRepository;
import com.jeanlima.springrestapiapp.service.EstoqueService;

public class EstoqueServiceImpl implements EstoqueService{
    @Autowired
    private EstoqueRepository estoqueRepository;

    @Override
    public Estoque salvar(Estoque estoque) {
        return estoqueRepository.save(estoque);
    }

    @Override
    public List<Estoque> listar() {
        return estoqueRepository.findAll();
    }

    @Override
    public Estoque buscarPorId(Integer id) {
        return estoqueRepository.findById(id)
            .orElseThrow(() -> new PedidoNaoEncontradoException());
    }

    @Override
    public void deletar(Integer id) {
        estoqueRepository.deleteById(id);
    }

    @Override
    public Optional<Estoque> buscarPorProduto(Produto produto) {
        return estoqueRepository.findByProduto(produto);
    }

    @Override
    public void atualizarQuantidade(Integer id, Integer quantidade) {
        Estoque estoque = buscarPorId(id);
        estoque.setQuantidade(quantidade);
        salvar(estoque);
    }
}
