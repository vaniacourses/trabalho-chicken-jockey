package net.originmobi.pdv.dto.produto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.originmobi.pdv.enumerado.produto.ProdutoSubstTributaria;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoMergerDTO {

    private Long codigo;
    private Long codFornecedor;
    private Long codCategoria;
    private Long codGrupo;
    private int balanca;
    private String descricao;
    private Double valorCusto;
    private Double valorVenda;
    private Date dataValidade;
    private String controleEstoque;
    private String situacao;
    private String unidade;
    private ProdutoSubstTributaria subtributaria;
    private String ncm;
    private String cest;
    private Long codTributacao;
    private Long codModbc;
    private String vendavel;
}
