package br.univille.chamados.dto;

import br.univille.chamados.entity.Perfil;
import br.univille.chamados.entity.Tecnico;

public record TecnicoResponse(Long id, String nome, String email, Perfil perfil) {
    public static TecnicoResponse de(Tecnico tecnico) {
        return new TecnicoResponse(
            tecnico.getId(), tecnico.getNome(), tecnico.getEmail(), tecnico.getPerfil());
    }
}
