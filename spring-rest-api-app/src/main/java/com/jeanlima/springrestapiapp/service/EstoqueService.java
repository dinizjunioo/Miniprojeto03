package com.jeanlima.springrestapiapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.jeanlima.springrestapiapp.model.Estoque;
import com.jeanlima.springrestapiapp.model.Produto;

@Service
public interface EstoqueService {
    public Estoque salvar(Estoque estoque);
    public List<Estoque> listar();
    public Estoque buscarPorId(Integer id);
    public void deletar(Integer id);
    public Optional<Estoque> buscarPorProduto(Produto produto);
    public void atualizarQuantidade(Integer id, Integer quantidade);
}
