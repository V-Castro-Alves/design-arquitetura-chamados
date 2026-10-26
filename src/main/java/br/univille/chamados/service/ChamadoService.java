package br.univille.chamados.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.univille.chamados.dto.ChamadoRequest;
import br.univille.chamados.dto.ChamadoDetalheResponse;
import br.univille.chamados.dto.ChamadoResponse;
import br.univille.chamados.dto.ChamadoUpdateRequest;
import br.univille.chamados.dto.ComentarioResponse;
import br.univille.chamados.entity.Categoria;
import br.univille.chamados.entity.Chamado;
import br.univille.chamados.entity.Cliente;
import br.univille.chamados.entity.Tecnico;
import br.univille.chamados.exception.RecursoNaoEncontradoException;
import br.univille.chamados.repository.CategoriaRepository;
import br.univille.chamados.repository.ChamadoRepository;
import br.univille.chamados.repository.ClienteRepository;
import br.univille.chamados.repository.ComentarioRepository;
import br.univille.chamados.repository.TecnicoRepository;

@Service
public class ChamadoService {
    private final ChamadoRepository chamadoRepository;
    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final TecnicoRepository tecnicoRepository;
    private final ComentarioRepository comentarioRepository;

    public ChamadoService(ChamadoRepository chamadoRepository,
            ClienteRepository clienteRepository,
            CategoriaRepository categoriaRepository,
            TecnicoRepository tecnicoRepository,
            ComentarioRepository comentarioRepository) {
        this.chamadoRepository = chamadoRepository;
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.comentarioRepository = comentarioRepository;
    }

    @Transactional
    public ChamadoResponse criar(ChamadoRequest dto) {
        Chamado chamado = new Chamado(
            dto.titulo().trim(),
            dto.descricao().trim(),
            dto.prioridade(),
            buscarCliente(dto.clienteId()),
            buscarCategoria(dto.categoriaId()),
            buscarTecnicoOpcional(dto.tecnicoId()));

        return ChamadoResponse.de(chamadoRepository.save(chamado));
    }

    @Transactional(readOnly = true)
    public List<ChamadoResponse> listar() {
        return chamadoRepository.findAll().stream()
            .map(ChamadoResponse::de)
            .toList();
    }

    @Transactional(readOnly = true)
    public ChamadoDetalheResponse buscar(Long id) {
        Chamado chamado = buscarEntity(id);
        List<ComentarioResponse> comentarios = comentarioRepository
            .findByChamadoIdOrderByDataAsc(id).stream()
            .map(ComentarioResponse::de)
            .toList();
        return ChamadoDetalheResponse.de(chamado, comentarios);
    }

    @Transactional
    public ChamadoResponse atualizar(Long id, ChamadoUpdateRequest dto) {
        Chamado chamado = buscarEntity(id);
        chamado.setTitulo(dto.titulo().trim());
        chamado.setDescricao(dto.descricao().trim());
        chamado.setPrioridade(dto.prioridade());
        chamado.alterarStatus(dto.status());
        chamado.setCliente(buscarCliente(dto.clienteId()));
        chamado.setCategoria(buscarCategoria(dto.categoriaId()));
        chamado.setTecnico(buscarTecnicoOpcional(dto.tecnicoId()));
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public void excluir(Long id) {
        chamadoRepository.delete(buscarEntity(id));
    }

    private Chamado buscarEntity(Long id) {
        return chamadoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Chamado " + id + " não encontrado"));
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Cliente " + id + " não encontrado"));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Categoria " + id + " não encontrada"));
    }

    private Tecnico buscarTecnicoOpcional(Long id) {
        if (id == null) {
            return null;
        }
        return tecnicoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Técnico " + id + " não encontrado"));
    }
}
