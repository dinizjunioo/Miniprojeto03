package com.jeanlima.springrestapiapp.service.impl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jeanlima.springrestapiapp.enums.StatusPedido;
import com.jeanlima.springrestapiapp.exception.PedidoNaoEncontradoException;
import com.jeanlima.springrestapiapp.exception.RegraNegocioException;
import com.jeanlima.springrestapiapp.model.Cliente;
import com.jeanlima.springrestapiapp.model.Estoque;
import com.jeanlima.springrestapiapp.model.ItemPedido;
import com.jeanlima.springrestapiapp.model.Pedido;
import com.jeanlima.springrestapiapp.model.Produto;
import com.jeanlima.springrestapiapp.repository.ClienteRepository;
import com.jeanlima.springrestapiapp.repository.ItemPedidoRepository;
import com.jeanlima.springrestapiapp.repository.PedidoRepository;
import com.jeanlima.springrestapiapp.repository.ProdutoRepository;
import com.jeanlima.springrestapiapp.rest.dto.ItemPedidoDTO;
import com.jeanlima.springrestapiapp.rest.dto.PedidoDTO;
import com.jeanlima.springrestapiapp.service.EstoqueService;
import com.jeanlima.springrestapiapp.service.PedidoService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {
    
    private final PedidoRepository repository;
    private final ClienteRepository clientesRepository;
    private final ProdutoRepository produtosRepository;
    private final ItemPedidoRepository itemsPedidoRepository;

    @Autowired
    private EstoqueService estoqueService;

    @Override
    @Transactional
    public Pedido salvar(PedidoDTO dto) {
        Integer idCliente = dto.getCliente();
        Cliente cliente = clientesRepository
                .findById(idCliente)
                .orElseThrow(() -> new RegraNegocioException("Código de cliente inválido."));

        Pedido pedido = new Pedido();
        pedido.setTotal(dto.getTotal());
        pedido.setDataPedido(LocalDate.now());
        pedido.setCliente(cliente);
        pedido.setStatus(StatusPedido.REALIZADO);

        List<ItemPedido> itemsPedido = converterItems(pedido, dto.getItems());
        pedido.setItens(itemsPedido);
        BigDecimal total = itemsPedido.stream()
            .map(item -> item.getProduto().getPreco().multiply(new BigDecimal(item.getQuantidade())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setTotal(total);
        validarEstoque(itemsPedido);
        repository.save(pedido);
        atualizarEstoque(itemsPedido);
        return pedido;
    }
    private List<ItemPedido> converterItems(Pedido pedido, List<ItemPedidoDTO> items){
        if(items.isEmpty()){
            throw new RegraNegocioException("Não é possível realizar um pedido sem items.");
        }

        return items
                .stream()
                .map( dto -> {
                    Integer idProduto = dto.getProduto();
                    Produto produto = produtosRepository
                            .findById(idProduto)
                            .orElseThrow(
                                    () -> new RegraNegocioException(
                                            "Código de produto inválido: "+ idProduto
                                    ));

                    ItemPedido itemPedido = new ItemPedido();
                    itemPedido.setQuantidade(dto.getQuantidade());
                    itemPedido.setPedido(pedido);
                    itemPedido.setProduto(produto);
                    return itemPedido;
                }).collect(Collectors.toList());

    }
    @Override
    public Optional<Pedido> obterPedidoCompleto(Integer id) {
        return repository.findByIdFetchItens(id);
    }
    @Override
    public void atualizaStatus(Integer id, StatusPedido statusPedido) {
        repository
        .findById(id)
        .map( pedido -> {
            pedido.setStatus(statusPedido);
            return repository.save(pedido);
        }).orElseThrow(() -> new PedidoNaoEncontradoException() );
    }  
    
    @Override
    @Transactional
    public void atualizar(Integer id, PedidoDTO dto) {
        Pedido pedidoExistente = repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Pedido não encontrado."));

        Cliente cliente = clientesRepository.findById(dto.getCliente())
            .orElseThrow(() -> new RegraNegocioException("Código de cliente inválido."));

        pedidoExistente.setCliente(cliente);
        pedidoExistente.setDataPedido(LocalDate.now());

        List<ItemPedido> itensPedido = converterItens(pedidoExistente, dto.getItems());
        validarEstoque(itensPedido);

        BigDecimal total = itensPedido.stream()
            .map(item -> item.getProduto().getPreco().multiply(new BigDecimal(item.getQuantidade())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        pedidoExistente.setTotal(total);
        pedidoExistente.setItens(itensPedido);

        repository.save(pedidoExistente);
        atualizarEstoque(itensPedido);
    }
    
    private void validarEstoque(List<ItemPedido> itensPedido) {
        for (ItemPedido item : itensPedido) {
            Produto produto = item.getProduto();
            Estoque estoque = estoqueService.buscarPorProduto(produto)
                .orElseThrow(() -> new RegraNegocioException("Estoque não encontrado para o produto: " + produto.getDescricao()));
            if (estoque.getQuantidade() < item.getQuantidade()) {
                throw new RegraNegocioException("Estoque insuficiente para o produto: " + produto.getDescricao());
            }
        }
    }

    private void atualizarEstoque(List<ItemPedido> itensPedido) {
        for (ItemPedido item : itensPedido) {
            Produto produto = item.getProduto();
            Estoque estoque = estoqueService.buscarPorProduto(produto)
                .orElseThrow(() -> new RegraNegocioException("Estoque não encontrado para o produto: " + produto.getDescricao()));
            estoque.setQuantidade(estoque.getQuantidade() - item.getQuantidade());
            estoqueService.salvar(estoque);
        }
    }

    private List<ItemPedido> converterItens(Pedido pedido, List<ItemPedidoDTO> itensDTO) {
        if (itensDTO == null || itensDTO.isEmpty()) {
            throw new RegraNegocioException("Não é possível realizar um pedido sem itens.");
        }
    
        return itensDTO.stream().map(dto -> {
            Integer idProduto = dto.getProduto();
            Produto produto = produtosRepository.findById(idProduto)
                .orElseThrow(() -> new RegraNegocioException("Código de produto inválido: " + idProduto));
    
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setQuantidade(dto.getQuantidade());
            itemPedido.setPedido(pedido);
            itemPedido.setProduto(produto);
            return itemPedido;
        }).collect(Collectors.toList());
    }
    
}
