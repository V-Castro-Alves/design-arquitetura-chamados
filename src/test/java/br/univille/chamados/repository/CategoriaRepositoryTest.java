package br.univille.chamados.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import br.univille.chamados.entity.Categoria;

@DataJpaTest
class CategoriaRepositoryTest {
    @Autowired
    private CategoriaRepository repository;

    @Test
    void devePersistirEChecarNomeDuplicadoIgnorandoMaiusculas() {
        repository.saveAndFlush(new Categoria("Hardware", "Equipamentos"));

        assertThat(repository.existsByNomeIgnoreCase("hardware")).isTrue();
    }
}
