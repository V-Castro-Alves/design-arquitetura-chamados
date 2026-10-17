package br.univille.chamados.dto;

import br.univille.chamados.entity.Categoria;

public record CategoriaResponse(Long id, String nome, String descricao) {
    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(
            categoria.getId(), categoria.getNome(), categoria.getDescricao());
    }
}
