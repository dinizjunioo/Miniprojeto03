package com.jeanlima.springrestapiapp.rest.dto;



import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.jeanlima.springrestapiapp.enums.StatusPedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class InformacoesPedidoDTO {
    private Integer codigo;
    private String cpf;
    private String nomeCliente;
    private BigDecimal total;
    private LocalDate dataPedido;
    private StatusPedido status;
    private List<InformacaoItemPedidoDTO> items;
}
