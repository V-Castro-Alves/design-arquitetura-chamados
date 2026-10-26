package br.univille.chamados.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.univille.chamados.entity.Tecnico;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
