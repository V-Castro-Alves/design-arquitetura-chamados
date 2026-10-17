package br.univille.chamados.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.univille.chamados.dto.TecnicoRequest;
import br.univille.chamados.dto.TecnicoResponse;
import br.univille.chamados.entity.Tecnico;
import br.univille.chamados.exception.RecursoNaoEncontradoException;
import br.univille.chamados.exception.RegraNegocioException;
import br.univille.chamados.repository.TecnicoRepository;

@Service
public class TecnicoService {
    private final TecnicoRepository repository;

    public TecnicoService(TecnicoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TecnicoResponse criar(TecnicoRequest dto) {
        String email = dto.email().trim();
        validarEmailDuplicado(email, null);

        Tecnico tecnico = new Tecnico(dto.nome().trim(), email, dto.perfil());
        return TecnicoResponse.de(repository.save(tecnico));
    }

    @Transactional(readOnly = true)
    public List<TecnicoResponse> listar() {
        return repository.findAll().stream()
            .map(TecnicoResponse::de)
            .toList();
    }

    @Transactional(readOnly = true)
    public TecnicoResponse buscar(Long id) {
        return TecnicoResponse.de(buscarEntity(id));
    }

    @Transactional
    public TecnicoResponse atualizar(Long id, TecnicoRequest dto) {
        Tecnico tecnico = buscarEntity(id);
        String email = dto.email().trim();
        validarEmailDuplicado(email, id);

        tecnico.setNome(dto.nome().trim());
        tecnico.setEmail(email);
        tecnico.setPerfil(dto.perfil());
        return TecnicoResponse.de(tecnico);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntity(id));
    }

    private void validarEmailDuplicado(String email, Long id) {
        boolean duplicado = id == null
            ? repository.existsByEmailIgnoreCase(email)
            : repository.existsByEmailIgnoreCaseAndIdNot(email, id);

        if (duplicado) {
            throw new RegraNegocioException("Já existe técnico com o e-mail " + email);
        }
    }

    private Tecnico buscarEntity(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Técnico " + id + " não encontrado"));
    }
}
