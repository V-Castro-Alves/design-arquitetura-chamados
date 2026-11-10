package br.univille.chamados.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import br.univille.chamados.dto.ChamadoRequest;
import br.univille.chamados.entity.Categoria;
import br.univille.chamados.entity.Cliente;
import br.univille.chamados.entity.Prioridade;
import br.univille.chamados.repository.CategoriaRepository;
import br.univille.chamados.repository.ChamadoRepository;
import br.univille.chamados.repository.ClienteRepository;
import br.univille.chamados.service.ChamadoService;

@SpringBootTest
@AutoConfigureMockMvc
class WebControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ChamadoRepository chamadoRepository;
    @Autowired ClienteRepository clienteRepository;
    @Autowired CategoriaRepository categoriaRepository;
    @Autowired ChamadoService chamadoService;

    private Long chamadoId;

    @BeforeEach
    void preparar() {
        chamadoRepository.deleteAll();
        categoriaRepository.deleteAll();
        clienteRepository.deleteAll();
        var cliente = clienteRepository.save(
            new Cliente("Maria", "maria-" + System.nanoTime() + "@mail.com", "47999999999"));
        var categoria = categoriaRepository.save(new Categoria("Sistema", "Aplicações"));
        chamadoId = chamadoService.criar(new ChamadoRequest("Erro ao acessar",
            "A tela não carrega", Prioridade.ALTA, cliente.getId(), categoria.getId(), null)).id();
    }

    @Test
    void deveRenderizarAsCincoTelas() throws Exception {
        mockMvc.perform(get("/app"))
            .andExpect(status().isOk()).andExpect(view().name("index"));
        mockMvc.perform(get("/app/chamados"))
            .andExpect(status().isOk()).andExpect(view().name("chamados/lista"));
        mockMvc.perform(get("/app/chamados/novo"))
            .andExpect(status().isOk()).andExpect(view().name("chamados/formulario"));
        mockMvc.perform(get("/app/chamados/{id}", chamadoId))
            .andExpect(status().isOk()).andExpect(view().name("chamados/detalhe"));
        mockMvc.perform(get("/app/cadastros"))
            .andExpect(status().isOk()).andExpect(view().name("cadastros"));
    }
}
