package br.univille.chamados.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.univille.chamados.dto.ChamadoRequest;
import br.univille.chamados.dto.ChamadoAtribuicaoRequest;
import br.univille.chamados.dto.ChamadoDetalheResponse;
import br.univille.chamados.dto.ChamadoResponse;
import br.univille.chamados.dto.ChamadoUpdateRequest;
import br.univille.chamados.dto.ComentarioResponse;
import br.univille.chamados.entity.Categoria;
import br.univille.chamados.entity.Chamado;
import br.univille.chamados.entity.Cliente;
import br.univille.chamados.entity.Tecnico;
import br.univille.chamados.entity.Prioridade;
import br.univille.chamados.entity.StatusChamado;
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
    public Page<ChamadoResponse> listar(StatusChamado status, Prioridade prioridade,
            Long clienteId, Pageable pageable) {
        Page<Chamado> chamados;
        if (status != null && prioridade != null && clienteId != null) {
            chamados = chamadoRepository.findByStatusAndPrioridadeAndClienteId(
                status, prioridade, clienteId, pageable);
        } else if (status != null && prioridade != null) {
            chamados = chamadoRepository.findByStatusAndPrioridade(status, prioridade, pageable);
        } else if (status != null && clienteId != null) {
            chamados = chamadoRepository.findByStatusAndClienteId(status, clienteId, pageable);
        } else if (prioridade != null && clienteId != null) {
            chamados = chamadoRepository.findByPrioridadeAndClienteId(
                prioridade, clienteId, pageable);
        } else if (status != null) {
            chamados = chamadoRepository.findByStatus(status, pageable);
        } else if (prioridade != null) {
            chamados = chamadoRepository.findByPrioridade(prioridade, pageable);
        } else if (clienteId != null) {
            chamados = chamadoRepository.findByClienteId(clienteId, pageable);
        } else {
            chamados = chamadoRepository.findAll(pageable);
        }
        return chamados.map(ChamadoResponse::de);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listarPorCliente(Long clienteId, Pageable pageable) {
        buscarCliente(clienteId);
        return chamadoRepository.findByClienteId(clienteId, pageable).map(ChamadoResponse::de);
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
        validarNaoFechado(chamado);
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
    public ChamadoResponse atribuir(Long id, ChamadoAtribuicaoRequest dto) {
        Chamado chamado = buscarEntity(id);
        validarNaoFechado(chamado);
        chamado.setTecnico(buscarTecnico(dto.tecnicoId()));
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public ChamadoResponse alterarStatus(Long id, StatusChamado novoStatus) {
        Chamado chamado = buscarEntity(id);
        chamado.alterarStatus(novoStatus);
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

    private Tecnico buscarTecnico(Long id) {
        return tecnicoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Técnico " + id + " não encontrado"));
    }

    private void validarNaoFechado(Chamado chamado) {
        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new br.univille.chamados.exception.RegraNegocioException(
                "Chamado fechado não pode ser alterado");
        }
    }
}
