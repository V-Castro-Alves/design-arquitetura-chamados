package br.univille.chamados.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.univille.chamados.dto.ComentarioRequest;
import br.univille.chamados.dto.ComentarioResponse;
import br.univille.chamados.entity.Chamado;
import br.univille.chamados.entity.Comentario;
import br.univille.chamados.exception.RecursoNaoEncontradoException;
import br.univille.chamados.repository.ChamadoRepository;
import br.univille.chamados.repository.ComentarioRepository;

@Service
public class ComentarioService {
    private final ComentarioRepository comentarioRepository;
    private final ChamadoRepository chamadoRepository;

    public ComentarioService(ComentarioRepository comentarioRepository,
            ChamadoRepository chamadoRepository) {
        this.comentarioRepository = comentarioRepository;
        this.chamadoRepository = chamadoRepository;
    }

    @Transactional
    public ComentarioResponse criar(Long chamadoId, ComentarioRequest dto) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Chamado " + chamadoId + " não encontrado"));

        Comentario comentario = new Comentario(
            dto.texto().trim(), dto.autor().trim(), chamado);
        return ComentarioResponse.de(comentarioRepository.save(comentario));
    }
}
