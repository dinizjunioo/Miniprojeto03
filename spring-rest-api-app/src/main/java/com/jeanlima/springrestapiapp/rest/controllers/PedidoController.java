
package com.jeanlima.springrestapiapp.rest.controllers;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.jeanlima.springrestapiapp.enums.StatusPedido;
import com.jeanlima.springrestapiapp.model.Cliente;
import com.jeanlima.springrestapiapp.model.ItemPedido;
import com.jeanlima.springrestapiapp.model.Pedido;

import com.jeanlima.springrestapiapp.repository.ClienteRepository;
import com.jeanlima.springrestapiapp.repository.PedidoRepository;

import com.jeanlima.springrestapiapp.rest.dto.AtualizacaoStatusPedidoDTO;
import com.jeanlima.springrestapiapp.rest.dto.InformacaoItemPedidoDTO;
import com.jeanlima.springrestapiapp.rest.dto.InformacoesPedidoDTO;
import com.jeanlima.springrestapiapp.rest.dto.ItemPedidoDTO;
import com.jeanlima.springrestapiapp.rest.dto.PedidoDTO;

import com.jeanlima.springrestapiapp.service.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService service;

    @Autowired
    private ClienteRepository clientes;

    @Autowired
    private PedidoRepository pedidoRepository;

    
    //Requisição para retornar dados do cliente e seus pedidos:
    @GetMapping("/{id}/pedidos")
    //public InformacoesPedidoDTO 
    void getClientePedidos(@PathVariable Integer id) {
        Cliente cliente = clientes.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));

        List<Pedido> pedidos = pedidoRepository.findByCliente(cliente);

        List<PedidoDTO> pedidosDTO = pedidos.stream()
        .map(pedido -> {
            PedidoDTO pedidoDTO = new PedidoDTO();
            // Preencher dados do PedidoDTO
            pedidoDTO.setCliente(pedido.getCliente().getId());
            pedidoDTO.setTotal(pedido.getTotal());
            // Mapear itens do pedido para ItemPedidoDTO
            List<ItemPedidoDTO> itensDTO = pedido.getItens().stream()
                .map(item -> {
                    ItemPedidoDTO itemDTO = new ItemPedidoDTO();
                    itemDTO.setProduto(item.getProduto().getId());
                    itemDTO.setQuantidade(item.getQuantidade());
                    return itemDTO;
                })
                .collect(Collectors.toList());
    
            pedidoDTO.setItems(itensDTO);
    
            return pedidoDTO;
        })
        .collect(Collectors.toList());
    
        List<InformacoesPedidoDTO> infosPedidoDTO = pedidos.stream()
        .map(infoPedido -> {
            InformacoesPedidoDTO infosPedidos = new InformacoesPedidoDTO();
            infosPedidos.setCodigo(infoPedido.getId());
            infosPedidos.setNomeCliente(infoPedido.getCliente().getNome());
            infosPedidos.setCpf(infoPedido.getCliente().getCpf());
            infosPedidos.setTotal(infoPedido.getTotal());
            infosPedidos.setDataPedido(infoPedido.getDataPedido());
            infosPedidos.setStatus(infoPedido.getStatus());
            
            List<InformacaoItemPedidoDTO> infosItemPedido = pedidosDTO.stream()
                //.flatMap(pedido -> pedido.getItems().stream())
                .map(infoItemPedido -> {
                    InformacaoItemPedidoDTO itemPedido = new InformacaoItemPedidoDTO();
                    //itemPedido.setDescricaoProduto(infoItemPedido.getDescricao());
                    itemPedido.setPrecoUnitario(infoItemPedido.getTotal());
                    itemPedido.setQuantidade(infoItemPedido.getItems().get(infoItemPedido.getCliente()).getQuantidade());
                    return itemPedido;
            }).collect(Collectors.toList());
            return infosPedidos;
        }).collect(Collectors.toList());

        //return new InformacoesPedidoDTO(cliente, infosPedidoDTO);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Integer save( @RequestBody PedidoDTO dto ){
        Pedido pedido = service.salvar(dto);
        return pedido.getId();
    }

     @GetMapping("{id}")
    public InformacoesPedidoDTO getById( @PathVariable Integer id ){
        return service
                .obterPedidoCompleto(id)
                .map( p -> converter(p) )
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
    }

    private InformacoesPedidoDTO converter(Pedido pedido){
        return InformacoesPedidoDTO
                .builder()
                .codigo(pedido.getId())
                .dataPedido(pedido.getDataPedido())
                .cpf(pedido.getCliente().getCpf())
                .nomeCliente(pedido.getCliente().getNome())
                .total(pedido.getTotal())
                .status(pedido.getStatus())
                .items(converter(pedido.getItens()))
                .build();
    }

    private List<InformacaoItemPedidoDTO> converter(List<ItemPedido> itens){
        if(CollectionUtils.isEmpty(itens)){
            return Collections.emptyList();
        }
        return itens.stream().map(
                item -> InformacaoItemPedidoDTO
                            .builder()
                            .descricaoProduto(item.getProduto().getDescricao())
                            .precoUnitario(item.getProduto().getPreco())
                            .quantidade(item.getQuantidade())
                            .build()
        ).collect(Collectors.toList());
    }

     @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStatus(@PathVariable Integer id ,
                             @RequestBody AtualizacaoStatusPedidoDTO dto){
        String novoStatus = dto.getNovoStatus();
        service.atualizaStatus(id, StatusPedido.valueOf(novoStatus));
    }

    
}
