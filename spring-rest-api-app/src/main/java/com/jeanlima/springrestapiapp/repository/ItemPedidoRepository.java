package com.jeanlima.springrestapiapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jeanlima.springrestapiapp.model.ItemPedido;
import com.jeanlima.springrestapiapp.model.Pedido;



public interface ItemPedidoRepository extends JpaRepository<ItemPedido,Integer>{
     void deleteByPedido(Pedido pedido);
}
