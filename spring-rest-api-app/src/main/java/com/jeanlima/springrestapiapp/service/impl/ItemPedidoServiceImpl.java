package com.jeanlima.springrestapiapp.service.impl;

import org.springframework.stereotype.Service;

import com.jeanlima.springrestapiapp.exception.PedidoNaoEncontradoException;
import com.jeanlima.springrestapiapp.model.Pedido;
import com.jeanlima.springrestapiapp.repository.ItemPedidoRepository;
import com.jeanlima.springrestapiapp.repository.PedidoRepository;
import com.jeanlima.springrestapiapp.service.ItemPedidoService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ItemPedidoServiceImpl implements ItemPedidoService 
{


    private final PedidoRepository repository;
    private final ItemPedidoRepository itemsPedidoRepository;

    @Override
    @Transactional
    public void deletarPedido(Integer id)
    {
        Pedido pedido = repository.findById(id)
        .orElseThrow(() -> new PedidoNaoEncontradoException());

        // Deletar itens associados
        itemsPedidoRepository.deleteByPedido(pedido);

        // Agora deletar o pedido
        repository.delete(pedido);
    }

}
