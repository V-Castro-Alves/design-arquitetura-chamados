package br.univille.chamados.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.univille.chamados.dto.CategoriaRequest;
import br.univille.chamados.dto.ChamadoAtribuicaoRequest;
import br.univille.chamados.dto.ChamadoRequest;
import br.univille.chamados.dto.ChamadoResponse;
import br.univille.chamados.dto.ClienteRequest;
import br.univille.chamados.dto.ComentarioRequest;
import br.univille.chamados.dto.TecnicoRequest;
import br.univille.chamados.entity.Perfil;
import br.univille.chamados.entity.Prioridade;
import br.univille.chamados.entity.StatusChamado;
import br.univille.chamados.repository.ChamadoRepository;
import br.univille.chamados.service.CategoriaService;
import br.univille.chamados.service.ChamadoService;
import br.univille.chamados.service.ClienteService;
import br.univille.chamados.service.ComentarioService;
import br.univille.chamados.service.TecnicoService;

@Controller
@RequestMapping("/app")
public class WebController {
    private final ChamadoService chamadoService;
    private final ClienteService clienteService;
    private final CategoriaService categoriaService;
    private final TecnicoService tecnicoService;
    private final ComentarioService comentarioService;
    private final ChamadoRepository chamadoRepository;

    public WebController(ChamadoService chamadoService, ClienteService clienteService,
            CategoriaService categoriaService, TecnicoService tecnicoService,
            ComentarioService comentarioService, ChamadoRepository chamadoRepository) {
        this.chamadoService = chamadoService;
        this.clienteService = clienteService;
        this.categoriaService = categoriaService;
        this.tecnicoService = tecnicoService;
        this.comentarioService = comentarioService;
        this.chamadoRepository = chamadoRepository;
    }

    @GetMapping({"", "/"})
    public String painel(Model model) {
        model.addAttribute("total", chamadoRepository.count());
        model.addAttribute("abertos", chamadoRepository.countByStatus(StatusChamado.ABERTO));
        model.addAttribute("atendimento",
            chamadoRepository.countByStatus(StatusChamado.EM_ATENDIMENTO));
        model.addAttribute("fechados", chamadoRepository.countByStatus(StatusChamado.FECHADO));
        model.addAttribute("recentes", chamadoService.listar(null, null, null,
            PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "dataAbertura"))).getContent());
        return "index";
    }

    @GetMapping("/chamados")
    public String listarChamados(@RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(defaultValue = "0") int page, Model model) {
        Page<ChamadoResponse> chamados = chamadoService.listar(status, prioridade, clienteId,
            PageRequest.of(Math.max(page, 0), 10, Sort.by(Sort.Direction.DESC, "dataAbertura")));
        model.addAttribute("chamados", chamados);
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("prioridadeSelecionada", prioridade);
        model.addAttribute("clienteSelecionado", clienteId);
        model.addAttribute("statusOpcoes", StatusChamado.values());
        model.addAttribute("prioridades", Prioridade.values());
        model.addAttribute("clientes", clienteService.listar());
        return "chamados/lista";
    }

    @GetMapping("/chamados/novo")
    public String novoChamado(Model model) {
        carregarOpcoes(model);
        return "chamados/formulario";
    }

    @PostMapping("/chamados")
    public String criarChamado(@RequestParam String titulo, @RequestParam String descricao,
            @RequestParam Prioridade prioridade, @RequestParam Long clienteId,
            @RequestParam Long categoriaId, @RequestParam(required = false) Long tecnicoId,
            RedirectAttributes redirect) {
        try {
            var chamado = chamadoService.criar(new ChamadoRequest(titulo, descricao, prioridade,
                clienteId, categoriaId, tecnicoId));
            redirect.addFlashAttribute("sucesso", "Chamado aberto com sucesso.");
            return "redirect:/app/chamados/" + chamado.id();
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/app/chamados/novo";
        }
    }

    @GetMapping("/chamados/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        var chamado = chamadoService.buscar(id);
        model.addAttribute("chamado", chamado);
        model.addAttribute("cliente", clienteService.buscar(chamado.clienteId()));
        model.addAttribute("categoria", categoriaService.buscar(chamado.categoriaId()));
        model.addAttribute("tecnico", chamado.tecnicoId() == null
            ? null : tecnicoService.buscar(chamado.tecnicoId()));
        model.addAttribute("tecnicos", tecnicoService.listar());
        model.addAttribute("statusOpcoes", StatusChamado.values());
        return "chamados/detalhe";
    }

    @PostMapping("/chamados/{id}/comentarios")
    public String comentar(@PathVariable Long id, @RequestParam String autor,
            @RequestParam String texto, RedirectAttributes redirect) {
        executar(() -> comentarioService.criar(id, new ComentarioRequest(texto, autor)),
            "Comentário adicionado.", redirect);
        return "redirect:/app/chamados/" + id;
    }

    @PostMapping("/chamados/{id}/status")
    public String alterarStatus(@PathVariable Long id, @RequestParam StatusChamado status,
            RedirectAttributes redirect) {
        executar(() -> chamadoService.alterarStatus(id, status),
            "Status atualizado.", redirect);
        return "redirect:/app/chamados/" + id;
    }

    @PostMapping("/chamados/{id}/atribuir")
    public String atribuir(@PathVariable Long id, @RequestParam Long tecnicoId,
            RedirectAttributes redirect) {
        executar(() -> chamadoService.atribuir(id, new ChamadoAtribuicaoRequest(tecnicoId)),
            "Técnico atribuído.", redirect);
        return "redirect:/app/chamados/" + id;
    }

    @GetMapping("/cadastros")
    public String cadastros(Model model) {
        model.addAttribute("clientes", clienteService.listar());
        model.addAttribute("tecnicos", tecnicoService.listar());
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("perfis", Perfil.values());
        return "cadastros";
    }

    @PostMapping("/clientes")
    public String criarCliente(@RequestParam String nome, @RequestParam String email,
            @RequestParam(required = false) String telefone, RedirectAttributes redirect) {
        executar(() -> clienteService.criar(new ClienteRequest(nome, email, telefone)),
            "Cliente cadastrado.", redirect);
        return "redirect:/app/cadastros#clientes";
    }

    @PostMapping("/tecnicos")
    public String criarTecnico(@RequestParam String nome, @RequestParam String email,
            @RequestParam Perfil perfil, RedirectAttributes redirect) {
        executar(() -> tecnicoService.criar(new TecnicoRequest(nome, email, perfil)),
            "Técnico cadastrado.", redirect);
        return "redirect:/app/cadastros#tecnicos";
    }

    @PostMapping("/categorias")
    public String criarCategoria(@RequestParam String nome,
            @RequestParam(required = false) String descricao, RedirectAttributes redirect) {
        executar(() -> categoriaService.criar(new CategoriaRequest(nome, descricao)),
            "Categoria cadastrada.", redirect);
        return "redirect:/app/cadastros#categorias";
    }

    private void carregarOpcoes(Model model) {
        model.addAttribute("prioridades", Prioridade.values());
        model.addAttribute("clientes", clienteService.listar());
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("tecnicos", tecnicoService.listar());
    }

    private void executar(Runnable acao, String mensagem, RedirectAttributes redirect) {
        try {
            acao.run();
            redirect.addFlashAttribute("sucesso", mensagem);
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
    }
}
