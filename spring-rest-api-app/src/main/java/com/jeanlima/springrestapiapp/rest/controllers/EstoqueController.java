package com.jeanlima.springrestapiapp.rest.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.jeanlima.springrestapiapp.model.Estoque;
import com.jeanlima.springrestapiapp.model.Produto;
import com.jeanlima.springrestapiapp.repository.ProdutoRepository;
import com.jeanlima.springrestapiapp.service.EstoqueService;

@RestController
@RequestMapping("/api/estoques")
public class EstoqueController {

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Estoque save(@RequestBody Estoque estoque) {
        return estoqueService.salvar(estoque);
    }

    @GetMapping("{id}")
    public Estoque getById(@PathVariable Integer id) {
        return estoqueService.buscarPorId(id);
    }

    @GetMapping
    public List<Estoque> findAll() {
        return estoqueService.listar();
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        estoqueService.deletar(id);
    }

    @GetMapping("/produto/{nome}")
    public Estoque findByProdutoNome(@PathVariable String nome) {
        Produto produto = produtoRepository.findByDescricao(nome)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
        return estoqueService.buscarPorProduto(produto)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque não encontrado"));
    }

    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateQuantidade(@PathVariable Integer id, @RequestBody Map<String, Object> fieldsToUpdate) {
        if (fieldsToUpdate.containsKey("quantidade")) {
            Integer quantidade = (Integer) fieldsToUpdate.get("quantidade");
            estoqueService.atualizarQuantidade(id, quantidade);
        }
    }
}
