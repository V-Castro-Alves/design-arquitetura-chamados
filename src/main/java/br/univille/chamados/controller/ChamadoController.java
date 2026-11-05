package br.univille.chamados.controller;

import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.univille.chamados.dto.ChamadoRequest;
import br.univille.chamados.dto.ChamadoAtribuicaoRequest;
import br.univille.chamados.dto.ChamadoStatusRequest;
import br.univille.chamados.dto.ChamadoDetalheResponse;
import br.univille.chamados.dto.ChamadoResponse;
import br.univille.chamados.dto.ChamadoUpdateRequest;
import br.univille.chamados.dto.ComentarioRequest;
import br.univille.chamados.dto.ComentarioResponse;
import br.univille.chamados.entity.Prioridade;
import br.univille.chamados.entity.StatusChamado;
import br.univille.chamados.service.ChamadoService;
import br.univille.chamados.service.ComentarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {
    private final ChamadoService service;
    private final ComentarioService comentarioService;

    public ChamadoController(ChamadoService service, ComentarioService comentarioService) {
        this.service = service;
        this.comentarioService = comentarioService;
    }

    @PostMapping
    public ResponseEntity<ChamadoResponse> criar(
            @Valid @RequestBody ChamadoRequest dto) {
        ChamadoResponse criado = service.criar(dto);
        return ResponseEntity.created(URI.create("/chamados/" + criado.id())).body(criado);
    }

    @GetMapping
    public Page<ChamadoResponse> listar(
            StatusChamado status, Prioridade prioridade, Long clienteId, Pageable pageable) {
        return service.listar(status, prioridade, clienteId, pageable);
    }

    @GetMapping("/{id}")
    public ChamadoDetalheResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ComentarioResponse> adicionarComentario(
            @PathVariable Long id, @Valid @RequestBody ComentarioRequest dto) {
        ComentarioResponse criado = comentarioService.criar(id, dto);
        return ResponseEntity
            .created(URI.create("/chamados/" + id + "/comentarios/" + criado.id()))
            .body(criado);
    }

    @PatchMapping("/{id}/atribuir")
    public ChamadoResponse atribuir(
            @PathVariable Long id, @Valid @RequestBody ChamadoAtribuicaoRequest dto) {
        return service.atribuir(id, dto);
    }

    @PatchMapping("/{id}/status")
    public ChamadoResponse alterarStatus(
            @PathVariable Long id, @Valid @RequestBody ChamadoStatusRequest dto) {
        return service.alterarStatus(id, dto.status());
    }

    @PutMapping("/{id}")
    public ChamadoResponse atualizar(
            @PathVariable Long id, @Valid @RequestBody ChamadoUpdateRequest dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
